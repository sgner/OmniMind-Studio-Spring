package com.ai.chat.a.redis;

import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.dto.SysSettingDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisComponent {
       private final StringRedisTemplate stringRedisTemplate;
       public void saveSystemSettingInRedis(SysSettingDTO sysSettingDTO){
           if(sysSettingDTO == null){
               sysSettingDTO = new SysSettingDTO();
           }
           String sysSetting = JSONObject.toJSONString(sysSettingDTO);
           stringRedisTemplate.opsForValue().set(Constants.REDIS_KEY_SYS_SETTING,sysSetting);
       }
       public SysSettingDTO getSystemSettingFromRedis(){
           return JSONObject.parseObject(stringRedisTemplate.opsForValue().get(Constants.REDIS_KEY_SYS_SETTING),SysSettingDTO.class);
       }
       public void removeSystemSettingFromRedis(){
           stringRedisTemplate.delete(Constants.REDIS_KEY_SYS_SETTING);
       }

       public void saveUploadFileInRedis(String key,String FilePath){
            stringRedisTemplate.opsForValue().set(key,FilePath,5, TimeUnit.MINUTES);
       }
       public String getUploadFileFromRedis(String key){
             return stringRedisTemplate.opsForValue().get(key);
       }
       public void removeUploadFileFromRedis(String key){
           stringRedisTemplate.delete(key);
       }
}
