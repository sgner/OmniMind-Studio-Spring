package com.ai.chat.a.api.textCorrection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
@Component
@NoArgsConstructor
@Data
@Slf4j
public class XfTextCorrectionbw {
    private static String hostUrl = "";
    private static String appid = "";
    // 个性化资源唯一标识，由字母、数字或下划线组成
    public static String res_id = "";
    // 黑名单
    private static String black_list="";
    // 白名单
    private static String white_list = "";
    public static String uid = UUID.randomUUID().toString();
    public void call(){
            String json = getRequestJson();
//            log.info(json);
            String backResult = doPostJson(hostUrl, json);
            log.info("上传资源返回结果：" + backResult);
    }
    public XfTextCorrectionbw(String iBlack_list, String iWhite_list,String ires_id,String iHostUrl,String iAppid) {
        black_list = iBlack_list;
        white_list = iWhite_list;
        res_id = ires_id;
        hostUrl = iHostUrl;
        appid = iAppid;
        log.info("黑白名单加载完成");
    }
    // 请求参数json拼接
    public static String getRequestJson() {
        return "{\n" +
                "  \"common\": {\n" +
                "    \"app_id\": \"" + appid + "\",\n" +
                "    \"uid\": \"" + uid + "\"\n" +
                "  },\n" +
                "  \"business\": {\n" +
                "    \"res_id\": \"" + res_id + "\"\n" +
                "  },\n" +
                "  \"data\": \"" + getBase64TextData(black_list, white_list) + "\"\n" +
                "}";
    }

    // 读取文件
    public static String getBase64TextData(String black_list, String white_list) {
        String tempJson = "{\n" +
                "  \"black_list\":  \"" + black_list + "\",\n" +
                "  \"white_list\": \"" + white_list + "\"\n" +
                "}";
        return Base64.getEncoder().encodeToString(tempJson.getBytes());
    }

    // 根据json和url发起post请求
    public static String doPostJson(String url, String json) {
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
}
