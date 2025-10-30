package com.ai.chat.a.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 上下文合并服务
 * 整合多源检索结果，为LLM生成提供上下文
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextMergeService {

    private static final int MAX_CONTEXT_LENGTH = 4000; // 最大上下文长度
    private static final int MAX_DOCUMENTS = 10; // 最大文档数量

    /**
     * 合并多源检索结果
     * 
     * @param retrievalResult 并行检索结果（字符串列表）
     * @param query 原始查询
     * @return 合并后的上下文
     */
    public String mergeContext(List<String> retrievalResult, String query) {
        log.info("开始合并上下文，检索结果数量: {}", retrievalResult.size());

        // 1. 将字符串转换为Document对象
        List<Document> documents = convertStringsToDocuments(retrievalResult);

        // 2. 对文档进行评分和排序
        List<ScoredDocument> scoredDocuments = scoreAndSortDocuments(documents);

        // 3. 选择最相关的文档
        List<Document> selectedDocuments = selectMostRelevantDocuments(scoredDocuments, MAX_DOCUMENTS);

        // 4. 构建上下文
        String context = buildContext(selectedDocuments, query);

        log.info("上下文合并完成，最终上下文长度: {}", context.length());
        return context;
    }

    /**
     * 将字符串列表转换为Document对象
     * 
     * @param contentList 内容字符串列表
     * @return Document对象列表
     */
    private List<Document> convertStringsToDocuments(List<String> contentList) {
        return contentList.stream()
                .map(content -> {
                    Document doc = new Document(content);
                    // 根据内容特征推断来源
                    String source = inferSourceFromContent(content);
                    doc.getMetadata().put("source", source);
                    return doc;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据内容特征推断来源
     * 
     * @param content 内容
     * @return 来源标识
     */
    private String inferSourceFromContent(String content) {
        if (content.startsWith("Q: ") && content.contains("\nA: ")) {
            return "short-term-memory";
        } else if (content.contains("历史记录") || content.contains("之前") || content.contains("上次")) {
            return "long-term-memory";
        } else if (content.contains("知识") || content.contains("文档") || content.contains("资料")) {
            return "knowledge-base";
        } else {
            return "unknown";
        }
    }

    /**
     * 对文档进行评分和排序
     * 
     * @param documents 文档列表
     * @return 评分后的文档列表
     */
    private List<ScoredDocument> scoreAndSortDocuments(List<Document> documents) {
        List<ScoredDocument> scoredDocuments = new ArrayList<>();

        for (Document doc : documents) {
            String source = doc.getMetadata().getOrDefault("source", "unknown").toString();
            double sourceWeight = getSourceWeight(source);
            double score = calculateDocumentScore(doc, source, sourceWeight);
            scoredDocuments.add(new ScoredDocument(doc, score, source));
        }

        // 按分数降序排序
        scoredDocuments.sort(Comparator.comparingDouble(ScoredDocument::getScore).reversed());

        return scoredDocuments;
    }

    /**
     * 获取来源权重
     * 
     * @param source 来源标识
     * @return 权重值
     */
    private double getSourceWeight(String source) {
        switch (source) {
            case "short-term-memory":
                return 1.0; // 最高权重
            case "long-term-memory":
                return 0.8; // 中等权重
            case "knowledge-base":
                return 0.6; // 较低权重
            default:
                return 0.5; // 默认权重
        }
    }

    /**
     * 计算文档评分
     * 
     * @param document 文档
     * @param source 来源
     * @param sourceWeight 来源权重
     * @return 评分
     */
    private double calculateDocumentScore(Document document, String source, double sourceWeight) {
        double score = sourceWeight; // 基础分数为来源权重

        // 如果文档有相似度分数，使用它
        if (document.getMetadata().containsKey("score")) {
            Object scoreObj = document.getMetadata().get("score");
            if (scoreObj instanceof Number) {
                double similarityScore = ((Number) scoreObj).doubleValue();
                // 将相似度分数(0-1)转换为权重(0-1)
                score = score * 0.7 + similarityScore * 0.3;
            }
        }

        // 根据文档长度调整分数（避免过长或过短的文档）
        int contentLength = document.getContent().length();
        if (contentLength < 50) {
            score *= 0.8; // 过短文档降低分数
        } else if (contentLength > 1000) {
            score *= 0.9; // 过长文档略微降低分数
        }

        return score;
    }

    /**
     * 选择最相关的文档
     * 
     * @param scoredDocuments 评分后的文档列表
     * @param maxDocuments 最大文档数量
     * @return 选中的文档列表
     */
    private List<Document> selectMostRelevantDocuments(List<ScoredDocument> scoredDocuments, int maxDocuments) {
        List<Document> selectedDocuments = new ArrayList<>();
        int currentLength = 0;

        for (ScoredDocument scoredDoc : scoredDocuments) {
            if (selectedDocuments.size() >= maxDocuments) {
                break;
            }

            Document doc = scoredDoc.getDocument();
            int docLength = doc.getContent().length();

            // 检查添加此文档是否会超过最大长度限制
            if (currentLength + docLength > MAX_CONTEXT_LENGTH) {
                // 如果超过限制，尝试截断文档
                if (selectedDocuments.size() < maxDocuments - 1) {
                    String truncatedContent = truncateDocument(doc.getContent(), MAX_CONTEXT_LENGTH - currentLength - 100);
                    if (!truncatedContent.isEmpty()) {
                        Document truncatedDoc = new Document(truncatedContent, doc.getMetadata());
                        selectedDocuments.add(truncatedDoc);
                        currentLength += truncatedContent.length();
                    }
                }
                break;
            }

            selectedDocuments.add(doc);
            currentLength += docLength;
        }

        return selectedDocuments;
    }

    /**
     * 截断文档内容
     * 
     * @param content 原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    private String truncateDocument(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }

        // 尝试在句子边界截断
        String truncated = content.substring(0, maxLength);
        int lastSentenceEnd = Math.max(
                Math.max(truncated.lastIndexOf('.'), truncated.lastIndexOf('!')),
                truncated.lastIndexOf('?')
        );

        if (lastSentenceEnd > maxLength * 0.7) {
            return truncated.substring(0, lastSentenceEnd + 1);
        }

        // 如果没有合适的句子边界，尝试在单词边界截断
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > maxLength * 0.8) {
            return truncated.substring(0, lastSpace) + "...";
        }

        // 最后直接截断并添加省略号
        return truncated.substring(0, maxLength - 3) + "...";
    }

    /**
     * 构建上下文字符串
     * 
     * @param documents 文档列表
     * @param query 原始查询
     * @return 上下文字符串
     */
    private String buildContext(List<Document> documents, String query) {
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("用户查询: ").append(query).append("\n\n");

        // 按来源分组文档
        Map<String, List<Document>> documentsBySource = documents.stream()
                .collect(Collectors.groupingBy(doc -> {
                    Object sourceObj = doc.getMetadata().get("source");
                    return sourceObj != null ? sourceObj.toString() : "unknown";
                }));

        // 为每个来源添加标题和内容
        for (Map.Entry<String, List<Document>> entry : documentsBySource.entrySet()) {
            String source = entry.getKey();
            List<Document> sourceDocs = entry.getValue();

            String sourceTitle = getSourceTitle(source);
            contextBuilder.append("## ").append(sourceTitle).append("\n\n");

            for (int i = 0; i < sourceDocs.size(); i++) {
                Document doc = sourceDocs.get(i);
                contextBuilder.append(i + 1).append(". ").append(doc.getContent()).append("\n\n");
            }
        }

        return contextBuilder.toString();
    }

    /**
     * 获取来源标题
     * 
     * @param source 来源标识
     * @return 来源标题
     */
    private String getSourceTitle(String source) {
        switch (source) {
            case "short-term-memory":
                return "近期对话";
            case "long-term-memory":
                return "历史记忆";
            case "knowledge-base":
                return "知识库";
            default:
                return "其他信息";
        }
    }

    /**
     * 评分文档内部类
     */
    private static class ScoredDocument {
        private final Document document;
        private final double score;
        private final String source;

        public ScoredDocument(Document document, double score, String source) {
            this.document = document;
            this.score = score;
            this.source = source;
        }

        public Document getDocument() {
            return document;
        }

        public double getScore() {
            return score;
        }

        public String getSource() {
            return source;
        }
    }
}