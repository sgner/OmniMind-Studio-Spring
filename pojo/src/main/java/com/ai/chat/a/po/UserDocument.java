package com.ai.chat.a.po;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Builder
public class UserDocument {
    private String id;
    private String userId;
    private String documentId;
    private String sessionId;
}
