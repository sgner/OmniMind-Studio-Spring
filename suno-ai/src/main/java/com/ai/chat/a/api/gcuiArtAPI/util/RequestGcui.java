package com.ai.chat.a.api.gcuiArtAPI.util;

import com.ai.chat.a.api.gcuiArtAPI.dto.SunoFastDTO;
import com.ai.chat.a.properties.SunoGcuiProperties;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestGcui {
    private final SunoGcuiProperties sunoGcuiProperties;

    // 配置代理，需要使用VPN
    private final  Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809));
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .build();
    public void GenerateSongRequest(SunoFastDTO sunoDTO){
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(sunoDTO), MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sunoGcuiProperties.getBaseUrl()+"/api/get_limit")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String processResponse = response.body().string();
                log.info(processResponse);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.info("AI 生成失败");
                e.printStackTrace();
            }
        });
        log.info("生成任务已提交......");
    }


}
