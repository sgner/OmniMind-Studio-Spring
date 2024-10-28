package com.ai.chat.a.starategy.FileHandle;

import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TextFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }
}
