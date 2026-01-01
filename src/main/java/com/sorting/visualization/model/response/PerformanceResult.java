package com.sorting.visualization.model.response;

import lombok.Data;

import java.util.List;

@Data
public class PerformanceResult {
    private String requestId;
    private String type = "PERFORMANCE_RESULT";
    private String algorithm;          // 算法名称
    private Long time;                 // 总运行时间（毫秒）
    private Integer comparisons;       // 总比较次数
    private Integer swaps;             // 总交换次数
    private Integer dataSize;          // 数据大小
    private String distribution;       // 数据分布
    private List<Object> sortedData;   // 排序后的数据
    private Boolean sorted = true;     // 是否排序成功
    private Long timestamp;
}