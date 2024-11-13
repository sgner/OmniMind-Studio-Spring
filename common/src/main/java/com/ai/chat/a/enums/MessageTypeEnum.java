package com.ai.chat.a.enums;

public enum MessageTypeEnum {
    SUNO_AUDIO(14,"","suno音乐生成成功"),
    TOOL_MODEL(15,"","创建工具模型会话成功"),
    INIT(0, "", "连接WS获取信息"),
    CREATE_SESSION(13, "", "创建会话"),
    ADD_FRIEND(1, "", "添加机器人打招呼消息"),
    CHAT(2, "", "普通聊天消息"),
    GROUP_CREATE(3, "群组已经创建好，可以和机器人畅聊了", "群创建成功"),
    MEDIA_CHAT(4, "", "媒体文件"),
    FILE_UPLOAD(5, "", "文件上传完成"),
    FORCE_OFF_LINE(6, "", "强制下线"),
    ADD_GROUP(7, "%s加入了群组", "加入群聊"),
    CONTACT_NAME_UPDATE(8, "", "更新群昵称"),
    REMOVE_GROUP(9, "%s被你移出了群聊", "被你移出了群聊"),
    ADD_FRIEND_SELF(10, "%s已添加为你的好友", "添加好友打招呼消息发送给自己"),
    ROBOT_RESPONSE(11, "", "机器人回复消息"),
    UPLOAD_TIME_OUT(12,"", "上传超时");
    private Integer type;
    private String initMessage;
    private String desc;

    MessageTypeEnum(Integer type, String initMessage, String desc) {
        this.type = type;
        this.initMessage = initMessage;
        this.desc = desc;
    }


    public static MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum item : MessageTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getInitMessage() {
        return initMessage;
    }

    public String getDesc() {
        return desc;
    }
}
