package com.sorting.visualization.service;

import com.sorting.visualization.entity.User;
import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.mapper.UserMapper;
import com.sorting.visualization.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 用户注册/登录的失败语义（W2/W3）：业务失败必须抛 ApiException，由全局处理器映射为状态码。
 */
class UserServiceTest {

    private UserMapper userMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = new JwtUtil("sortviz-2026-secret-key-for-database-course-design", 3_600_000L);
        userService = new UserService(userMapper, jwtUtil);
    }

    @Test
    @DisplayName("用户名已存在返回 409")
    void duplicateUsername() {
        when(userMapper.selectOne(any())).thenReturn(new User());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.register("student01", "123456", "student"));
        assertEquals(ErrorCode.USERNAME_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("登录失败返回 401")
    void loginFailure() {
        when(userMapper.selectOne(any())).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.login("student01", "wrong"));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("注册成功返回 success=true")
    void registerSuccess() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId("0123456789abcdef0123456789abcdef");
            return 1;
        });

        Map<String, Object> result = userService.register("student02", "123456", null);
        assertEquals(true, result.get("success"));
        assertEquals("0123456789abcdef0123456789abcdef", result.get("userId"));
    }

    @Test
    @DisplayName("登录成功签发三段式 JWT")
    void loginSuccessIssuesJwt() {
        User user = new User();
        user.setUserId("0123456789abcdef0123456789abcdef");
        user.setUsername("student01");
        user.setPasswordHash("123456");
        user.setRole("student");
        when(userMapper.selectOne(any())).thenReturn(user);

        Map<String, Object> result = userService.login("student01", "123456");
        String token = (String) result.get("token");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertEquals("student", result.get("role"));
    }

    @Test
    @DisplayName("注册角色只认 teacher，其余降级为 student")
    void registerRoleWhitelist() {
        assertEquals("teacher", UserService.normalizeRegisterRole("teacher"));
        assertEquals("student", UserService.normalizeRegisterRole("admin"));
        assertEquals("student", UserService.normalizeRegisterRole(null));
        assertTrue(UserService.normalizeRegisterRole("STUDENT").equals("student"));
    }
}
