package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

@Slf4j
public class QuickSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);

        // 使用栈代替递归以避免栈溢出
        Stack<QuickSortRange> stack = new Stack<>();
        stack.push(new QuickSortRange(0, workingData.size() - 1));

        while (!stack.isEmpty()) {
            QuickSortRange range = stack.pop();
            int low = range.low;
            int high = range.high;

            if (low < high) {
                Highlight highlight = new Highlight();
                List<Integer> currentRange = new ArrayList<>();
                for (int i = low; i <= high; i++) {
                    currentRange.add(i);
                }
                highlight.setCompare(currentRange);

                addStep(workingData, highlight,
                        String.format("快速排序子数组 [%d, %d]", low + 1, high + 1));

                // 分区操作
                int pivotIndex = partition(workingData, low, high, comparator);

                // 将左右子数组入栈
                stack.push(new QuickSortRange(low, pivotIndex - 1));
                stack.push(new QuickSortRange(pivotIndex + 1, high));
            }
        }

        return completeTeaching(workingData, steps.size() + 1);
    }

    private int partition(List<T> data, int low, int high, Comparator<T> comparator) {
        T pivot = data.get(high);

        Highlight highlight = new Highlight();
        highlight.setPivot(List.of(high));

        addStep(data, highlight,
                String.format("选择基准元素: 第%d个元素[%s]", high + 1, pivot));

        int i = low - 1;

        for (int j = low; j < high; j++) {
            highlight = new Highlight();
            highlight.setCompare(List.of(j, high));
            highlight.setPivot(List.of(high));

            addStep(data, highlight,
                    String.format("比较第%d个元素[%s]和基准元素[%s]",
                            j + 1, data.get(j), pivot));

            recordComparison();
            if (compare(comparator, data.get(j), pivot) <= 0) {
                i++;

                if (i != j) {
                    swap(data, i, j);

                    highlight = new Highlight();
                    highlight.setSwap(List.of(i, j));
                    highlight.setPivot(List.of(high));

                    addStep(data, highlight,
                            String.format("交换第%d个和第%d个元素", i + 1, j + 1));
                }
            }
        }

        swap(data, i + 1, high);

        highlight = new Highlight();
        highlight.setSwap(List.of(i + 1, high));

        addStep(data, highlight,
                String.format("将基准元素交换到正确位置: 第%d位", i + 2));

        return i + 1;
    }

    @Override
    public PerformanceResult<T> perform(List<T> data, Comparator<T> comparator) {
        List<T> workingData = new ArrayList<>(data);
        comparisons = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();

        quickSort(workingData, 0, workingData.size() - 1, comparator);

        long time = System.currentTimeMillis() - startTime;

        PerformanceResult<T> result = new PerformanceResult<>();
        result.setSortedData(workingData);
        result.setComparisons(comparisons);
        result.setSwaps(swaps);
        result.setTime(time);

        return result;
    }

    private void quickSort(List<T> data, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pi = partitionFast(data, low, high, comparator);
            quickSort(data, low, pi - 1, comparator);
            quickSort(data, pi + 1, high, comparator);
        }
    }

    private int partitionFast(List<T> data, int low, int high, Comparator<T> comparator) {
        T pivot = data.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            comparisons++;
            if (comparator.compare(data.get(j), pivot) <= 0) {
                i++;
                if (i != j) {
                    swap(data, i, j);
                }
            }
        }

        swap(data, i + 1, high);
        return i + 1;
    }

    @Override
    public String getAlgorithmName() {
        return "快速排序";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n log n) - O(n²)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(log n)";
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean supportsDataType(Class<?> dataType) {
        return true;
    }

    private class QuickSortRange {
        int low;
        int high;

        QuickSortRange(int low, int high) {
            this.low = low;
            this.high = high;
        }
    }
}