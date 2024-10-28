package com.ai.chat.a.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.ai.chat.a.dto.AddRobotDTO;
import com.ai.chat.a.enums.UserRobotTypeEnum;
import com.ai.chat.a.po.Robot;
import com.ai.chat.a.mapper.RobotMapper;
import com.ai.chat.a.service.RobotService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl extends ServiceImpl<RobotMapper, Robot> implements RobotService {
    @Override
    public void saveRobot(AddRobotDTO addRobot) {
        Robot robot = BeanUtil.copyProperties(addRobot, Robot.class);
        robot.setId(UserRobotTypeEnum.ROBOT.getPrefix()+ IdUtil.simpleUUID());
        this.save(robot);
    }
}
