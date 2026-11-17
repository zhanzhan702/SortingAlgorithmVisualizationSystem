package com.sorting.visualization.controller;

import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.model.response.PageResult;
import com.sorting.visualization.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * W4：分页接口。
 *
 * <p>原有 /api/history/* 已存在，为不修改原文件，这里新增 /api/page/* 下的分页接口，
 * 统一返回 {@link PageResult}（records / total / page / size / pages）。</p>
 */
@RestController
@RequestMapping("/api/page")
@RequiredArgsConstructor
public class PaginationController {

    private final PaginationService paginationService;

    /** 教学实验分页；userId 传 all 或不传表示查询全部用户 */
    @GetMapping("/experiments")
    public PageResult<TeachingExperiment> pageExperiments(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paginationService.pageExperiments(userId, page, size);
    }

    /** 性能批次分页；userId 传 all 或不传表示查询全部用户 */
    @GetMapping("/performance")
    public PageResult<PerformanceBatch> pagePerformance(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paginationService.pagePerformanceBatches(userId, page, size);
    }
}
