package com.houtu.monitor.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorUtils {

    /**
     * 根据percentile计算数组中中位数
     * @param values 数组
     * @param percentile 百分比
     * @return 中位数
     */
    public static long calcQuantile(Long[] values, int percentile) {
        if (values == null
                || values.length == 0
                || percentile <= 0
                || percentile > 100)
            throw new IllegalArgumentException("Invalid argument");
        if (values.length == 1)
            return values[0];
        BigDecimal position = new BigDecimal(percentile).divide(BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(values.length)).stripTrailingZeros();
        if (position.scale() == 0) {
            return values[position.intValue() - 1];
        }
        int floorPosition = position.intValue();
        if (floorPosition <= 1)
            return values[0];
        int preIndex = floorPosition - 1;
        int afterIndex = floorPosition;
        return values[preIndex] + BigDecimal.valueOf(values[afterIndex])
                .subtract(BigDecimal.valueOf(values[preIndex]))
                .multiply(position.subtract(BigDecimal.valueOf(floorPosition)))
                .setScale(0, RoundingMode.HALF_DOWN)
                .longValue();
    }
}
