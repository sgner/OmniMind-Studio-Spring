package com.ai.chat.a.starategy.FileHandle;

import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class AudioFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        return null;
    }
}
