package com.sorting.visualization.config;

import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.security.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册鉴权与角色拦截器。
 *
 * <p>策略是「默认全部受保护、显式白名单」：{@code /api/**} 一律要求令牌，
 * 只有登录、注册与算法列表按公开接口放行。新增控制器不需要再改这里，天然受保护。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /** 公开接口白名单 */
    static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/algorithms"
    };

    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS)
                .order(1);

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(2);
    }
}
