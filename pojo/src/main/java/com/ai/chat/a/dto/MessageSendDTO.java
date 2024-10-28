package com.ai.chat.a.dto;
import com.ai.chat.a.entity.WsInitData;
import com.ai.chat.a.serialize.LocalDateTimeToMillisSerializer;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageDTO.class, name = "message"),
        @JsonSubTypes.Type(value = WsInitData.class,name = "init_data")
})
public class MessageSendDTO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -1045752033171142417L;
    //消息ID
    private Long messageId;
    //会话ID
    private String sessionId;
    //发送人
    private String sendUserId;
    //发送人昵称
    private String sendUserNickName;
    //联系人ID
    private String contactId;
    //联系人名称
    private String contactName;
    //消息内容
    private String messageContent;
    //最后的消息
    private String lastMessage;
    //消息类型
    private Integer messageType;
    //发送时间
    private LocalDateTime sendTime;
    //联系人类型
    private Integer contactType;
    //扩展信息
    private T extendData;

    //消息状态 0:发送中  1:已发送 对于文件是异步上传用状态处理
    private Integer status;

    //文件信息
    private Long fileSize;
    private String fileName;
    private Integer fileType;

    //群员
    private Integer memberCount;
}
