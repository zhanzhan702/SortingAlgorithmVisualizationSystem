package com.sorting.visualization.controller;

import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * W5：账号相关接口（登出 / 修改密码）。
 *
 * <p>独立成新文件，映射到 /api/account，避免修改原有 AuthController。</p>
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** 登出 */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        return accountService.logout();
    }

    /**
     * 修改密码。操作对象取自 JWT 中的 userId（由 AuthInterceptor 写入请求属性），
     * 不接受请求体传入的 userId，避免拿着自己的令牌改他人密码。
     */
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(HttpServletRequest request,
                                              @RequestBody Map<String, String> body) {
        Object userId = request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return accountService.changePassword(
                userId == null ? null : userId.toString(),
                body.get("oldPassword"),
                body.get("newPassword"));
    }
}
