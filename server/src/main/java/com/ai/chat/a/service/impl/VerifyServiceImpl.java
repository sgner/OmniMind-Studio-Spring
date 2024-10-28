package com.ai.chat.a.service.impl;

import com.ai.chat.a.constant.CheckCodeConstant;
import com.ai.chat.a.service.VerifyService;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service

public class VerifyServiceImpl implements VerifyService{
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Override
    public void storageCodeInRedis(Captcha captcha, String temporaryId) {
        stringRedisTemplate.opsForValue().set(temporaryId, captcha.text(), CheckCodeConstant.EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    public String getCodeFromRedis(String temporaryId) {
        return stringRedisTemplate.opsForValue().get(temporaryId);
    }

    @Override
    public boolean checkCheckCode(String code,String temporaryId) {
        return code.equals(getCodeFromRedis(temporaryId));
    }
}
