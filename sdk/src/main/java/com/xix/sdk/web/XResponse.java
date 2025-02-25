package com.xix.sdk.web;

import lombok.Data;

@Data
public class XResponse<T> {

    private Integer code;
    private String message;
    private T data;
    private long timestamp;


    public XResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public XResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> XResponse<T> success(T data) {
        return new XResponse<>(0, "success", data);
    }

    public static <T> XResponse<T> success(String message) {
        return new XResponse<>(0, message, null);
    }

    public static <T> XResponse<T> failed(Integer code, String message) {
        return new XResponse<>(code, message, null);
    }


}
