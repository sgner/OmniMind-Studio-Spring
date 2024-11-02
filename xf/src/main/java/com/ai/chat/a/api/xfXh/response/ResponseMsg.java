package com.ai.chat.a.api.xfXh.response;

import lombok.Data;

/**
 * Created with IntelliJ IDEA
 *
 * @author zhwang40
 * @date：2024/3/21
 * @time：14:32
 * @descripion:
 */
@Data
public class ResponseMsg<T> {
    private boolean success;

    private Integer code;

    private String message;

    private T data;
}
