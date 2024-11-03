package com.ai.chat.a.po;
import com.ai.chat.a.serialize.LocalDateTimeToMillisSerializer;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversations {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String question;
    private String userId;
    @JsonSerialize(using = LocalDateTimeToMillisSerializer.class)
    private LocalDateTime createTime;
    private Integer status;
    private String files;
    private Integer questionType;
    private String sessionId;
    private String userName;
}
