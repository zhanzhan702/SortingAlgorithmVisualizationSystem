package com.sorting.visualization.model.response;

import lombok.Data;

@Data
public class SortComplete {
    private String requestId;
    private String type = "SORT_COMPLETE";
    private String message = "排序完成";
    private FinalStatistics finalStats;
    private Long timestamp;

    @Data
    public static class FinalStatistics {
        private Integer totalComparisons;
        private Integer totalSwaps;
        private Long totalTime;
        private String algorithm;
    }
}