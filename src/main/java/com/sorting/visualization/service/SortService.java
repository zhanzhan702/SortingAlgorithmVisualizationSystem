package com.sorting.visualization.service;

import com.sorting.visualization.algorithm.AlgorithmConstants;
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

        for (String algo : AlgorithmConstants.ALGORITHM_IDS) {
            java.util.Map<String, String> algoInfo = new java.util.HashMap<>();
            algoInfo.put("id", algo.toLowerCase());
            algoInfo.put("name", AlgorithmConstants.getChineseName(algo));
            algoInfo.put("complexity", AlgorithmConstants.getComplexity(algo));
            algoInfo.put("type", AlgorithmConstants.getStabilityType(algo));
            algorithms.add(algoInfo);
        }

        return algorithms;
    }
}