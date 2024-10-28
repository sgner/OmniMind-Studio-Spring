package com.ai.chat.a.annotation;
import com.ai.chat.a.Validation.RegisterValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = RegisterValidation.class)
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface UserValidation {
    String message() default "两次输入的密码不一致";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}