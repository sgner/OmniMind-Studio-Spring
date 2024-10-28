package com.ai.chat.a;

import com.ai.chat.a.utils.PBKDF2Util;
import org.junit.jupiter.api.Test;

import static com.ai.chat.a.utils.PBKDF2Util.validatePassword;

public class PBKDF2UtilTest {
    @Test
    public void test() {
        String salt = PBKDF2Util.generateSalt();
        String password = "<PASSWORD>";
        String hashPassword = PBKDF2Util.hashPassword(password, salt);
        System.out.println("Salt: " + salt);
        System.out.println("Hashed Password: " + hashPassword);

        // 验证密码
        boolean isValid = validatePassword(password, hashPassword, salt);
        System.out.println("Password valid: " + isValid);
    }
}
