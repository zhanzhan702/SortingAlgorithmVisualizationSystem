package com.sorting.visualization.controller;

import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理（W2）。
 *
 * <p>所有 REST 异常在这里统一转换为 {@link ApiError}，并返回与错误码对应的 HTTP 状态码。
 * 500 类错误只向客户端返回通用文案，堆栈与根因只写日志，避免泄露数据库结构、
 * 文件路径等内部信息。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 业务异常（含 W1 后续抛出的 UNAUTHORIZED / FORBIDDEN） ----------

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        logFailure(code, ex.getMessage(), ex, request);
        return build(code, ex.getMessage(), ex.getDetails(), request);
    }

    // ---------- 参数校验：请求体 / 查询参数绑定 ----------

    /**
     * {@code @Valid @RequestBody} 与 {@code @Valid @ModelAttribute} 校验失败，
     * 以及查询参数类型转换失败（如 page=abc），都会走到这里。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex, HttpServletRequest request) {
        List<ApiError.Violation> violations = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            violations.add(new ApiError.Violation(
                    fieldError.getField(),
                    fieldMessage(fieldError)));
        }
        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                violations.add(new ApiError.Violation(
                        globalError.getObjectName(),
                        defaultMessage(globalError.getDefaultMessage(), "参数不合法"))));

        String message = violations.isEmpty()
                ? ErrorCode.VALIDATION_ERROR.getDefaultMessage()
                : violations.get(0).getMessage();
        logFailure(ErrorCode.VALIDATION_ERROR, message, ex, request);
        return build(ErrorCode.VALIDATION_ERROR, message, violations, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<ApiError.Violation> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.add(new ApiError.Violation(
                    violation.getPropertyPath() == null ? null : violation.getPropertyPath().toString(),
                    violation.getMessage()));
        }
        String message = violations.isEmpty()
                ? ErrorCode.VALIDATION_ERROR.getDefaultMessage()
                : violations.get(0).getMessage();
        logFailure(ErrorCode.VALIDATION_ERROR, message, ex, request);
        return build(ErrorCode.VALIDATION_ERROR, message, violations, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
                                                            HttpServletRequest request) {
        String message = "缺少必要参数: " + ex.getParameterName();
        logFailure(ErrorCode.MISSING_PARAMETER, message, ex, request);
        return build(ErrorCode.MISSING_PARAMETER, message,
                List.of(new ApiError.Violation(ex.getParameterName(), message)), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String field = ex.getName();
        String message = "参数类型不正确: " + field;
        logFailure(ErrorCode.TYPE_MISMATCH, message, ex, request);
        return build(ErrorCode.TYPE_MISMATCH, message,
                List.of(new ApiError.Violation(field, message)), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                         HttpServletRequest request) {
        logFailure(ErrorCode.MALFORMED_JSON, "请求体不是合法的 JSON", ex, request);
        return build(ErrorCode.MALFORMED_JSON, "请求体不是合法的 JSON", null, request);
    }

    // ---------- 路由 / 协议 ----------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        logFailure(ErrorCode.NOT_FOUND, "接口或静态资源不存在", ex, request);
        return build(ErrorCode.NOT_FOUND, "接口或静态资源不存在", null, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                              HttpServletRequest request) {
        String message = "不支持的请求方法: " + ex.getMethod();
        logFailure(ErrorCode.METHOD_NOT_ALLOWED, message, ex, request);
        return build(ErrorCode.METHOD_NOT_ALLOWED, message, null, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                 HttpServletRequest request) {
        String message = "不支持的请求内容类型: " + ex.getContentType();
        logFailure(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message, ex, request);
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message, null, request);
    }

    // ---------- 数据访问 ----------

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException ex, HttpServletRequest request) {
        logFailure(ErrorCode.CONFLICT, "数据唯一约束冲突", ex, request);
        return build(ErrorCode.CONFLICT, "数据已存在，请勿重复提交", null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        logFailure(ErrorCode.CONFLICT, "数据完整性约束冲突", ex, request);
        return build(ErrorCode.CONFLICT, "数据不符合完整性约束", null, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        logFailure(ErrorCode.DATABASE_ERROR, ex.getMessage(), ex, request);
        return build(ErrorCode.DATABASE_ERROR, ErrorCode.DATABASE_ERROR.getDefaultMessage(), null, request);
    }

    // ---------- 兜底 ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("未预期的异常: {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), null, request);
    }

    // ---------- 工具方法 ----------

    private ResponseEntity<ApiError> build(ErrorCode code, String message, Object details,
                                           HttpServletRequest request) {
        ApiError body = ApiError.of(code, message, request.getRequestURI(), requestId(request), details);
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    private String requestId(HttpServletRequest request) {
        String value = request.getHeader("X-Request-Id");
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String defaultMessage(String message, String fallback) {
        return (message == null || message.isBlank()) ? fallback : message;
    }

    /**
     * 单个字段的错误文案。
     *
     * <p>类型转换失败（如 {@code page=abc}）时，Spring 的 defaultMessage 是英文的
     * 「Failed to convert property value of type ...」，既与本项目统一的中文文案不一致，
     * 又把内部类型名暴露给客户端，这里统一改写。</p>
     */
    private String fieldMessage(FieldError fieldError) {
        if ("typeMismatch".equals(fieldError.getCode())) {
            return "参数 " + fieldError.getField() + " 类型不正确";
        }
        return defaultMessage(fieldError.getDefaultMessage(), "参数不合法");
    }

    private void logFailure(ErrorCode code, String message, Exception ex, HttpServletRequest request) {
        if (code.getStatus() >= 500) {
            log.error("请求处理失败: {} {} -> {} {}", request.getMethod(), request.getRequestURI(),
                    code.getCode(), message, ex);
        } else {
            log.warn("请求校验失败: {} {} -> {} {}",
                    request.getMethod(), request.getRequestURI(), code.getCode(), message);
        }
    }
}
