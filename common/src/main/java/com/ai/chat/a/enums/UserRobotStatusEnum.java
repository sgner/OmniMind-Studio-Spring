package com.ai.chat.a.enums;


import com.ai.chat.a.utils.StringTools;

public enum UserRobotStatusEnum {
    NOT_SUBSCRIBE(0, "未订阅"),
    SUBSCRIBE(1, "已订阅");

    private Integer status;

    private String desc;

    UserRobotStatusEnum(Integer status, String desc) {
        this.status = status;
        this.status = status;
    }

    public static UserRobotStatusEnum getByStatus(String status) {
        try {
            if (StringTools.isEmpty(status)) {
                return null;
            }
            return UserRobotStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserRobotStatusEnum getByStatus(Integer status) {
        for (UserRobotStatusEnum item : UserRobotStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
