package com.litedb;

import java.util.*;
import java.util.stream.Collectors;

public class QueryEngine {
    private final Map<String, Table> database = new HashMap<>();
    private final QueryParser parser = new QueryParser();

    public void registerTable(Table table) {
        database.put(table.getTableName().toLowerCase(), table);
    }

    public void executeQuery(String sql) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取查询计划
            QueryParser.QueryPlan plan = parser.parse(sql);

            // 2. 获取目标表
            Table table = database.get(plan.tableName.toLowerCase());
            if (table == null) {
                System.out.println("表不存在: " + plan.tableName);
                return;
            }

            // 3. 执行执行计划
            List<Map<String, String>> resultSet = executeWhere(table, plan.conditions);
            resultSet = executeOrderBy(resultSet, plan.orderByColumn, plan.isDesc);
            executeSelectAndPrint(resultSet, plan.selectClause, startTime);

        } catch (Exception e) {
            System.out.println("[SQL 错误] " + e.getMessage());
        }
    }

    private List<Map<String, String>> executeWhere(Table table, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return new ArrayList<>(table.getAllRows());
        }

        List<Map<String, String>> currentSet = null;

        for (Condition condition : conditions) {
            // 核心优化：命中等值查询的哈希索引
            if (condition.getOperator().equals("=") && table.hasIndex(condition.getColumn()) && currentSet == null) {
                System.out.println("[执行计划] 命中哈希索引: " + condition.getColumn());
                currentSet = table.getRowsByHashIndex(condition.getColumn(), condition.getValue());
                continue;
            }

            if (currentSet == null) currentSet = new ArrayList<>(table.getAllRows());

            // 流式过滤
            currentSet = currentSet.stream()
                    .filter(condition::evaluate)
                    .collect(Collectors.toList());
        }
        return currentSet == null ? new ArrayList<>() : currentSet;
    }

    private List<Map<String, String>> executeOrderBy(List<Map<String, String>> resultSet, String orderByColumn, boolean isDesc) {
        if (orderByColumn == null || orderByColumn.isEmpty()) return resultSet;

        resultSet.sort((r1, r2) -> {
            String v1 = r1.get(orderByColumn);
            String v2 = r2.get(orderByColumn);
            if (v1 == null || v2 == null) return 0;

            try {
                double d1 = Double.parseDouble(v1);
                double d2 = Double.parseDouble(v2);
                return isDesc ? Double.compare(d2, d1) : Double.compare(d1, d2);
            } catch (NumberFormatException e) {
                return isDesc ? v2.compareTo(v1) : v1.compareTo(v2);
            }
        });
        return resultSet;
    }

    private void executeSelectAndPrint(List<Map<String, String>> resultSet, String selectClause, long startTime) {
        if (resultSet.isEmpty()) {
            System.out.println("0 rows affected.");
            System.out.println("查询耗时: " + (System.currentTimeMillis() - startTime) + " ms\n");
            return;
        }

        String upperSelect = selectClause.toUpperCase();
        if (upperSelect.contains("COUNT(") || upperSelect.contains("SUM(") || upperSelect.contains("AVG(") ||
                upperSelect.contains("MAX(") || upperSelect.contains("MIN(")) {
            handleAggregation(resultSet, selectClause);
        } else {
            String[] cols = selectClause.equals("*") ? resultSet.get(0).keySet().toArray(new String[0]) : selectClause.split(",");

            for (String col : cols) System.out.print(col.trim() + "\t|");
            System.out.println("\n-------------------------------------------------");

            for (Map<String, String> row : resultSet) {
                for (String col : cols) System.out.print(row.get(col.trim()) + "\t|");
                System.out.println();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n返回 " + resultSet.size() + " 行数据.");
        System.out.println("查询耗时: " + (endTime - startTime) + " ms\n");
    }

    private void handleAggregation(List<Map<String, String>> resultSet, String selectClause) {
        String funcAndCol = selectClause.trim().toUpperCase();
        String func = funcAndCol.substring(0, funcAndCol.indexOf('('));
        String col = selectClause.substring(selectClause.indexOf('(') + 1, selectClause.indexOf(')')).trim();

        double sum = 0, max = -Double.MAX_VALUE, min = Double.MAX_VALUE;
        int count = 0;

        for (Map<String, String> row : resultSet) {
            String valStr = row.get(col);
            if (valStr == null || valStr.isEmpty()) continue;

            if (func.equals("COUNT")) { count++; continue; }

            try {
                double val = Double.parseDouble(valStr);
                sum += val;
                if (val > max) max = val;
                if (val < min) min = val;
                count++;
            } catch (NumberFormatException ignored) {}
        }

        System.out.println(func + "(" + col + ")");
        System.out.println("-------------------------");
        switch (func) {
            case "COUNT": System.out.println(count); break;
            case "SUM": System.out.println(sum); break;
            case "AVG": System.out.println(count == 0 ? 0 : sum / count); break;
            case "MAX": System.out.println(count == 0 ? "NULL" : max); break;
            case "MIN": System.out.println(count == 0 ? "NULL" : min); break;
        }
    }
}