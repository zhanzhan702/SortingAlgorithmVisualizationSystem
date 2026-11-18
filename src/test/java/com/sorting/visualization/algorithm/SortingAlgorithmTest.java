package com.sorting.visualization.algorithm;

import com.sorting.visualization.algorithm.SortingAlgorithm.PerformanceResult;
import com.sorting.visualization.algorithm.SortingAlgorithm.TeachingResult;
import com.sorting.visualization.algorithm.impl.BubbleSort;
import com.sorting.visualization.algorithm.impl.HeapSort;
import com.sorting.visualization.algorithm.impl.InsertionSort;
import com.sorting.visualization.algorithm.impl.MergeSort;
import com.sorting.visualization.algorithm.impl.QuickSort;
import com.sorting.visualization.algorithm.impl.ShellSort;
import com.sorting.visualization.model.response.StepUpdate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：六种排序算法核心逻辑测试
 *
 * 覆盖点：
 * 1. 排序结果正确性（升序 / 降序 / 重复元素 / 负数 / 已有序 / 逆序）
 * 2. 边界条件（空数组 / 单元素 / 两元素）
 * 3. 性能模式返回的比较次数、交换次数、耗时等统计量
 * 4. 教学模式的步骤序列（首步"算法开始"、末步"排序完成"、每步快照完整）
 * 5. 不修改调用方传入的原始集合
 * 6. 元数据方法（名称 / 时间复杂度 / 空间复杂度 / 稳定性 / 支持类型）
 */
@DisplayName("排序算法单元测试")
class SortingAlgorithmTest {

    private static final Comparator<Integer> ASC = Comparator.naturalOrder();
    private static final Comparator<Integer> DESC = Comparator.reverseOrder();

    /** 六种算法一次性参数化，任何一种回归都能被独立定位 */
    static Stream<SortingAlgorithm<Integer>> algorithms() {
        return Stream.of(
                new BubbleSort<Integer>(),
                new InsertionSort<Integer>(),
                new ShellSort<Integer>(),
                new QuickSort<Integer>(),
                new HeapSort<Integer>(),
                new MergeSort<Integer>()
        );
    }

    private static List<Integer> listOf(Integer... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    // ==================== 性能模式 perform ====================

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：无序数组升序排序结果正确")
    void performSortsAscending(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(5, 3, 8, 1, 9, 2, 7);

        PerformanceResult<Integer> result = algorithm.perform(input, ASC);

        assertEquals(listOf(1, 2, 3, 5, 7, 8, 9), result.getSortedData());
        assertNotNull(result.getComparisons(), "比较次数统计不能为空");
        assertNotNull(result.getSwaps(), "交换次数统计不能为空");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：传入降序比较器时结果正确")
    void performSortsDescending(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(5, 3, 8, 1, 9, 2, 7);

        PerformanceResult<Integer> result = algorithm.perform(input, DESC);

        assertEquals(listOf(9, 8, 7, 5, 3, 2, 1), result.getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：已有序数组不改变顺序")
    void performKeepsAscendingOrderForSortedInput(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(1, 2, 3, 4, 5);

        List<Integer> sorted = algorithm.perform(input, ASC).getSortedData();

        assertEquals(listOf(1, 2, 3, 4, 5), sorted);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：完全逆序数组能被正确翻转")
    void performSortsReversedInput(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(5, 4, 3, 2, 1);

        assertEquals(listOf(1, 2, 3, 4, 5), algorithm.perform(input, ASC).getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：重复元素全部保留且有序")
    void performHandlesDuplicateValues(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(3, 1, 3, 2, 1, 3);

        assertEquals(listOf(1, 1, 2, 3, 3, 3), algorithm.perform(input, ASC).getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：负数与零参与排序")
    void performHandlesNegativeNumbers(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(-3, 0, -10, 5, -1);

        assertEquals(listOf(-10, -3, -1, 0, 5), algorithm.perform(input, ASC).getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：空数组不抛异常")
    void performHandlesEmptyList(SortingAlgorithm<Integer> algorithm) {
        PerformanceResult<Integer> result = algorithm.perform(Collections.emptyList(), ASC);

        assertTrue(result.getSortedData().isEmpty(), "空数组排序后仍应为空");
        assertEquals(0, result.getComparisons(), "空数组不应产生比较");
        assertEquals(0, result.getSwaps(), "空数组不应产生交换");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：单元素数组原样返回")
    void performHandlesSingleElement(SortingAlgorithm<Integer> algorithm) {
        PerformanceResult<Integer> result = algorithm.perform(listOf(42), ASC);

        assertEquals(listOf(42), result.getSortedData());
        assertEquals(0, result.getComparisons(), "单元素无需比较");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：两元素数组可以完成交换")
    void performHandlesTwoElements(SortingAlgorithm<Integer> algorithm) {
        assertEquals(listOf(1, 2), algorithm.perform(listOf(2, 1), ASC).getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：不修改调用方传入的原始集合")
    void performDoesNotMutateInputList(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(3, 1, 2);

        algorithm.perform(input, ASC);

        assertEquals(listOf(3, 1, 2), input, "原始集合应保持不变");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：200 个随机元素排序结果与 JDK 排序一致")
    void performMatchesJdkSortOnRandomData(SortingAlgorithm<Integer> algorithm) {
        Random random = new Random(20261118L);
        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            input.add(random.nextInt(1000) - 500);
        }
        List<Integer> expected = new ArrayList<>(input);
        Collections.sort(expected);

        assertEquals(expected, algorithm.perform(input, ASC).getSortedData());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("perform：比较次数/交换次数为非负统计值")
    void performReportsNonNegativeStatistics(SortingAlgorithm<Integer> algorithm) {
        PerformanceResult<Integer> result = algorithm.perform(listOf(4, 2, 9, 1), ASC);

        assertTrue(result.getComparisons() >= 0);
        assertTrue(result.getSwaps() >= 0);
        assertTrue(result.getTime() >= 0);
    }

    // ==================== 教学模式 teach ====================

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("teach：步骤序列首尾完整，每一步都有数组快照")
    void teachProducesCompleteStepSequence(SortingAlgorithm<Integer> algorithm) {
        List<Integer> input = listOf(4, 2, 7, 1);

        TeachingResult<Integer> result = algorithm.teach(new ArrayList<>(input), ASC);
        List<StepUpdate> steps = result.getSteps();

        assertFalse(steps.isEmpty(), "教学步骤不能为空");
        assertEquals("算法开始", steps.get(0).getDescription(), "首步应为算法开始");
        assertTrue(steps.get(steps.size() - 1).getDescription().contains("排序完成"),
                "末步描述应包含排序完成");
        assertEquals(listOf(1, 2, 4, 7), result.getSortedData());

        for (StepUpdate step : steps) {
            assertNotNull(step.getData(), "每步都应携带数组快照");
            assertEquals(input.size(), step.getData().size(), "快照长度应与原数组一致");
            assertNotNull(step.getHighlight(), "每步都应携带高亮信息");
            assertNotNull(step.getStats(), "每步都应携带统计信息");
            assertTrue(step.getStep() >= 1, "步骤序号从 1 开始");
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("teach：末步标记全部元素为已排序")
    void teachMarksAllIndicesSortedAtLastStep(SortingAlgorithm<Integer> algorithm) {
        TeachingResult<Integer> result = algorithm.teach(listOf(3, 1, 2), ASC);
        List<StepUpdate> steps = result.getSteps();
        StepUpdate last = steps.get(steps.size() - 1);

        assertNotNull(last.getHighlight().getSorted());
        assertEquals(3, last.getHighlight().getSorted().size(), "3 个元素都应标记为已排序");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("teach：统计汇总与实际步骤一致")
    void teachReportsTotals(SortingAlgorithm<Integer> algorithm) {
        TeachingResult<Integer> result = algorithm.teach(listOf(5, 2, 8, 1), ASC);

        assertNotNull(result.getTotalComparisons());
        assertTrue(result.getTotalComparisons() > 0, "无序数据必然产生比较");
        assertNotNull(result.getTotalSwaps());
        assertTrue(result.getTotalSwaps() >= 0);
        assertNotNull(result.getTotalTime());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("teach：空数组也能正常结束")
    void teachHandlesEmptyList(SortingAlgorithm<Integer> algorithm) {
        TeachingResult<Integer> result = algorithm.teach(new ArrayList<>(), ASC);

        assertTrue(result.getSortedData().isEmpty());
        assertTrue(result.getSteps().size() >= 2, "至少包含开始与完成两步");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("teach：降序比较器生效")
    void teachRespectsDescendingComparator(SortingAlgorithm<Integer> algorithm) {
        TeachingResult<Integer> result = algorithm.teach(listOf(2, 5, 1), DESC);

        assertEquals(listOf(5, 2, 1), result.getSortedData());
    }

    // ==================== 元数据 ====================

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("algorithms")
    @DisplayName("元数据：名称/复杂度/稳定性声明完整")
    void exposesAlgorithmMetadata(SortingAlgorithm<Integer> algorithm) {
        assertFalse(algorithm.getAlgorithmName().isEmpty(), "算法名称不能为空");
        assertFalse(algorithm.getTimeComplexity().isEmpty(), "时间复杂度不能为空");
        assertFalse(algorithm.getSpaceComplexity().isEmpty(), "空间复杂度不能为空");
        assertTrue(algorithm.supportsDataType(Integer.class), "六种算法都应支持整数类型");
    }

    @Test
    @DisplayName("元数据：六种算法的名称互不相同")
    void algorithmNamesAreUnique() {
        List<String> names = algorithms().map(SortingAlgorithm::getAlgorithmName).toList();

        assertEquals(6, names.size());
        assertEquals(6, names.stream().distinct().count(), "算法名称不应重复");
    }

    @Test
    @DisplayName("元数据：稳定性声明与算法特性一致")
    void stabilityFlagsMatchAlgorithmCharacteristics() {
        assertTrue(new BubbleSort<Integer>().isStable(), "冒泡排序稳定");
        assertTrue(new InsertionSort<Integer>().isStable(), "直接插入排序稳定");
        assertTrue(new MergeSort<Integer>().isStable(), "归并排序稳定");
        assertFalse(new ShellSort<Integer>().isStable(), "希尔排序不稳定");
        assertFalse(new QuickSort<Integer>().isStable(), "快速排序不稳定");
        assertFalse(new HeapSort<Integer>().isStable(), "堆排序不稳定");
    }

    @Test
    @DisplayName("teach：每次调用都会重置步骤，单元素数据只产生开始与完成两步")
    void teachIsRepeatable() {
        BubbleSort<Integer> algorithm = new BubbleSort<>();

        TeachingResult<Integer> first = algorithm.teach(listOf(3, 1, 2), ASC);
        int firstStepCount = first.getSteps().size();
        assertEquals(listOf(1, 2, 3), first.getSortedData());
        assertTrue(firstStepCount > 2, "三个乱序元素必然产生多步演示");

        TeachingResult<Integer> second = algorithm.teach(listOf(1), ASC);
        assertEquals(listOf(1), second.getSortedData());
        assertEquals(2, second.getSteps().size(), "单元素只有开始与完成两步，说明步骤被重置");
    }
}
