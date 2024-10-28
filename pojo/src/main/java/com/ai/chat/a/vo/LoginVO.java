package com.ai.chat.a.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginVO {
    private String userId;
    private String jwt;
    private String email;
    private String username;
}
