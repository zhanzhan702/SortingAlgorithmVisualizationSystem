package com.sorting.visualization.model.request;

import lombok.Data;

@Data
public class ControlRequest {
    private String type;
    private String action;      // PAUSE, RESUME, STOP, STEP_FORWARD
    private String requestId;   // 对应排序的requestId
    private Integer interval;   // 可选：更新步进间隔（ms）
    private Long timestamp;
}