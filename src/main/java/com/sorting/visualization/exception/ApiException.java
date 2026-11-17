package com.sorting.visualization.exception;

/**
 * 业务异常基类（运行时异常）。
 *
 * <p>Service / Controller / WebSocket 校验失败时抛出，由
 * {@code GlobalExceptionHandler}（REST）或 {@code MessageHandler}（WebSocket）统一转换为
 * 结构化错误响应。后续 W1 鉴权只需抛出 {@code new ApiException(ErrorCode.UNAUTHORIZED)}，
 * 无需再补映射逻辑。</p>
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    /** 结构化明细，例如字段级校验错误列表，可为 null */
    private final transient Object details;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null, null);
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public ApiException(ErrorCode errorCode, String message, Object details) {
        this(errorCode, message, details, null);
    }

    public ApiException(ErrorCode errorCode, String message, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
