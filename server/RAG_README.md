# RAG功能实现说明

## 概述

根据用户提供的RAG对话功能改进流程图，我们实现了一个完整的RAG（Retrieval-Augmented Generation）系统，包括以下核心功能：

1. 文本归一化和hash处理
2. Embedding缓存检查与生成
3. 并行执行短期记忆、长期记忆和知识库检索
4. 上下文合并与LLM生成答案
5. 短期记忆更新和RAG缓存写回
6. 定时归档长期记忆

## 核心组件

### 1. EmbeddingCacheService (Embedding缓存服务)

**文件位置**: `com.ai.chat.a.rag.EmbeddingCacheService`

**功能**:
- 实现文本归一化和hash功能
- 检查Redis中的Embedding缓存
- 未命中时生成Embedding并缓存
- 支持批量处理和文档Embedding

**核心方法**:
- `getEmbedding(String text)`: 获取单个文本的Embedding
- `getEmbeddings(List<String> texts)`: 批量获取文本Embedding
- `getDocumentEmbedding(String document)`: 获取文档Embedding

### 2. ParallelRetrievalService (并行检索服务)

**文件位置**: `com.ai.chat.a.rag.ParallelRetrievalService`

**功能**:
- 并行执行短期记忆(Redis)、长期记忆(Milvus)和知识库(Milvus)检索
- 使用CompletableFuture实现并行处理
- 封装检索结果

**核心方法**:
- `retrieveContext(String query, String userId, String sessionId, int topK)`: 并行检索上下文
- `retrieveShortTermMemory(String userId, String sessionId)`: 检索短期记忆
- `retrieveLongTermMemory(String query, String userId, int topK)`: 检索长期记忆
- `retrieveKnowledgeBase(String query, int topK)`: 检索知识库

### 3. ContextMergeService (上下文合并服务)

**文件位置**: `com.ai.chat.a.rag.ContextMergeService`

**功能**:
- 合并多源检索结果
- 对文档进行评分和排序
- 选择最相关的文档
- 构建最终上下文

**核心方法**:
- `mergeContext(RetrievalResult retrievalResult, String query)`: 合并上下文
- `scoreAndSortDocuments(List<Document> documents, String query)`: 评分和排序文档
- `selectMostRelevantDocuments(List<Document> documents, int maxDocuments)`: 选择最相关文档
- `buildContext(List<Document> documents)`: 构建上下文

### 4. MemoryUpdateService (记忆更新服务)

**文件位置**: `com.ai.chat.a.rag.MemoryUpdateService`

**功能**:
- 更新短期记忆(Redis)
- 写回RAG缓存
- 将短期记忆归档到长期记忆
- 检查和清理过期缓存

**核心方法**:
- `updateMemoryAndCacheAsync(String userId, String sessionId, String query, String answer)`: 异步更新记忆和缓存
- `updateShortTermMemory(String userId, String sessionId, String query, String answer)`: 更新短期记忆
- `updateRAGCache(String userId, String sessionId, String query, String answer)`: 更新RAG缓存
- `archiveShortTermToLongTerm(String userId, String sessionId, int batchSize)`: 归档短期记忆

### 5. LongTermMemoryArchiveService (长期记忆归档服务)

**文件位置**: `com.ai.chat.a.rag.LongTermMemoryArchiveService`

**功能**:
- 定时归档长期记忆
- 归档活跃会话
- 清理过期缓存和短期记忆

**定时任务**:
- `archiveLongTermMemory()`: 每天凌晨2点执行，归档所有用户的短期记忆
- `archiveActiveSessions()`: 每小时执行，归档活跃会话的短期记忆
- `cleanExpiredRAGCache()`: 每周日凌晨3点执行，清理过期的RAG缓存
- `cleanExpiredShortTermMemory()`: 每天凌晨2:30执行，清理过期的短期记忆

## 集成到现有系统

### AAIOpenAIChatClient修改

**文件位置**: `com.ai.chat.a.chat.openai.AAIOpenAIChatClient`

**修改内容**:
1. 添加RAG相关服务依赖注入
2. 修改`doGenerateRAG`方法，集成新的RAG流程
3. 使用`MemoryUpdateService`替代原有的短期记忆和RAG缓存更新逻辑

### RagConfig配置

**文件位置**: `com.ai.chat.a.config.RagConfig`

**功能**:
- 启用定时任务和异步处理
- 配置定时任务线程池
- 注册RAG相关服务Bean

## RAG流程说明

1. **文本归一化和hash处理**
   - 用户查询文本经过归一化处理
   - 计算SHA-256哈希值用于缓存键

2. **检查Embedding缓存**
   - 优先从Redis缓存中获取Embedding
   - 未命中时生成新Embedding并缓存

3. **并行检索**
   - 同时执行短期记忆、长期记忆和知识库检索
   - 使用CompletableFuture实现并行处理
   - 汇总所有检索结果

4. **上下文合并**
   - 对检索结果进行评分和排序
   - 选择最相关的文档
   - 构建最终上下文

5. **LLM生成答案**
   - 将上下文和用户问题一起提交给LLM
   - 生成最终答案

6. **更新记忆和缓存**
   - 异步更新短期记忆(Redis)
   - 写回RAG缓存

7. **定时归档**
   - 定期将短期记忆归档到长期记忆
   - 清理过期缓存

## 性能优化

1. **并行处理**: 使用CompletableFuture并行执行多个检索任务
2. **缓存机制**: Embedding和RAG结果缓存减少重复计算
3. **异步更新**: 记忆和缓存更新不阻塞主流程
4. **批量处理**: 支持批量Embedding生成和检索

## 扩展性

1. **模块化设计**: 各功能模块独立，易于扩展和维护
2. **配置灵活**: 可通过配置调整缓存时间、归档策略等
3. **接口抽象**: 核心接口设计便于替换实现

## 注意事项

1. **资源管理**: 合理配置线程池大小，避免资源耗尽
2. **缓存策略**: 根据实际使用情况调整缓存过期时间
3. **监控日志**: 添加详细日志记录，便于问题排查
4. **异常处理**: 完善异常处理机制，确保系统稳定性