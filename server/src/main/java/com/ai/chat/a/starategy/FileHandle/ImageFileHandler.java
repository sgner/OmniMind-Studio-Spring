package com.ai.chat.a.starategy.FileHandle;
import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Component
@Slf4j
public class ImageFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        return null;
    }
}
