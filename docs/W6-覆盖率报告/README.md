# W6 单元测试与覆盖率报告

> 负责人：王章强（3242508201）　提交：`b1cdbac`　分支：`develop`

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

## 四、统计范围

覆盖率统计纳入单元测试覆盖的核心业务逻辑层：

| 纳入统计 | 说明 |
| --- | --- |
| `algorithm`（含 `impl`） | 六种排序算法、比较器工厂、算法常量 |
| `util` | 数据校验、JSON 工具、伪代码生成 |
| `security` | JWT、鉴权与角色拦截器 |
| `service` | `SortService`、`UserService` |

不纳入统计的部分：`entity` / `mapper` / `model` / `exception`（POJO 与 MyBatis 映射接口，无可测逻辑）、`controller` / `websocket` / `config`（Web 层与容器配置，按项目计划由 11-21 全链路回归统一验证）、启动类与依赖外部环境的服务类。

## 五、复跑方式

```bash
# 需要 JDK 17+
mvn test
# 报告：target/site/jacoco/index.html
```

本目录同时归档了 2026-11-19 的原始产物，不装 Maven 也能直接看：

| 文件 / 目录 | 内容 |
| --- | --- |
| `jacoco/index.html` | JaCoCo 逐行报告（绿色已覆盖、红色未覆盖），可逐类点开 |
| `jacoco/jacoco.csv` | 覆盖率原始数据，便于二次统计 |
| `覆盖率汇总.html` | 一页纸汇总，可直接截图进《测试报告》 |
| `surefire-reports/` | 15 个测试类的执行结果与耗时 |
| `测试结果汇总.txt` | 278 例明细与四项覆盖率指标 |
