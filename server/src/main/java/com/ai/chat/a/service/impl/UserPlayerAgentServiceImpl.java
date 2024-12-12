package com.ai.chat.a.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.entity.CreateSessionData;
import com.ai.chat.a.enums.MessageStatusEnum;
import com.ai.chat.a.enums.MessageTypeEnum;
import com.ai.chat.a.enums.UserRobotTypeEnum;
import com.ai.chat.a.mapper.UserPlayerAgentMapper;
import com.ai.chat.a.mq.MessageHandle;
import com.ai.chat.a.po.*;
import com.ai.chat.a.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPlayerAgentServiceImpl extends ServiceImpl<UserPlayerAgentMapper, UserPlayerAgent> implements UserPlayerAgentService {
    private final UserPlayerAgentMapper userPlayerAgentMapper;
    private final SessionService sessionService;
    private final ConversationsService conversationsService;
    private final AnswersService answersService;
    private final MessageHandle messageHandle;
    @Override
    public Integer countPlayerNum(String userId) {
        return userPlayerAgentMapper.countPlayerNum(userId);
    }

    @Override
    public Integer countAgentNum(String userId, String playerId) {
        return userPlayerAgentMapper.countAgentNum(userId, playerId);
    }

    @Override
    public void createSession(Role role, Player player,String userId, String message,String sessionId) {
        Conversations conversations = Conversations.builder()
                .createTime(LocalDateTime.now())
                .sessionId(sessionId)
                .userName(player.getPlayerName())
                .userId(userId)
                .build();
        conversationsService.save(conversations);
        Answers answers = Answers.builder()
                .answerRobotType(UserRobotTypeEnum.COSPLAY.getType())
                .answerTargetUserId(player.getPlayerId())
                .answerType(MessageTypeEnum.CHAT.getType())
                .sessionId(sessionId)
                .answer(message)
                .answerRobotName(role.getAgentName())
                .answerRobotId(role.getAgentId())
                .status(MessageStatusEnum.SENDED.getStatus())
                .conversationId(conversations.getId())
                .createTime(LocalDateTime.now()).build();

        answersService.save(answers);
        Session session = Session.builder()
                .userAvatar(player.getPlayerAvatar())
                .robotAvatar(role.getAgentAvatar())
                .lastTime(LocalDateTime.now())
                .sessionId(sessionId)
                .lastMessage(message)
                .robotType(UserRobotTypeEnum.COSPLAY.getType())
                .userName(player.getPlayerName())
                .robotId(role.getAgentId())
                .userId(userId)
                .robotName(role.getAgentName())
                .noReadCount(0)
                .status(1)
                .build();
        sessionService.save(session);


        MessageDTO messageDTO = MessageDTO.builder().question(conversations).answers(List.of(answers)).build();
        SessionChatUser sessionChatUser = BeanUtil.copyProperties(session, SessionChatUser.class);
        CreateSessionData createSessionData = CreateSessionData.builder().sessionChatUser(sessionChatUser).messageDTO(messageDTO).build();
        MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                .extendData(createSessionData)
                .messageType(MessageTypeEnum.CREATE_SESSION.getType())
                .contactId(userId)
                .contactName(player.getPlayerName())
                .messageContent(message)
                .lastMessage(message)
                .sendTime(LocalDateTime.now())
                .sessionId(sessionId)
                .sendUserId(role.getAgentId())
                .contactType(UserRobotTypeEnum.COSPLAY.getType())
                .build();
         messageHandle.sendMessage(messageSendDTO);
    }
}
