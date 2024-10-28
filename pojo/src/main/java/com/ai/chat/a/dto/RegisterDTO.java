package com.ai.chat.a.dto;

import com.ai.chat.a.annotation.UserValidation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@UserValidation
public class RegisterDTO {
     @NotEmpty
     @Pattern(regexp = "^(?!\\d+$)[\\s\\S]{5,12}$",message = "用户名在5-12位,不能纯数字")
     private String username;
     @NotEmpty
     @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",message = "邮箱格式不正确")
     private String email;
     @NotEmpty
     @Pattern(regexp="^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",message = "密码长度在8-16位,必须包含大写字母,特殊字符")
     private String password;
     @NotEmpty
     @Pattern(regexp="^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",message = "密码长度在8-16位,必须包含大写字母,特殊字符")
     private String rePassword;
}
