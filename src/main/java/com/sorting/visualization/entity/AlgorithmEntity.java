package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("algorithms")
public class AlgorithmEntity {
    @TableId(type = IdType.AUTO)
    private Long algoId;
    private String algoName;
    private String category;
    private String timeComplexity;
    private String spaceComplexity;
    private Boolean isStable;
    private String pseudocode;
    private String description;
    private String advantages;
}
