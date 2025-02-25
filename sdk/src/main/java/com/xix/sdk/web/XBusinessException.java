package com.xix.sdk.web;

public class XBusinessException extends RuntimeException {

    private final Integer code;

    public XBusinessException(String message) {
        this(5000, message);
    }

    public XBusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
