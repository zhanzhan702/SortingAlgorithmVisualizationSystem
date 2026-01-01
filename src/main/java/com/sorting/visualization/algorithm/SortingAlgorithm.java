package com.sorting.visualization.algorithm;

import com.sorting.visualization.model.response.StepUpdate;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

/**
 * 排序算法接口
 *
 * @param <T> 数据类型
 */
public interface SortingAlgorithm<T> {

    /**
     * 教学模式：执行排序并返回所有步骤
     */
    TeachingResult<T> teach(List<T> data, Comparator<T> comparator);

    /**
     * 性能模式：执行排序并返回结果
     */
    PerformanceResult<T> perform(List<T> data, Comparator<T> comparator);

    /**
     * 获取算法名称
     */
    String getAlgorithmName();

    /**
     * 获取时间复杂度
     */
    String getTimeComplexity();

    /**
     * 获取空间复杂度
     */
    String getSpaceComplexity();

    /**
     * 是否稳定
     */
    boolean isStable();

    /**
     * 支持的数据类型
     */
    boolean supportsDataType(Class<?> dataType);

    @Data
    class TeachingResult<T> {
        private List<StepUpdate> steps;          // 所有步骤
        private List<T> sortedData;              // 排序后的数据
        private Integer totalComparisons;        // 总比较次数
        private Integer totalSwaps;              // 总交换次数
        private Long totalTime;                  // 总时间
    }

    @Data
    class PerformanceResult<T> {
        private List<T> sortedData;              // 排序后的数据
        private Integer comparisons;             // 比较次数
        private Integer swaps;                   // 交换次数
        private Long time;                       // 运行时间
    }
}