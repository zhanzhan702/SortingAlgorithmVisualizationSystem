package com.sorting.visualization.algorithm;

import com.sorting.visualization.model.Highlight;
import com.sorting.visualization.model.response.StepUpdate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 排序算法基类
 *
 * @param <T> 数据类型
 */
public abstract class AbstractSortingAlgorithm<T> implements SortingAlgorithm<T> {

    protected int comparisons = 0;
    protected int swaps = 0;
    protected long startTime = 0;

    @Getter
    protected List<StepUpdate> steps = new ArrayList<>();
    @Getter
    protected StepUpdate.Statistics currentStats = new StepUpdate.Statistics();

    /**
     * 初始化步骤记录
     */
    protected void initTeaching(List<T> data) {
        steps.clear();
        comparisons = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();
        currentStats = new StepUpdate.Statistics();

        // 记录初始状态
        addStep(data, new Highlight(), "算法开始");
    }

    /**
     * 添加一个步骤
     */
    protected void addStep(List<T> data, Highlight highlight, String description) {
        StepUpdate step = new StepUpdate();
        step.setStep(steps.size() + 1);
        step.setTotalSteps(0); // 将在完成时设置
        step.setData(new ArrayList<>(data));
        step.setHighlight(highlight);

        currentStats.setComparisons(comparisons);
        currentStats.setSwaps(swaps);
        currentStats.setTime(System.currentTimeMillis() - startTime);
        step.setStats(currentStats);

        step.setDescription(description);

        steps.add(step);
    }

    /**
     * 记录比较操作
     */
    protected void recordComparison() {
        comparisons++;
    }

    /**
     * 记录交换操作
     */
    protected void recordSwap() {
        swaps++;
    }

    /**
     * 交换列表中的两个元素
     */
    protected void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
        swaps++;
    }

    /**
     * 比较两个元素
     */
    protected int compare(Comparator<T> comparator, T a, T b) {
        comparisons++;
        return comparator.compare(a, b);
    }

    /**
     * 完成教学步骤记录
     */
    protected TeachingResult<T> completeTeaching(List<T> sortedData, int totalSteps) {
        // 更新所有步骤的总步数
        for (StepUpdate step : steps) {
            step.setTotalSteps(totalSteps);
        }

        // 添加最终步骤
        Highlight highlight = new Highlight();
        List<Integer> sortedIndices = new ArrayList<>();
        for (int i = 0; i < sortedData.size(); i++) {
            sortedIndices.add(i);
        }
        highlight.setSorted(sortedIndices);

        addStep(sortedData, highlight, "排序完成");

        TeachingResult<T> result = new TeachingResult<>();
        result.setSteps(steps);
        result.setSortedData(sortedData);
        result.setTotalComparisons(comparisons);
        result.setTotalSwaps(swaps);
        result.setTotalTime(System.currentTimeMillis() - startTime);

        return result;
    }
}
