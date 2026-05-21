# SortingAlgorithmVisualizationSystem

一个支持教学演示和性能测试的排序算法可视化系统，通过WebSocket实现前后端实时通信，直观展示多种排序算法的执行过程和性能对比。

## 技术栈

### 后端
- **Java**: 17
- **Spring Boot**: 3.1.5
- **WebSocket**: Jakarta WebSocket API 2.1.0
- **构建工具**: Maven

### 前端
- **Vue.js**: 3.4.0
- **Vite**: 5.0.0
- **状态管理**: Pinia 2.1.7
- **图表库**: Chart.js 4.4.0

## 功能特性

- **多种排序算法支持**: 包括冒泡排序、快速排序、归并排序等 6 种经典算法
- **实时可视化**: 通过 WebSocket 实时展示排序过程，支持 SVG 增量渲染
- **走查模式**: 支持暂停/继续、停止、单步执行，精确控制排序过程
- **暂停调参**: 暂停时可调整步进间隔，恢复后立即生效
- **性能对比**: 提供算法执行时间、比较次数、交换次数等性能指标及 Chart.js 图表
- **数据输入**: 支持随机数据生成、手动输入或文件导入
- **多种数据类型**: 支持整数、浮点数、Person 结构体
- **教学辅助**: 显示算法伪代码（从后端 API 获取）和详细说明
- **响应式设计**: 4 层断点适配不同屏幕尺寸（1100px / 900px / 768px / 480px）

## 安装和运行

### 环境要求
- Java 17 或更高版本
- Node.js 16 或更高版本
- Maven 3.6 或更高版本

### 后端运行
1. 进入项目根目录
2. 运行 Maven 构建：
   ```bash
   mvn clean install
   ```
3. 启动 Spring Boot 应用：
   ```bash
   mvn spring-boot:run
   ```
   或直接运行 JAR 文件：
   ```bash
   java -jar target/visualization-backend-1.0.0.jar
   ```

后端服务将在 `http://localhost:8080` 启动。

### 前端运行
1. 进入前端目录：
   ```bash
   cd frontend-vue
   ```
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```

前端应用将在 `http://localhost:5173` 启动。

### 完整应用访问
启动前后端后，推荐在浏览器中访问 `http://localhost:5173` 使用前端开发服务器（热更新），或访问 `http://localhost:8080` 使用后端内置的静态资源。

## 项目结构

```
SortingAlgorithmVisualizationSystem/
├── src/main/java/com/sorting/visualization/
│   ├── algorithm/               # 排序算法实现
│   │   ├── AlgorithmConstants.java   # 算法常量（消除硬编码）
│   │   ├── SortingAlgorithm.java     # 排序接口
│   │   ├── AbstractSortingAlgorithm.java  # 算法基类
│   │   ├── ComparatorFactory.java    # 比较器工厂
│   │   └── impl/                     # 6 种算法实现
│   ├── config/                  # 配置类
│   │   ├── CorsConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/              # REST 控制器
│   │   ├── HealthController.java
│   │   └── WebSocketController.java
│   ├── model/                   # 数据模型
│   │   ├── Highlight.java
│   │   ├── Person.java
│   │   ├── request/             # 请求模型
│   │   └── response/            # 响应模型
│   ├── service/                 # 业务逻辑
│   │   └── SortService.java
│   ├── util/                    # 工具类
│   │   ├── DataValidator.java
│   │   ├── JsonUtil.java
│   │   └── PseudoCodeUtil.java
│   └── websocket/               # WebSocket 处理器
│       ├── MessageHandler.java      # 消息分发与处理
│       ├── SessionState.java        # 会话状态（含暂停/单步控制）
│       └── WebSocketSessionManager.java  # 会话管理
├── src/main/resources/
│   ├── application.properties   # 应用配置
│   └── static/                  # 前端静态文件
├── frontend-vue/                # 前端 Vue.js 项目
│   ├── src/
│   │   ├── components/          # Vue 组件
│   │   │   ├── ControlSection.vue    # 控制面板（含暂停/继续/停止/单步）
│   │   │   ├── ControlPanel/         # 控制面板子组件
│   │   │   ├── InfoPanel/            # 信息面板组件
│   │   │   └── Visualization/        # 可视化组件
│   │   ├── composables/         # Vue 组合式函数
│   │   │   ├── useWebSocket.js       # WebSocket 通信（含 CONTROL 消息）
│   │   │   ├── useVisualizer.js      # SVG 可视化渲染
│   │   │   └── useDataGenerator.js   # 数据生成
│   │   ├── stores/              # Pinia 状态管理
│   │   │   ├── algorithm.js     # 算法状态（伪代码从 API 获取）
│   │   │   ├── data.js          # 数据状态
│   │   │   ├── ui.js            # UI 状态（含 isPaused）
│   │   │   ├── performance.js   # 性能结果
│   │   │   └── comparator.js    # 比较器配置
│   │   ├── utils/               # 前端工具函数
│   │   ├── views/               # 页面视图
│   │   └── assets/              # 样式文件
│   ├── public/                  # 公共静态资源
│   └── package.json             # 前端依赖配置
├── pom.xml                      # Maven 配置
└── README.md                    # 项目说明
```

## WebSocket 通信协议

### 请求消息

| 消息类型 | 说明 | 示例 |
|---------|------|------|
| `SORT_REQUEST` | 发起排序请求 | `{"type":"SORT_REQUEST","mode":"TEACHING","algorithm":"BUBBLE","data":[...]}` |
| `CONTROL` | 控制命令 | `{"type":"CONTROL","action":"PAUSE"}` |

### 控制命令 (`CONTROL`)

| Action | 说明 | 可选参数 |
|--------|------|----------|
| `PAUSE` | 暂停排序 | — |
| `RESUME` | 继续排序 | `interval`: 更新步进间隔（毫秒） |
| `STOP` | 停止排序 | — |
| `STEP_FORWARD` | 单步执行（暂停时） | — |

### 响应消息

| 消息类型 | 说明 |
|---------|------|
| `STEP_UPDATE` | 排序步骤更新（含数据快照、高亮信息、统计） |
| `PERFORMANCE_RESULT` | 性能测试结果 |
| `SORT_COMPLETE` | 排序完成（含最终统计） |
| `ERROR` | 错误信息 |
| `CONNECTED` | WebSocket 连接成功 |
| `PAUSED` | 排序已暂停确认 |
| `RESUMED` | 排序已继续确认 |
| `STOPPED` | 排序已停止确认 |

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

## 暂停控制机制

系统使用事件驱动（`Object.wait()/notify()`）实现零 CPU 开销的暂停/恢复：

```
PAUSE  -> isPaused = true  -> 发送线程调用 waitIfPaused() -> 阻塞等待
RESUME -> isPaused = false -> pauseLock.notifyAll()      -> 发送线程恢复
STEP   -> stepCounter++    -> pauseLock.notifyAll()      -> 发送线程执行一步后重新阻塞
```

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。