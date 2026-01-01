package com.sorting.visualization.websocket;

import com.sorting.visualization.util.JsonUtil;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 添加会话
     */
    public void addSession(String sessionId, Session session) {
        SessionState state = new SessionState(sessionId, session);
        sessionStates.put(sessionId, state);
        log.info("添加WebSocket会话: sessionId={}, 当前会话数: {}", sessionId, sessionStates.size());
    }

    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        if (state != null) {
            state.clear();
            sessionStates.remove(sessionId);
            log.info("移除WebSocket会话: sessionId={}, 剩余会话数: {}", sessionId, sessionStates.size());
        }
    }

    /**
     * 获取会话状态
     */
    public SessionState getSessionState(String sessionId) {
        return sessionStates.get(sessionId);
    }

    /**
     * 发送消息到会话
     */
    public boolean sendMessage(String sessionId, Object message) {
        SessionState state = sessionStates.get(sessionId);
        if (state == null || state.getSession() == null || !state.getSession().isOpen()) {
            log.warn("会话不存在或已关闭: sessionId={}", sessionId);
            return false;
        }

        try {
            String jsonMessage = JsonUtil.toJson(message);
            state.getSession().getBasicRemote().sendText(jsonMessage);
            log.debug("发送消息到会话: sessionId={}, messageType={}",
                    sessionId, message.getClass().getSimpleName());
            return true;
        } catch (IOException e) {
            log.error("发送消息失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 开始处理请求
     */
    public void startProcessing(String sessionId, String requestId, String algorithm, String mode) {
        SessionState state = sessionStates.get(sessionId);
        if (state != null) {
            state.startProcessing(requestId, algorithm, mode);
        }
    }

    /**
     * 暂停处理
     */
    public void pauseProcessing(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        if (state != null) {
            state.pauseProcessing();
        }
    }

    /**
     * 恢复处理
     */
    public void resumeProcessing(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        if (state != null) {
            state.resumeProcessing();
        }
    }

    /**
     * 停止处理
     */
    public void stopProcessing(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        if (state != null) {
            state.stopProcessing();
        }
    }

    /**
     * 检查会话是否正在处理
     */
    public boolean isProcessing(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        return state != null && state.isProcessing();
    }

    /**
     * 检查会话是否已暂停
     */
    public boolean isPaused(String sessionId) {
        SessionState state = sessionStates.get(sessionId);
        return state != null && state.isPaused();
    }

    /**
     * 获取执行器服务
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 关闭所有会话
     */
    public void closeAllSessions() {
        log.info("开始关闭所有WebSocket会话, 数量: {}", sessionStates.size());

        for (Map.Entry<String, SessionState> entry : sessionStates.entrySet()) {
            try {
                entry.getValue().clear();
                if (entry.getValue().getSession() != null && entry.getValue().getSession().isOpen()) {
                    entry.getValue().getSession().close();
                }
            } catch (Exception e) {
                log.error("关闭会话失败: sessionId={}", entry.getKey(), e);
            }
        }

        sessionStates.clear();
        executorService.shutdown();
        log.info("所有WebSocket会话已关闭");
    }

    /**
     * 获取会话统计
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalSessions", sessionStates.size());

        int processingCount = 0;
        int pausedCount = 0;

        for (SessionState state : sessionStates.values()) {
            if (state.isProcessing()) {
                processingCount++;
                if (state.isPaused()) {
                    pausedCount++;
                }
            }
        }

        stats.put("processingSessions", processingCount);
        stats.put("pausedSessions", pausedCount);

        return stats;
    }
}