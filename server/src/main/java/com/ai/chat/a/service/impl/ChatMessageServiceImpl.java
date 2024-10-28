package com.ai.chat.a.service.impl;

import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.service.AnswersService;
import com.ai.chat.a.service.ChatMessageService;
import com.ai.chat.a.service.ConversationsService;
import com.ai.chat.a.service.SessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Resource
    private ConversationsService conversationsService;
    @Resource
    private AnswersService answersService;
    @Resource
    private SessionService sessionService;
    @Override
    public void sendMessage(UserChatDTO userChatDTO,String generated) {
//        Conversations conversations = BeanUtil.copyProperties(userChatDTO, Conversations.class);
//        conversations.setUserId(ThreadLocalUtil.get());
//        conversations.setStatus(MessageStatusEnum.SENDED.getStatus());
//        conversationsService.save(conversations);
//        Session session = sessionService.getById(userChatDTO.getSessionId());
//        Answers.builder()
//                .answer(generated)
//                .answerRobotType(session.getRobotType())
//                .answerTargetUserId(ThreadLocalUtil.get())
//                .answerType()
//
    }

}
