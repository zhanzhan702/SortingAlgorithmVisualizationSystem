package com.sorting.visualization.controller;

import com.sorting.visualization.model.request.LoginRequest;
import com.sorting.visualization.model.request.RegisterRequest;
import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest body) {
        return userService.register(body.getUsername(), body.getPassword(), body.getRole());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body) {
        return userService.login(body.getUsername(), body.getPassword());
    }

    /**
     * 当前登录者信息：直接读拦截器解析令牌后写入的 attribute，
     * 客户端传什么都不作数（这也是 W1 的验收点之一）。
     */
    @GetMapping("/me")
    public Map<String, Object> me(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String userId,
            @RequestAttribute(value = AuthInterceptor.ATTR_USERNAME, required = false) String username,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("username", username == null ? "" : username);
        result.put("role", role == null ? "" : role);
        return result;
    }
}
