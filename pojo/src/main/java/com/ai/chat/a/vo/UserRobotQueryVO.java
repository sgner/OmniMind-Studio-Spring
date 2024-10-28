package com.ai.chat.a.vo;

import com.ai.chat.a.po.Group;
import com.ai.chat.a.po.Robot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserRobotQueryVO {
    Map<Integer, List<Robot>> robotMap;
    List<Group> groupList;
}
