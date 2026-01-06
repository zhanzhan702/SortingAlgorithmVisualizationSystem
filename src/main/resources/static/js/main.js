// 主应用程序入口
document.addEventListener('DOMContentLoaded', function () {
    // 初始化所有模块
    Utils.logMessage('应用程序启动中...', 'info');

    // 初始化可视化模块
    Visualizer.init();

    // 初始化控制器
    Controller.init();

    // 尝试连接服务器
    WebSocketManager.init("ws://localhost:8080/websocket");

    // 初始化图表
    ChartRenderer.init();

    // 初始化比较器管理器
    ComparisonManager.init();

    // 初始化伪代码
    Controller.updatePseudoCode();

    // 显示初始比较器信息
    Utils.logMessage(`当前比较器: ${ComparisonManager.getComparatorDescription()}`, 'info');

    // 生成初始数据
    setTimeout(() => {
        Controller.handleGenerateData();
    }, 500);

    Utils.logMessage('应用程序初始化完成', 'success');
    Utils.logMessage('请连接服务器并选择算法开始排序', 'info');

    document.getElementById('teaching-visualization').style.display = 'block';
    document.getElementById('performance-visualization').style.display = 'none';
});