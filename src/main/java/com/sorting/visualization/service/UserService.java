package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.entity.User;
import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.mapper.UserMapper;
import com.sorting.visualization.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /** 注册（W3：空值校验 + 失败语义化，不再返回 200 + success:false） */
    public Map<String, Object> register(String username, String password, String role) {
        String name = username == null ? null : username.trim();
        if (name == null || name.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "密码不能为空");
        }

        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, name)) != null) {
            throw new ApiException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(name);
        user.setPasswordHash(password);
        // 注册入口只允许 student / teacher，避免客户端自己填 admin 提权
        user.setRole(normalizeRegisterRole(role));

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册时唯一索引兜底：先查后插之间存在竞态
            throw new ApiException(ErrorCode.USERNAME_EXISTS, "用户名已存在", null, e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getUserId());
        return result;
    }

    /** 注册时的角色白名单：只认 teacher，其余一律 student */
    static String normalizeRegisterRole(String role) {
        return "teacher".equalsIgnoreCase(role == null ? "" : role.trim()) ? "teacher" : "student";
    }

    /** 登录：校验通过后签发 JWT（HS256）；凭证错误返回 401 */
    public Map<String, Object> login(String username, String password) {
        String name = username == null ? null : username.trim();
        User user = name == null ? null
                : userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, name));

        if (user == null || user.getPasswordHash() == null
                || password == null || !user.getPasswordHash().equals(password)) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        String role = user.getRole() == null ? "student" : user.getRole();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("token", jwtUtil.generateToken(user.getUserId(), user.getUsername(), role));
        result.put("expiresIn", jwtUtil.getExpirationMillis() / 1000);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("role", role);
        return result;
    }
}
