package com.xix.sdk.web;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        if (httpServletRequest.getMethod().equals("OPTIONS") || httpServletRequest.getRequestURI().contains("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        XRequestWrapper xRequestWrapper = new XRequestWrapper((HttpServletRequest) request);
        XResponseWrapper xResponseWrapper = new XResponseWrapper((HttpServletResponse) response);
        chain.doFilter(xRequestWrapper, xResponseWrapper);
        String info = System.lineSeparator() + "===========================request begin==============================================" + System.lineSeparator() +
                "URI         : " + uriAndQueryString(xRequestWrapper) + System.lineSeparator() +
                "Method      : " + xRequestWrapper.getMethod() + System.lineSeparator() +
                "Headers     : " + headers(xRequestWrapper) + System.lineSeparator() +
                "Request body: " + xRequestWrapper.getBodyString() + System.lineSeparator() +
                "Request body: " + xResponseWrapper.getBodyString() + System.lineSeparator() +
                "==========================request end================================================";
        log.debug(info);
        xResponseWrapper.body2Response();
    }

    private String uriAndQueryString(HttpServletRequest request) {
        return StringUtils.isEmpty(request.getQueryString()) ? request.getRequestURI() : request.getRequestURI() + "?" + request.getQueryString();
    }

    private String headers(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        HashMap<String, Object> h = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String k = headerNames.nextElement();
            h.put(k, request.getHeader(k));
        }
        return JSONObject.toJSONString(h);
    }

    static class XRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] body;

        @SneakyThrows
        public XRequestWrapper(HttpServletRequest request) {
            super(request);
            body = request.getInputStream().readAllBytes();
        }

        @SneakyThrows
        public String getBodyString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // Not implemented
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }
    }

    static class XResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream cachedOutputStream = new ByteArrayOutputStream();
        private final ServletOutputStream outputStream;

        public XResponseWrapper(HttpServletResponse response) {
            super(response);
            this.outputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // No implementation needed
                }

                @Override
                public void write(int b) throws IOException {
                    cachedOutputStream.write(b); // 缓存响应数据
                }

                @Override
                public void write(byte[] b) throws IOException {
                    cachedOutputStream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    cachedOutputStream.write(b, off, len);
                }
            };
        }

        @SneakyThrows
        public String getBodyString() {
            return cachedOutputStream.toString(StandardCharsets.UTF_8); // 获取缓存的响应内容
        }

        @SneakyThrows
        public void body2Response() {
            super.getResponse().getOutputStream().write(cachedOutputStream.toByteArray());
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(outputStream, false);
        }
    }
}
