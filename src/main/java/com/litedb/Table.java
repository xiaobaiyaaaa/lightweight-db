package com.litedb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Table {
    private final String tableName;
    private final List<String> columns;
    private final List<Map<String, String>> rows;
    private final Map<String, Map<String, List<Integer>>> hashIndices;

    public Table(String tableName, String csvFilePath) throws IOException {
        this.tableName = tableName;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.hashIndices = new HashMap<>();
        loadCsv(csvFilePath);
    }

    private void loadCsv(String csvFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return;

            columns.addAll(Arrays.asList(headerLine.split(",")));

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    row.put(columns.get(i).trim(), i < values.length ? values[i].trim() : "");
                }
                rows.add(row);
            }
        }
    }

    public void createHashIndex(String columnName) {
        if (!columns.contains(columnName)) return;
        Map<String, List<Integer>> index = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String value = rows.get(i).get(columnName);
            index.computeIfAbsent(value, k -> new ArrayList<>()).add(i);
        }
        hashIndices.put(columnName, index);
        System.out.println("已在列 '" + columnName + "' 上建立哈希索引。");
    }

    public boolean hasIndex(String columnName) {
        return hashIndices.containsKey(columnName);
    }

    public List<Map<String, String>> getRowsByHashIndex(String columnName, String value) {
        List<Integer> rowIndices = hashIndices.get(columnName).getOrDefault(value, new ArrayList<>());
        List<Map<String, String>> result = new ArrayList<>();
        for (int idx : rowIndices) {
            result.add(rows.get(idx));
        }
        return result;
    }

    public List<Map<String, String>> getAllRows() { return rows; }
    public String getTableName() { return tableName; }
}