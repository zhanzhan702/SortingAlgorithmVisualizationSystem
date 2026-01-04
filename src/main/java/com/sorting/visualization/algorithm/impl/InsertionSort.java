package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class InsertionSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();

        // 添加初始步骤
        Highlight highlight;


        for (int i = 1; i < n; i++) {
            T key = workingData.get(i);
            int j = i - 1;

            highlight = new Highlight();
            List<Integer> sorted = new ArrayList<>();
            for (int k = 0; k < i; k++) {
                sorted.add(k);
            }
            highlight.setSorted(sorted);
            highlight.setCompare(List.of(i));

            addStep(workingData, highlight,
                    String.format("处理第%d个元素[%s]，将其插入到已排序序列中", i + 1, key));

            // 向后移动元素，为key找到合适位置
            while (j >= 0) {
                highlight = new Highlight();
                highlight.setCompare(List.of(j, i));
                highlight.setSorted(sorted);

                addStep(workingData, highlight,
                        String.format("比较当前元素[%s]和第%d个元素[%s]",
                                key, j + 1, workingData.get(j)));

                recordComparison();
                if (compare(comparator, workingData.get(j), key) > 0) {
                    workingData.set(j + 1, workingData.get(j));

                    highlight = new Highlight();
                    highlight.setSwap(List.of(j, j + 1));
                    highlight.setSorted(sorted);

                    addStep(workingData, highlight,
                            String.format("将第%d个元素向右移动", j + 1));

                    j--;
                } else {
                    break;
                }
            }

            workingData.set(j + 1, key);

            highlight = new Highlight();
            highlight.setSorted(sorted);
            highlight.setSwap(List.of(j + 1));

            addStep(workingData, highlight,
                    String.format("将元素[%s]插入到第%d个位置", key, j + 2));
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

        for (int i = 1; i < n; i++) {
            T key = workingData.get(i);
            int j = i - 1;

            while (j >= 0) {
                comparisons++;
                if (comparator.compare(workingData.get(j), key) > 0) {
                    workingData.set(j + 1, workingData.get(j));
                    swaps++;
                    j--;
                } else {
                    break;
                }
            }
            workingData.set(j + 1, key);
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
        return "直接插入排序";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n²)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(1)";
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean supportsDataType(Class<?> dataType) {
        return true;
    }
}