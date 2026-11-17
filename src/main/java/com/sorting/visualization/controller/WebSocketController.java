package com.sorting.visualization.controller;

import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.response.ErrorResponse;
import com.sorting.visualization.security.JwtUtil;
import com.sorting.visualization.util.JsonUtil;
import com.sorting.visualization.websocket.MessageHandler;
import com.sorting.visualization.websocket.SessionState;
import com.sorting.visualization.websocket.WebSocketSessionManager;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/websocket")
@Slf4j
public class WebSocketController {

    // 使用静态变量，因为每个WebSocket连接都会创建新的Controller实例
    private static WebSocketSessionManager sessionManager;
    private static MessageHandler messageHandler;
    private static JwtUtil jwtUtil;

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

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        WebSocketController.jwtUtil = jwtUtil;
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

        // 解析 token 参数，提取 userId（兼容 JWT 与旧的 token-{userId} 占位串）
        try {
            String token = extractToken(session);
            String userId = resolveUserId(token, sessionId);
            SessionState st = sessionManager.getSessionState(sessionId);
            if (st != null) st.setUserId(userId);
            log.info("WebSocket 认证信息: sessionId={}, userId={}", sessionId, userId);
        } catch (Exception e) {
            log.warn("解析 token 失败: sessionId={}", sessionId, e);
            SessionState st = sessionManager.getSessionState(sessionId);
            if (st != null) st.setUserId(null);
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
                sendErrorMessage(session, ErrorCode.INTERNAL_ERROR, "服务器内部错误", null);
            }
        } catch (Exception e) {
            // 兜底：不把异常信息回传给客户端，避免泄露内部实现细节
            log.error("处理WebSocket消息失败: sessionId={}", sessionId, e);
            sendErrorMessage(session, ErrorCode.INTERNAL_ERROR,
                    ErrorCode.INTERNAL_ERROR.getDefaultMessage(), null);
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

        log.error("WebSocket连接错误: sessionId={}", sessionId, error);

        // 发送错误消息
        if (session != null && session.isOpen()) {
            sendErrorMessage(session, ErrorCode.WEBSOCKET_ERROR,
                    ErrorCode.WEBSOCKET_ERROR.getDefaultMessage(), null);
        }
    }

    /** 从握手 URL 的 query 中取 token（JWT 只含 base64url 字符，无需解码） */
    private static String extractToken(Session session) {
        URI uri = session.getRequestURI();
        String query = uri == null ? null : uri.getQuery();
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            if (pair.startsWith("token=")) {
                String token = pair.substring("token=".length()).trim();
                return token.isEmpty() ? null : token;
            }
        }
        return null;
    }

    /**
     * 兼容两种令牌：
     * 1. W1 起前端传的是 JWT，按签名解析出 userId（subject）；
     * 2. 旧格式 {@code token-{userId}}，直接截取。
     *
     * <p>解析失败返回 null：连接仍允许建立（未登录浏览），但实验结果不会入库。</p>
     */
    static String resolveUserId(String token, String sessionId) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith("token-")) {
            return token.length() > 6 ? token.substring(6) : null;
        }
        if (jwtUtil == null || token.indexOf('.') < 0) {
            return null;
        }
        try {
            return jwtUtil.parse(token).getSubject();
        } catch (Exception e) {
            log.warn("WebSocket 令牌无效: sessionId={}", sessionId);
            return null;
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Session session, ErrorCode code, String message, String requestId) {
        try {
            ErrorResponse error = ErrorResponse.of(code, message, requestId);
            session.getBasicRemote().sendText(JsonUtil.toJson(error));
        } catch (IOException e) {
            log.error("发送错误消息失败: sessionId={}", session.getId(), e);
        }
    }
}
