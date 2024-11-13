package com.ai.chat.a.api.lumalabsApi.request;

import com.ai.chat.a.api.lumalabsApi.dto.TtvDTO;
import com.ai.chat.a.api.lumalabsApi.response.GenVideoResponse;
import com.ai.chat.a.mq.GenVideoSender;
import com.ai.chat.a.properties.LumaProperties;
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
public class GenVideoRequest {
    private final LumaProperties lumaProperties;
    private final GenVideoSender genVideoSender;
    private final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809));
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .build();
    public void ttv(TtvDTO ttvDTO, String userId, String sessionId){
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(ttvDTO), MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                      .url(lumaProperties.getBaseUrl()+"/generations")
                      .addHeader("accept","application/json")
                      .addHeader("authorization","Bearer "+lumaProperties.getApiKey())
                      .addHeader("content-type","application/json")
                      .post(requestBody)
                      .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                 log.info("响应失败:   ");
                 e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String result = response.body().string();
                GenVideoResponse genVideoResponse = JSONObject.parseObject(result, GenVideoResponse.class);

            }
        });

    }

}
