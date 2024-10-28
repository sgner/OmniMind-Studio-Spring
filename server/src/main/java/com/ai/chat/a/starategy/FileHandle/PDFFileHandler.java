package com.ai.chat.a.starategy.FileHandle;

import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PDFFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}
