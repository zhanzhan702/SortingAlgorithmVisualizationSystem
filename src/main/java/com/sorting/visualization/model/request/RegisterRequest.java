package com.sorting.visualization.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求体。
 *
 * <p>角色只允许 {@code student} / {@code teacher}：原实现直接接收任意字符串，
 * 调用方可以构造 {@code {"role":"admin"}} 注册出管理员账号。</p>
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须为 3-50 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须为 6-64 个字符")
    private String password;

    @Pattern(regexp = "student|teacher", message = "角色只能是 student 或 teacher")
    private String role = "student";
}
