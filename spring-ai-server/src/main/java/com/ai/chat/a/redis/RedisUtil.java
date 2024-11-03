package com.ai.chat.a.redis;

import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.UserUploadFile;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component(value = "openai_redis")
public class RedisUtil {
     private final StringRedisTemplate stringRedisTemplate;
     public List<UserUploadFile> getUserUploadFile(UserChatDTO userChatDTO){
         String uploadFile = stringRedisTemplate.opsForValue().get(ThreadLocalUtil.get() + userChatDTO.getSessionId());
         if(uploadFile == null){
             return null;
         }
         return JSONObject.parseArray(uploadFile, UserUploadFile.class);
     }
     public void setUserUploadFile(UserChatDTO userChatDTO,List<UserUploadFile> uploadFiles){
         stringRedisTemplate.opsForValue().set(ThreadLocalUtil.get()+userChatDTO.getSessionId(),JSONObject.toJSONString(uploadFiles),1,TimeUnit.MINUTES);
     }
     public Boolean updateExpirationTime(String key){
         Long currentExpiration = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
         if (currentExpiration != null && currentExpiration > 0) {
             return stringRedisTemplate.expire(key, currentExpiration + 60, TimeUnit.SECONDS);
         }
         return false;
     }

}
