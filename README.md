# LiteDB - 轻量级数据库引擎

这是一个基于IDEA使用 Java 实现的轻量级、无第三方依赖的内存数据库引擎。本项目主要用于展示数据库底层的核心工程设计。

## ✨ 核心特性

- **零外部依赖**：无需安装 MySQL、SQLite 或引入庞大的 Spring 框架，基于纯 JDK 实现。
- **架构分析**：严格遵循单一职责原则，分离 SQL 语法解析（QueryParser）与 执行（QueryEngine）。
- **支持基础 SQL 语法**：
  - 支持 SELECT 查询与全表扫描（*）。
  - 支持 WHERE 条件过滤（支持 =, >, <, >=, <=, != 及 AND / OR 组合）。
  - 支持 ORDER BY 排序（含 DESC 降序）。
  - 支持聚合函数计算（COUNT, SUM, AVG, MAX, MIN）。
- **极速查询优化**：内置内存哈希索引（Hash Index），针对等值查询可实现 O(1) 级别的时间复杂度。
- **自动化测试机制**：内置 CSV 数据模拟生成器，可一键生成十万级测试数据。

## 📂 项目结构

项目源码位于 src/main/java/com/litedb/ 目录下，包含以下 5 个核心组件：

- Main.java：系统入口，负责自动生成测试数据、加载表结构并提供 CLI（命令行）交互界面。
- Table.java：底层存储引擎，负责从 CSV 磁盘文件加载数据至内存，并维护哈希索引结构。
- QueryParser.java：SQL 解析器，负责拦截用户输入的纯文本 SQL 字符串，经过词法/语法分析后，生成结构化的查询计划（QueryPlan）。
- Condition.java：条件节点对象，负责 WHERE 子句中的具体判断逻辑。
- QueryEngine.java：查询执行引擎，接收解释器传来的 QueryPlan，调度 Table 数据进行过滤、排序及聚合计算，并将结果打印至终端。

## 🚀 快速启动

### 1. 运行环境
- JDK 8 及以上版本（推荐 JDK 17 或 23）
- 任意主流 Java IDE（IntelliJ IDEA, Eclipse）或纯命令行

### 2. 启动方式
在 IDEA 中打开项目后，直接右键运行 Main.java 即可。
程序首次启动时，会自动在项目根目录生成一份 test_users.csv 测试文件。

### 3. 命令行交互测试
启动成功后，你将在控制台看到 LiteDB> 提示符，可以输入指令：

**基础查询与过滤：**
```sql
SELECT * FROM users
SELECT id, name, age FROM users WHERE age > 25 AND department = 'Tech'
```

**排序：**
```sql
SELECT * FROM users WHERE salary >= 8000 ORDER BY salary DESC
```

**聚合函数：**
```sql
SELECT COUNT(id) FROM users
SELECT AVG(salary) FROM users WHERE department = 'Sales'
```

**体验哈希索引加速：**
先执行一条普通的等值查询，记录耗时：
```sql
SELECT * FROM users WHERE age = 30
```
然后以 age 列构建哈希索引：
```sql
INDEX ON age
```
再次执行同样的查询，观察耗时变化：
```sql
SELECT * FROM users WHERE age = 30
```