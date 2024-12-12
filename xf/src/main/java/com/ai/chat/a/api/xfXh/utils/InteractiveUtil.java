package com.ai.chat.a.api.xfXh.utils;

import cn.hutool.json.JSONUtil;
import com.ai.chat.a.api.xfXh.dto.InteractiveRequest;
import com.ai.chat.a.api.xfXh.dto.InteractiveResponse;
import com.ai.chat.a.dto.CosResponseDTO;
import com.ai.chat.a.mq.CosResponseSender;
import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ai.chat.a.api.xfXh.dto.InteractiveDto;
import com.ai.chat.a.api.xfXh.response.ResponseMsg;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: yxliu37
 * @Date: 2024/3/21 10:35
 * @Description: 玩家人格交互
 */
@Component
@Slf4j
public class  InteractiveUtil {

    private static final String suffixUrl = "/open/interactive";
    private final static OkHttpClient client = new OkHttpClient();
    @Resource
    private CosResponseSender cosResponseSender;

    /**
     * 玩家人格会话
     * @param url
     * @param appId
     * @param userId
     * @param agentId
     * @param chatId
     * @param preChatId
     * @param context
     * @param secret
     */
    public StringBuffer chat(String url, String appId, String userId, String agentId, String chatId, String preChatId, List<InteractiveRequest.Text> context, String secret,Role role,Player player,String sessionId,String playUserId) {
        //构造请求
        InteractiveRequest interactiveRequest = new InteractiveRequest();

        //设置请求头
        final InteractiveRequest.Header header = new InteractiveRequest.Header();
        header.setApp_id(appId);
        header.setUid(userId);
        header.setAgent_id(agentId);
        interactiveRequest.setHeader(header);

        //设置parameter
        InteractiveRequest.Parameter parameter = new InteractiveRequest.Parameter();
        InteractiveRequest.Chat chat = new InteractiveRequest.Chat();
        chat.setChat_id(chatId);
        //如重新开启会话比希望连续上次会话，需要带上pre_chat_id
        chat.setPre_chat_id(preChatId);
        parameter.setChat(chat);
        interactiveRequest.setParameter(parameter);

        //设置payload
        InteractiveRequest.Payload payload = new InteractiveRequest.Payload();
        InteractiveRequest.Message message = new InteractiveRequest.Message();
        message.setText(context);
        payload.setMessage(message);
        interactiveRequest.setPayload(payload);

        // 构造url鉴权
        long ts = System.currentTimeMillis();
        String signature = AuthUtil.getSignature(appId, secret, ts);
        String requestUrl = url + chatId + "?" + "appId=" + appId + "&timestamp=" + ts + "&signature=" + signature;
        // ws
        Request wsRequest = (new Request.Builder()).url(requestUrl).build();
        final OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        final StringBuffer buffer = new StringBuffer();
        WebSocket webSocket = okHttpClient.newWebSocket(wsRequest, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                log.info("websocket close");
                webSocket.close(1002, "websocket finish");
                okHttpClient.connectionPool().evictAll();
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                log.info("websocket failure");
                webSocket.close(1001, "websocket finish");
                okHttpClient.connectionPool().evictAll();
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    InteractiveResponse response = JSONObject.parseObject(text, InteractiveResponse.class);
                    if (response.getHeader().getCode() == 0) {
                        log.info("会话成功！");
                        List<InteractiveResponse.Text> textList = response.getPayload().getChoices().getText();
                        buffer.append(textList.get(0).getContent());
                        if (response.getHeader().getStatus() == 2) {
                            log.info("回答结束，回答内容：" + buffer);
                            log.info("本轮问答用量：" + JSONUtil.toJsonStr(response.getPayload().getUsage()));
                            cosResponseSender.sendMessage(CosResponseDTO.builder().context(JSONObject.toJSONString(context)).userId(playUserId).sessionId(sessionId).message(buffer.toString()).player(player).role(role).build());
                            webSocket.close(1000, "websocket finish");
                            okHttpClient.connectionPool().evictAll();
                        }
                    }

                } catch (Exception e) {
                    log.info("会话异常！异常信息：" + text);
                    webSocket.close(1000, "websocket error");
                }
            }
        });
        log.info("requestUrl:" + requestUrl);
        String sentText = JSONUtil.toJsonStr(interactiveRequest);
        log.info("sendText:" + sentText);
        webSocket.send(sentText);
        return buffer;
    }

    /**
     * 新增短期记忆
     * @param url
     * @param secret
     * @param appId
     * @param agentId
     * @param interactionType
     * @param description
     * @throws Exception
     */
    public void generate(String url, String secret, String appId, String agentId, String interactionType, String description, String playerInvolved, String agentInvolved) throws Exception {
        url = url + suffixUrl + "/generate";
        System.out.println("url:" + url);
        InteractiveDto interactiveDto = InteractiveDto.builder()
                .appId(appId)
                .agentId(agentId)
                .interactionType(interactionType)
                .description(description)
                .playerInvolved(playerInvolved)
                .agentInvolved(agentInvolved)
                .build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(interactiveDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
        });
        System.out.println(responseMsg);
        if (responseMsg.getCode() != 10000) {
            throw new Exception("生成记忆失败，responseMsg =" + responseMsg);
        }
    }

    /**
     * 全新会话
     * @param url
     * @param secret
     * @param appId
     * @param chatId
     * @throws Exception
     */
    public void clearCache(String url, String secret, String appId, String chatId) throws Exception {
        url = url + suffixUrl + "/clear-cache";
        System.out.println("url:" + url);

        FormBody formBody = new FormBody.Builder()
                .add("appId",appId)
                .add("chatId",chatId)
                .build();
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
        });
        System.out.println(responseMsg);
        if (responseMsg.getCode() != 10000) {
            throw new Exception("重新会话失败，responseMsg =" + responseMsg);
        }
    }
}
