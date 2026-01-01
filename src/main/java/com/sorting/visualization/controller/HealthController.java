package com.sorting.visualization.controller;

import com.sorting.visualization.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @Autowired
    private WebSocketSessionManager sessionManager;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "sorting-visualization-backend");
        result.put("timestamp", System.currentTimeMillis());

        // 添加会话统计
        result.put("websocket", sessionManager.getSessionStats());

        log.debug("健康检查通过");
        return result;
    }

    /**
     * 系统信息接口
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();

        // 系统信息
        result.put("name", "排序算法可视化系统后端");
        result.put("version", "1.0.0");
        result.put("description", "提供排序算法可视化WebSocket服务");
        result.put("author", "Sorting Visualization Team");

        // 技术栈
        Map<String, String> techStack = new HashMap<>();
        techStack.put("framework", "Spring Boot 3.1.5");
        techStack.put("java", "17");
        techStack.put("websocket", "javax.websocket");
        techStack.put("build", "Maven");
        result.put("techStack", techStack);

        // 支持的算法
        result.put("supportedAlgorithms", new String[]{
                "BUBBLE", "INSERTION", "SHELL", "QUICK", "HEAP", "MERGE"
        });

        // 支持的数据类型
        result.put("supportedDataTypes", new String[]{
                "INTEGER", "DOUBLE", "PERSON"
        });

        log.info("系统信息查询");
        return result;
    }

    /**
     * 算法信息接口
     */
    @GetMapping("/api/algorithms")
    public Map<String, Object> getAlgorithms() {
        Map<String, Object> result = new HashMap<>();

        // 算法列表
        String[] algorithms = {"BUBBLE", "INSERTION", "SHELL", "QUICK", "HEAP", "MERGE"};
        result.put("algorithms", algorithms);

        // 详细算法信息
        Map<String, Object> algorithmDetails = new HashMap<>();
        for (String algo : algorithms) {
            algorithmDetails.put(algo, com.sorting.visualization.util.PseudoCodeUtil.getAlgorithmInfo(algo));
        }
        result.put("algorithmDetails", algorithmDetails);

        log.info("获取算法列表");
        return result;
    }
}