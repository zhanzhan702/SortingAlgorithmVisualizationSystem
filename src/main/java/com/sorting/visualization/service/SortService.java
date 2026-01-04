package com.sorting.visualization.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SortService {

    /**
     * 获取算法信息
     */
    public java.util.Map<String, Object> getAlgorithmInfo(String algorithm) {
        return com.sorting.visualization.util.PseudoCodeUtil.getAlgorithmInfo(algorithm);
    }

    /**
     * 获取算法列表
     */
    public java.util.List<java.util.Map<String, String>> getAlgorithms() {
        java.util.List<java.util.Map<String, String>> algorithms = new java.util.ArrayList<>();

        String[] algorithmNames = {"BUBBLE", "INSERTION", "SHELL", "QUICK", "HEAP", "MERGE"};

        for (String algo : algorithmNames) {
            java.util.Map<String, String> algoInfo = new java.util.HashMap<>();
            algoInfo.put("id", algo.toLowerCase());
            algoInfo.put("name", getChineseName(algo));
            algoInfo.put("complexity", getComplexity(algo));
            algoInfo.put("type", getAlgorithmType(algo));
            algorithms.add(algoInfo);
        }

        return algorithms;
    }

    private String getChineseName(String algorithm) {
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

    private String getComplexity(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "BUBBLE", "INSERTION" -> "O(n²)";
            case "SHELL", "HEAP", "MERGE" -> "O(n log n)";
            case "QUICK" -> "O(n log n) - O(n²)";
            default -> "未知";
        };
    }

    private String getAlgorithmType(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "BUBBLE", "INSERTION", "MERGE" -> "stable";
            case "SHELL", "QUICK", "HEAP" -> "unstable";
            default -> "unknown";
        };
    }
}