package com.sorting.visualization.model.request;

import lombok.Data;

@Data
public class ControlRequest {
    private String type;
    private String action;      // PAUSE, RESUME, STOP
    private String requestId;   // 对应排序的requestId
    private Long timestamp;
}