package com.sorting.visualization.controller;

import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.model.response.PageResult;
import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * W4：分页接口。
 *
 * <p>原有 /api/history/* 已存在，为不修改原文件，这里新增 /api/page/* 下的分页接口，
 * 统一返回 {@link PageResult}（records / total / page / size / pages）。</p>
 *
 * <p>数据范围与 /api/history/* 保持一致：学生只能翻自己的记录（传别人的 userId 或 all
 * 都会被改写成本人），教师与管理员可以翻任意用户或全部。</p>
 */
@RestController
@RequestMapping("/api/page")
@RequiredArgsConstructor
public class PaginationController {

    private final PaginationService paginationService;

    /** 教学实验分页；userId 传 all 或不传表示查询全部用户（仅教师/管理员生效） */
    @GetMapping("/experiments")
    public PageResult<TeachingExperiment> pageExperiments(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        return paginationService.pageExperiments(
                scopeUserId(userId, currentUserId, role), page, size);
    }

    /** 性能批次分页；userId 传 all 或不传表示查询全部用户（仅教师/管理员生效） */
    @GetMapping("/performance")
    public PageResult<PerformanceBatch> pagePerformance(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        return paginationService.pagePerformanceBatches(
                scopeUserId(userId, currentUserId, role), page, size);
    }

    /**
     * 把请求里的 userId 收敛为当前调用者可见的范围。
     *
     * @return 学生恒返回本人主键（忽略传入值）；教师/管理员返回 null 表示不加用户过滤
     */
    static String scopeUserId(String requested, String currentUserId, String role) {
        boolean privileged = "admin".equals(role) || "teacher".equals(role);
        if (!privileged) {
            return currentUserId;
        }
        if (requested == null || requested.isBlank() || "all".equalsIgnoreCase(requested)) {
            return null;
        }
        return requested;
    }
}
