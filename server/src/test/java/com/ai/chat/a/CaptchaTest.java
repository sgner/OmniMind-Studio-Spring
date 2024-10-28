package com.ai.chat.a;

import com.ai.chat.a.service.VerifyService;
import com.ai.chat.a.utils.ChooseCaptchaUtil;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.io.IOException;
import java.util.Random;
@SpringBootTest
public class CaptchaTest {
    @Autowired
VerifyService verifyService;
    @Test
    public void test() throws IOException, FontFormatException {

//        Integer len = new Random().nextInt(1,3)+1;
//        System.out.println(len);
//        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(130, 48,3);
//        System.out.println(arithmeticCaptcha.text());
//        System.out.println(arithmeticCaptcha.toBase64(""));
//        System.out.println(arithmeticCaptcha.getArithmeticString());

//        Captcha captcha = ChooseCaptchaUtil.getCaptcha();
//        System.out.println(captcha.toBase64());
//        String s = captcha.toBase64();
//        System.out.println(captcha.text());
        System.out.println(verifyService.checkCheckCode("围牛哥乡", "b8ba2d1e-be9b-4e6a-9187-6e08c513ce07"));
    }
}
