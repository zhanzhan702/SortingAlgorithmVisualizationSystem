package com.sorting.visualization.model;

/**
 * 支持的排序数据类型。
 *
 * <p>原实现中 {@code INT -> INTEGER} 的归一化在 DataValidator、ComparatorFactory、
 * MessageHandler 三处各写了一遍，容易出现规则不一致；统一收敛到这里。</p>
 */
public enum DataType {

    INTEGER,
    DOUBLE,
    PERSON;

    /**
     * 解析前端/接口传入的数据类型，兼容 {@code INT} 与大小写差异。
     *
     * @return 无法识别时返回 {@code null}
     */
    public static DataType from(String rawType) {
        if (rawType == null) {
            return null;
        }
        return switch (rawType.trim().toUpperCase()) {
            case "INT", "INTEGER" -> INTEGER;
            case "DOUBLE" -> DOUBLE;
            case "PERSON" -> PERSON;
            default -> null;
        };
    }

    public static boolean isSupported(String rawType) {
        return from(rawType) != null;
    }

    /** 入库用的规范值（performance_batches.data_type 为 ENUM('INTEGER','DOUBLE','PERSON')） */
    public String code() {
        return name();
    }
}
