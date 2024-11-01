package com.ai.chat.a.service;

import com.ai.chat.a.dto.CommentDTO;
import com.ai.chat.a.po.Session;
import com.ai.chat.a.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserService extends IService<User> {
    public void addContact4Robot(String userId);
    public void subscribeRobot(String userId, String robotId, LocalDateTime endTime);
    public Session newSession(String robotId);
}
