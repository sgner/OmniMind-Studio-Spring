package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.ConversationsMapper;
import com.ai.chat.a.po.Conversations;
import com.ai.chat.a.service.ConversationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ConversationsServiceImpl extends ServiceImpl<ConversationsMapper, Conversations> implements ConversationsService {
}
