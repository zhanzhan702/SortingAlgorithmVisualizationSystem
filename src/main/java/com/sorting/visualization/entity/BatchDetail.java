package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("batch_details")
public class BatchDetail {
    @TableId(type = IdType.AUTO)
    private Long detailId;
    private Long batchId;
    private Long algoId;
    private Integer comparisons;
    private Integer swaps;
    private Long timeMicros;
    @TableField("`rank`")
    private Integer rank;
}
