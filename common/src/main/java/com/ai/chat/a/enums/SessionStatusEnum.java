package com.ai.chat.a.enums;

public enum SessionStatusEnum {

    NORMAL(1,"正常创建，一切正常"),
    CLOSE(2,"关闭"),
    REMOVE(3,"移除");

    private Integer status;
    private String desc;

    SessionStatusEnum(Integer status, String desc){
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
