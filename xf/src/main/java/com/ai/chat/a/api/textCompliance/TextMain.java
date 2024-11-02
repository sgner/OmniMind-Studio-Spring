package com.ai.chat.a.api.textCompliance;

import com.ai.chat.a.api.textCompliance.utils.MyUtil;

import java.util.Map;

/**
 * 1、文本内容合规审核接口
 * 2、appid与secret信息请在控制台获取 https://console.xfyun.cn/services/text_audit
 */
public class TextMain {
    private static String url = "https://audit.iflyaisol.com//audit/v2/syncText";
    private static final String APPID = Constants.APPID;
    private static final String APISecret = Constants.APISecret;
    private static final String APIKey = Constants.APIKey;

    private static final String content = "塔利班组织联合东突组织欲图";// 送检文本

    // 词库指定
    private static final String lib_ids_1 = "xxx"; // 根据自己创建获取词库ID  黑名单
    private static final String lib_ids_2 = "xxx"; // 根据自己创建获取词库ID  白名单

    public static void main(String[] args) throws Exception {
        /**
         * 业务参数
         * --- 如果需要使用黑白名单资源，放开lib_ids与categories参数
         * */
        String json = "{\n" +
                "  \"is_match_all\": 1,\n" +
                "  \"content\": \"" + content + "\"\n" + // 放开lib_ids与categories参数，注意在content后面加逗号使之成为合法json
              /*  "  \"lib_ids\": [\n" +
                "    \"" + lib_ids_1 + "\",\n" +
                "    \"" + lib_ids_2 + "\"\n" +
                "  ],\n" +
                "  \"categories\": [\n" +
                "    \"pornDetection\",\n" +
                "    \"violentTerrorism\",\n" +
                "    \"political\",\n" +
                "    \"lowQualityIrrigation\",\n" +
                "    \"contraband\",\n" +
                "    \"advertisement\",\n" +
                "    \"uncivilizedLanguage\"\n" +
                "  ]\n" +*/
                "}";
        // 获取鉴权
        Map<String, String> urlParams = MyUtil.getAuth(APPID, APIKey, APISecret);
        // 发起请求
        String returnResult = MyUtil.doPostJson(url, urlParams, json);
        System.out.println("文本合规返回结果：\n" + returnResult);
    }
}
