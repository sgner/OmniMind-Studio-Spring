package com.ai.chat.a.starategy.FileHandle;

import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class WordFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 判断文件后缀
        if (fileName.endsWith(".docx")) {
            return handleDocxFile(file);
        } else if (fileName.endsWith(".doc")) {
            return handleDocFile(file);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: 只支持 .doc 和 .docx 文件");
        }
    }
    private String handleDocxFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            // 获取所有段落内容并合并为一个字符串
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    // 处理 .doc 文件
    private String handleDocFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            // 提取文本内容
            return extractor.getText();
        }
    }
}
