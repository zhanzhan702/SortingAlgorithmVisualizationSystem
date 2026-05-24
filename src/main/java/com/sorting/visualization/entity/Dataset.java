package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("datasets")
public class Dataset {
    @TableId(type = IdType.AUTO)
    private Long datasetId;
    private String name;
    private String dataType;    // INTEGER/DOUBLE/PERSON
    private Integer dataSize;
    private String distribution;
    private String dataJson;    // JSON 字符串
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
