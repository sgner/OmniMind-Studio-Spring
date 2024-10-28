package com.ai.chat.a.utils;

import com.ai.chat.a.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
@Slf4j
public class FileUtil {
    public static String detectFileType(MultipartFile file) {
        Tika tika = new Tika();
        try {
            log.info("文件类型为: " + tika.detect(file.getInputStream()));
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("未知文件");
        }
    }
    public static Resource convertMultipartFileToResource(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        return new InputStreamResource(inputStream);
    }

    public static String formatFileSize(Long fileLength) {
        String fileSizeString = "";
        if (fileLength == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        if (fileLength < 1024) {
            fileSizeString = df.format((double) fileLength) + "B";
        }
        else if (fileLength < 1048576) {
            fileSizeString = df.format((double) fileLength / 1024) + "K";
        }
        else if (fileLength < 1073741824) {
            fileSizeString = df.format((double) fileLength / 1048576) + "M";
        }
        else {
            fileSizeString = df.format((double) fileLength / 1073741824) + "G";
        }
        return fileSizeString;
    }
    public static int getFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return 5; // 无效文件或空文件
        }

        // 获取文件的 Content-Type
        String contentType = detectFileType(file);

        if (contentType == null) {
            return 5; // 无法获取文件类型
        }

        // 判断文件类型
        if (contentType.startsWith("image/")) {
            return 1; // 图片类型
        } else if (contentType.startsWith("video/")) {
            return 2; // 视频类型
        } else if (contentType.startsWith("text/")) {
            return 3; // 文本文件类型
        } else if (contentType.startsWith("audio/")) {
            return 4; // 音频文件类型
        }else if (contentType.contains("zip")|| contentType.contains("rar")|| contentType.contains("7z")|| contentType.contains("tar")|| contentType.contains("gzip") ||contentType.contains("bzip")) {
            return 6; //压缩文件
        }else if(contentType.contains("pdf")){
            return 7; //pdf文件
        }else if(contentType.contains("wordprocessingml")||contentType.contains(".ms-word")||contentType.contains("msword")){
            return 8; //word文件
        } else if (contentType.contains("presentationml")||contentType.contains("ms-powerpoint")) {
            return 9; //ppt文件
        }else if(contentType.contains("x-cfb")||contentType.contains("ms-excel")||contentType.contains("spreadsheetml.sheet")||contentType.contains("spreadsheetml.template")){
            return 10; //excel文件
        } else {
            if(StringTools.getFileSuffix(file.getOriginalFilename()).equals(".csv")){
                return 11; //csv文件
            }
            return 5; // 其他文件类型
        }
    }
}
