package com.litedb;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    public static class QueryPlan {
        public String selectClause;
        public String tableName;
        public List<List<Condition>> orConditions = new ArrayList<>();
        public String orderByColumn;
        public boolean isDesc = false;
    }

    public QueryPlan parse(String sql) throws Exception {
        QueryPlan plan = new QueryPlan();
        sql = sql.trim().replaceAll(" +", " ");

        if (!sql.toUpperCase().startsWith("SELECT ")) {
            throw new Exception("语法错误：目前仅支持 SELECT 查询。");
        }

        String upperSql = sql.toUpperCase();
        int fromIdx = upperSql.indexOf(" FROM ");
        if (fromIdx == -1) throw new Exception("语法错误：缺少 FROM 关键字。");

        plan.selectClause = sql.substring(7, fromIdx).trim();
        String remainder = sql.substring(fromIdx + 6).trim();

        String[] remainderParts = remainder.split(" ", 2);
        plan.tableName = remainderParts[0].trim();

        if (remainderParts.length == 1) return plan;

        remainder = remainderParts[1].trim();
        String upperRemainder = remainder.toUpperCase();
        String whereClause = null;
        String orderByClause = null;

        if (upperRemainder.startsWith("WHERE ")) {
            int orderByIdx = upperRemainder.indexOf(" ORDER BY ");
            if (orderByIdx != -1) {
                whereClause = remainder.substring(6, orderByIdx).trim();
                orderByClause = remainder.substring(orderByIdx + 10).trim();
            } else {
                whereClause = remainder.substring(6).trim();
            }
        } else if (upperRemainder.startsWith("ORDER BY ")) {
            orderByClause = remainder.substring(9).trim();
        }

        if (whereClause != null && !whereClause.isEmpty()) {
            //先按 OR 拆分外层
            String[] orParts = whereClause.split("(?i) OR ");
            for (String orPart : orParts) {
                List<Condition> andGroup = new ArrayList<>();
                //再按 AND 拆分内层
                String[] andParts = orPart.split("(?i) AND ");
                for (String condStr : andParts) {
                    String op = extractOperator(condStr);
                    String[] parts = condStr.split(op);
                    if (parts.length != 2) throw new Exception("条件语法错误: " + condStr);
                    andGroup.add(new Condition(parts[0], op, parts[1]));
                }
                plan.orConditions.add(andGroup);
            }
        }

        if (orderByClause != null && !orderByClause.isEmpty()) {
            String[] orderParts = orderByClause.split(" ");
            plan.orderByColumn = orderParts[0].trim();
            if (orderParts.length > 1 && orderParts[1].equalsIgnoreCase("DESC")) {
                plan.isDesc = true;
            }
        }

        return plan;
    }

    private String extractOperator(String cond) {
        if (cond.contains(">=")) return ">=";
        if (cond.contains("<=")) return "<=";
        if (cond.contains("!=")) return "!=";
        if (cond.contains("=")) return "=";
        if (cond.contains(">")) return ">";
        if (cond.contains("<")) return "<";
        throw new IllegalArgumentException("不支持的运算符: " + cond);
    }
}