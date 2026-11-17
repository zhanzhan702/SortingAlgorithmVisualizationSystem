package com.sorting.visualization.exception;

/**
 * 统一错误码。
 *
 * <p>REST（{@code @RestControllerAdvice}）与 WebSocket（ERROR 消息）共用同一套错误码，
 * 新增错误码时只改这里，避免在 Controller / MessageHandler 里散落字符串常量。</p>
 *
 * <p>{@link #getCode()} 返回枚举名（如 {@code VALIDATION_ERROR}），{@link #getStatus()}
 * 为对应的 HTTP 状态码，{@link #getDefaultMessage()} 是可直接展示给用户的中文兜底文案。</p>
 */
public enum ErrorCode {

    // ---------- 400 参数 / 请求格式 ----------
    VALIDATION_ERROR(400, "参数校验失败"),
    MISSING_PARAMETER(400, "缺少必要参数"),
    TYPE_MISMATCH(400, "参数类型不正确"),
    MALFORMED_JSON(400, "请求体不是合法的 JSON"),
    BAD_REQUEST(400, "请求不合法"),
    INVALID_DATA_TYPE(400, "不支持的数据类型"),
    UNSUPPORTED_ALGORITHM(400, "不支持的排序算法"),
    DATA_TOO_LARGE(400, "数据量超过上限"),
    INVALID_INTERVAL(400, "步进间隔必须在 100-5000 毫秒之间"),
    INVALID_ROLE(400, "非法的用户角色"),
    INVALID_DATE(400, "日期格式不正确，应为 yyyy-MM-dd"),
    INVALID_ACTION(400, "未知的控制命令"),
    MISSING_MESSAGE_TYPE(400, "缺少消息类型字段 type"),
    UNKNOWN_MESSAGE_TYPE(400, "未知的消息类型"),

    // ---------- 401 / 403（与 W1 的 AuthErrors 保持一致） ----------
    INVALID_CREDENTIALS(401, "用户名或密码错误"),
    UNAUTHORIZED(401, "未登录或登录状态已失效"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    TOKEN_INVALID(401, "访问令牌无效"),
    FORBIDDEN(403, "没有权限执行该操作"),

    // ---------- 404 ----------
    NOT_FOUND(404, "请求的资源不存在"),
    EXPERIMENT_NOT_FOUND(404, "实验记录不存在"),
    BATCH_NOT_FOUND(404, "性能批次不存在"),
    USER_NOT_FOUND(404, "用户不存在"),

    // ---------- 405 / 415 ----------
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的请求内容类型"),

    // ---------- 409 状态冲突 ----------
    CONFLICT(409, "资源状态冲突"),
    USERNAME_EXISTS(409, "用户名已存在"),
    ALREADY_PROCESSING(409, "当前会话正在处理其他请求，请等待完成或先停止当前请求"),
    NO_ACTIVE_TASK(409, "当前没有正在执行的排序任务"),
    INVALID_STATE(409, "当前状态不允许该操作"),

    // ---------- 500 ----------
    INTERNAL_ERROR(500, "服务器内部错误"),
    ALGORITHM_ERROR(500, "排序算法执行失败"),
    DATABASE_ERROR(500, "数据库操作失败"),
    BACKUP_FAILED(500, "数据库备份失败"),
    CLEANUP_FAILED(500, "清理实验数据失败"),
    WEBSOCKET_ERROR(500, "WebSocket 连接错误"),
    PERSISTENCE_ERROR(500, "实验结果保存失败");

    private final int status;
    private final String defaultMessage;

    ErrorCode(int status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    /** HTTP 状态码 */
    public int getStatus() {
        return status;
    }

    /** 对外暴露的错误码（即枚举名） */
    public String getCode() {
        return name();
    }

    /** 默认中文提示 */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
