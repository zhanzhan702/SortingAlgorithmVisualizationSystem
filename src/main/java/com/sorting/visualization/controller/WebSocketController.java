// controller/WebSocketController.java
package com.sorting.visualization.controller;

import com.sorting.visualization.model.response.ErrorResponse;
import com.sorting.visualization.util.JsonUtil;
import com.sorting.visualization.websocket.MessageHandler;
import com.sorting.visualization.websocket.WebSocketSessionManager;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/websocket")
@Slf4j
public class WebSocketController {

    // 使用静态变量，因为每个WebSocket连接都会创建新的Controller实例
    private static WebSocketSessionManager sessionManager;
    private static MessageHandler messageHandler;

    /**
     * 使用setter方法注入，因为@ServerEndpoint不能使用构造函数注入
     */
    @Autowired
    public void setWebSocketSessionManager(WebSocketSessionManager sessionManager) {
        WebSocketController.sessionManager = sessionManager;
    }

    @Autowired
    public void setMessageHandler(MessageHandler messageHandler) {
        WebSocketController.messageHandler = messageHandler;
    }

    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(Session session) {
        String sessionId = session.getId();

        // 初始化会话管理器
        if (sessionManager != null) {
            sessionManager.addSession(sessionId, session);
        }

        // 发送连接成功消息
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "CONNECTED");
            response.put("message", "WebSocket连接成功");
            response.put("sessionId", sessionId);
            response.put("timestamp", System.currentTimeMillis());

            session.getBasicRemote().sendText(JsonUtil.toJson(response));

            log.info("WebSocket连接建立: sessionId={}, remoteAddress={}",
                    sessionId, session.getRequestURI());
        } catch (IOException e) {
            log.error("发送连接成功消息失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = session.getId();

        log.debug("收到WebSocket消息: sessionId={}, message={}", sessionId, message);

        try {
            // 将消息交给消息处理器处理
            if (messageHandler != null) {
                messageHandler.handleMessage(sessionId, session, message);
            } else {
                log.error("消息处理器未初始化");
                sendErrorMessage(session, "INTERNAL_ERROR", "服务器内部错误", null);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}, message={}, error={}",
                    sessionId, message, e.getMessage(), e);
            sendErrorMessage(session, "INTERNAL_ERROR", "处理消息失败: " + e.getMessage(), null);
        }
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();

        // 从会话管理器移除
        if (sessionManager != null) {
            sessionManager.removeSession(sessionId);
        }

        log.info("WebSocket连接关闭: sessionId={}", sessionId);
    }

    /**
     * 连接错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session != null ? session.getId() : "unknown";

        log.error("WebSocket连接错误: sessionId={}, error={}", sessionId, error.getMessage(), error);

        // 发送错误消息
        if (session != null && session.isOpen()) {
            sendErrorMessage(session, "WEBSOCKET_ERROR", "WebSocket连接错误: " + error.getMessage(), null);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Session session, String code, String message, String requestId) {
        try {
            ErrorResponse error = new ErrorResponse();
            error.setRequestId(requestId);
            error.setMessage(message);
            error.setCode(code);
            error.setTimestamp(System.currentTimeMillis());

            session.getBasicRemote().sendText(JsonUtil.toJson(error));
        } catch (IOException e) {
            log.error("发送错误消息失败: sessionId={}", session.getId(), e);
        }
    }
}