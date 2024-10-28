package com.ai.chat.a.dto;

import com.ai.chat.a.po.Answers;
import com.ai.chat.a.po.Conversations;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private Conversations question;
    private List<Answers> answers;
}
