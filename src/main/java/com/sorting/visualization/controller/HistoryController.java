package com.sorting.visualization.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sorting.visualization.entity.BatchDetail;
import com.sorting.visualization.entity.ExperimentStep;
import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.mapper.BatchDetailMapper;
import com.sorting.visualization.mapper.PerformanceBatchMapper;
import com.sorting.visualization.mapper.TeachingExperimentMapper;
import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final ExperimentService experimentService;
    private final TeachingExperimentMapper experimentMapper;
    private final BatchDetailMapper batchDetailMapper;
    private final PerformanceBatchMapper performanceBatchMapper;

    /** 分页查询教学实验历史（学生只能查自己，教师/管理员可查指定用户） */
    @GetMapping("/experiments")
    public Map<String, Object> getExperiments(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        String target = resolveTargetUser(userId, currentUserId, role);
        Page<TeachingExperiment> result = experimentService.getUserExperiments(target, page, size);
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        return map;
    }

    /** 查询某次实验的步骤快照（回放用） */
    @GetMapping("/experiments/{expId}/steps")
    public List<ExperimentStep> getExperimentSteps(@PathVariable Long expId) {
        return experimentService.getExperimentSteps(expId);
    }

    /** 分页查询所有用户的教学实验（教师用） */
    @GetMapping("/experiments/all")
    public Map<String, Object> getAllExperiments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TeachingExperiment> p = new Page<>(page, size);
        Page<TeachingExperiment> result = experimentMapper.selectPage(p,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeachingExperiment>()
                        .orderByDesc(TeachingExperiment::getStartedAt));
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        return map;
    }

    /** 查询时间段内统计 */
    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam String start, @RequestParam String end,
                                         @RequestParam(defaultValue = "TEACHING") String type) {
        Map<String, Object> map = new HashMap<>();
        if ("PERFORMANCE".equalsIgnoreCase(type)) {
            map.put("data", batchDetailMapper.statsByDateRange(start, end));
        } else {
            map.put("data", experimentMapper.statsByDateRange(start, end));
        }
        return map;
    }

    /** 分页查询性能测试批次（学生只能查自己；教师/管理员 userId 为空时查全部） */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceBatches(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        Page<PerformanceBatch> p = new Page<>(page, size);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PerformanceBatch>()
                .orderByDesc(PerformanceBatch::getCreatedAt);
        boolean privileged = isPrivileged(role);
        if (!privileged) {
            wrapper.eq(PerformanceBatch::getUserId, currentUserId);
        } else if (userId != null && !userId.isEmpty() && !"all".equals(userId)) {
            wrapper.eq(PerformanceBatch::getUserId, userId);
        }
        Page<PerformanceBatch> result = performanceBatchMapper.selectPage(p, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        return map;
    }

    /** 教师与管理员可以查他人数据 */
    static boolean isPrivileged(String role) {
        return "admin".equals(role) || "teacher".equals(role);
    }

    /** 学生传任意 userId 都会被改写成令牌里的本人主键，避免越权读他人实验 */
    static String resolveTargetUser(String requested, String currentUserId, String role) {
        if (!isPrivileged(role)) {
            return currentUserId;
        }
        if (requested == null || requested.isBlank() || "all".equals(requested)) {
            return currentUserId;
        }
        return requested;
    }

    /** 查询性能批次的明细 */
    @GetMapping("/performance/{batchId}/details")
    public List<Map<String, Object>> getPerformanceDetails(@PathVariable Long batchId) {
        return batchDetailMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sorting.visualization.entity.BatchDetail>()
                        .eq(com.sorting.visualization.entity.BatchDetail::getBatchId, batchId)
                        .orderByAsc(com.sorting.visualization.entity.BatchDetail::getRank));
    }
}
