package com.sorting.visualization.util;

import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * DataValidator 单元测试（W3）：把参数校验的语义固定下来，防止后续改动悄悄放宽规则。
 */
class DataValidatorTest {

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

    // ---------- 基础请求 ----------

    @Test
    @DisplayName("合法教学请求通过校验")
    void validTeachingRequest() {
        assertDoesNotThrow(() -> validator.validateSortRequest(teachingRequest(5, 3, 9, 1)));
    }

    @Test
    @DisplayName("请求为 null 时返回 VALIDATION_ERROR")
    void nullRequestRejected() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(null)).getErrorCode());
    }

    @Test
    @DisplayName("非法模式被拒绝")
    void invalidModeRejected() {
        SortRequest request = teachingRequest(1, 2);
        request.setMode("DEMO");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("模式大小写不敏感")
    void modeIsCaseInsensitive() {
        SortRequest request = teachingRequest(3, 1, 2);
        request.setMode("teaching");
        assertDoesNotThrow(() -> validator.validateSortRequest(request));
    }

    @Test
    @DisplayName("不支持的算法返回 UNSUPPORTED_ALGORITHM")
    void unsupportedAlgorithmRejected() {
        SortRequest request = teachingRequest(1, 2);
        request.setAlgorithm("STALIN_SORT");
        assertEquals(ErrorCode.UNSUPPORTED_ALGORITHM,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("不支持的数据类型返回 INVALID_DATA_TYPE")
    void invalidDataTypeRejected() {
        SortRequest request = teachingRequest(1, 2);
        request.setDataType("STRING");
        assertEquals(ErrorCode.INVALID_DATA_TYPE,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("空数据被拒绝")
    void emptyDataRejected() {
        SortRequest request = teachingRequest();
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("教学数据超过 100 返回 DATA_TOO_LARGE")
    void teachingDataTooLarge() {
        SortRequest request = teachingRequest(intArray(101));
        assertEquals(ErrorCode.DATA_TOO_LARGE,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("性能模式允许 1000 个数据")
    void performanceAllowsThousand() {
        SortRequest request = teachingRequest(intArray(1000));
        request.setMode("PERFORMANCE");
        request.setInterval(null);
        assertDoesNotThrow(() -> validator.validateSortRequest(request));
    }

    @Test
    @DisplayName("步进间隔越界返回 INVALID_INTERVAL")
    void invalidIntervalRejected() {
        SortRequest request = teachingRequest(1, 2);
        request.setInterval(50);
        assertEquals(ErrorCode.INVALID_INTERVAL,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    // ---------- 数值校验 ----------

    @Test
    @DisplayName("null 元素被拒绝，且提示第几个数据")
    void nullElementRejected() {
        SortRequest request = teachingRequest(1, null, 3);
        ApiException ex = assertThrows(ApiException.class, () -> validator.validateSortRequest(request));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("第2个"), ex.getMessage());
    }

    @Test
    @DisplayName("小数不能作为整数数据")
    void fractionalIntegerRejected() {
        SortRequest request = teachingRequest(1, 2.5);
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("3.0 这类整数值的浮点数可以当整数用")
    void integralDoubleAccepted() {
        assertDoesNotThrow(() -> validator.validateSortRequest(teachingRequest(1, 3.0, 2)));
    }

    @Test
    @DisplayName("数字字符串可以当整数用")
    void numericStringAccepted() {
        assertDoesNotThrow(() -> validator.validateSortRequest(teachingRequest("42", "7")));
    }

    @Test
    @DisplayName("NaN 不能作为浮点数数据")
    void nanRejected() {
        SortRequest request = teachingRequest(1.0, Double.NaN);
        request.setDataType("DOUBLE");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("Infinity 不能作为浮点数数据")
    void infinityRejected() {
        SortRequest request = teachingRequest(1.0, Double.POSITIVE_INFINITY);
        request.setDataType("DOUBLE");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    // ---------- Person ----------

    @Test
    @DisplayName("Person 缺少 name 被拒绝")
    void personMissingNameRejected() {
        Map<String, Object> person = personMap();
        person.remove("name");
        SortRequest request = teachingRequest(person);
        request.setDataType("PERSON");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("Person 年龄越界被拒绝")
    void personAgeOutOfRangeRejected() {
        Map<String, Object> person = personMap();
        person.put("age", 130);
        SortRequest request = teachingRequest(person);
        request.setDataType("PERSON");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("Person 邮箱格式错误被拒绝")
    void personInvalidEmailRejected() {
        Map<String, Object> person = personMap();
        person.put("email", "not-an-email");
        SortRequest request = teachingRequest(person);
        request.setDataType("PERSON");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("合法 Person 数据通过校验")
    void validPersonAccepted() {
        SortRequest request = teachingRequest(personMap());
        request.setDataType("PERSON");
        assertDoesNotThrow(() -> validator.validateSortRequest(request));
    }

    @Test
    @DisplayName("前端生成的 Person 邮箱含中文时必须放行")
    void personChineseEmailAccepted() {
        Map<String, Object> person = personMap();
        person.put("email", "张.伟@gmail.com");
        SortRequest request = teachingRequest(person);
        request.setDataType("PERSON");
        assertDoesNotThrow(() -> validator.validateSortRequest(request));
    }

    // ---------- 分布 / 比较器 / requestId ----------

    @Test
    @DisplayName("非法数据分布被拒绝")
    void invalidDistributionRejected() {
        SortRequest request = teachingRequest(3, 1, 2);
        request.setDistribution("CHAOS");
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("前端使用的 CUSTOM 分布必须放行")
    void customDistributionAccepted() {
        SortRequest request = teachingRequest(3, 1, 2);
        request.setDistribution("CUSTOM");
        assertDoesNotThrow(() -> validator.validateSortRequest(request));
    }

    @Test
    @DisplayName("非法比较方法被拒绝")
    void invalidComparatorMethodRejected() {
        SortRequest request = teachingRequest(3, 1, 2);
        SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
        info.setMethod("random");
        request.setComparatorInfo(info);
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("数值类型不允许指定 structField")
    void structFieldOnlyForPerson() {
        SortRequest request = teachingRequest(3, 1, 2);
        SortRequest.ComparatorInfo info = new SortRequest.ComparatorInfo();
        info.setStructField("age");
        request.setComparatorInfo(info);
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    @Test
    @DisplayName("超长 requestId 被拒绝")
    void tooLongRequestIdRejected() {
        SortRequest request = teachingRequest(3, 1, 2);
        request.setRequestId("x".repeat(65));
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(ApiException.class, () -> validator.validateSortRequest(request)).getErrorCode());
    }

    // ---------- 数据转换 ----------

    @Test
    @DisplayName("convertData 把 INT 转成 Integer 列表")
    void convertToIntegerList() {
        List<Integer> converted = validator.convertData(new ArrayList<>(Arrays.asList(1, "2", 3.0)), "INT");
        assertEquals(List.of(1, 2, 3), converted);
    }

    @Test
    @DisplayName("convertData 把 PERSON 转成 Person 列表")
    void convertToPersonList() {
        List<Person> converted = validator.convertData(new ArrayList<>(List.of(personMap())), "PERSON");
        assertEquals(1, converted.size());
        assertEquals(20, converted.get(0).getAge());
    }

    @Test
    @DisplayName("convertData 拒绝未知数据类型")
    void convertUnknownDataTypeRejected() {
        assertEquals(ErrorCode.INVALID_DATA_TYPE,
                assertThrows(ApiException.class,
                        () -> validator.convertData(new ArrayList<>(List.of(1)), "STRING")).getErrorCode());
    }

    // ---------- 辅助方法 ----------

    private SortRequest teachingRequest(Object... data) {
        SortRequest request = new SortRequest();
        request.setRequestId("req-1");
        request.setMode("TEACHING");
        request.setAlgorithm("BUBBLE");
        request.setDataType("INTEGER");
        request.setData(new ArrayList<>(Arrays.asList(data)));
        request.setInterval(1000);
        request.setDistribution("RANDOM");
        return request;
    }

    private Object[] intArray(int size) {
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            values[i] = (i * 7) % 100;
        }
        return values;
    }

    private Map<String, Object> personMap() {
        Map<String, Object> person = new HashMap<>();
        person.put("id", 1);
        person.put("name", "张三");
        person.put("age", 20);
        person.put("score", 88.5);
        person.put("email", "zhangsan@example.com");
        return person;
    }
}
