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

- **多种排序算法支持**: 包括冒泡排序、快速排序、归并排序等经典算法
- **实时可视化**: 通过WebSocket实时展示排序过程
- **性能对比**: 提供算法执行时间、比较次数等性能指标
- **交互控制**: 支持开始、重置排序过程
- **数据输入**: 支持随机数据生成、手动输入或文件导入
- **教学辅助**: 显示算法伪代码和详细说明
- **响应式设计**: 支持不同屏幕尺寸的适配

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
启动前后端后，在浏览器中访问 `http://localhost:8080` 即可使用完整应用。

## 项目结构

```
SortingAlgorithmVisualizationSystem/
├── backend/                          # 后端源码目录
│   └── src/main/
│       ├── java/com/sorting/visualization/
│       │   ├── algorithm/            # 排序算法实现
│       │   ├── config/               # 配置类
│       │   ├── controller/           # REST 控制器
│       │   ├── model/                # 数据模型
│       │   ├── service/              # 业务逻辑服务
│       │   ├── util/                 # 工具类
│       │   └── websocket/            # WebSocket 处理器
│       └── resources/                # 资源文件
│           ├── application.properties # 应用配置
│           └── static/               # 前端静态文件
├── frontend-vue/                     # 前端 Vue.js 项目
│   ├── src/
│   │   ├── components/               # Vue 组件
│   │   │   ├── ControlPanel/         # 控制面板组件
│   │   │   ├── InfoPanel/            # 信息面板组件
│   │   │   └── Visualization/        # 可视化组件
│   │   ├── composables/              # Vue 组合式函数
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── utils/                    # 前端工具函数
│   │   └── views/                    # 页面视图
│   ├── public/                       # 公共静态资源
│   └── package.json                  # 前端依赖配置
├── pom.xml                           # Maven 配置
└── README.md                         # 项目说明
```

## 开发指南

### 添加新排序算法
1. 在 `backend/src/main/java/com/sorting/visualization/algorithm/impl/` 中实现算法类
2. 实现 `SortingAlgorithm` 接口
3. 在前端的算法选择器中添加新选项

### WebSocket 通信
系统使用 WebSocket 进行实时数据传输，主要消息类型包括：
- 排序步骤更新
- 性能指标更新
- 控制命令响应

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
