package com.sorting.visualization.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：算法常量表
 *
 * 覆盖点：算法白名单、中文名称映射、复杂度映射、稳定性映射、非法输入。
 */
@DisplayName("算法常量单元测试")
class AlgorithmConstantsTest {

    @Test
    @DisplayName("支持六种算法，顺序固定")
    void exposesSixAlgorithmIds() {
        assertEquals(6, AlgorithmConstants.ALGORITHM_IDS.size());
        assertEquals(
                java.util.List.of("BUBBLE", "INSERTION", "SHELL", "QUICK", "HEAP", "MERGE"),
                AlgorithmConstants.ALGORITHM_IDS
        );
    }

    @Test
    @DisplayName("算法校验忽略大小写")
    void validatesAlgorithmIgnoringCase() {
        assertTrue(AlgorithmConstants.isValidAlgorithm("bubble"));
        assertTrue(AlgorithmConstants.isValidAlgorithm("QUICK"));
        assertFalse(AlgorithmConstants.isValidAlgorithm("TIM"));
    }

    @Test
    @DisplayName("null 与空字符串不是合法算法")
    void nullAndBlankAreInvalid() {
        assertFalse(AlgorithmConstants.isValidAlgorithm(null));
        assertFalse(AlgorithmConstants.isValidAlgorithm(""));
    }

    @Test
    @DisplayName("中文名称映射正确")
    void mapsChineseNames() {
        assertEquals("冒泡排序", AlgorithmConstants.getChineseName("BUBBLE"));
        assertEquals("直接插入排序", AlgorithmConstants.getChineseName("INSERTION"));
        assertEquals("希尔排序", AlgorithmConstants.getChineseName("SHELL"));
        assertEquals("快速排序", AlgorithmConstants.getChineseName("QUICK"));
        assertEquals("堆排序", AlgorithmConstants.getChineseName("HEAP"));
        assertEquals("归并排序", AlgorithmConstants.getChineseName("MERGE"));
        assertEquals("未知算法", AlgorithmConstants.getChineseName("TIM"));
    }

    @Test
    @DisplayName("时间复杂度映射正确")
    void mapsComplexity() {
        assertEquals("O(n²)", AlgorithmConstants.getComplexity("BUBBLE"));
        assertEquals("O(n²)", AlgorithmConstants.getComplexity("INSERTION"));
        assertEquals("O(n log n)", AlgorithmConstants.getComplexity("SHELL"));
        assertEquals("O(n log n)", AlgorithmConstants.getComplexity("HEAP"));
        assertEquals("O(n log n)", AlgorithmConstants.getComplexity("MERGE"));
        assertEquals("O(n log n) - O(n²)", AlgorithmConstants.getComplexity("QUICK"));
        assertEquals("未知", AlgorithmConstants.getComplexity("TIM"));
    }

    @Test
    @DisplayName("稳定性映射正确")
    void mapsStability() {
        assertEquals("stable", AlgorithmConstants.getStabilityType("BUBBLE"));
        assertEquals("stable", AlgorithmConstants.getStabilityType("INSERTION"));
        assertEquals("stable", AlgorithmConstants.getStabilityType("MERGE"));
        assertEquals("unstable", AlgorithmConstants.getStabilityType("SHELL"));
        assertEquals("unstable", AlgorithmConstants.getStabilityType("QUICK"));
        assertEquals("unstable", AlgorithmConstants.getStabilityType("HEAP"));
        assertEquals("unknown", AlgorithmConstants.getStabilityType("TIM"));
    }
}
