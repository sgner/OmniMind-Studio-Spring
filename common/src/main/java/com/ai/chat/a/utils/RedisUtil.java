package com.ai.chat.a.utils;

import com.ai.chat.a.constant.UserConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;
      public String getUserHeartBeat(String userId){
           return  redisTemplate.opsForValue().get(userId);
      }
      public void setUserHeartBeat(String userId){
          redisTemplate.opsForValue().set(UserConstant.USER_WS_HEART_BEAT+userId,userId,6, TimeUnit.SECONDS);
      }
      public void removeUserHeartBeat(String userId){
          redisTemplate.delete(UserConstant.USER_WS_HEART_BEAT+userId);
      }
      public void removeUserRobotCache(String userId){
            redisTemplate.delete("setRobotListCache::"+userId);
      }
}
