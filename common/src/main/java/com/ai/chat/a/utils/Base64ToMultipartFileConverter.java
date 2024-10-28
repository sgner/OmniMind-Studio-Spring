package com.ai.chat.a.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import org.springframework.mock.web.MockMultipartFile;
public class Base64ToMultipartFileConverter {

    public static MultipartFile base64ToMultipartFile(String base64, String fileName) throws IOException {
        // 去除Base64前缀（如果有）
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }

        // 将Base64字符串解码为字节数组
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        // 创建 MultipartFile 对象，fileName 是文件名，第二个参数是内容类型，最后是字节数组
        return new MockMultipartFile(fileName, fileName, "application/octet-stream", decodedBytes);
    }

}
