package com.wustzdy.springboot.flowable.demo.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * NumKit
 */
public class NumKit {

    // double format
    public static final DecimalFormat DF = new DecimalFormat("##.00");

    /**
     * int值
     *
     * @param value        value
     * @param min          min
     * @param max          max
     * @param defaultValue defaultValue
     * @return int
     */
    public static int intValue(Integer value, int min, int max, int defaultValue) {
        if (value == null) return defaultValue;
        if (value < min || value > max) {
            return defaultValue;
        }
        return value;
    }

    /**
     * int值
     *
     * @param obj obj
     * @return int
     */
    public static int intValueOf(Object obj) {
        long value = longValueOf(obj);
        return (int) value;
    }

    /**
     * long值
     *
     * @param obj obj
     * @return int
     */
    public static long longValueOf(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Double) {
            return ((Double) obj).longValue();
        } else if (obj instanceof Float) {
            return ((Float) obj).longValue();
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).longValue();
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).longValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof String) {
            return NumberUtils.toLong((String) obj, 0);
        } else {
            return NumberUtils.toLong(obj.toString(), 0);
        }
    }

    /**
     * 格式化double
     *
     * @param value double value
     * @return double
     */
    public static double formatDouble(double value) {
        return NumberUtils.toDouble(DF.format(value));
    }

    /**
     * 格式化百分比
     *
     * @param percent percent
     * @return double
     */
    public static double percent(double percent) {
        return formatDouble(percent);
    }

    /**
     * 格式化百分比
     *
     * @param percent percent
     * @return double
     */
    public static double percent(String percent) {
        return NumberUtils.toDouble(DF.format(percent));
    }

    /**
     * 格式化百分比
     *
     * @param front front
     * @param total total
     * @return double
     */
    public static double percent(double front, double total) {
        if (front == 0.0D)
            return 0;
        if (front == total)
            return 100;
        return percent(front / total);
    }
}
