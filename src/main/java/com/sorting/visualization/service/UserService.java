package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.entity.User;
import com.sorting.visualization.mapper.UserMapper;
import com.sorting.visualization.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /** 注册 */
    public Map<String, Object> register(String username, String password, String role) {
        Map<String, Object> result = new HashMap<>();
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password);
        // 注册入口只允许 student / teacher，避免客户端自己填 admin 提权
        user.setRole(normalizeRegisterRole(role));
        userMapper.insert(user);
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getUserId());
        return result;
    }

    /** 注册时的角色白名单：只认 teacher，其余一律 student */
    static String normalizeRegisterRole(String role) {
        return "teacher".equalsIgnoreCase(role == null ? "" : role.trim()) ? "teacher" : "student";
    }

    /** 登录：校验通过后签发 JWT（HS256），取代原先的 {@code token-<userId>} 占位串 */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !user.getPasswordHash().equals(password)) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        String role = user.getRole() == null ? "student" : user.getRole();
        result.put("success", true);
        result.put("token", jwtUtil.generateToken(user.getUserId(), user.getUsername(), role));
        result.put("expiresIn", jwtUtil.getExpirationMillis() / 1000);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("role", role);
        return result;
    }
}