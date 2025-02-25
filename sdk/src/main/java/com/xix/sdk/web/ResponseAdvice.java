package com.xix.sdk.web;

import com.alibaba.fastjson2.JSONObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @SneakyThrows
    @Override
    @NonNull
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, @NonNull MediaType mediaType, @NonNull Class<? extends HttpMessageConverter<?>> clazz, @NonNull ServerHttpRequest serverHttpRequest, @NonNull ServerHttpResponse serverHttpResponse) {
        if (serverHttpRequest.getURI().getPath().startsWith("/xix/actuator")) {
            return body;
        }
        if (body instanceof String) {
            return JSONObject.toJSONString(XResponse.success(body));
        }
        if (body instanceof XResponse<?>) {
            return body;
        }
        return XResponse.success(body);
    }
}
