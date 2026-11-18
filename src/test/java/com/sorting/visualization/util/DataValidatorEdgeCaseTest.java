package com.sorting.visualization.util;

import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：DataValidator 边界补充用例。
 *
 * <p>W3 的 {@link DataValidatorTest} 已锁定主要语义，本类补齐"边界与异常分支"，
 * 把长尾分支（requestId 格式、整数范围、email 长度、比较器字段、转换阶段空值）
 * 一并固定下来，便于覆盖率报告呈现真实的健壮性。</p>
 */
@DisplayName("数据校验器边界用例（W6 补充）")
class DataValidatorEdgeCaseTest {

    private DataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DataValidator();
        ReflectionTestUtils.setField(validator, "maxTeachingSize", 100);
        ReflectionTestUtils.setField(validator, "maxPerformanceSize", 1000);
        ReflectionTestUtils.setField(validator, "minAge", 0);
        ReflectionTestUtils.setField(validator, "maxAge", 120);
        ReflectionTestUtils.setField(validator, "minScore", 0.0d);
        ReflectionTestUtils.setField(validator, "maxScore", 150.0d);
    }

    // ---------------- 辅助构建 ----------------

    private static List<Object> data(Object... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    private static SortRequest request(String mode, String algorithm, String dataType, List<Object> data, Integer interval) {
        SortRequest request = new SortRequest();
        request.setRequestId("req-1");
        request.setMode(mode);
        request.setAlgorithm(algorithm);
        request.setDataType(dataType);
        request.setData(data);
        request.setInterval(interval);
        request.setDistribution("RANDOM");
        return request;
    }

    private static SortRequest teaching(List<Object> data) {
        return request("TEACHING", "BUBBLE", "INTEGER", data, 1000);
    }

    private static Person person(Integer id, String name, Integer age, Double score, String email) {
        return new Person(id, name, age, score, email);
    }

    private void assertCode(ErrorCode expected, Executable executable) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(expected, exception.getErrorCode(), exception.getMessage());
    }

    @Nested
    @DisplayName("requestId 与模式")
    class RequestIdAndModeTest {

        @Test
        @DisplayName("缺省 requestId 允许通过")
        void missingRequestIdAllowed() {
            SortRequest request = teaching(data(1, 2));
            request.setRequestId(null);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("空白 requestId 允许通过")
        void blankRequestIdAllowed() {
            SortRequest request = teaching(data(1, 2));
            request.setRequestId("   ");

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("含非法字符的 requestId 被拒绝")
        void illegalCharacterRequestIdRejected() {
            SortRequest request = teaching(data(1, 2));
            request.setRequestId("req 1@#$");

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("64 位以内的字母数字下划线短横线合法")
        void legalRequestIdAccepted() {
            SortRequest request = teaching(data(1, 2));
            request.setRequestId("A-z_09-" + "x".repeat(56));

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("模式缺失被拒绝")
        void missingModeRejected() {
            SortRequest request = teaching(data(1, 2));
            request.setMode(null);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("模式带空格与大小写差异仍可识别")
        void modeTrimmedAndCaseInsensitive() {
            SortRequest request = teaching(data(1, 2));
            request.setMode(" performance ");

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }
    }

    @Nested
    @DisplayName("步进间隔边界")
    class IntervalTest {

        @Test
        @DisplayName("教学模式间隔超过 5000ms 被拒绝")
        void intervalTooLargeRejected() {
            SortRequest request = teaching(data(1, 2));
            request.setInterval(5001);

            assertCode(ErrorCode.INVALID_INTERVAL, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("教学模式间隔缺失被拒绝")
        void missingIntervalRejected() {
            SortRequest request = teaching(data(1, 2));
            request.setInterval(null);

            assertCode(ErrorCode.INVALID_INTERVAL, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("性能模式不校验间隔")
        void performanceIgnoresInterval() {
            SortRequest request = request("PERFORMANCE", "QUICK", "INTEGER", data(3, 1), 10);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }
    }

    @Nested
    @DisplayName("整数与浮点内容边界")
    class NumberContentTest {

        @Test
        @DisplayName("超出 int 范围的 Long 被拒绝")
        void longOutOfIntRangeRejected() {
            SortRequest request = teaching(data(Integer.MAX_VALUE + 1L));

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("Short 与 Byte 可作为整数")
        void shortAndByteAccepted() {
            assertDoesNotThrow(() -> validator.validateSortRequest(teaching(data((short) 5, (byte) 7))));
        }

        @Test
        @DisplayName("布尔值不能作为整数")
        void booleanAsIntegerRejected() {
            assertCode(ErrorCode.VALIDATION_ERROR,
                    () -> validator.validateSortRequest(teaching(data(true))));
        }

        @Test
        @DisplayName("带首尾空格的数字字符串被接受")
        void numericStringWithSpacesAccepted() {
            assertDoesNotThrow(() -> validator.validateSortRequest(teaching(data(" 42 "))));
        }

        @Test
        @DisplayName("非数字字符串不能作为浮点数")
        void nonNumericStringAsDoubleRejected() {
            SortRequest request = request("TEACHING", "BUBBLE", "DOUBLE", data("abc"), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("布尔值不能作为浮点数")
        void booleanAsDoubleRejected() {
            SortRequest request = request("TEACHING", "BUBBLE", "DOUBLE", data(Boolean.FALSE), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("浮点字符串接受首尾空格")
        void doubleStringWithSpacesAccepted() {
            SortRequest request = request("TEACHING", "BUBBLE", "DOUBLE", data(" 1.5 "), 1000);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }
    }

    @Nested
    @DisplayName("Person 字段边界")
    class PersonFieldTest {

        @Test
        @DisplayName("Person 实例直接提交可通过")
        void personInstanceAccepted() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, 88.5, "zhangsan@mju.edu.cn")), 1000);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("负数 id 被拒绝")
        void negativeIdRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(-1, "张三", 20, 88.5, null)), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("超长姓名被拒绝")
        void tooLongNameRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张".repeat(51), 20, 88.5, null)), 1000);

            ApiException exception = assertThrows(ApiException.class,
                    () -> validator.validateSortRequest(request));
            assertTrue(exception.getMessage().contains("长度不能超过 50"), exception.getMessage());
        }

        @Test
        @DisplayName("年龄缺失被拒绝")
        void missingAgeRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", null, 88.5, null)), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("分数缺失被拒绝")
        void missingScoreRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, null, null)), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("分数为 NaN 被拒绝")
        void nanScoreRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, Double.NaN, null)), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("超长邮箱被拒绝")
        void tooLongEmailRejected() {
            String email = "a".repeat(95) + "@x.com";
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, 88.5, email)), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("email 缺省或空白时允许通过")
        void blankEmailAllowed() {
            assertDoesNotThrow(() -> validator.validateSortRequest(request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, 88.5, "   ")), 1000)));
        }

        @Test
        @DisplayName("Map 中字段类型无法转换时给出格式错误")
        void unconvertiblePersonMapRejected() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", 1);
            map.put("name", "张三");
            map.put("age", "not-a-number");
            SortRequest request = request("TEACHING", "MERGE", "PERSON", data(map), 1000);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }
    }

    @Nested
    @DisplayName("分布与比较器配置")
    class DistributionAndComparatorTest {

        @Test
        @DisplayName("分布缺省或空白不校验")
        void blankDistributionAllowed() {
            SortRequest request = teaching(data(1, 2));
            request.setDistribution("  ");

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("分布大小写不敏感")
        void distributionCaseInsensitive() {
            SortRequest request = teaching(data(1, 2));
            request.setDistribution(" sorted ");

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("比较器缺省不校验")
        void nullComparatorInfoAllowed() {
            SortRequest request = teaching(data(1, 2));
            request.setComparatorInfo(null);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("合法排序方向与比较方法通过")
        void validDirectionAndMethodAccepted() {
            SortRequest request = teaching(data(3, 1, 2));
            SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
            info.setDirection(" descending ");
            info.setMethod("ABSOLUTE");
            request.setComparatorInfo(info);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("非法排序方向被拒绝")
        void invalidDirectionRejected() {
            SortRequest request = teaching(data(3, 1, 2));
            SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
            info.setDirection("sideways");
            request.setComparatorInfo(info);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("Person 指定合法结构体字段通过")
        void personStructFieldAccepted() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, 88.5, null)), 1000);
            SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
            info.setStructField(" email ");
            request.setComparatorInfo(info);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("Person 指定不支持的字段被拒绝")
        void personUnsupportedFieldRejected() {
            SortRequest request = request("TEACHING", "MERGE", "PERSON",
                    data(person(1, "张三", 20, 88.5, null)), 1000);
            SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
            info.setStructField("password");
            request.setComparatorInfo(info);

            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.validateSortRequest(request));
        }

        @Test
        @DisplayName("结构体字段为空白时不校验")
        void blankStructFieldAllowed() {
            SortRequest request = teaching(data(3, 1, 2));
            SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
            info.setStructField("  ");
            request.setComparatorInfo(info);

            assertDoesNotThrow(() -> validator.validateSortRequest(request));
        }
    }

    @Nested
    @DisplayName("转换阶段边界")
    class ConvertBoundaryTest {

        @Test
        @DisplayName("convertData 拒绝空数据")
        void emptyDataRejectedOnConvert() {
            assertCode(ErrorCode.VALIDATION_ERROR,
                    () -> validator.convertData(new ArrayList<>(), "INTEGER"));
        }

        @Test
        @DisplayName("convertData 拒绝 null 数据")
        void nullDataRejectedOnConvert() {
            assertCode(ErrorCode.VALIDATION_ERROR, () -> validator.convertData(null, "INTEGER"));
        }

        @Test
        @DisplayName("convertData 整数列表遇到 null 元素报错")
        void nullElementInIntegerListRejected() {
            assertCode(ErrorCode.VALIDATION_ERROR,
                    () -> validator.convertData(data(1, null), "INTEGER"));
        }

        @Test
        @DisplayName("convertData 浮点转换兼容整数与字符串")
        void convertsDoubles() {
            List<Double> converted = validator.convertData(
                    new ArrayList<>(Arrays.asList(1.5, 2, " 3.25 ")), "DOUBLE");

            assertEquals(List.of(1.5, 2.0, 3.25), converted);
        }

        @Test
        @DisplayName("convertData 转换失败时抛出校验异常")
        void invalidIntegerConversionRejected() {
            assertCode(ErrorCode.VALIDATION_ERROR,
                    () -> validator.convertData(data("abc"), "INTEGER"));
        }

        @Test
        @DisplayName("convertData 拒绝非对象类型的 Person 元素")
        void invalidPersonElementRejected() {
            assertCode(ErrorCode.VALIDATION_ERROR,
                    () -> validator.convertData(data("张三"), "PERSON"));
        }
    }
}

