package com.ai.chat.a.api.xfXh.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ai.chat.a.api.xfXh.dto.AgentCharactersDto;
import com.ai.chat.a.api.xfXh.response.AgentCharacter;
import com.ai.chat.a.api.xfXh.response.ResponseMsg;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
@NoArgsConstructor
@Component
@Slf4j
public class AgentUtil {
    private String appId;
    private String secret;
    private String url;
    private final static OkHttpClient client = new OkHttpClient();

    private static final String suffixUrl = "/open/agent";

    public AgentUtil(String appId, String secret, String url){
         this.appId = appId;
         this.secret = secret;
         this.url = url;
    }

//    public String createAgentCharacter(String playerId) throws Exception {
//        String createUrl = url + suffixUrl + "/edit-character";
//        log.info("url:" + createUrl);
//        log.info("appId: {}",appId);
//        log.info("playerId: {}",playerId);
//        log.info("agentName: {}",agentName);
//        log.info("agentType: {}",agentType);
//        log.info("description: {}",description);
//        AgentCharactersDto charactersDto = AgentCharactersDto.builder()
//                .appId(appId)
//                .playerId(playerId)
//                .agentName(agentName)
//                .agentType(agentType)
//                .description(description).build();
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(charactersDto));
//        long ts = System.currentTimeMillis();
//        Request request = new Request.Builder()
//                .url(createUrl)
//                .post(requestBody)
//                .addHeader("appId",appId)
//                .addHeader("timestamp", String.valueOf(ts))
//                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
//                .build();
//        Response response = client.newCall(request).execute();
//        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
//        });
//        log.info(responseMsg+"");
//        if (responseMsg.getCode() != 10000){
//            throw new Exception("创建人格失败，responseMsg =" + responseMsg);
//        }
//        return responseMsg.getData();
//    }
    public String createAgentCharacter(String mission,String keyPersonality,String playerId, String agentName, String agentType, String description, String personalityDescription, String identity, String hobby, String openingIntroduction) throws Exception {
        String createUrl = url + suffixUrl + "/edit-character";
        log.info("url:" + createUrl);
        log.info("appId: {}",appId);
        log.info("playerId: {}",playerId);
        log.info("agentName: {}",agentName);
        log.info("agentType: {}",agentType);
        log.info("description: {}",description);
        AgentCharactersDto charactersDto = AgentCharactersDto.builder()
                .appId(appId)
                .playerId(playerId)
                .agentName(agentName)
                .agentType(agentType)
                .description(description)
                .personalityDescription(personalityDescription)
                .hobby(hobby)
                .identity(identity)
                .openingIntroduction(openingIntroduction)
                .mission(mission)
                .keyPersonality(keyPersonality)
                .build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(charactersDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(createUrl)
                .post(requestBody)
                .addHeader("appId",appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
        });
        log.info(responseMsg+"");
        if (responseMsg.getCode() != 10000){
            throw new Exception("创建人格失败，responseMsg =" + responseMsg);
        }
        return responseMsg.getData();
    }
    public void editAgentCharacter(String playerId, String agentId,String agentName,String agentType,String description) throws Exception {
        String editUrl = url + suffixUrl + "/edit-character";
        log.info("url:" + editUrl);
        AgentCharactersDto charactersDto = AgentCharactersDto.builder()
                .appId(appId)
                .agentId(agentId)
                .playerId(playerId)
                .agentName(agentName)
                .agentType(agentType)
                .description(description).build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(charactersDto));
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(editUrl)
                .post(requestBody)
                .addHeader("appId",appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<String> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<String>>() {
        });
        log.info(responseMsg+"");
        if (responseMsg.getCode() != 10000){
            throw new Exception("编辑人格失败，responseMsg =" + responseMsg);
        }
    }

    public void getAgentCharacter(String agentId) throws Exception {
        String getUrl = url + suffixUrl + "/get-character";
        StringBuilder sb = new StringBuilder();
        sb.append(getUrl).append("?appId=").append(appId).append("&agentId=").append(agentId);
        log.info("url:" + sb);
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(sb.toString())
                .addHeader("appId",appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<AgentCharacter> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<AgentCharacter>>() {
        });
        log.info(responseMsg+"");
        if (responseMsg.getCode() != 10000){
            throw new Exception("获取人格失败，responseMsg =" + responseMsg);
        }
    }

    public void deleteAgentCharacter(String agentId) throws Exception {
        String deleteUrl = url + suffixUrl + "/delete-character";
        StringBuilder sb = new StringBuilder();
        sb.append(deleteUrl).append("?appId=").append(appId).append("&agentId=").append(agentId);
        log.info("url:" + sb);
        long ts = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(sb.toString())
                .delete()
                .addHeader("appId",appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", AuthUtil.getSignature(appId, secret, ts))
                .build();
        Response response = client.newCall(request).execute();
        ResponseMsg<Boolean> responseMsg = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseMsg<Boolean>>() {
        });
        log.info(responseMsg+"");
        if (responseMsg.getCode() != 10000){
            throw new Exception("删除人格失败，responseMsg =" + responseMsg);
        }
    }

}
