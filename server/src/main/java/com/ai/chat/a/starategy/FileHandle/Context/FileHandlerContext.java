package com.ai.chat.a.starategy.FileHandle.Context;

import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.starategy.FileHandle.*;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Component
public class FileHandlerContext {
    private final Map<String, FileHandlerStrategy> strategyMap = new HashMap<>();
    public FileHandlerContext(){
        strategyMap.put("image",new ImageFileHandler());
        strategyMap.put("video",new VideoFileHandler());
        strategyMap.put("audio",new AudioFileHandler());
        strategyMap.put("text",new TextFileHandler());
        strategyMap.put("csv",new CsvFIleHandler());
        strategyMap.put("pdf",new PDFFileHandler());
        strategyMap.put("zip",new ZipFileHandler());
        strategyMap.put("rar",new ZipFileHandler());
        strategyMap.put("7z",new ZipFileHandler());
        strategyMap.put("tar",new ZipFileHandler());
        strategyMap.put("gzip",new ZipFileHandler());
        strategyMap.put("bzip",new ZipFileHandler());
        strategyMap.put("wordprocessingml",new WordFileHandler());
        strategyMap.put(".ms-word",new WordFileHandler());
        strategyMap.put("msword",new WordFileHandler());
        strategyMap.put("x-tika-ooxml",new WordFileHandler());
        strategyMap.put("presentationml",new PPTFileHandler());
        strategyMap.put(".ms-powerpoint",new PPTFileHandler());
        strategyMap.put("x-cfb",new ExcelFileHandler());
        strategyMap.put("ms-excel",new ExcelFileHandler());
        strategyMap.put("spreadsheetml.sheet",new ExcelFileHandler());
        strategyMap.put("spreadsheetml.template",new ExcelFileHandler());
    }
    public String handleFile(String mimeType, MultipartFile file) throws IOException {
        for(String key: strategyMap.keySet()){
             if (mimeType.contains(key)){
                  return Constants.FILE_PRE_PROMPT+Constants.FILE_PRE_TYPE+mimeType+Constants.FILE_PRE+strategyMap.get(key).handleFile(file)+Constants.FILE_SUF_PROMPT;
             }
        }
       return null;
    }
}
