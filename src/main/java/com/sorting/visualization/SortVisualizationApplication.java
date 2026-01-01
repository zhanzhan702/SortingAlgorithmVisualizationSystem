package com.sorting.visualization;

import com.sorting.visualization.websocket.WebSocketSessionManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class SortVisualizationApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        log.info("正在启动排序算法可视化系统后端...");

        try {
            context = SpringApplication.run(SortVisualizationApplication.class, args);
            log.info("排序算法可视化系统后端启动成功!");
            log.info("服务器地址: http://localhost:8080");
            log.info("WebSocket端点: ws://localhost:8080/websocket");
            log.info("健康检查: http://localhost:8080/health");
            log.info("系统信息: http://localhost:8080/info");
            log.info("算法列表: http://localhost:8080/api/algorithms");
        } catch (Exception e) {
            log.error("启动排序算法可视化系统后端失败", e);
            System.exit(1);
        }
    }

    @PreDestroy
    public void onShutdown() {
        log.info("正在关闭排序算法可视化系统后端...");

        // 关闭WebSocket会话
        if (context != null) {
            WebSocketSessionManager sessionManager = context.getBean(WebSocketSessionManager.class);
            if (sessionManager != null) {
                sessionManager.closeAllSessions();
            }
        }

        log.info("排序算法可视化系统后端已关闭");
    }
}