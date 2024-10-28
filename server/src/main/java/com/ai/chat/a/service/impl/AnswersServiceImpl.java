package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.AnswersMapper;
import com.ai.chat.a.po.Answers;
import com.ai.chat.a.service.AnswersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AnswersServiceImpl extends ServiceImpl<AnswersMapper, Answers> implements AnswersService {
}
