package com.ai.chat.a.Validation;

import com.ai.chat.a.annotation.UserValidation;
import com.ai.chat.a.dto.RegisterDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegisterValidation implements ConstraintValidator<UserValidation, RegisterDTO> {
    @Override
    public boolean isValid(RegisterDTO obj, ConstraintValidatorContext context) {
        return obj.getPassword().equals(obj.getRePassword());
    }
}