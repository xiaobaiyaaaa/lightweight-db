package com.litedb;

import java.util.Map;

public class Condition {
    private final String column;
    private final String operator;
    private final String value;

    public Condition(String column, String operator, String value) {
        this.column = column.trim();
        this.operator = operator.trim();
        // 自动剥离目标值两边的单引号或双引号
        this.value = value.trim().replaceAll("^['\"]|['\"]$", "");
    }

    public String getColumn() { return column; }
    public String getOperator() { return operator; }
    public String getValue() { return value; }

    public boolean evaluate(Map<String, String> row) {
        String rowValue = row.get(column);
        if (rowValue == null) return false;

        try {
            double rVal = Double.parseDouble(rowValue);
            double tVal = Double.parseDouble(value);
            switch (operator) {
                case "=": return rVal == tVal;
                case ">": return rVal > tVal;
                case "<": return rVal < tVal;
                case ">=": return rVal >= tVal;
                case "<=": return rVal <= tVal;
                case "!=": return rVal != tVal;
                default: throw new IllegalArgumentException("不支持的运算符: " + operator);
            }
        } catch (NumberFormatException e) {
            int cmp = rowValue.compareTo(value);
            switch (operator) {
                case "=": return cmp == 0;
                case ">": return cmp > 0;
                case "<": return cmp < 0;
                case ">=": return cmp >= 0;
                case "<=": return cmp <= 0;
                case "!=": return cmp != 0;
                default: throw new IllegalArgumentException("不支持的运算符: " + operator);
            }
        }
    }
}