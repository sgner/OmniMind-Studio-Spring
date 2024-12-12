package com.ai.chat.a.api.xfXh.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ai.chat.a.api.xfXh.dto.PlayerDto;
import com.ai.chat.a.api.xfXh.response.ResponseMsg;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@NoArgsConstructor
@Slf4j
/**
 * @Author: yxliu37
 * @Date: 2024/3/21 16:07
 * @Description: 玩家相关
 */
public class PlayerUtil {
    private String url;
    private String appId;
    private String secret;

    private final static OkHttpClient client = new OkHttpClient();

    private static final String suffixUrl = "/open/player";

    public PlayerUtil(String url, String appid, String apiSecret) {
          this.url = url;
          this.appId = appid;
          this.secret = apiSecret;
    }

    /**
     * 检查玩家是否注册
     * @param playerName
     * @throws Exception
     */
    public Boolean ifRegister(String playerName) throws Exception {
       String ifRegisterUrl = url + suffixUrl + "/if-register"
                + "?appId=" + appId
                + "&playerName=" + playerName;
        log.info("url:" + ifRegisterUrl);
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(ifRegisterUrl)
                .get()
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", Objects.requireNonNull(AuthUtil.getSignature(appId, secret, ts)))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<Boolean> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<Boolean>>() {
        });
        log.info(responseMsg.toString());
        if (responseMsg.getCode() == null ||responseMsg.getCode() != 10000) {
            log.info("查询失败，responseMsg =" + responseMsg);
            return false;
        }
        return responseMsg.getData();
    }

    /**
     * 注册玩家
     * @param playerName
     * @throws Exception
     */
    public String register(String playerName,String playerType,String desc,String senderIdentity) throws Exception {
        String registerUrl = url + suffixUrl + "/register";
        log.info("url:" + registerUrl);
        PlayerDto playerDto = PlayerDto.builder()
                .appId(appId)
                .playerName(playerName)
                .playerType(playerType)
                .description(desc)
                .senderIdentity(senderIdentity)
                .build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),JSON.toJSONString(playerDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(registerUrl)
                .post(requestBody)
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
        });
        log.info(responseMsg+"");
        if (responseMsg.getCode() != 10000) {
            throw new Exception("注册玩家失败，responseMsg =" + responseMsg);
        }
        return responseMsg.getData();
    }

    /**
     * 编辑玩家信息
     * @param playerId
     * @param playerName
     * @throws Exception
     */
    public void modify(String playerId, String playerName,String playerType,String desc,String identity) throws Exception {
        String modifyUrl= url + suffixUrl + "/modify";
        System.out.println("url:" + modifyUrl);
        PlayerDto playerDto = PlayerDto.builder()
                .playerId(playerId)
                .appId(appId)
                .playerName(playerName)
                .playerType(playerType)
                .description(desc)
                .senderIdentity(identity)
                .build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(playerDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(modifyUrl)
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
            throw new Exception("编辑玩家失败，responseMsg =" + responseMsg);
        }
    }

    /**
     * 删除玩家
     * @param playerId
     * @param playerName
     * @throws Exception
     */
    public void delete(String playerId, String playerName) throws Exception {
       String deleteUrl = url + suffixUrl + "/delete";
        System.out.println("url:" + deleteUrl);
        PlayerDto playerDto = PlayerDto.builder()
                .playerId(playerId)
                .appId(appId)
                .playerName(playerName).build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(playerDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(deleteUrl)
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
            throw new Exception("编辑玩家失败，responseMsg =" + responseMsg);
        }
    }
}
