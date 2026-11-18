package com.sorting.visualization.service;

import com.sorting.visualization.algorithm.AlgorithmConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：算法服务
 *
 * 说明：SortService 为纯逻辑服务（只做算法元数据聚合），
 * 不依赖 Mapper / 数据库，可直接实例化测试，无需启动 Spring 容器。
 */
@DisplayName("算法服务单元测试")
class SortServiceTest {

    private final SortService sortService = new SortService();

    @Test
    @DisplayName("getAlgorithms：返回全部六种算法")
    void returnsAllAlgorithms() {
        List<Map<String, String>> algorithms = sortService.getAlgorithms();

        assertEquals(AlgorithmConstants.ALGORITHM_IDS.size(), algorithms.size());
    }

    @Test
    @DisplayName("getAlgorithms：每个算法包含 id/name/complexity/type 四个字段")
    void eachAlgorithmHasCompleteFields() {
        for (Map<String, String> algorithm : sortService.getAlgorithms()) {
            assertEquals(4, algorithm.size());
            assertNotNull(algorithm.get("id"));
            assertNotNull(algorithm.get("name"));
            assertNotNull(algorithm.get("complexity"));
            assertNotNull(algorithm.get("type"));
        }
    }

    @Test
    @DisplayName("getAlgorithms：id 为小写，名称与常量表一致")
    void algorithmIdIsLowerCase() {
        List<Map<String, String>> algorithms = sortService.getAlgorithms();

        for (Map<String, String> algorithm : algorithms) {
            String id = algorithm.get("id");
            assertFalse(id.isBlank());
            assertEquals(id.toLowerCase(), id, "id 应始终为小写");
            assertEquals(AlgorithmConstants.getChineseName(id), algorithm.get("name"));
        }
        assertEquals("bubble", algorithms.get(0).get("id"));
    }

    @Test
    @DisplayName("getAlgorithmInfo：转发到伪代码工具类")
    void delegatesAlgorithmInfo() {
        Map<String, Object> info = sortService.getAlgorithmInfo("merge");

        assertEquals("归并排序", info.get("name"));
        assertTrue(info.containsKey("pseudocode"));
    }

    @Test
    @DisplayName("getAlgorithms：返回的集合可被修改而不影响后续调用")
    void returnedListIsIndependent() {
        List<Map<String, String>> first = sortService.getAlgorithms();
        first.clear();

        assertEquals(6, sortService.getAlgorithms().size());
    }
}
