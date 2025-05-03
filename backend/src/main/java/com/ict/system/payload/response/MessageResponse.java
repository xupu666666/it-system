package com.ict.system.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MessageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("message")
    private String message;

    // 默认构造函数
    public MessageResponse() {
    }

    // 接受字符串参数的构造函数
    public MessageResponse(String message) {
        this.message = message;
    }

    // 确保 getter 和 setter 存在
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}