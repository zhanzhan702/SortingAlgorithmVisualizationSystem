package com.sorting.visualization.model.response;

import com.sorting.visualization.exception.ErrorCode;
import lombok.Data;

/**
 * WebSocket 错误消息体（type 固定为 {@code ERROR}）。
 *
 * <p>错误码统一来自 {@link ErrorCode}，与 REST 的 {@link ApiError} 共用同一套语义；
 * 前端可以用 {@code requestId} 把错误关联回具体请求。</p>
 */
@Data
public class ErrorResponse {

    private String requestId;
    private String type = "ERROR";
    private String message;
    private String code;
    private Object details;
    private Long timestamp;

    public static ErrorResponse of(ErrorCode errorCode, String message, String requestId) {
        ErrorResponse response = new ErrorResponse();
        response.setRequestId(requestId);
        response.setCode(errorCode.getCode());
        response.setMessage(message == null || message.isBlank()
                ? errorCode.getDefaultMessage() : message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
