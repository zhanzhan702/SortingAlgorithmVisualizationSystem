# 排序算法可视化教学与实验数据管理平台

一个支持教学演示和性能测试的排序算法可视化系统——**数据库（数据结构）课程设计项目**。

通过 WebSocket 实现前后端实时通信，直观展示 6 种经典排序算法的执行过程与性能对比。
集成 **MySQL 8.0 + MyBatis-Plus**，完整实现触发器、视图、存储过程等数据库对象。

## 技术栈

### 后端

| 技术         | 版本                  |
| ------------ | --------------------- |
| Java         | 17                    |
| Spring Boot  | 4.0.6                 |
| MyBatis-Plus | 3.5.15                |
| MySQL        | 8.0                   |
| Druid 连接池 | 1.2.20                |
| WebSocket    | Jakarta WebSocket API |
| Lombok       | 1.18.36               |
| 构建工具     | Maven                 |

### 前端

| 技术               | 版本  |
| ------------------ | ----- |
| Vue.js             | 3.4   |
| Vite               | 5.4   |
| Pinia（状态管理）  | 2.1.7 |
| Vue Router（路由） | 4.x   |
| Chart.js（图表）   | 4.4   |

### 数据库对象

| 类型     | 名称       | 用途                                                                                                           |
| -------- | ---------- | -------------------------------------------------------------------------------------------------------------- |
| 表       | 7 张       | users, algorithms, teaching_experiments, experiment_steps, performance_batches, batch_details, algorithm_stats |
| 触发器   | 2 个       | trg_after_experiment_insert, trg_after_batch_detail_insert — 自动维护 algorithm_stats                          |
| 视图     | 2 个       | v_algorithm_ranking（排名）, v_user_activity（活跃度）                                                         |
| 存储过程 | 1 个       | sp_user_report(userId) — 用户综合报告（教学+性能，3个结果集）                                                  |
| 主键策略 | UUID(32位) | users 表使用 VARCHAR(32) + MyBatis-Plus ASSIGN_UUID                                                            |

## 功能特性

### 教学模式

- **6 种排序算法**: 冒泡、快速、直接插入、希尔、堆、二路归并
- **实时可视化**: WebSocket 推送每一步排序状态，SVG 柱状图渲染
- **走查控制**: 暂停/继续、停止、单步执行
- **暂停调参**: 暂停时可动态调整步进间隔
- **步骤快照**: 勾选"保存回放"后，每一步数据存入 experiment_steps 表
- **伪代码展示**: 从后端 API 获取算法伪代码
- **多种数据类型**: 整数、浮点数、Person 结构体
- **多种数据分布**: 随机、有序、逆序、大量重复、正态分布

### 性能模式

- **6 算法对比**: 依次执行所有算法，Chart.js 柱状图展示
- **性能指标**: 运行时间(µs)、比较次数、交换次数
- **结果持久化**: 自动保存到 performance_batches + batch_details 表

### 实验历史

- **教学记录**: 分页查看历史实验，支持详情弹窗（含步骤列表）
- **性能记录**: 批次列表 + 排名详情弹窗
- **回放功能**: 加载历史实验数据到可视化页面
- **教师模式**: 可切换查看"全部用户"的实验记录

### 管理后台

- **算法统计**：合并展示教学/性能双维度指标 + 时/元素 + 速度排名（合并原"综合排名"tab）
- **用户活跃**: v_user_activity 视图 — 用户活动汇总
- **用户报告**: sp_user_report 存储过程 — 教学+性能完整报告 + 算法明细对比表
- **数据库备份**: 一键导出 SQL 备份文件（含 TRUNCATE + 反引号列名 + utf8mb4），一行命令恢复
  ```bash
  mysql -u root -p --default-character-set=utf8mb4 sorting_visualization < backups/xxx.sql
  ```

### 用户权限

| 功能                       | Student | Teacher | Admin |
| -------------------------- | :-----: | :-----: | :---: |
| 运行排序实验               |   ✅    |   ✅    |  ✅   |
| 查看自己实验历史           |   ✅    |   ✅    |  ✅   |
| 查看全部用户实验           |   ❌    |   ✅    |  ✅   |
| 管理后台（统计/排名/活跃） |   ❌    |   ✅    |  ✅   |
| 数据库备份                 |   ❌    |   ❌    |  ✅   |

## 环境要求

- Java 17+
- Node.js 16+
- Maven 3.6+
- MySQL 8.0+

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS sorting_visualization
  DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化表结构（首次）

```bash
mysql -u root -p sorting_visualization < src/main/resources/db/schema.sql
mysql -u root -p sorting_visualization < src/main/resources/db/data.sql
mysql -u root -p sorting_visualization < src/main/resources/db/procedures.sql
```

> 或跳过手动初始化：应用首次启动时 `DatabaseInitializer` 会自动创建触发器/视图/存储过程，并执行 UUID 迁移。

### 3. 配置数据库密码

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.password=你的MySQL密码
```

### 4. 构建并启动

```bash
# 构建前端到 src/main/resources/static/
cd frontend-vue && npm install && npm run build && cd ..

# 启动后端（含前端静态资源）
mvn spring-boot:run
```

浏览器访问 **`http://localhost:8080`** 即可。

## 预设账号

| 用户名    | 密码         | 角色   |
| --------- | ------------ | ------ |
| `admin`   | `admin123`   | 管理员 |
| `teacher` | `teacher123` | 教师   |
| `student` | `student123` | 学生   |

## 项目结构

```
SortingAlgorithmVisualizationSystem/
├── src/main/java/com/sorting/visualization/
│   ├── algorithm/          # 6 种排序算法实现
│   ├── config/             # CORS、WebSocket、DatabaseInitializer 配置
│   ├── controller/         # REST API 控制器
│   ├── entity/             # 数据库实体（User UUID）
│   ├── mapper/             # MyBatis-Plus Mapper
│   ├── model/              # 请求/响应 DTO
│   ├── service/            # 业务逻辑（Experiment/Batch/User/Backup）
│   ├── util/               # JSON、数据验证等工具
│   └── websocket/          # WebSocket 会话管理 + 消息处理器
├── src/main/resources/
│   ├── application.properties  # 应用配置
│   ├── db/                     # SQL 脚本
│   │   ├── schema.sql          # 建表 DDL
│   │   ├── data.sql            # 初始数据
│   │   ├── procedures.sql      # 触发器+视图+存储过程
│   └── static/                 # 前端构建产物
└── frontend-vue/
    ├── src/
    │   ├── components/     # Vue 组件
    │   ├── composables/    # 组合式函数（WebSocket）
    │   ├── stores/         # Pinia 状态管理
    │   ├── utils/          # 工具函数
    │   ├── views/          # 页面视图
    │   └── router/         # 路由配置（含角色守卫）
    └── vite.config.js
```

## 鉴权与 API 端点

### 鉴权规则

所有 `/api/**` 默认需要 JWT：请求头 `Authorization: Bearer <token>`，
令牌由 `POST /api/auth/login` 签发（HS256，默认 24 小时，密钥与时长见 `jwt.secret` / `jwt.expiration`）。

| 范围                                                     | 要求                 |
| -------------------------------------------------------- | -------------------- |
| `/api/auth/login`、`/api/auth/register`、`/api/algorithms` | 公开                 |
| `/api/history/**`、`/api/auth/me`                         | 任意已登录用户       |
| `/api/admin/**` 的查询                                    | `teacher` 或 `admin` |
| `POST /api/admin/cleanup`、`POST /api/admin/backup`       | 仅 `admin`           |

未登录返回 401（`UNAUTHORIZED` / `TOKEN_INVALID` / `TOKEN_EXPIRED`），权限不足返回 403（`FORBIDDEN`），
错误体统一为 `{"success": false, "code": "...", "message": "..."}`。

### 认证

- `POST /api/auth/register` — 注册（角色只能是 `student` / `teacher`）
- `POST /api/auth/login` — 登录，返回 `token` / `role` / `expiresIn`
- `GET /api/auth/me` — 当前登录者信息（从令牌解析，不信客户端传参）

### 历史记录（需登录）

- `GET /api/history/experiments?userId=` — 用户实验（学生只能取到本人数据）
- `GET /api/history/experiments/all` — 全部实验（教师）
- `GET /api/history/experiments/{id}/steps` — 步骤快照
- `GET /api/history/performance?userId=` — 性能批次
- `GET /api/history/performance/{id}/details` — 批次明细

### 管理后台（`teacher` / `admin`，写操作仅 `admin`）

- `GET /api/admin/stats` — 算法统计（触发器）
- `GET /api/admin/ranking` — 综合排名（视图）
- `GET /api/admin/activity` — 用户活跃（视图）
- `GET /api/admin/report?userId=` — 用户报告（存储过程）
- `POST /api/admin/backup` — 数据库备份（仅 `admin`）
- `GET /api/admin/backups` — 备份文件列表
- `POST /api/admin/cleanup` — 清空实验数据（仅 `admin`）

### 统一错误响应与参数校验（W2/W3）

失败响应的 HTTP 状态码与错误体一致，前端可直接取 `message` 展示、取 `details` 定位字段：

```json
{
  "success": false,
  "timestamp": 1763380800000,
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "用户名长度必须为 3-50 个字符",
  "path": "/api/auth/register",
  "details": [{ "field": "username", "message": "用户名长度必须为 3-50 个字符" }]
}
```

| 错误码                                                                                          | HTTP      | 场景                                   |
| ----------------------------------------------------------------------------------------------- | --------- | -------------------------------------- |
| `VALIDATION_ERROR` / `MISSING_PARAMETER` / `TYPE_MISMATCH`                                      | 400       | 字段规则不满足、缺参、类型不匹配       |
| `MALFORMED_JSON`                                                                                | 400       | 请求体不是合法 JSON                    |
| `INVALID_DATA_TYPE` / `UNSUPPORTED_ALGORITHM` / `DATA_TOO_LARGE` / `INVALID_INTERVAL` / `INVALID_DATE` / `INVALID_ACTION` | 400       | 排序请求与查询参数取值非法             |
| `INVALID_CREDENTIALS` / `UNAUTHORIZED` / `TOKEN_EXPIRED` / `TOKEN_INVALID`                      | 401       | 凭证错误或令牌失效                     |
| `FORBIDDEN`                                                                                     | 403       | 角色不足                               |
| `NOT_FOUND` / `EXPERIMENT_NOT_FOUND` / `BATCH_NOT_FOUND`                                        | 404       | 资源不存在                             |
| `METHOD_NOT_ALLOWED` / `UNSUPPORTED_MEDIA_TYPE`                                                 | 405 / 415 | 方法或内容类型不支持                   |
| `USERNAME_EXISTS` / `ALREADY_PROCESSING` / `NO_ACTIVE_TASK` / `INVALID_STATE`                   | 409       | 唯一约束或状态冲突                     |
| `INTERNAL_ERROR` / `DATABASE_ERROR` / `ALGORITHM_ERROR` / `BACKUP_FAILED` / `CLEANUP_FAILED`     | 500       | 服务端异常（只回通用文案，细节见日志） |

校验规则摘要：

| 位置       | 规则                                                                                                     |
| ---------- | -------------------------------------------------------------------------------------------------------- |
| 注册       | 用户名 3–50 字符、密码 6–64 字符，角色只能是 `student` / `teacher`（传 `admin` 返回 400）                 |
| 登录       | 用户名/密码非空；凭证错误返回 401                                                                        |
| 分页       | `page ≥ 1`，`size` 1–100                                                                                 |
| 历史记录   | `expId` / `batchId` 必须为正整数且存在，否则 404；`start` / `end` 为 `yyyy-MM-dd`；`type` ∈ `TEACHING` / `PERFORMANCE` |
| WebSocket  | `mode` / `algorithm` / `dataType` 白名单；数据量教学 1–100、性能 1–1000；`interval` 100–5000；`distribution`、`comparatorInfo` 白名单；Person 的 age/score/email 规则 |

WebSocket 的 `ERROR` 消息同样携带 `code` 与 `requestId`，错误码与上表一致。

### WebSocket

- `ws://localhost:8080/websocket?token=<JWT>` — 可视化会话通道；握手时可用 `token` 参数绑定用户身份（不强制校验，未登录也可浏览，但教学实验与性能结果不会入库）

## License

MIT

---

## WebSocket 通信协议

### 请求消息

| 消息类型       | 说明         | 示例                                                                          |
| -------------- | ------------ | ----------------------------------------------------------------------------- |
| `SORT_REQUEST` | 发起排序请求 | `{"type":"SORT_REQUEST","mode":"TEACHING","algorithm":"BUBBLE","data":[...]}` |
| `CONTROL`      | 控制命令     | `{"type":"CONTROL","action":"PAUSE"}`                                         |

### 控制命令 (`CONTROL`)

| Action         | 说明               | 可选参数                         |
| -------------- | ------------------ | -------------------------------- |
| `PAUSE`        | 暂停排序           | —                                |
| `RESUME`       | 继续排序           | `interval`: 更新步进间隔（毫秒） |
| `STOP`         | 停止排序           | —                                |
| `STEP_FORWARD` | 单步执行（暂停时） | —                                |

### 响应消息

| 消息类型             | 说明                                       |
| -------------------- | ------------------------------------------ |
| `STEP_UPDATE`        | 排序步骤更新（含数据快照、高亮信息、统计） |
| `PERFORMANCE_RESULT` | 性能测试结果                               |
| `SORT_COMPLETE`      | 排序完成（含最终统计）                     |
| `ERROR`              | 错误信息（含 `code` / `requestId`）        |
| `WARNING`            | 非致命提示（如排序完成但结果未能落库）     |
| `CONNECTED`          | WebSocket 连接成功                         |
| `PAUSED`             | 排序已暂停确认                             |
| `RESUMED`            | 排序已继续确认                             |
| `STOPPED`            | 排序已停止确认                             |

## 开发指南

### 添加新排序算法

1. 在 `impl/` 中创建新算法类，继承 `AbstractSortingAlgorithm<T>`
2. 实现 `teach()` 和 `perform()` 方法
3. 在 `AlgorithmConstants.ALGORITHM_IDS` 中添加算法标识
4. 在 `MessageHandler` 构造函数中注册算法实例
5. 在前端的 `availableAlgorithms` 中添加选项

### 算法常量管理

所有算法元信息（名称、复杂度、稳定性）集中在 `AlgorithmConstants` 类中，新增算法只需修改一处即可同步到 `SortService`、`HealthController`、`DataValidator` 等所有引用方。

### 伪代码管理

伪代码维护在后端 `PseudoCodeUtil` 中，前端通过 `GET /api/algorithms` 接口获取，避免前后端重复维护。

### 运行测试

```bash
# 全量单元测试（15 个测试类、278 个用例）+ 生成 JaCoCo 覆盖率报告
mvn test

# 只跑某一类，例如排序算法
mvn test -Dtest=SortingAlgorithmTest
```

- 覆盖率报告：`target/site/jacoco/index.html`（行 97.1% / 分支 88.2%，统计口径见
  [`docs/W6-覆盖率报告/README.md`](docs/W6-覆盖率报告/README.md)）
- 归档报告：[`docs/W6-覆盖率报告/`](docs/W6-覆盖率报告/) 内含 JaCoCo 逐行报告与 15 个测试类的 surefire 结果，可直接打开
- 全部测试都是纯单元测试：**不需要启动 Spring 容器，也不需要连接 MySQL**

### 鉴权实现位置

| 类                                        | 作用                                              |
| ----------------------------------------- | ------------------------------------------------- |
| `security/JwtUtil`                        | HS256 签发与校验，密钥来自 `jwt.secret`           |
| `security/AuthInterceptor`                | 校验 `Authorization` 头，把用户信息写入 request   |
| `security/RoleInterceptor`                | `/api/admin/**` 的角色分级                         |
| `config/WebConfig`                        | 注册拦截器，`/api/**` 默认保护 + 白名单           |
| `config/JwtConfig`                        | 把配置项注入 `JwtUtil`                            |
| `frontend-vue/src/utils/api.js`           | 前端统一注入令牌，401 跳登录、403 提示            |

## 暂停控制机制

系统使用事件驱动（`Object.wait()/notify()`）实现零 CPU 开销的暂停/恢复：

```
PAUSE  → isPaused = true  → 发送线程调用 waitIfPaused() → 阻塞等待
RESUME → isPaused = false → pauseLock.notifyAll()        → 发送线程恢复
STEP   → stepCounter++     → pauseLock.notifyAll()        → 执行一步后重新阻塞
```

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！
