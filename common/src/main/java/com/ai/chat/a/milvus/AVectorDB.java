package com.ai.chat.a.milvus;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AVectorDB {
    private final DocumentTransformer tokenTextSplitter;
    private final VectorStore vectorStore;

    /**
     * 暂时作为milvus的操作方法，包含上传，搜索和删除
     * */
    public Boolean addDocument(Resource resource) {
         try{
             List<Document> documents = new TikaDocumentReader(resource).get();
             List<Document> apply = tokenTextSplitter.apply(documents);
             log.info(JSONObject.toJSONString(apply));
             vectorStore.add(apply);
             log.info("上传成功");
             return true;
         }catch (Exception e){
             e.printStackTrace();
             return false;
         }
     }
     public Boolean addDocument(String content){
        try{
            List<Document> documents = List.of(new Document(UUID.randomUUID().toString(), content, Map.of("id", ThreadLocalUtil.get())));
            List<Document> apply = tokenTextSplitter.apply(documents);
            vectorStore.add(apply);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
     }
     public Prompt searchDocumentPrompt(String prompt,String model){

        // TODO query前先从数据库里获取当前用户的向量id，然后根据该id进行过滤搜索

         List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(prompt));
         List<String> contentList = documents.stream().map(Document::getContent).toList();
         SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(
                """
                {content}
                上面是相关的资料和历史记录作为参考，现在请你结合该资料和你自己的认知回答下面用户的问题:\s
                {question}
                """
         );
         return systemPromptTemplate.create(Map.of("content", contentList, "question", prompt), OpenAiChatOptions
                 .builder()
                 .withModel(model)
                 .withHttpHeaders(Map.of("Accept-Encoding","identity"))
                 .build());

     }

}
