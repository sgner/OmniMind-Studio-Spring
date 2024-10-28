package com.ai.chat.a.utils;
import com.ai.chat.a.constant.CheckCodeConstant;
import com.wf.captcha.*;

import java.awt.*;
import java.io.IOException;
import java.util.Random;
import com.wf.captcha.base.Captcha;

public class ChooseCaptchaUtil {
    public static Captcha getCaptcha() throws IOException, FontFormatException {
        String captchaType = randomCaptchaType();
        if(captchaType.equals(CheckCodeConstant.ARITHMETIC_CAPTCHA)){
            int len = new Random().nextInt(1,3)+1;
            return new ArithmeticCaptcha(CheckCodeConstant.CODE_WIDTH, CheckCodeConstant.CODE_HEIGHT,len);
        }else if(captchaType.equals(CheckCodeConstant.SPEC_CODE)|| captchaType.equals(CheckCodeConstant.GIF_CODE)){
            int font = randomCaptchaFont();
            int chatType = randomCaptchaChatType();
            if(captchaType.equals(CheckCodeConstant.SPEC_CODE)){
                SpecCaptcha specCaptcha = new SpecCaptcha(CheckCodeConstant.CODE_WIDTH, CheckCodeConstant.CODE_HEIGHT, 5);
                specCaptcha.setFont(font);
                specCaptcha.setCharType(chatType);
                return specCaptcha;
            }
            GifCaptcha gifCaptcha = new GifCaptcha(CheckCodeConstant.CODE_WIDTH, CheckCodeConstant.CODE_HEIGHT, 5);
            gifCaptcha.setFont(font);
            gifCaptcha.setCharType(chatType);
            return gifCaptcha;
        }else if(captchaType.equals(CheckCodeConstant.CHINESE_CODE)){
            return new ChineseCaptcha(CheckCodeConstant.CODE_WIDTH, CheckCodeConstant.CODE_HEIGHT, 4);
        }
        return new ChineseGifCaptcha(CheckCodeConstant.CODE_WIDTH, CheckCodeConstant.CODE_HEIGHT, 4);
    }
    private static String randomCaptchaType(){Integer len = new Random().nextInt(1,3)+1;
        String captchaType = "";
        int type = new Random().nextInt(5)+1;
        captchaType = switch (type) {
            case 1 -> CheckCodeConstant.CHINESE_CODE;
            case 2 -> CheckCodeConstant.GIF_CODE;
            case 3 -> CheckCodeConstant.CHINESE_GIF_CODE;
            case 4 -> CheckCodeConstant.SPEC_CODE;
            default -> CheckCodeConstant.ARITHMETIC_CAPTCHA;
        };
        return captchaType;
    }
    private static int randomCaptchaChatType(){
        int captchaChatType = 0;
        int type = new Random().nextInt(6)+1;
        captchaChatType = switch (type) {
            case 1 -> Captcha.TYPE_DEFAULT;
            case 2 -> Captcha.TYPE_ONLY_NUMBER;
            case 3 -> Captcha.TYPE_ONLY_CHAR;
            case 4 -> Captcha.TYPE_ONLY_UPPER;
            case 5 -> Captcha.TYPE_ONLY_LOWER;
            default -> Captcha.TYPE_NUM_AND_UPPER;
        };
        return captchaChatType;
    }
    private static int randomCaptchaFont(){
        int captchaFont = 0;
        int type = new Random().nextInt(10)+1;
        captchaFont = switch (type) {
            case 1 -> Captcha.FONT_1;
            case 2 -> Captcha.FONT_2;
            case 3 -> Captcha.FONT_3;
            case 4 -> Captcha.FONT_4;
            case 5 -> Captcha.FONT_5;
            case 6 -> Captcha.FONT_6;
            case 7 -> Captcha.FONT_7;
            case 8 -> Captcha.FONT_8;
            case 9 -> Captcha.FONT_9;
            default -> Captcha.FONT_10;
        };
        return captchaFont;
    }
}
