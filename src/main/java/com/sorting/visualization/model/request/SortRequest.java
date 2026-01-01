package com.sorting.visualization.model.request;

import lombok.Data;

import java.util.List;

@Data
public class SortRequest {
    private String requestId;
    private String type = "SORT_REQUEST";
    private String mode;           // TEACHING 或 PERFORMANCE
    private String algorithm;      // BUBBLE, INSERTION, SHELL, QUICK, HEAP, MERGE
    private List<Object> data;     // 数据数组
    private String dataType;       // INTEGER, DOUBLE, PERSON
    private Integer interval;      // 步进间隔（毫秒）
    private String distribution;   // RANDOM, SORTED, REVERSE, DUPLICATE, NORMAL
    private Boolean ascending = true;  // 排序方向
    private ComparatorInfo comparatorInfo;  // 比较器信息
    private Long timestamp;

    @Data
    public static class ComparatorInfo {
        private String direction;      // ascending, descending
        private String method;         // numeric, absolute, reverse
        private String description;    // 比较器描述
        private String structField;    // Person结构体排序字段（可选）
    }
}