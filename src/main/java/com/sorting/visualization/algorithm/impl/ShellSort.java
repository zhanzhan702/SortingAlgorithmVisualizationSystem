package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ShellSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();

        // 使用希尔增量序列
        for (int gap = n / 2; gap > 0; gap /= 2) {
            Highlight highlight = new Highlight();

            addStep(workingData, highlight,
                    String.format("当前增量: %d", gap));

            // 对每个子序列进行插入排序
            for (int i = gap; i < n; i++) {
                T temp = workingData.get(i);
                int j = i;

                highlight = new Highlight();
                List<Integer> currentGroup = new ArrayList<>();
                for (int k = i; k >= 0; k -= gap) {
                    currentGroup.add(k);
                }
                highlight.setCompare(currentGroup);

                addStep(workingData, highlight,
                        String.format("处理第%d个元素[%s]，增量序列索引: %d",
                                i + 1, temp, i % gap));

                while (j >= gap) {
                    highlight = new Highlight();
                    highlight.setCompare(List.of(j, j - gap));

                    addStep(workingData, highlight,
                            String.format("比较当前元素[%s]和第%d个元素[%s]（距离为%d）",
                                    temp, j - gap + 1, workingData.get(j - gap), gap));

                    recordComparison();
                    if (compare(comparator, workingData.get(j - gap), temp) > 0) {
                        workingData.set(j, workingData.get(j - gap));

                        highlight = new Highlight();
                        highlight.setSwap(List.of(j, j - gap));

                        addStep(workingData, highlight,
                                String.format("将第%d个元素向后移动%d个位置", j - gap + 1, gap));

                        j -= gap;
                    } else {
                        break;
                    }
                }

                workingData.set(j, temp);

                highlight = new Highlight();
                highlight.setSwap(List.of(j));

                addStep(workingData, highlight,
                        String.format("将元素[%s]插入到第%d个位置", temp, j + 1));
            }

            highlight = new Highlight();

            addStep(workingData, highlight,
                    String.format("增量%d的排序完成", gap));
        }

        return completeTeaching(workingData, steps.size() + 1);
    }

    @Override
    public PerformanceResult<T> perform(List<T> data, Comparator<T> comparator) {
        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();
        comparisons = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();

        // 希尔排序
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                T temp = workingData.get(i);
                int j = i;

                while (j >= gap) {
                    comparisons++;
                    if (comparator.compare(workingData.get(j - gap), temp) > 0) {
                        workingData.set(j, workingData.get(j - gap));
                        swaps++;
                        j -= gap;
                    } else {
                        break;
                    }
                }
                workingData.set(j, temp);
            }
        }

        long time = System.currentTimeMillis() - startTime;

        PerformanceResult<T> result = new PerformanceResult<>();
        result.setSortedData(workingData);
        result.setComparisons(comparisons);
        result.setSwaps(swaps);
        result.setTime(time);

        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "希尔排序";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n log n) - O(n²)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(1)";
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean supportsDataType(Class<?> dataType) {
        return true;
    }
}