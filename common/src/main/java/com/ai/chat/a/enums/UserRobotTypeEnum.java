package com.ai.chat.a.enums;

import com.ai.chat.a.utils.StringTools;

public enum UserRobotTypeEnum {
    ROBOT(0, "R", "机器人"),
    GROUP(1, "G", "群");

    private Integer type;
    private String prefix;
    private String desc;

    UserRobotTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    public String getPrefix() {
        return prefix;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static UserRobotTypeEnum getByName(String name) {
        try {
            if (StringTools.isEmpty(name)) {
                return null;
            }
            return UserRobotTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserRobotTypeEnum getByPrefix(String prefix) {
        if (StringTools.isEmpty(prefix) || prefix.trim().length() == 0) {
            return null;
        }
        prefix = prefix.substring(0, 1);
        for (UserRobotTypeEnum typeEnum : UserRobotTypeEnum.values()) {
            if (typeEnum.getPrefix().equals(prefix)) {
                return typeEnum;
            }
        }
        return null;
    }
}
