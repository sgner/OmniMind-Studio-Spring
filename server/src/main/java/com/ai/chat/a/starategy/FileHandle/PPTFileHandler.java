package com.ai.chat.a.starategy.FileHandle;

import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class PPTFileHandler implements FileHandlerStrategy {

    @Override
    public String handleFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (fileName.endsWith(".pptx")) {
            return handlePptxFile(file);
        } else if (fileName.endsWith(".ppt")) {
            return handlePptFile(file);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: 只支持 .ppt 和 .pptx 文件");
        }
    }

    private String handlePptxFile(MultipartFile file) throws IOException {
        // 使用 XMLSlideShow 处理 pptx 文件
        try (InputStream inputStream = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(inputStream)) {

            StringBuilder content = new StringBuilder();
            // 获取所有幻灯片
            for (XSLFSlide slide : ppt.getSlides()) {
                // 获取幻灯片中的形状
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        // 提取文本
                        content.append(textShape.getText()).append("\n");
                    }
                }
            }
            return content.toString();
        }
    }

    private String handlePptFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             HSLFSlideShow ppt = new HSLFSlideShow(inputStream)) {

            StringBuilder content = new StringBuilder();
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        content.append(textShape.getText()).append("\n");
                    }
                }
            }
            return content.toString();
        }
    }
}
