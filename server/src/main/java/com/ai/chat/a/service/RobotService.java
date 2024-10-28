package com.ai.chat.a.service;

import com.ai.chat.a.dto.AddRobotDTO;
import com.ai.chat.a.po.Robot;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RobotService extends IService<Robot> {
    public void saveRobot(AddRobotDTO robot);
}
