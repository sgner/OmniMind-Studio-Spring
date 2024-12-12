package com.ai.chat.a.service.impl;

import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.entity.UserUploadFile;
import com.ai.chat.a.enums.MessageStatusEnum;
import com.ai.chat.a.enums.MessageTypeEnum;
import com.ai.chat.a.enums.UserRobotTypeEnum;
import com.ai.chat.a.mq.MessageHandle;
import com.ai.chat.a.po.*;
import com.ai.chat.a.mapper.SessionMapper;
import com.ai.chat.a.redis.RedisComponent;
import com.ai.chat.a.service.AnswersService;
import com.ai.chat.a.service.ConversationsService;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.service.UserService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    private final AnswersService answersService;
    private final ConversationsService  conversationsService;
    private final MessageHandle messageHandle;
    private final UserService userService;
    private final RedisComponent redisComponent;

    public SessionServiceImpl(RedisComponent redisComponent, AnswersService answersService, ConversationsService conversionsService, UserService userService,@Lazy MessageHandle messageHandle){
         this.answersService = answersService;
         this.conversationsService = conversionsService;
         this.userService = userService;
         this.messageHandle = messageHandle;
         this.redisComponent = redisComponent;
    }
    @Transactional
    @Override
    public void updateSession4UploadFail(Session currentSession,String errorMsg) {
        try{
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            User user = userService.getOne(userLambdaQueryWrapper.eq(User::getId, currentSession.getUserId()));
            Conversations conversations = Conversations.builder()
                    .userName(user.getUsername())
                    .question("")
                    .createTime(LocalDateTime.now())
                    .sessionId(currentSession.getSessionId())
                    .questionType(MessageTypeEnum.CHAT.getType())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .userId(user.getId()).build();
            conversationsService.save(conversations);
            Answers answers = Answers.builder()
                    .answerType(MessageTypeEnum.CHAT.getType())
                    .answerRobotType(UserRobotTypeEnum.ROBOT.getType())
                    .answerTargetUserId(user.getId())
                    .answer(errorMsg)
                    .createTime(LocalDateTime.now())
                    .answerRobotName(currentSession.getRobotName())
                    .sessionId(currentSession.getSessionId())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .conversationId(conversations.getId())
                    .answerRobotId(currentSession.getRobotId())
                    .build();
            answersService.save(answers);
            LambdaUpdateWrapper<Session> sessionLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            LambdaUpdateWrapper<Session> set = sessionLambdaUpdateWrapper
                    .eq(Session::getSessionId, currentSession.getSessionId())
                    .eq(Session::getUserId, user.getId())
                    .set(Session::getLastMessage, answers.getAnswer())
                    .set(Session::getLastTime, answers.getCreateTime())
                    .setSql("no_read_count = no_read_count+1");
            this.update(set);
            MessageDTO message = MessageDTO.builder().question(conversations).answers(List.of(answers)).build();
            MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                    .contactName(user.getUsername())
                    .extendData(message)
                    .messageType(MessageTypeEnum.CHAT.getType())
                    .contactType(currentSession.getRobotType())
                    .sessionId(currentSession.getSessionId())
                    .lastMessage(answers.getAnswer())
                    .sendTime(answers.getCreateTime())
                    .messageContent(answers.getAnswer())
                    .sendUserId(answers.getAnswerRobotId())
                    .sendUserNickName(answers.getAnswerRobotName())
                    .contactId(user.getId())
                    .build();
           messageHandle.sendMessage(messageSendDTO);
        } catch (Exception e){
                e.printStackTrace();
        }

    }

    @Override
    public void updateSession(Session currentSession, UserChatDTO userChatDTO, OpenAIResponse response) {
        try{
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            User user = userService.getOne(userLambdaQueryWrapper.eq(User::getId, currentSession.getUserId()));
            String redisStore = redisComponent.getUploadFileFromRedis(ThreadLocalUtil.get()+currentSession.getSessionId());
            redisComponent.removeUploadFileFromRedis(ThreadLocalUtil.get()+currentSession.getSessionId());
            List<UserUploadFile> userUploadFiles = JSONObject.parseArray(redisStore, UserUploadFile.class);
            String jsonString = "";
            try{
                jsonString = JSONObject.toJSONString(userUploadFiles);
            }catch (Exception e){
                jsonString = "";
            }

            Conversations conversations = Conversations.builder()
                    .userName(user.getUsername())
                    .question(userChatDTO.getQuestion())
                    .files(jsonString)
                    .createTime(LocalDateTime.now())
                    .sessionId(currentSession.getSessionId())
                    .questionType(MessageTypeEnum.CHAT.getType())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .userId(user.getId()).build();
            conversationsService.save(conversations);
            Answers answers = Answers.builder()
                    .answerType(MessageTypeEnum.CHAT.getType())
                    .answerRobotType(UserRobotTypeEnum.ROBOT.getType())
                    .answerTargetUserId(user.getId())
                    .answer(response.getResponse())
                    .fileName(response.getFileName())
                    .filePath(response.getFilePath())
                    .fileSize(response.getFileSize())
                    .fileType(response.getFileType())
                    .createTime(LocalDateTime.now())
                    .answerRobotName(currentSession.getRobotName())
                    .sessionId(currentSession.getSessionId())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .conversationId(conversations.getId())
                    .answerRobotId(currentSession.getRobotId())
                    .build();
            answersService.save(answers);
            LambdaUpdateWrapper<Session> sessionLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            LambdaUpdateWrapper<Session> set = sessionLambdaUpdateWrapper
                    .eq(Session::getSessionId, currentSession.getSessionId())
                    .eq(Session::getUserId, user.getId())
                    .set(Session::getLastMessage, answers.getAnswer())
                    .set(Session::getLastTime, answers.getCreateTime())
                    .setSql("no_read_count = no_read_count+1");;
            this.update(set);
            MessageDTO message = MessageDTO.builder().question(conversations).answers(List.of(answers)).build();
            MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                    .contactName(user.getUsername())
                    .extendData(message)
                    .messageType(MessageTypeEnum.CHAT.getType())
                    .contactType(currentSession.getRobotType())
                    .sessionId(currentSession.getSessionId())
                    .lastMessage(answers.getAnswer())
                    .sendTime(answers.getCreateTime())
                    .messageContent(answers.getAnswer())
                    .sendUserId(answers.getAnswerRobotId())
                    .sendUserNickName(answers.getAnswerRobotName())
                    .contactId(user.getId())
                    .build();
            messageHandle.sendMessage(messageSendDTO);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updateSession(String userId,Session currentSession, String question, String response, Player player ,Role role) {
        try{
            Conversations conversations = Conversations.builder()
                    .userName(player.getPlayerName())
                    .createTime(LocalDateTime.now())
                    .sessionId(currentSession.getSessionId())
                    .question(question)
                    .questionType(MessageTypeEnum.CHAT.getType())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .userId(userId).build();
            conversationsService.save(conversations);
            Answers answers = Answers.builder()
                    .answerType(MessageTypeEnum.CHAT.getType())
                    .answerRobotType(UserRobotTypeEnum.COSPLAY.getType())
                    .answerTargetUserId(player.getPlayerId())
                    .answer(response)
                    .createTime(LocalDateTime.now())
                    .answerRobotName(currentSession.getRobotName())
                    .sessionId(currentSession.getSessionId())
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .conversationId(conversations.getId())
                    .answerRobotId(currentSession.getRobotId())
                    .build();
            answersService.save(answers);
            LambdaUpdateWrapper<Session> sessionLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            LambdaUpdateWrapper<Session> set = sessionLambdaUpdateWrapper
                    .eq(Session::getSessionId, currentSession.getSessionId())
                    .eq(Session::getUserId, userId)
                    .set(Session::getLastMessage, answers.getAnswer())
                    .set(Session::getLastTime, answers.getCreateTime())
                    .setSql("no_read_count = no_read_count+1");;
            this.update(set);
            MessageDTO message = MessageDTO.builder().question(conversations).answers(List.of(answers)).build();
            MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                    .extendData(message)
                    .messageType(MessageTypeEnum.CHAT.getType())
                    .contactType(currentSession.getRobotType())
                    .sessionId(currentSession.getSessionId())
                    .lastMessage(answers.getAnswer())
                    .sendTime(answers.getCreateTime())
                    .messageContent(answers.getAnswer())
                    .sendUserId(answers.getAnswerRobotId())
                    .sendUserNickName(answers.getAnswerRobotName())
                    .contactId(userId)
                    .build();
            log.info("发送消息:{}", messageSendDTO);
            messageHandle.sendMessage(messageSendDTO);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
//    private static long convertToMilliseconds(String dateTimeStr) {
//        // 定义日期时间格式
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        // 解析日期时间字符串为 LocalDateTime 对象
//        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
//        // 转换为毫秒值
//        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
//    }
}
