package com.ai.chat.a.milvus;
import com.ai.chat.a.utils.FileUtil;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AVectorDB {
    private final DocumentTransformer tokenTextSplitter;
    private final VectorStore vectorStore;

    /**
     * 暂时作为milvus的操作方法，包含上传，搜索和删除
     * */
    public List<String> addDocument(Resource resource) {
         try{
             List<Document> documents = new TikaDocumentReader(resource).get();
             List<Document> apply = tokenTextSplitter.apply(documents);
             log.info(JSONObject.toJSONString(apply));
             vectorStore.add(apply);
             log.info("上传成功");
             return apply.stream().map(Document::getId).toList();
         }catch (Exception e){
             e.printStackTrace();
             return null;
         }
     }
     public List<String> addDocument(String content){
        try{
            String documentId= UUID.randomUUID().toString();
            List<Document> documents = List.of(new Document(documentId, content, Map.of("id", ThreadLocalUtil.get())));
            List<Document> apply = tokenTextSplitter.apply(documents);
            vectorStore.add(apply);
            return apply.stream().map(Document::getId).toList();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
     }
     public List<String> addDocument(List<String> contentList){
        try{
            List<Document> documents = new ArrayList<>();
            contentList.forEach(content->{
                documents.add(new Document(UUID.randomUUID().toString(),content,Map.of("id",ThreadLocalUtil.get())));
            });
            List<Document> apply = tokenTextSplitter.apply(documents);
            vectorStore.add(apply);
            return apply.stream().map(Document::getId).toList();
        }catch (Exception e){
             e.printStackTrace();
             return null;
        }

     }
     public List<String> searchDocumentPrompt(String prompt,List<String> idList){
        // TODO query前先从数据库里获取当前用户的向量id，然后根据该id进行过滤搜索
         List<Document> documents = vectorStore.similaritySearch(SearchRequest
                 .query(prompt)
                 .withFilterExpression(new Filter.Expression(Filter.ExpressionType.IN,new Filter.Key("id"),new Filter.Value(idList))));
         return documents.stream().map(Document::getContent).toList();

     }

     public Boolean deleteDocument(List<String> ids){
        try{
            vectorStore.delete(ids);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
     }
}
