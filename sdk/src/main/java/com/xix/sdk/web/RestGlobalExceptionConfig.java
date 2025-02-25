package com.xix.sdk.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestGlobalExceptionConfig {

    public static final int ERROR_CODE = 5000;

    @ExceptionHandler(Exception.class)
    public XResponse<?> exception(Exception e) {
        log.error("捕获到全局异常", e);
        return XResponse.failed(ERROR_CODE, "服务暂不稳定，请稍后再试");
    }

    @ExceptionHandler(XBusinessException.class)
    public XResponse<?> exception(XBusinessException e) {
        log.error("捕获到全局异常", e);
        return XResponse.failed(e.getCode(), e.getMessage());
    }

}

