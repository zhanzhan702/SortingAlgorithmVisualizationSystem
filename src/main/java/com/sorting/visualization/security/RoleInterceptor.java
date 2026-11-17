package com.sorting.visualization.security;

import com.sorting.visualization.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 角色校验拦截器：只挂在 {@code /api/admin/**} 上，必须排在 {@link AuthInterceptor} 之后。
 *
 * <p>规则分两级：</p>
 * <ul>
 *   <li>写操作（清空数据、触发备份）只有 {@code admin} 可以执行</li>
 *   <li>其余管理端查询，{@code admin} 与 {@code teacher} 都可以查看</li>
 * </ul>
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";

    /** 仅管理员可执行的写接口 */
    static final Set<String> ADMIN_ONLY_PATHS = Set.of(
            "/api/admin/cleanup",
            "/api/admin/backup");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        Object roleAttr = request.getAttribute(AuthInterceptor.ATTR_ROLE);
        String role = roleAttr == null ? null : roleAttr.toString();
        String path = normalize(request.getRequestURI(), request.getContextPath());

        if (ADMIN_ONLY_PATHS.contains(path)) {
            if (!ROLE_ADMIN.equals(role)) {
                AuthErrors.write(request, response, ErrorCode.FORBIDDEN, "该操作仅管理员可执行");
                return false;
            }
            return true;
        }

        if (!ROLE_ADMIN.equals(role) && !ROLE_TEACHER.equals(role)) {
            AuthErrors.write(request, response, ErrorCode.FORBIDDEN, "需要教师或管理员权限");
            return false;
        }
        return true;
    }

    /** 去掉 contextPath 与结尾斜杠，避免 {@code /api/admin/cleanup/} 绕过白名单 */
    static String normalize(String requestUri, String contextPath) {
        String path = requestUri == null ? "" : requestUri;
        boolean hasContextPath = contextPath != null && !contextPath.isEmpty() && !"/".equals(contextPath);
        if (hasContextPath && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
