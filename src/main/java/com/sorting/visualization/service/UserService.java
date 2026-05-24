package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.entity.User;
import com.sorting.visualization.mapper.UserMapper;
import com.sorting.visualization.util.JwtUtil;
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

    /** 注册 */
    public Map<String, Object> register(String username, String password, String role) {
        Map<String, Object> result = new HashMap<>();
        // 检查用户名是否存在
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password); // 实际应 BCrypt 加密，此处简化
        user.setRole(role != null ? role : "student");
        userMapper.insert(user);
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getUserId());
        return result;
    }

    /** 登录 */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !user.getPasswordHash().equals(password)) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }
}
