package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("performance_batches")
public class PerformanceBatch {
    @TableId(type = IdType.AUTO)
    private Long batchId;
    private String userId;
    private Integer dataSize;
    private String distribution;
    private String dataType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
