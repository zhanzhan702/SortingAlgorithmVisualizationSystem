package com.sorting.visualization.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/** W1：鉴权拦截器 */
class AuthInterceptorTest {

    private static final String SECRET = "sortviz-2026-secret-key-for-database-course-design";

    private JwtUtil jwtUtil;
    private AuthInterceptor interceptor;
    private final Object handler = new Object();

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3600_000L);
        interceptor = new AuthInterceptor(jwtUtil);
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        return request;
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    @Test
    @DisplayName("不带令牌：401 且返回 JSON 错误体")
    void missingTokenReturns401() throws Exception {
        MockHttpServletResponse response = response();

        boolean passed = interceptor.preHandle(request("GET", "/api/admin/stats"), response, handler);

        assertThat(passed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getContentAsString()).contains(AuthErrors.CODE_UNAUTHORIZED);
    }

    @Test
    @DisplayName("令牌有效：放行并把用户信息放入 attribute")
    void validTokenPasses() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/admin/stats");
        request.addHeader("Authorization", "Bearer " + jwtUtil.generateToken("u-9", "admin", "admin"));
        MockHttpServletResponse response = response();

        boolean passed = interceptor.preHandle(request, response, handler);

        assertThat(passed).isTrue();
        assertThat(request.getAttribute(AuthInterceptor.ATTR_USER_ID)).isEqualTo("u-9");
        assertThat(request.getAttribute(AuthInterceptor.ATTR_USERNAME)).isEqualTo("admin");
        assertThat(request.getAttribute(AuthInterceptor.ATTR_ROLE)).isEqualTo("admin");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("省略 Bearer 前缀也能识别")
    void tokenWithoutBearerPrefixAccepted() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/history/experiments");
        request.addHeader("Authorization", jwtUtil.generateToken("u-1", "student", "student"));

        assertThat(interceptor.preHandle(request, response(), handler)).isTrue();
        assertThat(request.getAttribute(AuthInterceptor.ATTR_USER_ID)).isEqualTo("u-1");
    }

    @Test
    @DisplayName("乱码令牌：401 TOKEN_INVALID")
    void malformedTokenReturns401() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/history/experiments");
        request.addHeader("Authorization", "Bearer not-a-jwt");
        MockHttpServletResponse response = response();

        assertThat(interceptor.preHandle(request, response, handler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(AuthErrors.CODE_TOKEN_INVALID);
    }

    @Test
    @DisplayName("过期令牌：401 TOKEN_EXPIRED")
    void expiredTokenReturns401() throws Exception {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        MockHttpServletRequest request = request("GET", "/api/history/experiments");
        request.addHeader("Authorization", "Bearer " + shortLived.generateToken("u-1", "student", "student"));
        MockHttpServletResponse response = response();

        assertThat(interceptor.preHandle(request, response, handler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(AuthErrors.CODE_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("CORS 预检请求直接放行")
    void optionsRequestPasses() throws Exception {
        assertThat(interceptor.preHandle(request("OPTIONS", "/api/admin/stats"), response(), handler)).isTrue();
    }

    @Test
    @DisplayName("空白 Authorization 头按未登录处理")
    void blankHeaderTreatedAsMissing() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/admin/stats");
        request.addHeader("Authorization", "   ");
        MockHttpServletResponse response = response();

        assertThat(interceptor.preHandle(request, response, handler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
