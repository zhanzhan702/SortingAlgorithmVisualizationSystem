package com.sorting.visualization.controller;

import com.sorting.visualization.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * WebSocket 握手令牌解析：W1 起前端传的是 JWT，原来的 {@code token-{userId}}
 * 解析逻辑会让 userId 恒为 null，导致实验/性能结果永远不入库。
 */
class WebSocketControllerAuthTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("sortviz-2026-secret-key-for-database-course-design", 3_600_000L);
        new WebSocketController().setJwtUtil(jwtUtil);
    }

    @Test
    @DisplayName("JWT 令牌解析出 userId")
    void jwtTokenResolvesUserId() {
        String token = jwtUtil.generateToken("0123456789abcdef0123456789abcdef", "student", "student");
        assertEquals("0123456789abcdef0123456789abcdef",
                WebSocketController.resolveUserId(token, "session-1"));
    }

    @Test
    @DisplayName("兼容旧的 token-{userId} 占位串")
    void legacyTokenStillWorks() {
        assertEquals("abc123", WebSocketController.resolveUserId("token-abc123", "session-2"));
    }

    @Test
    @DisplayName("无令牌时 userId 为 null（允许未登录浏览，但不落库）")
    void blankTokenReturnsNull() {
        assertNull(WebSocketController.resolveUserId(null, "session-3"));
        assertNull(WebSocketController.resolveUserId("   ", "session-3"));
        assertNull(WebSocketController.resolveUserId("token-", "session-3"));
    }

    @Test
    @DisplayName("伪造令牌解析失败返回 null，而不是抛异常")
    void invalidTokenReturnsNull() {
        assertNull(WebSocketController.resolveUserId("eyJhbGciOiJIUzI1NiJ9.fake.signature", "session-4"));
        assertNull(WebSocketController.resolveUserId("not-a-token", "session-4"));
    }
}
