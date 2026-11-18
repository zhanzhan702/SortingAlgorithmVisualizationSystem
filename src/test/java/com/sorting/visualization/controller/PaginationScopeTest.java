package com.sorting.visualization.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * W4：分页接口的数据范围收敛。
 *
 * <p>与 /api/history/* 对齐：学生传别人的 userId 或 all 都不能扩大可见范围。</p>
 */
class PaginationScopeTest {

    private static final String ME = "user-me";
    private static final String OTHER = "user-other";

    @Test
    @DisplayName("学生传他人 userId 会被改写成本人")
    void studentCannotQueryOthers() {
        assertThat(PaginationController.scopeUserId(OTHER, ME, "student")).isEqualTo(ME);
    }

    @Test
    @DisplayName("学生传 all 也只看自己")
    void studentAllIsScopedToSelf() {
        assertThat(PaginationController.scopeUserId("all", ME, "student")).isEqualTo(ME);
        assertThat(PaginationController.scopeUserId("", ME, "student")).isEqualTo(ME);
    }

    @Test
    @DisplayName("教师/管理员查 all 或留空表示不过滤用户")
    void privilegedAllMeansNoFilter() {
        assertThat(PaginationController.scopeUserId("all", ME, "teacher")).isNull();
        assertThat(PaginationController.scopeUserId("", ME, "admin")).isNull();
        assertThat(PaginationController.scopeUserId(null, ME, "admin")).isNull();
    }

    @Test
    @DisplayName("教师可以指定要查的用户")
    void privilegedCanPickUser() {
        assertThat(PaginationController.scopeUserId(OTHER, ME, "teacher")).isEqualTo(OTHER);
    }

    @Test
    @DisplayName("角色缺失按非特权处理")
    void missingRoleIsNotPrivileged() {
        assertThat(PaginationController.scopeUserId(OTHER, ME, null)).isEqualTo(ME);
    }
}
