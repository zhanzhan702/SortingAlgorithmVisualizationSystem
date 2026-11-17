package com.sorting.visualization.model.response;

import com.sorting.visualization.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST 接口的统一错误响应体。
 *
 * <pre>
 * {
 *   "timestamp": 1763380800000,
 *   "status": 400,
 *   "code": "VALIDATION_ERROR",
 *   "message": "用户名长度必须为 3-50 个字符",
 *   "path": "/api/auth/register",
 *   "requestId": null,
 *   "details": [{"field": "username", "message": "用户名长度必须为 3-50 个字符"}]
 * }
 * </pre>
 */
@Data
public class ApiError {

    /** 与 W1 的 AuthErrors 及原有响应保持一致：失败时 success=false */
    private boolean success = false;
    private long timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private String requestId;
    private Object details;

    public static ApiError of(ErrorCode errorCode, String message, String path,
                              String requestId, Object details) {
        ApiError error = new ApiError();
        error.setSuccess(false);
        error.setTimestamp(System.currentTimeMillis());
        error.setStatus(errorCode.getStatus());
        error.setCode(errorCode.getCode());
        error.setMessage(message == null || message.isBlank()
                ? errorCode.getDefaultMessage() : message);
        error.setPath(path);
        error.setRequestId(requestId);
        error.setDetails(details);
        return error;
    }

    /** 字段级错误明细 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Violation {
        private String field;
        private String message;
    }
}
