package com.sorting.visualization.security;

import com.sorting.visualization.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器：校验 {@code Authorization: Bearer <token>}。
 *
 * <p>校验通过后把用户信息放进 request attribute，后续拦截器与控制器直接取用，
 * 不再信任客户端传来的 {@code userId} 参数。</p>
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "auth.userId";
    public static final String ATTR_USERNAME = "auth.username";
    public static final String ATTR_ROLE = "auth.role";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // CORS 预检不带令牌，直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            AuthErrors.write(request, response, ErrorCode.UNAUTHORIZED, "未登录或缺少访问令牌");
            return false;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            request.setAttribute(ATTR_USER_ID, claims.getSubject());
            request.setAttribute(ATTR_USERNAME, claims.get(JwtUtil.CLAIM_USERNAME, String.class));
            request.setAttribute(ATTR_ROLE, claims.get(JwtUtil.CLAIM_ROLE, String.class));
            return true;
        } catch (ExpiredJwtException e) {
            AuthErrors.write(request, response, ErrorCode.TOKEN_EXPIRED, "登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            AuthErrors.write(request, response, ErrorCode.TOKEN_INVALID, "访问令牌无效");
        }
        return false;
    }

    /** 从 Authorization 头取令牌，兼容不带 Bearer 前缀的写法 */
    static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return null;
        }
        header = header.trim();
        if (header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            header = header.substring(BEARER_PREFIX.length()).trim();
        }
        return header.isEmpty() ? null : header;
    }
}
