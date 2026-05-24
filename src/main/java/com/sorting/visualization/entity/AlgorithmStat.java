package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("algorithm_stats")
public class AlgorithmStat {
    @TableId(type = IdType.AUTO)
    private Long statId;
    private Long algoId;
    private Integer totalExperiments;
    private BigDecimal avgExpComparisons;
    private BigDecimal avgExpSwaps;
    private BigDecimal avgExpTimeMicros;
    private Integer totalBatches;
    private BigDecimal avgBatchComparisons;
    private BigDecimal avgBatchSwaps;
    private BigDecimal avgBatchTimeMicros;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
