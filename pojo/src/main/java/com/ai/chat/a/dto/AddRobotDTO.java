package com.ai.chat.a.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddRobotDTO {
    private String name;
    private String avatar;
    private String info;
    private long categoryId;
    private String categoryName;
}
