package com.ai.chat.a.milvus;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class AVectorDB {
    private final DocumentTransformer tokenTextSplitter;
    private final VectorStore vectorStore;
     public Boolean addDocument(Resource resource) {
         try{
             List<Document> documents = new TikaDocumentReader(resource).get();
             List<Document> apply = tokenTextSplitter.apply(documents);
             log.info(JSONObject.toJSONString(apply));
//             vectorStore.add(apply);
             log.info("上传成功");
             return true;
         }catch (Exception e){
             e.printStackTrace();
             return false;
         }
     }
}
