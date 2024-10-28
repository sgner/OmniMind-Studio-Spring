package com.ai.chat.a.service;

import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletRequest;

public interface VerifyService {

    public void storageCodeInRedis(Captcha captcha, String temporaryId);
    public String getCodeFromRedis(String temporaryId);
    public boolean checkCheckCode(String code,String temporaryId);
}
