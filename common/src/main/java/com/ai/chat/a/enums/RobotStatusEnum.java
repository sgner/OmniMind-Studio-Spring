package com.ai.chat.a.enums;

public enum RobotStatusEnum {

        OFFLINE(0, "离线"),
                ONLINE(1, "在线"),

                FIXING(2, "修复中");

        private Integer status;
        private String desc;
        RobotStatusEnum(Integer status, String desc){
            this.status = status;
            this.desc = desc;
        }
        public Integer getStatus() {
            return status;
        }
        public String getDesc() {
            return desc;
        }

}
