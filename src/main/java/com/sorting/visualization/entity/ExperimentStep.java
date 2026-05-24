package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("experiment_steps")
public class ExperimentStep {
    @TableId(type = IdType.AUTO)
    private Long stepId;
    private Long expId;
    private Integer stepNumber;
    private String dataJson;      // 数组快照 JSON
    private String highlightJson; // 高亮信息 JSON
    private String description;
}
