package com.ai.chat.a.api.util;

import com.ai.chat.a.api.dto.GenerateLyricsPromptDTO;
import com.ai.chat.a.api.dto.GenerateSongPromptDTO;
import com.ai.chat.a.api.response.GenerateLyricsResponse;
import com.ai.chat.a.api.response.LyricsResponse;
import com.ai.chat.a.api.response.GenerateSongResponse;
import com.ai.chat.a.api.response.SongResponse;
import com.ai.chat.a.mq.ProcessHandle;
import com.ai.chat.a.properties.SunoProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 使用 okhttp 发送请求
 * */
@Component
@Slf4j
public class Request {
    @Resource
    private ProcessHandle processHandle;
    private final OkHttpClient client = new OkHttpClient();
    @Resource
    private SunoProperties properties;

    public void lyricsRequest(GenerateLyricsPromptDTO prompt) {
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(prompt), MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(properties.getBaseUrl() + "/suno/submit/lyrics")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + properties.getApiKey()).build();
        try{
          client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.info("error: {}", e);
                    log.info("call"+JSONObject.toJSONString(call));
                }

              @Override
              public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                  try (response) {
                      // 处理响应体内容
                      if (response.isSuccessful()) {
                          assert response.body() != null;
                          String responseBody = response.body().string();
                          log.info("收到响应: {}", responseBody);
                          LyricsResponse lyricsResponse = JSON.parseObject(responseBody, LyricsResponse.class);
                          processHandle.sendMessage(lyricsResponse);
                      } else {
                          log.error("Request failed with code: {}", response.code());
                      }
                  }
              }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("请求发送成功等待处理......");
    }
    public void songRequest(GenerateSongPromptDTO prompt){
        log.info(JSONObject.toJSONString(prompt));
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(prompt), MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(properties.getBaseUrl() + "/suno/submit/music")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + properties.getApiKey()).build();

        try{
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.info("error: {}", e);
                    log.info("call"+JSONObject.toJSONString(call));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (response) {
                        // 处理响应体内容
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            String responseBody = response.body().string();
                            log.info("收到响应: {}", responseBody);
                            SongResponse songResponse = JSON.parseObject(responseBody, SongResponse.class);
                            processHandle.sendMessage(songResponse);
                        } else {
                            log.error("Request failed with code: {}", response.code());
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("请求发送成功等待处理......");
    }
    public void generateSongRequest(String id){
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(properties.getBaseUrl() + "/suno/fetch/" + id)
                .addHeader("Authorization", "Bearer " + properties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        try{
            client.newCall(request).enqueue(new Callback(){
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    assert response.body() != null;
                    String processResponse = response.body().string();
                    log.info("收到响应: {}", processResponse);
                    log.info("time: {}", LocalDateTime.now());
                    GenerateSongResponse generateSongResponse = JSON.parseObject(processResponse, GenerateSongResponse.class);
                    if(generateSongResponse.getData().getProgress().equals("100%")){
                        // TODO 要将进度返回给前端
                        processHandle.sendMessage(generateSongResponse);
                    }else {
                        checkProgressWithDelaySong(id, 1300);
                        log.info("正在生成歌曲({})......",generateSongResponse.getData().getProgress());
                        // TODO 将进度回馈给前端，生成歌曲的进度占总进度的50%
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.info("error: {}", e);
                }
            });

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public void generateLyricsRequest(String id){
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(properties.getBaseUrl() + "/suno/fetch/" + id)
                .addHeader("Authorization", "Bearer " + properties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();
        try{
                client.newCall(request).enqueue(new Callback(){
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        assert response.body() != null;
                        String processResponse = response.body().string();
                        log.info("收到响应: {}", processResponse);
                        log.info("time: {}", LocalDateTime.now());
                        GenerateLyricsResponse generateLyricsResponse = JSON.parseObject(processResponse, GenerateLyricsResponse.class);
                        if(generateLyricsResponse.getData().getProgress().equals("100%")){
                            // TODO 要将进度返回给前端，并且调用接口生成歌曲
                            processHandle.sendMessage(generateLyricsResponse);
                        }else {
                              checkProgressWithDelay(id, 1000);
                              log.info("正在生成歌词({})......",generateLyricsResponse.getData().getProgress());
                              // TODO 将进度回馈给前端，生成歌词的进度占总进度的50%
                        }
                    }
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                          log.info("error: {}", e);
                    }
                });

        }catch (Exception e){
              throw new RuntimeException(e);
        }
    }

    private void checkProgressWithDelaySong(String id, int delayMillis){
        try {
            Thread.sleep(delayMillis);
            generateSongRequest(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error in delay: {}", e);
        }
    }
    private void checkProgressWithDelay(String id, int delayMillis) {
        try {
            Thread.sleep(delayMillis);
            generateLyricsRequest(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error in delay: {}", e);
        }
    }

}
