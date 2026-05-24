package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teaching_experiments")
public class TeachingExperiment {
    @TableId(type = IdType.AUTO)
    private Long expId;
    private Long userId;
    private Long algoId;
    private Long datasetId;
    private Integer dataSize;
    private Integer totalSteps;
    private Integer comparisons;
    private Integer swaps;
    private Long timeMicros;
    private Integer intervalMs;
    private String status;      // COMPLETED/STOPPED/ERROR
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
