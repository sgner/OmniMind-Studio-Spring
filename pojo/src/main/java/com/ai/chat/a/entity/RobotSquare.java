package com.ai.chat.a.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RobotSquare {
    private String id;
    private String name;
    private String categoryName;
    private Integer status;
    private Boolean subscribed = false;
    private String information;
}
