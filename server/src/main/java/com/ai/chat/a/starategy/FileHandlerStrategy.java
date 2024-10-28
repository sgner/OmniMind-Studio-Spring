package com.ai.chat.a.starategy;

import com.ai.chat.a.entity.ReadMediaFile;
import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileHandlerStrategy {
   String handleFile(MultipartFile file) throws IOException;
}
