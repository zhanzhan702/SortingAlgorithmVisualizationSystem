package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sorting.visualization.entity.User;
import com.sorting.visualization.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * W5：账号相关业务逻辑（登出 / 修改密码）。
 *
 * <p>独立成新文件，避免修改原有 UserService。
 * 当前系统采用无状态 token（token-{userId}），登出仅需前端清除本地 token，
 * 后端登出接口仅返回成功确认。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserMapper userMapper;

    /** 登出（无状态 JWT，后端仅返回成功，前端清除本地 token） */
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已退出登录");
        return result;
    }

    /** 修改密码：校验旧密码后更新 password_hash */
    public Map<String, Object> changePassword(String userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        if (userId == null || userId.isBlank()) {
            result.put("success", false);
            result.put("message", "用户标识不能为空");
            return result;
        }
        if (oldPassword == null || oldPassword.isBlank()) {
            result.put("success", false);
            result.put("message", "旧密码不能为空");
            return result;
        }
        if (newPassword == null || newPassword.isBlank()) {
            result.put("success", false);
            result.put("message", "新密码不能为空");
            return result;
        }
        if (newPassword.length() < 6) {
            result.put("success", false);
            result.put("message", "新密码长度不能少于 6 位");
            return result;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        if (!user.getPasswordHash().equals(oldPassword)) {
            result.put("success", false);
            result.put("message", "旧密码错误");
            return result;
        }

        // 只更新 password_hash，避免把实体中与表结构不一致的字段一并写入
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, userId)
                .set(User::getPasswordHash, newPassword));
        log.info("用户 {} 修改密码成功", user.getUsername());
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }
}
