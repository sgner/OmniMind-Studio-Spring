package com.ai.chat.a.enums;

public enum UploadEnum {
    FAIL(0, "上传失败"),
    FAIL_OVER_NUM(2,"文件数量超过限制"),
    SUCCESS(1, "上传成功"),
    AUTH_FAIL(3, "余额不足"),
    ;
    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
    UploadEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
