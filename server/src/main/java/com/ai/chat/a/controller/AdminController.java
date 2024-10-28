package com.ai.chat.a.controller;

import com.ai.chat.a.dto.AddRobotDTO;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RobotService robotService;
    @PostMapping("/addRobot")
    public R addRobot(@RequestBody AddRobotDTO robot){
         robotService.saveRobot(robot);
         return R.success();
     }
}
