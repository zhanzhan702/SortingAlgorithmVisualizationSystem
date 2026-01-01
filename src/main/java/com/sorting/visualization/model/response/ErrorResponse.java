package com.sorting.visualization.model.response;

import lombok.Data;

@Data
public class ErrorResponse {
    private String requestId;
    private String type = "ERROR";
    private String message;
    private String code;               // 错误码
    private Object details;            // 错误详情
    private Long timestamp;

    // 错误码常量
    public static class ErrorCode {
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String ALGORITHM_ERROR = "ALGORITHM_ERROR";
        public static final String DATA_TOO_LARGE = "DATA_TOO_LARGE";
        public static final String INVALID_DATA_TYPE = "INVALID_DATA_TYPE";
        public static final String UNSUPPORTED_ALGORITHM = "UNSUPPORTED_ALGORITHM";
        public static final String WEBSOCKET_ERROR = "WEBSOCKET_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    }
}