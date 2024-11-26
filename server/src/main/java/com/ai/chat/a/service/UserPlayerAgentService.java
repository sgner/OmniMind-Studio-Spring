package com.ai.chat.a.service;

import com.ai.chat.a.po.UserPlayerAgent;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserPlayerAgentService extends IService<UserPlayerAgent> {
    public Integer countPlayerNum(String userId);

}
