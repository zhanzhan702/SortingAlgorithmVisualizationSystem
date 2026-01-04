package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class BubbleSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();

        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            Highlight highlight = new Highlight();
            List<Integer> sorted = new ArrayList<>();

            highlight.setSorted(sorted);
            addStep(workingData, highlight,
                    String.format("第%d轮排序开始", i + 1));

            for (int j = 0; j < n - i - 1; j++) {
                highlight = new Highlight();
                highlight.setCompare(List.of(j, j + 1));
                highlight.setSorted(sorted);

                addStep(workingData, highlight,
                        String.format("比较第%d个元素[%s]和第%d个元素[%s]",
                                j + 1, workingData.get(j), j + 2, workingData.get(j + 1)));

                recordComparison();
                if (compare(comparator, workingData.get(j), workingData.get(j + 1)) > 0) {
                    changed = true;
                    swap(workingData, j, j + 1);

                    highlight = new Highlight();
                    highlight.setSwap(List.of(j, j + 1));
                    highlight.setSorted(sorted);

                    addStep(workingData, highlight,
                            String.format("交换第%d个和第%d个元素", j + 1, j + 2));
                } else {
                    highlight = new Highlight();
                    highlight.setCompare(List.of(j, j + 1));
                    highlight.setSorted(sorted);

                    addStep(workingData, highlight,
                            "元素顺序正确，无需交换");
                }
            }
            for (int k = 0; k < i; k++) {
                sorted.add(n - 1 - k);
            }
            highlight = new Highlight();
            highlight.setSorted(sorted);

            addStep(workingData, highlight,
                    String.format("第%d轮排序完成，第%d个元素已就位", i + 1, n - i));

            if (!changed) {
                break;
            }
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

        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            for (int j = 0; j < n - i - 1; j++) {
                comparisons++;
                if (comparator.compare(workingData.get(j), workingData.get(j + 1)) > 0) {
                    changed = true;
                    swap(workingData, j, j + 1);
                }
            }
            if (!changed) {
                break;
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
        return "冒泡排序";
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
        return true; // 支持所有数据类型
    }
}