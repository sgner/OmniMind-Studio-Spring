package com.ai.chat.a.service.impl;

import com.ai.chat.a.mapper.UserPlayerAgentMapper;
import com.ai.chat.a.po.UserPlayerAgent;
import com.ai.chat.a.service.UserPlayerAgentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPlayerAgentServiceImpl extends ServiceImpl<UserPlayerAgentMapper, UserPlayerAgent> implements UserPlayerAgentService {
    private final UserPlayerAgentMapper userPlayerAgentMapper;
    @Override
    public Integer countPlayerNum(String userId) {
        return userPlayerAgentMapper.countPlayerNum(userId);
    }
}
