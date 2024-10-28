package com.ai.chat.a.po;
import com.ai.chat.a.serialize.LocalDateTimeToMillisSerializer;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answers {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private String answer;
    @JsonSerialize(using = LocalDateTimeToMillisSerializer.class)
    private LocalDateTime createTime;
    private Integer answerRobotType;
    private Integer status;
    private String fileSize;
    private String fileName;
    private String filePath;
    private Integer fileType;
    @TableField("answer_type")
    private Integer answerType;
    private String answerRobotId;
    private String sessionId;
    private String answerRobotName;
    private String answerTargetUserId;
    private String groupId;
}
