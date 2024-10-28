package com.ai.chat.a.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private String account;
    private String password;
    private String checkCode;
    private String temporaryId;
}
