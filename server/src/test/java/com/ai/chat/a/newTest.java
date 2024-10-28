package com.ai.chat.a;

import org.junit.jupiter.api.Test;
import org.springframework.ai.model.Media;
import org.springframework.util.MimeType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class newTest {
    @Test
    public void extractJsonString(){
        String input = "{ \"type\":\"image\", \"fetch\":true, \"desc\":\"The image shows a person wearing a stylish black dress with thin straps. The background appears to be indoors with wood paneling. The person is accessorized with long earrings and a choker-style necklace.\", \"prompt\":\"fashionable black dress, indoor setting, elegant accessories, stylish appearance\" }";

        // 正则表达式匹配 JSON 格式
        String regex = "\\{[^}]*\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String jsonString = matcher.group();
            System.out.println(jsonString);
        } else {
            System.out.println("未找到 JSON 字符串");
        }
    }
    @Test
    public void test() throws MalformedURLException {
        String path = "https://web-tianci.oss-cn-beijing.aliyuncs.com/a-ai/71a956c9-8a9e-4d8f-83fa-dc25f86e7610.jpg";
        Media media = new Media(new MimeType("video"), new URL(path));
        System.out.println(media.getMimeType());
    }
}
