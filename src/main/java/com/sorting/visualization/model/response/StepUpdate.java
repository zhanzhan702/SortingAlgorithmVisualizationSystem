package com.sorting.visualization.model.response;

import com.sorting.visualization.model.Highlight;
import lombok.Data;

import java.util.List;

@Data
public class StepUpdate {
    private String requestId;
    private String type = "STEP_UPDATE";
    private Integer step;
    private Integer totalSteps;
    private List<Object> data;          // 当前数组状态
    private Highlight highlight;
    private Statistics stats;
    private String description;         // 步骤描述
    private Boolean isFinal = false;    // 是否为最后一步
    private Long timestamp;

    @Data
    public static class Statistics {
        private Integer comparisons;     // 比较次数
        private Integer swaps;           // 交换次数
        private Long time;               // 已用时间（毫秒）

        public Statistics() {
            this.comparisons = 0;
            this.swaps = 0;
            this.time = 0L;
        }
    }
}