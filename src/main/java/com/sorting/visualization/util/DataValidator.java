package com.sorting.visualization.util;

import com.sorting.visualization.algorithm.AlgorithmConstants;
import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.DataType;
import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 排序请求的参数校验与数据转换（W3）。
 *
 * <p>校验失败统一抛出 {@link ApiException}，错误码来自 {@link ErrorCode}；
 * WebSocket 入口会把它原样转换成 {@code ERROR} 消息，HTTP 入口由全局异常处理器转换。
 * 校验通过后 {@link #convertData} 才做类型归一化，避免"校验一套规则、转换另一套规则"。</p>
 */
@Slf4j
@Component
public class DataValidator {

    /** 教学/性能模式允许的数据量上限由配置决定 */
    @Value("${sorting.max-teaching-size:100}")
    private int maxTeachingSize;

    @Value("${sorting.max-performance-size:1000}")
    private int maxPerformanceSize;

    @Value("${person.age.min:0}")
    private int minAge;

    @Value("${person.age.max:120}")
    private int maxAge;

    @Value("${person.score.min:0}")
    private double minScore;

    @Value("${person.score.max:150}")
    private double maxScore;

    private static final int MIN_INTERVAL = 100;
    private static final int MAX_INTERVAL = 5000;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 100;

    private static final Set<String> SUPPORTED_MODES = Set.of("TEACHING", "PERFORMANCE");
    private static final Set<String> SUPPORTED_DISTRIBUTIONS =
            Set.of("RANDOM", "SORTED", "REVERSE", "DUPLICATE", "NORMAL", "CUSTOM");
    private static final Set<String> SUPPORTED_DIRECTIONS = Set.of("ASCENDING", "DESCENDING");
    private static final Set<String> SUPPORTED_METHODS = Set.of("NUMERIC", "ABSOLUTE", "REVERSE");
    private static final Set<String> SUPPORTED_PERSON_FIELDS =
            Set.of("ID", "NAME", "AGE", "SCORE", "EMAIL");

    /** 前端使用 crypto.randomUUID()，允许字母、数字、下划线、短横线 */
    private static final Pattern REQUEST_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    /**
     * 邮箱只做"形状"校验：本地部分允许中文（前端 DataGenerator 生成的就是
     * {@code 张.伟@gmail.com} 这类地址），因此不能只接受 ASCII。
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * 验证排序请求。
     */
    public void validateSortRequest(SortRequest request) {
        if (request == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请求不能为空");
        }

        // requestId 仅用于错误关联，允许缺省
        String requestId = request.getRequestId();
        if (requestId != null && !requestId.isBlank() && !REQUEST_ID.matcher(requestId.trim()).matches()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "requestId 格式不正确（最长 64 位，仅允许字母、数字、下划线、短横线）");
        }

        // 模式
        String mode = normalize(request.getMode());
        if (mode == null || !SUPPORTED_MODES.contains(mode)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "无效的模式: " + request.getMode());
        }

        // 算法
        String algorithm = normalize(request.getAlgorithm());
        if (!AlgorithmConstants.isValidAlgorithm(algorithm)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_ALGORITHM,
                    "不支持的算法: " + request.getAlgorithm());
        }

        // 数据类型
        DataType dataType = DataType.from(request.getDataType());
        if (dataType == null) {
            throw new ApiException(ErrorCode.INVALID_DATA_TYPE,
                    "无效的数据类型: " + request.getDataType());
        }

        // 数据
        List<Object> data = request.getData();
        if (data == null || data.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "数据不能为空");
        }

        int maxSize = "TEACHING".equals(mode) ? maxTeachingSize : maxPerformanceSize;
        if (data.size() > maxSize) {
            throw new ApiException(ErrorCode.DATA_TOO_LARGE,
                    String.format("数据量超过限制: %d > %d", data.size(), maxSize));
        }

        // 数据内容
        validateDataContent(data, dataType);

        // 步进间隔：仅教学模式需要
        if ("TEACHING".equals(mode)) {
            Integer interval = request.getInterval();
            if (interval == null || interval < MIN_INTERVAL || interval > MAX_INTERVAL) {
                throw new ApiException(ErrorCode.INVALID_INTERVAL,
                        String.format("间隔时间必须在 %d-%d 毫秒之间", MIN_INTERVAL, MAX_INTERVAL));
            }
        }

        // 数据分布 / 比较器配置
        validateDistribution(request.getDistribution());
        validateComparatorInfo(request.getComparatorInfo(), dataType);

        log.info("请求验证通过: requestId={}, mode={}, algorithm={}, dataSize={}, dataType={}",
                request.getRequestId(), mode, algorithm, data.size(), dataType.code());
    }

    /**
     * 验证数据内容。
     */
    private void validateDataContent(List<Object> data, DataType dataType) {
        for (int i = 0; i < data.size(); i++) {
            Object item = data.get(i);
            if (item == null) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR,
                        String.format("第%d个数据不能为空", i + 1));
            }
            try {
                switch (dataType) {
                    case INTEGER -> validateInteger(item);
                    case DOUBLE -> validateDouble(item);
                    case PERSON -> validatePerson(item);
                }
            } catch (ApiException e) {
                throw new ApiException(e.getErrorCode(),
                        String.format("第%d个数据验证失败: %s", i + 1, e.getMessage()), null, e);
            }
        }
    }

    /**
     * 整数校验：拒绝小数、非有限值、超出 int 范围的值。
     * 允许 {@code 3.0} 这类"值为整数"的浮点数与数字字符串。
     */
    private void validateInteger(Object item) {
        if (item instanceof Integer || item instanceof Short || item instanceof Byte) {
            return;
        }
        if (item instanceof Long value) {
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "整数超出范围: " + value);
            }
            return;
        }
        if (item instanceof Number number) {
            double value = number.doubleValue();
            if (!Double.isFinite(value)) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的整数: " + item);
            }
            if (value != Math.rint(value)) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是整数: " + item);
            }
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "整数超出范围: " + item);
            }
            return;
        }
        if (item instanceof String text) {
            try {
                Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的整数: " + item);
            }
            return;
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的整数: " + item);
    }

    /**
     * 浮点数校验：拒绝 NaN / Infinity。
     */
    private void validateDouble(Object item) {
        if (item instanceof Number number) {
            if (!Double.isFinite(number.doubleValue())) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的浮点数: " + item);
            }
            return;
        }
        if (item instanceof String text) {
            double value;
            try {
                value = Double.parseDouble(text.trim());
            } catch (NumberFormatException e) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的浮点数: " + item);
            }
            if (!Double.isFinite(value)) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的浮点数: " + item);
            }
            return;
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, "不是有效的浮点数: " + item);
    }

    /**
     * Person 结构体校验。
     */
    private void validatePerson(Object item) {
        Person person;
        if (item instanceof Person p) {
            person = p;
        } else if (item instanceof Map) {
            person = JsonUtil.convertValue(item, Person.class);
            if (person == null) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person 数据格式错误");
            }
        } else {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person 数据必须是对象类型");
        }

        if (person.getId() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.id 不能为空");
        }
        if (person.getId() < 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.id 不能为负数: " + person.getId());
        }

        if (person.getName() == null || person.getName().isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.name 不能为空");
        }
        if (person.getName().trim().length() > MAX_NAME_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Person.name 长度不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }

        if (person.getAge() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.age 不能为空");
        }
        if (person.getAge() < minAge || person.getAge() > maxAge) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    String.format("Person.age 必须在 %d-%d 之间: %d", minAge, maxAge, person.getAge()));
        }

        if (person.getScore() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.score 不能为空");
        }
        if (!Double.isFinite(person.getScore())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.score 不是有效数字");
        }
        if (person.getScore() < minScore || person.getScore() > maxScore) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    String.format("Person.score 必须在 %.1f-%.1f 之间: %.1f",
                            minScore, maxScore, person.getScore()));
        }

        // email 允许缺省；一旦提供则必须是合法邮箱（与前端 fileParser 的格式要求对齐）
        if (person.getEmail() != null && !person.getEmail().isBlank()) {
            String email = person.getEmail().trim();
            if (email.length() > MAX_EMAIL_LENGTH) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR,
                        "Person.email 长度不能超过 " + MAX_EMAIL_LENGTH + " 个字符");
            }
            if (!EMAIL.matcher(email).matches()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person.email 格式不正确: " + email);
            }
        }
    }

    private void validateDistribution(String distribution) {
        if (distribution == null || distribution.isBlank()) {
            return;
        }
        String normalized = distribution.trim().toUpperCase();
        if (!SUPPORTED_DISTRIBUTIONS.contains(normalized)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "无效的数据分布: " + distribution);
        }
    }

    private void validateComparatorInfo(SortRequest.ComparatorInfo comparatorInfo, DataType dataType) {
        if (comparatorInfo == null) {
            return;
        }

        String direction = comparatorInfo.getDirection();
        if (direction != null && !direction.isBlank()
                && !SUPPORTED_DIRECTIONS.contains(direction.trim().toUpperCase())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "无效的排序方向: " + direction);
        }

        String method = comparatorInfo.getMethod();
        if (method != null && !method.isBlank()
                && !SUPPORTED_METHODS.contains(method.trim().toUpperCase())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "无效的比较方法: " + method);
        }

        String structField = comparatorInfo.getStructField();
        if (structField != null && !structField.isBlank()) {
            if (dataType != DataType.PERSON) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "仅 Person 类型支持指定结构体字段");
            }
            if (!SUPPORTED_PERSON_FIELDS.contains(structField.trim().toUpperCase())) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Person 不支持按该字段排序: " + structField);
            }
        }
    }

    /**
     * 数据转换：校验通过后才会调用，规则与校验保持一致。
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> convertData(List<Object> rawData, String dataType) {
        DataType type = DataType.from(dataType);
        if (type == null) {
            throw new ApiException(ErrorCode.INVALID_DATA_TYPE, "未知数据类型: " + dataType);
        }
        if (rawData == null || rawData.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "数据不能为空");
        }

        try {
            return switch (type) {
                case INTEGER -> (List<T>) convertToIntegerList(rawData);
                case DOUBLE -> (List<T>) convertToDoubleList(rawData);
                case PERSON -> (List<T>) convertToPersonList(rawData);
            };
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("数据转换失败: dataType={}", dataType, e);
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "数据转换失败", null, e);
        }
    }

    private List<Integer> convertToIntegerList(List<Object> rawData) {
        List<Integer> result = new ArrayList<>(rawData.size());
        for (Object item : rawData) {
            if (item == null) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "数据不能为 null");
            }
            if (item instanceof Integer value) {
                result.add(value);
            } else if (item instanceof Number number) {
                result.add(number.intValue());
            } else {
                result.add(Integer.parseInt(item.toString().trim()));
            }
        }
        return result;
    }

    private List<Double> convertToDoubleList(List<Object> rawData) {
        List<Double> result = new ArrayList<>(rawData.size());
        for (Object item : rawData) {
            if (item == null) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "数据不能为 null");
            }
            if (item instanceof Double value) {
                result.add(value);
            } else if (item instanceof Number number) {
                result.add(number.doubleValue());
            } else {
                result.add(Double.parseDouble(item.toString().trim()));
            }
        }
        return result;
    }

    private List<Person> convertToPersonList(List<Object> rawData) {
        List<Person> result = new ArrayList<>(rawData.size());
        for (Object item : rawData) {
            if (item instanceof Person person) {
                result.add(person);
            } else if (item instanceof Map) {
                Person person = JsonUtil.convertValue(item, Person.class);
                if (person == null) {
                    throw new ApiException(ErrorCode.VALIDATION_ERROR, "无法转换为 Person: " + item);
                }
                result.add(person);
            } else {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "无法转换为 Person: " + item);
            }
        }
        return result;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
