// util/DataValidator.java
package com.sorting.visualization.util;

import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataValidator {

    @Value("${sorting.max-teaching-size:100}")
    private int maxTeachingSize;

    @Value("${sorting.max-performance-size:20000}")
    private int maxPerformanceSize;

    @Value("${person.age.min:0}")
    private int minAge;

    @Value("${person.age.max:120}")
    private int maxAge;

    @Value("${person.score.min:0}")
    private double minScore;

    @Value("${person.score.max:150}")
    private double maxScore;

    /**
     * 验证排序请求
     */
    public void validateSortRequest(SortRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("请求不能为空", "VALIDATION_ERROR");
        }

        // 验证模式
        if (!"TEACHING".equals(request.getMode()) && !"PERFORMANCE".equals(request.getMode())) {
            throw new ValidationException("无效的模式: " + request.getMode(), "VALIDATION_ERROR");
        }

        // 验证算法
        if (!isValidAlgorithm(request.getAlgorithm())) {
            throw new ValidationException("不支持的算法: " + request.getAlgorithm(), "UNSUPPORTED_ALGORITHM");
        }

        // 验证数据类型
        if (!isValidDataType(request.getDataType())) {
            throw new ValidationException("无效的数据类型: " + request.getDataType(), "INVALID_DATA_TYPE");
        }

        // 验证数据
        List<Object> data = request.getData();
        if (data == null || data.isEmpty()) {
            throw new ValidationException("数据不能为空", "VALIDATION_ERROR");
        }

        // 验证数据大小
        int maxSize = "TEACHING".equals(request.getMode()) ? maxTeachingSize : maxPerformanceSize;
        if (data.size() > maxSize) {
            throw new ValidationException(
                    String.format("数据量超过限制: %d > %d", data.size(), maxSize),
                    "DATA_TOO_LARGE"
            );
        }

        // 验证数据内容
        validateDataContent(data, request.getDataType());

        // 验证间隔时间
        if ("TEACHING".equals(request.getMode())) {
            if (request.getInterval() == null || request.getInterval() < 100 || request.getInterval() > 5000) {
                throw new ValidationException("间隔时间必须在100-5000毫秒之间", "VALIDATION_ERROR");
            }
        }

        log.info("请求验证通过: requestId={}, mode={}, algorithm={}, dataSize={}, dataType={}",
                request.getRequestId(), request.getMode(), request.getAlgorithm(), data.size(), request.getDataType());
    }

    /**
     * 验证数据内容
     */
    private void validateDataContent(List<Object> data, String dataType) throws ValidationException {
        // 规范化数据类型
        String normalizedType = normalizeDataType(dataType);

        for (int i = 0; i < data.size(); i++) {
            Object item = data.get(i);

            try {
                switch (normalizedType) {
                    case "INTEGER":
                        validateInteger(item);
                        break;
                    case "DOUBLE":
                        validateDouble(item);
                        break;
                    case "PERSON":
                        validatePerson(item);
                        break;
                    default:
                        throw new ValidationException("未知数据类型: " + dataType, "INVALID_DATA_TYPE");
                }
            } catch (ValidationException e) {
                throw new ValidationException(
                        String.format("第%d个数据验证失败: %s", i + 1, e.getMessage()),
                        e.getCode()
                );
            }
        }
    }

    /**
     * 规范化数据类型（兼容INT和INTEGER）
     */
    private String normalizeDataType(String dataType) {
        if (dataType == null) return null;

        String type = dataType.toUpperCase();
        if ("INT".equals(type)) {
            return "INTEGER";
        }
        return type;
    }

    /**
     * 验证整数
     */
    private void validateInteger(Object item) throws ValidationException {
        if (item instanceof Integer) {
            return;
        }

        if (item instanceof Number) {
            // 允许数值类型，将在后续转换
            return;
        }

        try {
            Integer.parseInt(item.toString());
        } catch (NumberFormatException e) {
            throw new ValidationException("不是有效的整数: " + item, "VALIDATION_ERROR");
        }
    }

    /**
     * 验证浮点数
     */
    private void validateDouble(Object item) throws ValidationException {
        if (item instanceof Double || item instanceof Float) {
            return;
        }

        if (item instanceof Number) {
            // 允许数值类型
            return;
        }

        try {
            Double.parseDouble(item.toString());
        } catch (NumberFormatException e) {
            throw new ValidationException("不是有效的浮点数: " + item, "VALIDATION_ERROR");
        }
    }

    /**
     * 验证Person对象
     */
    @SuppressWarnings("unchecked")
    private void validatePerson(Object item) throws ValidationException {
        Person person;

        if (item instanceof Person) {
            person = (Person) item;
        } else if (item instanceof Map) {
            try {
                person = JsonUtil.convertValue(item, Person.class);
                if (person == null) {
                    throw new ValidationException("无法转换为Person对象", "VALIDATION_ERROR");
                }
            } catch (Exception e) {
                throw new ValidationException("Person数据格式错误: " + e.getMessage(), "VALIDATION_ERROR");
            }
        } else {
            throw new ValidationException("Person数据必须是对象类型", "VALIDATION_ERROR");
        }

        // 验证必要字段
        if (person.getId() == null) {
            throw new ValidationException("Person.id不能为空", "VALIDATION_ERROR");
        }

        if (person.getName() == null || person.getName().trim().isEmpty()) {
            throw new ValidationException("Person.name不能为空", "VALIDATION_ERROR");
        }

        // 验证年龄范围
        if (person.getAge() == null) {
            throw new ValidationException("Person.age不能为空", "VALIDATION_ERROR");
        }

        if (person.getAge() < minAge || person.getAge() > maxAge) {
            throw new ValidationException(
                    String.format("Person.age必须在%d-%d之间: %d", minAge, maxAge, person.getAge()),
                    "VALIDATION_ERROR"
            );
        }

        // 验证分数范围
        if (person.getScore() == null) {
            throw new ValidationException("Person.score不能为空", "VALIDATION_ERROR");
        }

        if (person.getScore() < minScore || person.getScore() > maxScore) {
            throw new ValidationException(
                    String.format("Person.score必须在%.1f-%.1f之间: %.1f", minScore, maxScore, person.getScore()),
                    "VALIDATION_ERROR"
            );
        }
    }

    /**
     * 验证算法是否支持
     */
    private boolean isValidAlgorithm(String algorithm) {
        if (algorithm == null) return false;

        String algo = algorithm.toUpperCase();
        return "BUBBLE".equals(algo) || "INSERTION".equals(algo) ||
                "SHELL".equals(algo) || "QUICK".equals(algo) ||
                "HEAP".equals(algo) || "MERGE".equals(algo);
    }

    /**
     * 验证数据类型是否支持
     */
    private boolean isValidDataType(String dataType) {
        if (dataType == null) return false;

        String type = dataType.toUpperCase();
        // 支持INT和INTEGER
        return "INTEGER".equals(type) || "INT".equals(type) ||
                "DOUBLE".equals(type) || "PERSON".equals(type);
    }

    /**
     * 数据转换：将对象转换为特定类型
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> convertData(List<Object> rawData, String dataType) throws ValidationException {
        try {
            // 规范化数据类型
            String normalizedType = normalizeDataType(dataType);

            switch (normalizedType) {
                case "INTEGER":
                    return (List<T>) convertToIntegerList(rawData);
                case "DOUBLE":
                    return (List<T>) convertToDoubleList(rawData);
                case "PERSON":
                    return (List<T>) convertToPersonList(rawData);
                default:
                    throw new ValidationException("未知数据类型: " + dataType, "INVALID_DATA_TYPE");
            }
        } catch (Exception e) {
            throw new ValidationException("数据转换失败: " + e.getMessage(), "VALIDATION_ERROR");
        }
    }

    private List<Integer> convertToIntegerList(List<Object> rawData) {
        List<Integer> result = new java.util.ArrayList<>();
        for (Object item : rawData) {
            if (item instanceof Integer) {
                result.add((Integer) item);
            } else if (item instanceof Number) {
                result.add(((Number) item).intValue());
            } else {
                result.add(Integer.parseInt(item.toString()));
            }
        }
        return result;
    }

    private List<Double> convertToDoubleList(List<Object> rawData) {
        List<Double> result = new java.util.ArrayList<>();
        for (Object item : rawData) {
            if (item instanceof Double) {
                result.add((Double) item);
            } else if (item instanceof Number) {
                result.add(((Number) item).doubleValue());
            } else {
                result.add(Double.parseDouble(item.toString()));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Person> convertToPersonList(List<Object> rawData) {
        List<Person> result = new java.util.ArrayList<>();
        for (Object item : rawData) {
            if (item instanceof Person) {
                result.add((Person) item);
            } else if (item instanceof Map) {
                result.add(JsonUtil.convertValue(item, Person.class));
            } else {
                throw new IllegalArgumentException("无法转换为Person: " + item);
            }
        }
        return result;
    }

    /**
     * 验证异常类
     */
    public static class ValidationException extends Exception {
        private final String code;

        public ValidationException(String message, String code) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}