package com.ai.chat.a.api.textCorrection;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
@Data
@NoArgsConstructor
@Component
@Slf4j
public class XfWebTextCorrectionClient {
    // 地址与鉴权信息
    private static String hostUrl;
    private static String appid;
    private static String apiSecret;
    private static String apiKey;
    private XfTextCorrectionbw xfTextCorrectionbw;
    // json
    private static Gson gson = new Gson();
public XfWebTextCorrectionClient(String iAppid,String iApiSecret,String iApiKey,String iHostUrl,XfTextCorrectionbw xfTextCorrectionbw){
      appid=iAppid;
      apiSecret=iApiSecret;
      apiKey=iApiKey;
      hostUrl= iHostUrl;
      this.xfTextCorrectionbw= xfTextCorrectionbw;
       xfTextCorrectionbw.call();
       log.info("文本纠错初始化成功！");
 }
public String call(String text) throws Exception {
    String url = getAuthUrl(apiKey, apiSecret);
    String json = getRequestJson(text);
    String backResult = doPostJson(url, json);
    log.info("文本纠错返回结果：" + backResult);
    JsonParse jsonParse = gson.fromJson(backResult, JsonParse.class);
    String base64Decode = new String(Base64.getDecoder().decode(jsonParse.payload.result.text), StandardCharsets.UTF_8);
    log.info("text字段base64解码后纠错信息：" + base64Decode);
    return base64Decode;
}

// 请求参数json拼接
//    public static String getRequestJsonWithBW(String text){
//        return "{\n" +
//                "  \"header\": {\n" +
//                "    \"app_id\": \"" + appid + "\",\n" +
//                "    \"uid\": \""+XfTextCorrectionbw.uid+"\",\n" +
//                "    \"status\": 3\n" +
//                "  },\n" +
//                "  \"parameter\": {\n" +
//                "    \"s9a87e3ec\": {\n" +
//                "    \"res_id\": \""+XfTextCorrectionbw.res_id+"\",\n" +
//                "      \"result\": {\n" +
//                "        \"encoding\": \"utf8\",\n" +
//                "        \"compress\": \"raw\",\n" +
//                "        \"format\": \"json\"\n" +
//                "      }\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"payload\": {\n" +
//                "    \"input\": {\n" +
//                "      \"encoding\": \"utf8\",\n" +
//                "      \"compress\": \"raw\",\n" +
//                "      \"format\": \"plain\",\n" +
//                "      \"status\": 3,\n" +
//                "      \"text\": \"" + getBase64TextData(text) + "\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//    }
    public String getRequestJson(String text) {
        return "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appid + "\",\n" +
                "    \"status\": 3\n" +
                "  },\n" +
                "  \"parameter\": {\n" +
                "    \"s9a87e3ec\": {\n" +
                "      \"result\": {\n" +
                "        \"encoding\": \"utf8\",\n" +
                "        \"compress\": \"raw\",\n" +
                "        \"format\": \"json\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"input\": {\n" +
                "      \"encoding\": \"utf8\",\n" +
                "      \"compress\": \"raw\",\n" +
                "      \"format\": \"plain\",\n" +
                "      \"status\": 3,\n" +
                "      \"text\": \"" + getBase64TextData(text) + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    // 读取文件
    public String getBase64TextData(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    // 根据json和url发起post请求
    public String doPostJson(String url, String json) {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
            resultString = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    // 鉴权方法
    public String getAuthUrl(String apiKey, String apiSecret) throws Exception {
       URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "POST " + url.getPath() + " HTTP/1.1";
        //System.out.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }

    //返回的json结果拆解
    static class JsonParse {
        Payload payload;
    }

    static class Payload {
        Result result;
    }

    static class Result {
        String text;
    }
}