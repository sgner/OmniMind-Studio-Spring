package com.ai.chat.a.controller;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.VerifyService;
import com.ai.chat.a.utils.ChooseCaptchaUtil;
import com.ai.chat.a.utils.PBKDF2Util;
import com.ai.chat.a.vo.VerifyVO;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.awt.*;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/verify")
@Slf4j
@RequiredArgsConstructor
public class VerifyController {
   private final VerifyService verifyService;

    @GetMapping()
    public R getVerify(@RequestParam("temporaryId") String temporaryId) throws IOException, FontFormatException {
        log.info("请求创建验证码");
        Captcha captcha = ChooseCaptchaUtil.getCaptcha();
        log.info("验证码：{}",captcha.text());
        if(temporaryId.isEmpty()){
            temporaryId = String.valueOf(UUID.randomUUID());
            log.info("生成临时id：{}",temporaryId);
        }else{
            log.info("临时id(已生成)：{}",temporaryId);
        }
        verifyService.storageCodeInRedis(captcha,temporaryId);
        VerifyVO verifyVO = VerifyVO.builder().image(captcha.toBase64()).temporaryId(temporaryId).build();
        return R.success(verifyVO);
    }

}
