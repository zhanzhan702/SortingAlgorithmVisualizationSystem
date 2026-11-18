# W6 单元测试与覆盖率报告

> 负责人：梁亚和（3242701322）　提交：`b1cdbac`　分支：`develop`

## 一、结果

| 指标 | 数值 |
| --- | --- |
| 单元测试用例 | **278 个**，0 失败 / 0 错误 / 0 跳过 |
| 行覆盖率 | **97.1%** |
| 分支覆盖率 | **88.2%** |
| 指令覆盖率 | 96.6% |
| 方法覆盖率 | 99.3% |
| 构建结果 | `BUILD SUCCESS` |

- 可视化汇总页：[`覆盖率汇总.html`](覆盖率汇总.html)（浏览器直接打开，可打印/截图进《测试报告》）
- 用例明细：[`测试结果汇总.txt`](测试结果汇总.txt)
- 逐行证据：本地执行 `mvn test` 后打开 `target/site/jacoco/index.html`

## 二、本次新增的测试类（7 个，1483 行）

| 测试类 | 包 | 用例 | 覆盖点 |
| --- | --- | --- | --- |
| `SortingAlgorithmTest` | `algorithm` | 111 | 六种算法 × 升序/降序/重复值/负数/已有序/逆序/空数组/单元素/200 随机元素与 `Collections.sort` 比对/不修改入参/教学步骤快照/稳定性/元数据 |
| `ComparatorFactoryTest` | `algorithm` | 25 | `direction` 为空与大小写容错、`method` 默认、`structField` 默认与 trim、PERSON 类型判定 |
| `AlgorithmConstantsTest` | `algorithm` | 5 | 常量表完整性、中文名与伪代码映射 |
| `DataValidatorEdgeCaseTest` | `util` | 33 | requestId 正则边界、间隔上下界、数值内容、Person 字段、分布与比较器白名单、类型转换边界 |
| `JsonUtilTest` | `util` | 12 | 序列化/反序列化、异常输入、泛型与集合 |
| `PseudoCodeUtilTest` | `util` | 8 | 六种算法伪代码、未知算法兜底 |
| `SortServiceTest` | `service` | 5 | 排序执行、步骤返回、异常输入 |

> 已有的 `DataValidatorTest`、`UserServiceTest` 及 controller/security 测试为 W2~W5 队友所写，本次未重复编写，只做统一执行与覆盖率汇总。

## 三、pom.xml 变更

新增 `jacoco-maven-plugin 0.8.12`：`prepare-agent` 挂钩测试阶段，`report` 生成 HTML/XML/CSV 报告。

## 四、统计口径说明（重要）

覆盖率只统计**可用纯单元测试覆盖的逻辑层**，`<excludes>` 中排除：

- `entity` / `mapper` / `model` / `exception`：POJO 与 MyBatis 接口，无业务逻辑
- `controller` / `websocket` / `config`：需要 Spring MVC / WebSocket 容器上下文
- `SortVisualizationApplication`：启动类
- `BackupService` / `BatchService` / `ExperimentService` / `AccountService` / `PaginationService`：方法实现强依赖 MySQL 连接

计入统计的包：`algorithm`（含 impl）、`util`、`security`、`service`（`SortService`、`UserService`）。

若按**全量类**（53 个类、不做任何排除）统计，行覆盖为 52.5%；差额集中在 WebSocket 会话管理与 MVC 控制器层，属集成测试范围，需连接数据库或起容器才能覆盖。

## 五、复跑方式

```bash
# 需要 JDK 17+，无需 MySQL
mvn test
# 报告：target/site/jacoco/index.html
```
