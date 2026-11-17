package com.sorting.visualization.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.response.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 拦截器里直接输出 JSON 错误，避免 401/403 落回默认的 HTML 错误页。
 *
 * <p>响应体与 {@code GlobalExceptionHandler} 走同一个 {@link ApiError} 契约
 * （success / timestamp / status / code / message / path / requestId / details），
 * 前端只需按一套结构解析；错误码统一取 {@link ErrorCode}，不再另存一份字符串常量。</p>
 */
public final class AuthErrors {

    public static final String CODE_UNAUTHORIZED = ErrorCode.UNAUTHORIZED.getCode();
    public static final String CODE_TOKEN_EXPIRED = ErrorCode.TOKEN_EXPIRED.getCode();
    public static final String CODE_TOKEN_INVALID = ErrorCode.TOKEN_INVALID.getCode();
    public static final String CODE_FORBIDDEN = ErrorCode.FORBIDDEN.getCode();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthErrors() {
    }

    /**
     * @param message 自定义提示，传 null 时用错误码的默认中文文案
     */
    public static void write(HttpServletRequest request, HttpServletResponse response,
                             ErrorCode code, String message) throws IOException {
        String requestId = request.getHeader("X-Request-Id");
        ApiError body = ApiError.of(code, message, request.getRequestURI(),
                requestId == null ? null : requestId.trim(), null);

        response.setStatus(code.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        MAPPER.writeValue(response.getWriter(), body);
    }
}
