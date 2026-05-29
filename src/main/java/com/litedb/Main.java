package com.litedb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 自动生成测试用的 CSV（如果你要做十万级测试，把这里的1000改成100000即可）
        String testCsv = "test_users.csv";
        generateMockDataIfNotExist(testCsv, 100000);

        QueryEngine engine = new QueryEngine();
        try {
            System.out.println("正在加载数据表...");
            Table table = new Table("users", testCsv);
            engine.registerTable(table);
            System.out.println("数据表 'users' 加载完成，共 " + table.getAllRows().size() + " 行。");

            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=== 轻量级数据库引擎启动 ===");
            System.out.println("示例查询 1: SELECT * FROM users WHERE age > 20 ORDER BY id DESC");
            System.out.println("示例查询 2: SELECT COUNT(id) FROM users");
            System.out.println("特殊指令: INDEX ON <column_name> (用于构建哈希索引)");
            System.out.println("输入 'exit' 退出程序。\n");

            while (true) {
                System.out.print("LiteDB> ");
                String sql = scanner.nextLine().trim();

                if (sql.equalsIgnoreCase("exit")) break;

                if (sql.toUpperCase().startsWith("INDEX ON ")) {
                    String colName = sql.substring(9).trim();
                    table.createHashIndex(colName);
                    continue;
                }

                if (!sql.isEmpty()) {
                    engine.executeQuery(sql);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateMockDataIfNotExist(String fileName, int rows) {
        File file = new File(fileName);
        if (file.exists()) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,name,age,department,salary\n");
            for (int i = 1; i <= rows; i++) {
                String name = "User_" + i;
                int age = 20 + (i % 40);
                String dept = i % 2 == 0 ? "Tech" : "Sales";
                double salary = 5000 + (i % 100) * 100;
                writer.write(String.format("%d,%s,%d,%s,%.2f\n", i, name, age, dept, salary));
            }
        } catch (IOException e) {
            System.out.println("创建测试文件失败: " + e.getMessage());
        }
    }
}