package com.sorting.visualization.model;

import lombok.Data;

import java.util.List;

@Data
public class Highlight {
    private List<Integer> compare;    // 比较中的元素索引
    private List<Integer> swap;       // 交换中的元素索引
    private List<Integer> heap;       // 堆调整中的元素索引
    private List<Integer> sorted;     // 已排序的元素索引
    private List<Integer> pivot;      // 基准元素索引（快速排序）

    public Highlight() {
    }
}