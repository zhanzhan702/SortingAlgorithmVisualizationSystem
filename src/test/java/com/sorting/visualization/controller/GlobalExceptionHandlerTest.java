package com.sorting.visualization.controller;

import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.model.response.ApiError;
import com.sorting.visualization.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全局异常处理（W2）契约测试：不启动 Spring 容器，避免依赖数据库。
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("缺少必填字段返回 400 + 字段明细")
    void missingFieldsReturnBadRequestWithDetails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("密码过短返回 400")
    void shortPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student01\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("password"));
    }

    @Test
    @DisplayName("注册角色越权返回 400，而不是注册出管理员")
    void adminRoleIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"hacker\",\"password\":\"123456\",\"role\":\"admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("用户名重复返回 409 USERNAME_EXISTS")
    void duplicateUsernameReturnsConflict() throws Exception {
        when(userService.register(any(), any(), any()))
                .thenThrow(new ApiException(ErrorCode.USERNAME_EXISTS));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student01\",\"password\":\"123456\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_EXISTS"))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("登录凭证错误返回 401 INVALID_CREDENTIALS")
    void badCredentialsReturnUnauthorized() throws Exception {
        when(userService.login(any(), any()))
                .thenThrow(new ApiException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student01\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("畸形 JSON 返回 400 MALFORMED_JSON")
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{oops}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    @Test
    @DisplayName("500 兜底不泄露内部异常信息")
    void internalErrorDoesNotLeakDetails() throws Exception {
        when(userService.login(any(), any()))
                .thenThrow(new IllegalStateException("数据库连接串 jdbc:mysql://secret"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student01\",\"password\":\"123456\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("服务器内部错误"));
    }

    @Test
    @DisplayName("page=abc 之类类型转换错误改为中文文案，不泄露 Spring 英文异常")
    void typeMismatchIsLocalized() throws Exception {
        BindException ex = new BindException(new com.sorting.visualization.model.request.PageQuery(), "pageQuery");
        ex.addError(new FieldError("pageQuery", "page", "abc", false,
                new String[]{"typeMismatch"}, null,
                "Failed to convert property value of type 'java.lang.String' to required type 'int'"));

        ApiError body = new GlobalExceptionHandler()
                .handleBindException(ex, new MockHttpServletRequest("GET", "/api/history/experiments"))
                .getBody();

        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getMessage()).isEqualTo("参数 page 类型不正确");
        assertThat(body.getMessage()).doesNotContain("Failed to convert");
    }
}
