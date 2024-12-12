package com.ai.chat.a.service;

import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import com.ai.chat.a.po.UserPlayerAgent;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserPlayerAgentService extends IService<UserPlayerAgent> {
    public Integer countPlayerNum(String userId);
    public Integer countAgentNum(String userId,String playerId);
    public void createSession(Role role, Player player, String userId,String message,String sessionId);
}
