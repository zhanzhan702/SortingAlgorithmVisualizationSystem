package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MergeSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);

        // 开始归并排序
        mergeSortTeach(workingData, 0, workingData.size() - 1, comparator, new ArrayList<>());

        return completeTeaching(workingData, steps.size() + 1);
    }

    private void mergeSortTeach(List<T> data, int left, int right, Comparator<T> comparator, List<T> temp) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            Highlight highlight = new Highlight();
            List<Integer> leftPart = new ArrayList<>();
            for (int i = left; i <= mid; i++) leftPart.add(i);
            List<Integer> rightPart = new ArrayList<>();
            for (int i = mid + 1; i <= right; i++) rightPart.add(i);

            highlight.setCompare(leftPart);
            highlight.setSwap(rightPart);

            addStep(data, highlight,
                    String.format("分解数组 [%d, %d] -> [%d, %d] 和 [%d, %d]",
                            left + 1, right + 1, left + 1, mid + 1, mid + 2, right + 1));

            // 递归排序左半部分
            mergeSortTeach(data, left, mid, comparator, temp);

            // 递归排序右半部分
            mergeSortTeach(data, mid + 1, right, comparator, temp);

            // 合并两个有序部分
            mergeTeach(data, left, mid, right, comparator, temp);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeTeach(List<T> data, int left, int mid, int right, Comparator<T> comparator, List<T> temp) {

        Highlight highlight = new Highlight();
        List<Integer> mergeRange = new ArrayList<>();
        for (int i = left; i <= right; i++) mergeRange.add(i);
        highlight.setCompare(mergeRange);

        addStep(data, highlight,
                String.format("合并有序子数组 [%d, %d] 和 [%d, %d]",
                        left + 1, mid + 1, mid + 2, right + 1));

        int i = left;
        int j = mid + 1;
        int k = 0;

        // 清除临时数组
        temp.clear();

        while (i <= mid && j <= right) {
            highlight = new Highlight();
            highlight.setCompare(List.of(i, j));

            addStep(data, highlight,
                    String.format("比较左子数组第%d个元素[%s]和右子数组第%d个元素[%s]",
                            i - left + 1, data.get(i), j - mid, data.get(j)));

            recordComparison();
            if (compare(comparator, data.get(i), data.get(j)) <= 0) {
                temp.add(data.get(i));

                highlight = new Highlight();
                highlight.setSwap(List.of(i));

                addStep(data, highlight,
                        String.format("取左子数组元素[%s]", data.get(i)));

                i++;
            } else {
                temp.add(data.get(j));

                highlight = new Highlight();
                highlight.setSwap(List.of(j));

                addStep(data, highlight,
                        String.format("取右子数组元素[%s]", data.get(j)));

                j++;
            }
            k++;
        }

        // 复制剩余元素
        while (i <= mid) {
            temp.add(data.get(i));

            highlight = new Highlight();
            highlight.setSwap(List.of(i));

            addStep(data, highlight,
                    String.format("复制左子数组剩余元素[%s]", data.get(i)));

            i++;
            k++;
        }

        while (j <= right) {
            temp.add(data.get(j));

            highlight = new Highlight();
            highlight.setSwap(List.of(j));

            addStep(data, highlight,
                    String.format("复制右子数组剩余元素[%s]", data.get(j)));

            j++;
            k++;
        }

        // 将临时数组复制回原数组
        for (i = 0; i < k; i++) {
            data.set(left + i, temp.get(i));
        }

        highlight = new Highlight();
        List<Integer> mergedRange = new ArrayList<>();
        for (int idx = left; idx <= right; idx++) mergedRange.add(idx);
        highlight.setSorted(mergedRange);

        addStep(data, highlight,
                String.format("合并完成，范围 [%d, %d] 已有序", left + 1, right + 1));
    }

    @Override
    public PerformanceResult<T> perform(List<T> data, Comparator<T> comparator) {
        List<T> workingData = new ArrayList<>(data);
        comparisons = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();

        mergeSort(workingData, 0, workingData.size() - 1, comparator, new ArrayList<>());

        long time = System.currentTimeMillis() - startTime;

        PerformanceResult<T> result = new PerformanceResult<>();
        result.setSortedData(workingData);
        result.setComparisons(comparisons);
        result.setSwaps(swaps);
        result.setTime(time);

        return result;
    }

    private void mergeSort(List<T> data, int left, int right, Comparator<T> comparator, List<T> temp) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // 递归排序左半部分
            mergeSort(data, left, mid, comparator, temp);

            // 递归排序右半部分
            mergeSort(data, mid + 1, right, comparator, temp);

            // 合并两个有序部分
            merge(data, left, mid, right, comparator, temp);
        }
    }

    @SuppressWarnings("unchecked")
    private void merge(List<T> data, int left, int mid, int right, Comparator<T> comparator, List<T> temp) {
        int i = left;
        int j = mid + 1;
        int k = 0;

        // 清除临时数组
        temp.clear();

        while (i <= mid && j <= right) {
            comparisons++;
            if (comparator.compare(data.get(i), data.get(j)) <= 0) {
                temp.add(data.get(i));
                i++;
            } else {
                temp.add(data.get(j));
                j++;
            }
            k++;
        }

        // 复制剩余元素
        while (i <= mid) {
            temp.add(data.get(i));
            i++;
            k++;
        }

        while (j <= right) {
            temp.add(data.get(j));
            j++;
            k++;
        }

        // 将临时数组复制回原数组 - 这里每个元素的复制都应该统计为移动
        // 归并排序没有交换，只有数据移动
        for (i = 0; i < k; i++) {
            data.set(left + i, temp.get(i));
            // 在归并排序中，每次设置操作可以视为一次数据移动
            // 但不是传统意义上的交换，所以统计为"移动"
            swaps++;  // 这里增加移动次数统计
        }
    }

    @Override
    public String getAlgorithmName() {
        return "归并排序";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n log n)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(n)";
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