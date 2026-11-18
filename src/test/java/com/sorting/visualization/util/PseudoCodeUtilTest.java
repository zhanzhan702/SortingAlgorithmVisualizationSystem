package com.sorting.visualization.util;

import com.sorting.visualization.algorithm.AlgorithmConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：伪代码与算法信息
 *
 * 覆盖点：六种算法伪代码存在、大小写兼容、未知算法降级、算法信息字段完整性。
 */
@DisplayName("伪代码工具类单元测试")
class PseudoCodeUtilTest {

    @Test
    @DisplayName("六种算法都有非空伪代码")
    void allAlgorithmsHavePseudoCode() {
        for (String algorithm : AlgorithmConstants.ALGORITHM_IDS) {
            String[] pseudo = PseudoCodeUtil.getPseudoCode(algorithm);

            assertTrue(pseudo.length > 0, algorithm + " 伪代码不能为空");
            assertTrue(pseudo[0].startsWith("function"), algorithm + " 伪代码应以 function 开头");
        }
    }

    @Test
    @DisplayName("算法标识不区分大小写")
    void pseudoCodeLookupIsCaseInsensitive() {
        assertArrayEquals(PseudoCodeUtil.getPseudoCode("BUBBLE"), PseudoCodeUtil.getPseudoCode("bubble"));
    }

    @Test
    @DisplayName("未知算法返回提示文案")
    void unknownAlgorithmReturnsFallback() {
        String[] pseudo = PseudoCodeUtil.getPseudoCode("TIM");

        assertArrayEquals(new String[]{"伪代码未找到"}, pseudo);
    }

    @Test
    @DisplayName("冒泡排序伪代码内容正确")
    void bubbleSortPseudoCodeIsCorrect() {
        String[] pseudo = PseudoCodeUtil.getPseudoCode("BUBBLE");

        assertEquals(5, pseudo.length);
        assertEquals("function bubbleSort(arr):", pseudo[0]);
        assertTrue(pseudo[4].contains("swap"));
    }

    @Test
    @DisplayName("getAlgorithmInfo：返回完整的展示字段")
    void returnsAlgorithmInfo() {
        Map<String, Object> info = PseudoCodeUtil.getAlgorithmInfo("QUICK");

        assertEquals("快速排序", info.get("name"));
        assertEquals("O(log n)", info.get("spaceComplexity"));
        assertEquals("不稳定", info.get("stability"));
        assertTrue(info.get("timeComplexity").toString().contains("O(n log n)"));
        assertTrue(info.get("advantages").toString().contains("平均性能最好"));
        assertTrue(info.get("pseudocode") instanceof String[]);
    }

    @Test
    @DisplayName("getAlgorithmInfo：小写标识同样可用")
    void algorithmInfoIsCaseInsensitive() {
        assertEquals("堆排序", PseudoCodeUtil.getAlgorithmInfo("heap").get("name"));
    }

    @Test
    @DisplayName("getAlgorithmInfo：未知算法降级为统一文案")
    void unknownAlgorithmInfoFallsBack() {
        Map<String, Object> info = PseudoCodeUtil.getAlgorithmInfo("TIM");

        assertEquals("未知算法", info.get("name"));
        assertEquals("未知", info.get("timeComplexity"));
        assertEquals("无", info.get("advantages"));
    }

    @Test
    @DisplayName("getAlgorithmInfo：六种算法均返回与常量表一致的中文名称、复杂度与伪代码")
    void algorithmInfoCoversAllAlgorithms() {
        for (String algorithm : AlgorithmConstants.ALGORITHM_IDS) {
            Map<String, Object> info = PseudoCodeUtil.getAlgorithmInfo(algorithm);

            assertEquals(AlgorithmConstants.getChineseName(algorithm), info.get("name"));
            assertTrue(info.get("timeComplexity").toString().length() > 0);
            assertTrue(info.get("spaceComplexity").toString().length() > 0);
            assertTrue(info.get("stability").toString().length() > 0);
            assertTrue(info.get("pseudocode") instanceof String[]);
        }
    }
}
