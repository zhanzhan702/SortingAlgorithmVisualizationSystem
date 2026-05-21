package com.sorting.visualization.algorithm;

import java.util.List;

/**
 * 算法相关常量，消除多处硬编码
 */
public final class AlgorithmConstants {

    private AlgorithmConstants() {}

    /** 所有支持的算法标识 */
    public static final List<String> ALGORITHM_IDS = List.of(
            "BUBBLE", "INSERTION", "SHELL", "QUICK", "HEAP", "MERGE"
    );

    /** 算法中文名称映射 */
    public static String getChineseName(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "BUBBLE" -> "冒泡排序";
            case "INSERTION" -> "直接插入排序";
            case "SHELL" -> "希尔排序";
            case "QUICK" -> "快速排序";
            case "HEAP" -> "堆排序";
            case "MERGE" -> "归并排序";
            default -> "未知算法";
        };
    }

    /** 算法时间复杂度 */
    public static String getComplexity(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "BUBBLE", "INSERTION" -> "O(n²)";
            case "SHELL", "HEAP", "MERGE" -> "O(n log n)";
            case "QUICK" -> "O(n log n) - O(n²)";
            default -> "未知";
        };
    }

    /** 算法稳定性类型 */
    public static String getStabilityType(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "BUBBLE", "INSERTION", "MERGE" -> "stable";
            case "SHELL", "QUICK", "HEAP" -> "unstable";
            default -> "unknown";
        };
    }

    /** 判断是否为合法算法 */
    public static boolean isValidAlgorithm(String algorithm) {
        return algorithm != null && ALGORITHM_IDS.contains(algorithm.toUpperCase());
    }
}
