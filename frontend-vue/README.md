# 排序算法可视化系统 — 前端

基于 **Vue 3 + Vite + Pinia + Chart.js** 的排序算法可视化前端。

## 技术栈

| 技术 | 用途 |
|------|------|
| Vue 3.4 | 组件化框架（Composition API + `<script setup>`） |
| Vite 5.4 | 构建工具 |
| Pinia 2.1.7 | 状态管理（algorithm/data/comparator/ui/performance/auth/history） |
| Vue Router 4 | Hash 路由（含角色守卫） |
| Chart.js 4.4 | 性能对比柱状图 |
| WebSocket | 实时排序动画推送 |

## 项目结构

```
src/
├── components/
│   ├── Common/           # LoadingOverlay, Modal
│   ├── ControlPanel/     # AlgorithmSelector, DataInput, SortOptions, StatsPanel, ConnectionStatus, ModeSelector
│   ├── InfoPanel/        # AlgorithmInfo, PseudoCode, SystemLog
│   └── Visualization/    # BarChart (SVG), PerformanceView (Chart.js)
├── composables/
│   └── useWebSocket.js   # WebSocket 连接/消息处理
├── stores/               # Pinia 状态管理
│   ├── algorithm.js      # 算法选择 + 伪代码
│   ├── data.js           # 数据集
│   ├── comparator.js     # 比较器配置
│   ├── ui.js             # UI 状态（模式/暂停/弹窗）
│   ├── performance.js    # 性能结果
│   ├── auth.js           # 认证（含 isAdmin/isTeacher）
│   └── history.js        # 实验历史
├── utils/                # 数据生成器、文件解析、工具函数
├── views/                # HomeView, HistoryView, AdminView, LoginView
└── router/               # 路由 + 角色权限守卫
```

## 角色权限

| 页面 | Student | Teacher | Admin |
|------|:---:|:---:|:---:|
| 首页（可视化） | ✅ | ✅ | ✅ |
| 实验历史 | ✅（仅自己） | ✅（含"全部实验"切换） | ✅ |
| 管理后台 | ❌ | ✅（4 Tab，无备份） | ✅（5 Tab 全部） |

## 开发

```bash
npm install
npm run dev        # 开发模式（localhost:5173，热更新）
npm run build      # 生产构建 → ../src/main/resources/static/
```

## 部署

构建产物直接输出到 Spring Boot 的 `src/main/resources/static/`，与后端一起通过 `mvn spring-boot:run` 在 `http://localhost:8080` 统一访问。
