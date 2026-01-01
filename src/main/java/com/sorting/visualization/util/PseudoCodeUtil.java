package com.sorting.visualization.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PseudoCodeUtil {

    private static final Map<String, String[]> PSEUDO_CODE_MAP = new ConcurrentHashMap<>();

    static {
        // 冒泡排序伪代码
        PSEUDO_CODE_MAP.put("BUBBLE", new String[]{
                "function bubbleSort(arr):",
                "    for i = 0 to n-1:",
                "        for j = 0 to n-i-2:",
                "            if arr[j] > arr[j+1]:",
                "                swap(arr[j], arr[j+1])"
        });

        // 直接插入排序伪代码
        PSEUDO_CODE_MAP.put("INSERTION", new String[]{
                "function insertionSort(arr):",
                "    for i = 1 to n-1:",
                "        key = arr[i]",
                "        j = i-1",
                "        while j >= 0 and arr[j] > key:",
                "            arr[j+1] = arr[j]",
                "            j = j-1",
                "        arr[j+1] = key"
        });

        // 希尔排序伪代码
        PSEUDO_CODE_MAP.put("SHELL", new String[]{
                "function shellSort(arr):",
                "    n = length(arr)",
                "    gap = n/2",
                "    while gap > 0:",
                "        for i = gap to n-1:",
                "            temp = arr[i]",
                "            j = i",
                "            while j >= gap and arr[j-gap] > temp:",
                "                arr[j] = arr[j-gap]",
                "                j = j-gap",
                "            arr[j] = temp",
                "        gap = gap/2"
        });

        // 快速排序伪代码
        PSEUDO_CODE_MAP.put("QUICK", new String[]{
                "function quickSort(arr, low, high):",
                "    if low < high:",
                "        pi = partition(arr, low, high)",
                "        quickSort(arr, low, pi-1)",
                "        quickSort(arr, pi+1, high)",
                "",
                "function partition(arr, low, high):",
                "    pivot = arr[high]",
                "    i = low-1",
                "    for j = low to high-1:",
                "        if arr[j] < pivot:",
                "            i = i+1",
                "            swap(arr[i], arr[j])",
                "    swap(arr[i+1], arr[high])",
                "    return i+1"
        });

        // 堆排序伪代码
        PSEUDO_CODE_MAP.put("HEAP", new String[]{
                "function heapSort(arr):",
                "    buildMaxHeap(arr)",
                "    for i = n-1 downto 1:",
                "        swap(arr[0], arr[i])",
                "        heapSize = heapSize-1",
                "        heapify(arr, 0, heapSize)",
                "",
                "function buildMaxHeap(arr):",
                "    heapSize = n",
                "    for i = floor(n/2) downto 0:",
                "        heapify(arr, i, heapSize)",
                "",
                "function heapify(arr, i, heapSize):",
                "    largest = i",
                "    left = 2*i+1",
                "    right = 2*i+2",
                "    if left < heapSize and arr[left] > arr[largest]:",
                "        largest = left",
                "    if right < heapSize and arr[right] > arr[largest]:",
                "        largest = right",
                "    if largest != i:",
                "        swap(arr[i], arr[largest])",
                "        heapify(arr, largest, heapSize)"
        });

        // 归并排序伪代码
        PSEUDO_CODE_MAP.put("MERGE", new String[]{
                "function mergeSort(arr, left, right):",
                "    if left < right:",
                "        mid = floor((left+right)/2)",
                "        mergeSort(arr, left, mid)",
                "        mergeSort(arr, mid+1, right)",
                "        merge(arr, left, mid, right)",
                "",
                "function merge(arr, left, mid, right):",
                "    n1 = mid-left+1",
                "    n2 = right-mid",
                "    create L[0..n1] and R[0..n2]",
                "    for i=0 to n1-1:",
                "        L[i] = arr[left+i]",
                "    for j=0 to n2-1:",
                "        R[j] = arr[mid+1+j]",
                "    i=0, j=0, k=left",
                "    while i<n1 and j<n2:",
                "        if L[i] <= R[j]:",
                "            arr[k] = L[i]",
                "            i = i+1",
                "        else:",
                "            arr[k] = R[j]",
                "            j = j+1",
                "        k = k+1",
                "    while i < n1:",
                "        arr[k] = L[i]",
                "        i = i+1",
                "        k = k+1",
                "    while j < n2:",
                "        arr[k] = R[j]",
                "        j = j+1",
                "        k = k+1"
        });
    }

    private PseudoCodeUtil() {
        // 工具类，防止实例化
    }

    /**
     * 获取算法的伪代码
     */
    public static String[] getPseudoCode(String algorithm) {
        String[] code = PSEUDO_CODE_MAP.get(algorithm.toUpperCase());
        if (code == null) {
            log.warn("未找到算法 {} 的伪代码", algorithm);
            return new String[]{"伪代码未找到"};
        }
        return code;
    }

    /**
     * 获取算法信息
     */
    public static Map<String, Object> getAlgorithmInfo(String algorithm) {
        Map<String, Object> info = new java.util.HashMap<>();

        switch (algorithm.toUpperCase()) {
            case "BUBBLE":
                info.put("name", "冒泡排序");
                info.put("timeComplexity", "最坏: O(n²), 平均: O(n²), 最好: O(n)");
                info.put("spaceComplexity", "O(1)");
                info.put("stability", "稳定");
                info.put("advantages", "实现简单，适合教学演示");
                break;

            case "INSERTION":
                info.put("name", "直接插入排序");
                info.put("timeComplexity", "最坏: O(n²), 平均: O(n²), 最好: O(n)");
                info.put("spaceComplexity", "O(1)");
                info.put("stability", "稳定");
                info.put("advantages", "实现简单，对小规模数据或基本有序数据效率高");
                break;

            case "SHELL":
                info.put("name", "希尔排序");
                info.put("timeComplexity", "取决于增量序列，通常 O(n log n)");
                info.put("spaceComplexity", "O(1)");
                info.put("stability", "不稳定");
                info.put("advantages", "比直接插入排序快，适用于中等规模数据");
                break;

            case "QUICK":
                info.put("name", "快速排序");
                info.put("timeComplexity", "最坏: O(n²), 平均: O(n log n), 最好: O(n log n)");
                info.put("spaceComplexity", "O(log n)");
                info.put("stability", "不稳定");
                info.put("advantages", "平均性能最好，是实际应用中最常用的排序算法");
                break;

            case "HEAP":
                info.put("name", "堆排序");
                info.put("timeComplexity", "O(n log n)");
                info.put("spaceComplexity", "O(1)");
                info.put("stability", "不稳定");
                info.put("advantages", "时间复杂度稳定为O(n log n)，适合大数据排序");
                break;

            case "MERGE":
                info.put("name", "归并排序");
                info.put("timeComplexity", "O(n log n)");
                info.put("spaceComplexity", "O(n)");
                info.put("stability", "稳定");
                info.put("advantages", "稳定排序，时间复杂度稳定，适合链表排序");
                break;

            default:
                info.put("name", "未知算法");
                info.put("timeComplexity", "未知");
                info.put("spaceComplexity", "未知");
                info.put("stability", "未知");
                info.put("advantages", "无");
        }

        return info;
    }
}