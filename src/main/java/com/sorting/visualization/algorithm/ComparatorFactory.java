package com.sorting.visualization.algorithm;

import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

@Slf4j
public class ComparatorFactory {

    /**
     * 创建比较器
     *
     * @param dataType       数据类型
     * @param comparatorInfo 比较器信息
     * @return Comparator<Object>
     */
    @SuppressWarnings("unchecked")
    public static Comparator<Object> createComparator(String dataType, SortRequest.ComparatorInfo comparatorInfo) {
        if (comparatorInfo == null) {
            comparatorInfo = new SortRequest.ComparatorInfo();
            comparatorInfo.setDirection("ascending");
            comparatorInfo.setMethod("numeric");
        }

        String direction = comparatorInfo.getDirection();
        String method = comparatorInfo.getMethod();
        boolean ascending = "ascending".equals(direction);

        // 规范化数据类型
        String normalizedType = normalizeDataType(dataType);

        // Person类型比较器
        if ("PERSON".equalsIgnoreCase(normalizedType)) {
            return createPersonComparator(ascending, method);
        }

        // 数值类型比较器（Integer, Double）
        return createNumericComparator(ascending, method);
    }

    /**
     * 规范化数据类型
     */
    private static String normalizeDataType(String dataType) {
        if (dataType == null) return null;

        String type = dataType.toUpperCase();
        if ("INT".equals(type)) {
            return "INTEGER";
        }
        return type;
    }

    /**
     * 创建Person比较器
     */
    private static Comparator<Object> createPersonComparator(boolean ascending, String method) {
        return (a, b) -> {
            Person p1 = (Person) a;
            Person p2 = (Person) b;

            // Person始终按score字段排序
            double value1 = p1.getScore() != null ? p1.getScore() : 0;
            double value2 = p2.getScore() != null ? p2.getScore() : 0;

            return compareValues(value1, value2, ascending, method);
        };
    }

    /**
     * 创建数值比较器
     */
    private static Comparator<Object> createNumericComparator(boolean ascending, String method) {
        return (a, b) -> {
            double value1 = convertToDouble(a);
            double value2 = convertToDouble(b);

            return compareValues(value1, value2, ascending, method);
        };
    }

    /**
     * 比较两个值
     */
    private static int compareValues(double value1, double value2, boolean ascending, String method) {
        double v1 = value1;
        double v2 = value2;

        // 根据比较方法处理值
        switch (method.toLowerCase()) {
            case "absolute":
                v1 = Math.abs(value1);
                v2 = Math.abs(value2);
                break;
            case "reverse":
                // 反向比较：交换比较逻辑
                if (ascending) {
                    return Double.compare(value2, value1);
                } else {
                    return Double.compare(value1, value2);
                }
        }

        // 正常比较
        if (ascending) {
            return Double.compare(v1, v2);
        } else {
            return Double.compare(v2, v1);
        }
    }

    /**
     * 将对象转换为double
     */
    private static double convertToDouble(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        } else if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Float) {
            return ((Float) obj).doubleValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
}