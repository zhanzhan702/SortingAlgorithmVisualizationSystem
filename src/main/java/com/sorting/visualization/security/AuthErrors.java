package com.sorting.visualization.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 拦截器里直接输出 JSON 错误，避免 401/403 落回默认的 HTML 错误页。
 * 结构与普通接口的返回保持一致：{@code success / code / message / timestamp}。
 */
public final class AuthErrors {

    public static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String CODE_TOKEN_INVALID = "TOKEN_INVALID";
    public static final String CODE_FORBIDDEN = "FORBIDDEN";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthErrors() {
    }

    public static void write(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", code);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        MAPPER.writeValue(response.getWriter(), body);
    }
}
