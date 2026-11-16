package com.sorting.visualization.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/** W1：管理员角色校验 */
class RoleInterceptorTest {

    private RoleInterceptor interceptor;
    private final Object handler = new Object();

    @BeforeEach
    void setUp() {
        interceptor = new RoleInterceptor();
    }

    private MockHttpServletRequest request(String method, String uri, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        if (role != null) {
            request.setAttribute(AuthInterceptor.ATTR_ROLE, role);
        }
        return request;
    }

    @Test
    @DisplayName("学生访问管理端：403")
    void studentForbidden() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean passed = interceptor.preHandle(request("GET", "/api/admin/stats", "student"), response, handler);

        assertThat(passed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains(AuthErrors.CODE_FORBIDDEN);
    }

    @Test
    @DisplayName("教师可看统计，但不能清空数据")
    void teacherCanReadButNotCleanup() throws Exception {
        assertThat(interceptor.preHandle(request("GET", "/api/admin/stats", "teacher"), new MockHttpServletResponse(), handler))
                .isTrue();
        assertThat(interceptor.preHandle(request("GET", "/api/admin/ranking", "teacher"), new MockHttpServletResponse(), handler))
                .isTrue();
        assertThat(interceptor.preHandle(request("POST", "/api/admin/backup", "teacher"), new MockHttpServletResponse(), handler))
                .isFalse();

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request("POST", "/api/admin/cleanup", "teacher"), response, handler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("仅管理员");
    }

    @Test
    @DisplayName("管理员可读写全部管理接口")
    void adminAllowed() throws Exception {
        for (String[] call : new String[][]{
                {"GET", "/api/admin/stats"},
                {"GET", "/api/admin/activity"},
                {"POST", "/api/admin/backup"},
                {"POST", "/api/admin/cleanup"}}) {
            assertThat(interceptor.preHandle(request(call[0], call[1], "admin"), new MockHttpServletResponse(), handler))
                    .as("%s %s", call[0], call[1])
                    .isTrue();
        }
    }

    @Test
    @DisplayName("结尾斜杠不能绕过管理员白名单")
    void trailingSlashCannotBypass() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request("POST", "/api/admin/cleanup/", "teacher"), response, handler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("没有角色信息（未经鉴权）按 403 处理，不默认放行")
    void missingRoleForbidden() throws Exception {
        assertThat(interceptor.preHandle(request("GET", "/api/admin/stats", null), new MockHttpServletResponse(), handler))
                .isFalse();
    }

    @Test
    @DisplayName("CORS 预检请求直接放行")
    void optionsPasses() throws Exception {
        assertThat(interceptor.preHandle(request("OPTIONS", "/api/admin/cleanup", null), new MockHttpServletResponse(), handler))
                .isTrue();
    }

    @Test
    @DisplayName("路径归一化：去掉 contextPath 与结尾斜杠")
    void normalizePath() {
        assertThat(RoleInterceptor.normalize("/app/api/admin/cleanup", "/app")).isEqualTo("/api/admin/cleanup");
        assertThat(RoleInterceptor.normalize("/api/admin/cleanup/", "")).isEqualTo("/api/admin/cleanup");
        assertThat(RoleInterceptor.normalize("/api/admin/stats", "/")).isEqualTo("/api/admin/stats");
    }
}
