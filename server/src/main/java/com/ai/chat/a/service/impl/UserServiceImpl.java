package com.ai.chat.a.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.dto.CommentDTO;
import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.dto.SysSettingDTO;
import com.ai.chat.a.entity.CreateSessionData;
import com.ai.chat.a.entity.WsInitData;
import com.ai.chat.a.enums.*;
import com.ai.chat.a.mq.MessageHandle;
import com.ai.chat.a.po.*;
import com.ai.chat.a.mapper.UserMapper;
import com.ai.chat.a.redis.RedisComponent;
import com.ai.chat.a.service.*;
import com.ai.chat.a.utils.StringTools;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RedisComponent redisComponent;
    private final SubscribeService subscribeService;
    private final AnswersService answersService;
    private final ConversationsService conversationsService;
    private final MessageHandle messageHandle;

    public UserServiceImpl(@Lazy MessageHandle messageHandle,RedisComponent redisComponent, SubscribeService subscribeService, AnswersService answersService, ConversationsService conversationsService, RobotService robotService,@Lazy SessionService sessionService) {
        this.redisComponent = redisComponent;
        this.subscribeService = subscribeService;
        this.answersService = answersService;
        this.conversationsService = conversationsService;
        this.robotService = robotService;
        this.sessionService = sessionService;
        this.messageHandle = messageHandle;
    }

    private final RobotService robotService;
    private final SessionService sessionService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        LocalDateTime curDate = LocalDateTime.now();
        SysSettingDTO sysSettingDto = redisComponent.getSystemSettingFromRedis();
        String robotId = sysSettingDto.getRobotUid();
        String robotName = sysSettingDto.getRobotNickName();
        String senMessage = sysSettingDto.getRobotWelcome();
        senMessage = StringTools.cleanHtmlTag(senMessage);
        //增加机器人好友
        Subscribe subscribe = Subscribe.builder()
                .userId(userId)
                .robotId(robotId)
                .createTime(curDate)
                .status(UserRobotStatusEnum.SUBSCRIBE.getStatus())
                .updateTime(curDate)
                .endTime(null)
                .build();
        subscribeService.save(subscribe);

        //增加会话信息
        String sessionId = StringTools.getChatSessionId4User(new String[]{userId, robotId});

        Robot robot = robotService.getOne(new LambdaQueryWrapper<Robot>().eq(Robot::getId, robotId));
        User user = this.getById(userId);

        Session session = Session.builder()
                .robotAvatar(robot.getAvatar())
                .userAvatar(user.getAvatar())
                .lastMessage(senMessage)
                .status(SessionStatusEnum.NORMAL.getStatus())
                .sessionId(sessionId)
                .robotType(UserRobotTypeEnum.ROBOT.getType())
                .lastTime(curDate)
                .userId(userId)
                .robotId(robotId)
                .robotName(robotName)
                .build();

        sessionService.save(session);
        //增加聊天消息
        Conversations conversations = Conversations.builder()
                .sessionId(sessionId)
                .questionType(MessageTypeEnum.CHAT.getType())
                .userId(userId)
                .createTime(curDate)
                .userName(user.getUsername()).build();
        conversationsService.save(conversations);


        Answers answers = Answers.builder()
                .sessionId(sessionId)
                .answerType(MessageTypeEnum.CHAT.getType())
                .answer(senMessage)
                .groupId(null)
                .answerRobotType(UserRobotTypeEnum.ROBOT.getType())
                .answerRobotId(robotId)
                .answerRobotName(robotName)
                .createTime(curDate)
                .conversationId(conversations.getId())
                .answerTargetUserId(userId).status(MessageStatusEnum.SENDED.getStatus()).build();
        answersService.save(answers);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Session subscribeRobot(String userId,String robotId,LocalDateTime endTime){
        Robot robot = robotService.getById(robotId);
        if(robot.getStatus()==RobotStatusEnum.OFFLINE.getStatus()){
            throw new RuntimeException("机器人已下线");
        }
        LambdaQueryWrapper<Subscribe> queryWrapper = new LambdaQueryWrapper<Subscribe>().eq(Subscribe::getUserId, userId).eq(Subscribe::getRobotId, robotId);
        Subscribe userSubscribe = subscribeService.getOne(queryWrapper);
        if(userSubscribe!=null){
            throw new RuntimeException("您已订阅过该机器人");
        }
        Subscribe subscribe = Subscribe.builder()
                .userId(userId)
                .robotId(robotId)
                .createTime(LocalDateTime.now())
                .endTime(endTime)
                .status(UserRobotStatusEnum.SUBSCRIBE.getStatus())
                .updateTime(LocalDateTime.now())
                .build();
        subscribeService.save(subscribe);
        String sessionId = StringTools.getChatSessionId4User(new String[]{userId, robotId});
        Session resultSession = Session.builder().build();
        if(robot.getCategoryId() != 6&& robot.getCategoryId()!= 3 && robot.getCategoryId() != 2){
            User user = this.getById(userId);
            Session session = Session.builder()
                    .userAvatar(user.getAvatar())
                    .robotAvatar(robot.getAvatar())
                    .sessionId(sessionId)
                    .userId(userId)
                    .robotType(UserRobotTypeEnum.ROBOT.getType())
                    .robotId(robotId)
                    .status(SessionStatusEnum.NORMAL.getStatus())
                    .robotName(robot.getName())
                    .lastMessage(Constants.SUBSCRIBE_SUCCESS)
                    .lastTime(LocalDateTime.now()).build();
            sessionService.save(session);
            resultSession = session;
            Conversations conversations = Conversations.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .userName(user.getUsername())
                    .createTime(LocalDateTime.now())
                    .build();
            conversationsService.save(conversations);

            Answers answers = Answers.builder()
                    .answer(Constants.SUBSCRIBE_SUCCESS)
                    .answerType(MessageTypeEnum.CHAT.getType())
                    .answerRobotId(robotId).createTime(LocalDateTime.now())
                    .conversationId(conversations.getId())
                    .answerRobotType(UserRobotTypeEnum.ROBOT.getType())
                    .answerTargetUserId(userId)
                    .status(MessageStatusEnum.SENDED.getStatus())
                    .answerRobotName(robot.getName())
                    .sessionId(sessionId)
                    .conversationId(conversations.getId())
                    .build();
            answersService.save(answers);

            // TODO 添加extendData
            MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                    .sendUserId(robotId)
                    .messageType(MessageTypeEnum.CHAT.getType())
                    .contactName(user.getUsername())
                    .messageContent(Constants.SUBSCRIBE_SUCCESS)
                    .sendUserNickName(robot.getName())
                    .lastMessage(Constants.SUBSCRIBE_SUCCESS)
                    .sessionId(sessionId)
                    .sendTime(LocalDateTime.now())
                    .contactType(UserRobotTypeEnum.ROBOT.getType())
                    .contactId(user.getId())
                    .build();
            messageHandle.sendMessage(messageSendDTO);
        }else if(robot.getCategoryId() != 2){
            Session session = Session.builder()
                    .sessionId(sessionId)
                    .robotName(robot.getName())
                    .userId(userId)
                    .robotId(robotId)
                    .robotType(robot.getCategoryId())
                    .status(SessionStatusEnum.NORMAL.getStatus())
                    .lastTime(LocalDateTime.now())
                    .build();
            sessionService.save(session);
            resultSession = session;
            MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                    .contactId(userId)
                    .messageType(MessageTypeEnum.TOOL_MODEL.getType())
                    .extendData(session)
                    .build();
            messageHandle.sendMessage(messageSendDTO);
        }else{
             // TODO 为角色扮演创建session工作区，向前端发送消息
            Session session = Session.builder()
                    .sessionId(sessionId)
                    .robotName(robot.getName())
                    .userId(userId)
                    .robotId(robotId)
                    .robotType(robot.getCategoryId())
                    .status(SessionStatusEnum.NORMAL.getStatus())
                    .lastTime(LocalDateTime.now())
                    .build();
            sessionService.save(session);
            resultSession = session;
            MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                    .contactId(userId)
                    .messageType(MessageTypeEnum.XF_COS.getType())
                    .extendData(session)
                    .build();
            messageHandle.sendMessage(messageSendDTO);
        }
        return resultSession;
    }

    @Override
    public Session newSession(String robotId) {
        Robot robot = robotService.getById(robotId);
        User user = this.getById(ThreadLocalUtil.get());
        String sessionId = StringTools.getChatSessionId4User(new String[]{ThreadLocalUtil.get(),robotId});
        Session session = Session.builder()
                .userAvatar(user.getAvatar())
                .robotAvatar(robot.getAvatar())
                .sessionId(sessionId)
                .robotId(robotId)
                .robotName(robot.getName())
                .robotType(UserRobotTypeEnum.ROBOT.getType())
                .status(SessionStatusEnum.NORMAL.getStatus())
                .lastMessage(Constants.NEW_SESSION)
                .lastTime(LocalDateTime.now())
                .userId(ThreadLocalUtil.get())
                .build();
        sessionService.save(session);
        Conversations conversations = Conversations.builder()
                .userId(user.getId())
                .sessionId(sessionId)
                .userName(user.getUsername())
                .createTime(LocalDateTime.now())
                .build();
        conversationsService.save(conversations);
        Answers answers = Answers.builder()
                .answer(Constants.NEW_SESSION)
                .answerType(MessageTypeEnum.CHAT.getType())
                .answerRobotId(robotId).createTime(LocalDateTime.now())
                .conversationId(conversations.getId())
                .answerRobotType(UserRobotTypeEnum.ROBOT.getType())
                .answerTargetUserId(user.getId())
                .status(MessageStatusEnum.SENDED.getStatus())
                .answerRobotName(robot.getName())
                .sessionId(sessionId)
                .conversationId(conversations.getId())
                .build();

        answersService.save(answers);
        MessageDTO messageDTO = MessageDTO.builder().question(conversations).answers(List.of(answers)).build();
        log.info("会话{}",session);
        SessionChatUser sessionChatUser = BeanUtil.copyProperties(session, SessionChatUser.class);
        CreateSessionData createSessionData = CreateSessionData.builder().sessionChatUser(sessionChatUser).messageDTO(messageDTO).build();
        MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                .extendData(createSessionData)
                .sendUserId(robotId)
                .messageType(MessageTypeEnum.CREATE_SESSION.getType())
                .contactName(user.getUsername())
                .messageContent(Constants.NEW_SESSION)
                .sendUserNickName(robot.getName())
                .lastMessage(Constants.NEW_SESSION)
                .sessionId(sessionId)
                .sendTime(LocalDateTime.now())
                .contactType(UserRobotTypeEnum.ROBOT.getType())
                .contactId(user.getId())
                .build();
        messageHandle.sendMessage(messageSendDTO);
        return session;
    }
}
