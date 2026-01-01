package com.sorting.visualization.algorithm.impl;

import com.sorting.visualization.algorithm.AbstractSortingAlgorithm;
import com.sorting.visualization.model.Highlight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class HeapSort<T> extends AbstractSortingAlgorithm<T> {

    @Override
    public TeachingResult<T> teach(List<T> data, Comparator<T> comparator) {
        initTeaching(data);

        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();

        Highlight highlight = new Highlight();

        addStep(workingData, highlight, "开始构建最大堆");

        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapifyTeach(workingData, n, i, comparator, "build");
        }

        highlight = new Highlight();
        List<Integer> heapIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) heapIndices.add(i);
        highlight.setHeap(heapIndices);

        addStep(workingData, highlight, "最大堆构建完成");

        // 一个一个从堆中取出元素
        for (int i = n - 1; i > 0; i--) {
            highlight = new Highlight();
            highlight.setSwap(List.of(0, i));

            addStep(workingData, highlight,
                    String.format("将堆顶元素(最大值)交换到末尾第%d位", i + 1));

            // 将当前根节点移动到末尾
            swap(workingData, 0, i);

            highlight = new Highlight();
            List<Integer> sorted = new ArrayList<>();
            for (int k = i; k < n; k++) sorted.add(k);
            highlight.setSorted(sorted);

            addStep(workingData, highlight,
                    String.format("重新调整堆，堆大小: %d", i));

            // 调整剩余元素的堆
            heapifyTeach(workingData, i, 0, comparator, "extract");
        }

        return completeTeaching(workingData, steps.size() + 1);
    }

    private void heapifyTeach(List<T> data, int heapSize, int i, Comparator<T> comparator, String mode) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        Highlight highlight = new Highlight();
        List<Integer> nodes = new ArrayList<>();
        nodes.add(i);
        if (left < heapSize) nodes.add(left);
        if (right < heapSize) nodes.add(right);
        highlight.setHeap(nodes);

        addStep(data, highlight,
                String.format("调整以节点%d为根的堆", i + 1));

        // 如果左子节点更大
        if (left < heapSize) {
            recordComparison();
            if (compare(comparator, data.get(left), data.get(largest)) > 0) {
                largest = left;
            }
        }

        // 如果右子节点更大
        if (right < heapSize) {
            recordComparison();
            if (compare(comparator, data.get(right), data.get(largest)) > 0) {
                largest = right;
            }
        }

        // 如果最大节点不是根节点
        if (largest != i) {
            swap(data, i, largest);

            highlight = new Highlight();
            highlight.setSwap(List.of(i, largest));

            addStep(data, highlight,
                    String.format("交换节点%d和节点%d", i + 1, largest + 1));

            // 递归调整受影响的子树
            heapifyTeach(data, heapSize, largest, comparator, mode);
        } else {
            highlight = new Highlight();
            highlight.setHeap(List.of(i));

            addStep(data, highlight,
                    String.format("节点%d已在正确位置", i + 1));
        }
    }

    @Override
    public PerformanceResult<T> perform(List<T> data, Comparator<T> comparator) {
        List<T> workingData = new ArrayList<>(data);
        int n = workingData.size();
        comparisons = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();

        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(workingData, n, i, comparator);
        }

        // 一个一个从堆中取出元素
        for (int i = n - 1; i > 0; i--) {
            // 将当前根节点移动到末尾
            swap(workingData, 0, i);

            // 调整剩余元素的堆
            heapify(workingData, i, 0, comparator);
        }

        long time = System.currentTimeMillis() - startTime;

        PerformanceResult<T> result = new PerformanceResult<>();
        result.setSortedData(workingData);
        result.setComparisons(comparisons);
        result.setSwaps(swaps);
        result.setTime(time);

        return result;
    }

    private void heapify(List<T> data, int heapSize, int i, Comparator<T> comparator) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // 如果左子节点更大
        if (left < heapSize) {
            comparisons++;
            if (comparator.compare(data.get(left), data.get(largest)) > 0) {
                largest = left;
            }
        }

        // 如果右子节点更大
        if (right < heapSize) {
            comparisons++;
            if (comparator.compare(data.get(right), data.get(largest)) > 0) {
                largest = right;
            }
        }

        // 如果最大节点不是根节点
        if (largest != i) {
            swap(data, i, largest);
            // 递归调整受影响的子树
            heapify(data, heapSize, largest, comparator);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "堆排序";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n log n)";
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