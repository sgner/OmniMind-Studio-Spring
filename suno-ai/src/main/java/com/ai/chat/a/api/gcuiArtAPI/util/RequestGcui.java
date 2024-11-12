package com.ai.chat.a.api.gcuiArtAPI.util;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoAudioResponseDTO;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoCustomDTO;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoDTO;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoFastDTO;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoAudioResponse;
import com.ai.chat.a.mq.SunoGcuiProcessSender;
import com.ai.chat.a.properties.SunoGcuiProperties;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestGcui {
    private final SunoGcuiProperties sunoGcuiProperties;
    private final SunoGcuiProcessSender sunoGcuiProcessSender;

    // 配置代理，需要使用VPN
    private final  Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809));
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .build();
    public void GenerateSongRequest(SunoDTO sunoDTO, String userId, String sessionId){
        String url = "/api/generate";
        if(sunoDTO instanceof SunoCustomDTO){
            url = "/api/custom_generate";
        }
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(sunoDTO), MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sunoGcuiProperties.getBaseUrl()+url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String processResponse = response.body().string();
                // TODO 发送消息
                List<SunoAudioResponse> sunoAudioResponses = JSONObject.parseArray(processResponse, SunoAudioResponse.class);
                log.info(sunoAudioResponses.toString());
                List<SunoAudioResponseDTO> sunoAudioResponseDTOS = BeanUtil.copyToList(sunoAudioResponses, SunoAudioResponseDTO.class);
                sunoAudioResponseDTOS.forEach(item ->{
                     item.setSessionId(sessionId);
                     item.setUserId(userId);
                });
                sunoGcuiProcessSender.sendMessage(sunoAudioResponseDTOS);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.info("AI 生成失败");
                e.printStackTrace();
                // TODO 推送到前端
            }
        });
        log.info("生成任务已提交......");
    }
public void getGenerateSongRequest(@Nullable String ids,int retryCount,String userId,String sessionId){
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(sunoGcuiProperties.getBaseUrl() + "/api/get")).newBuilder();
        urlBuilder.addQueryParameter("ids",ids);
        String url = urlBuilder.build().toString();
        log.info(url);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).build();
         okHttpClient.newCall(request).enqueue(new Callback(){
             @Override
             public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                 assert response.body() != null;
                 String getSongResponse = response.body().string();
                 log.info(getSongResponse);
                 List<SunoAudioResponse> sunoAudioResponses = JSONObject.parseArray(getSongResponse, SunoAudioResponse.class);
                 log.info(sunoAudioResponses.toString());
                 if(!sunoAudioResponses.isEmpty()){
                     List<SunoAudioResponse> list = sunoAudioResponses.stream().filter(item -> !item.getAudioUrl().isEmpty()).toList();
                     if(list.isEmpty()&& retryCount <10){
                           checkProgressWithDelaySong(ids,3000,retryCount+1,userId,sessionId);
                     }else {
                         if(retryCount >= 10){
                              log.info("超时");
                              // TODO 反馈
                         }else {
                              log.info("成功");
                             log.info(sunoAudioResponses.toString());
                             List<SunoAudioResponseDTO> sunoAudioResponseDTOS = BeanUtil.copyToList(sunoAudioResponses, SunoAudioResponseDTO.class);
                             sunoAudioResponseDTOS.forEach(item->{
                                  item.setUserId(userId);
                                  item.setSessionId(sessionId);
                             });
                             sunoGcuiProcessSender.endProcess(sunoAudioResponseDTOS);
                         }
                         getLimitRequest();
                         // TODO 推送到前端
                     }
                 }

             }

             @Override
             public void onFailure(@NotNull Call call, @NotNull IOException e) {
                 log.info("AI 获取失败");
             }
         });
    }

    public void getLimitRequest() throws IOException {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(sunoGcuiProperties.getBaseUrl()+"/api/get_limit")
                    .build();
        Response response = okHttpClient.newCall(request).execute();
        String string = response.body().string();
        log.info(string);
    }
    public void getGeneratedSongRequest(String ids,String userId){
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(sunoGcuiProperties.getBaseUrl() + "/api/get")).newBuilder();
        urlBuilder.addQueryParameter("ids",ids);
        String url = urlBuilder.build().toString();
        log.info(url);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).build();

    }

    private void checkProgressWithDelaySong(String id, int delayMillis,int retryCount,String userId,String sessionId){
        try {

            Thread.sleep(delayMillis);
            getGenerateSongRequest(id,retryCount,userId,sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error in delay: {}", e);
        }
    }

}
