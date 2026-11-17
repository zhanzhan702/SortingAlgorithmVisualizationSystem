package com.sorting.visualization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sorting.visualization.entity.BatchDetail;
import com.sorting.visualization.entity.ExperimentStep;
import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.exception.ApiException;
import com.sorting.visualization.exception.ErrorCode;
import com.sorting.visualization.mapper.BatchDetailMapper;
import com.sorting.visualization.mapper.PerformanceBatchMapper;
import com.sorting.visualization.mapper.TeachingExperimentMapper;
import com.sorting.visualization.model.request.PageQuery;
import com.sorting.visualization.security.AuthInterceptor;
import com.sorting.visualization.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern USER_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final ExperimentService experimentService;
    private final TeachingExperimentMapper experimentMapper;
    private final BatchDetailMapper batchDetailMapper;
    private final PerformanceBatchMapper performanceBatchMapper;

    /** 分页查询教学实验历史（学生只能查自己，教师/管理员可查指定用户） */
    @GetMapping("/experiments")
    public Map<String, Object> getExperiments(
            @Valid @ModelAttribute PageQuery query,
            @RequestParam(required = false) String userId,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        String target = resolveTargetUser(userId, currentUserId, role);
        Page<TeachingExperiment> result = experimentService.getUserExperiments(
                target, query.getPage(), query.getSize());
        return pageResult(result, query.getPage(), query.getSize());
    }

    /** 查询某次实验的步骤快照（回放用） */
    @GetMapping("/experiments/{expId}/steps")
    public List<ExperimentStep> getExperimentSteps(@PathVariable Long expId) {
        requirePositiveId(expId, "expId");
        if (experimentMapper.selectById(expId) == null) {
            throw new ApiException(ErrorCode.EXPERIMENT_NOT_FOUND, "实验记录不存在: " + expId);
        }
        return experimentService.getExperimentSteps(expId);
    }

    /** 分页查询所有用户的教学实验（教师用） */
    @GetMapping("/experiments/all")
    public Map<String, Object> getAllExperiments(@Valid @ModelAttribute PageQuery query) {
        Page<TeachingExperiment> p = new Page<>(query.getPage(), query.getSize());
        Page<TeachingExperiment> result = experimentMapper.selectPage(p,
                new LambdaQueryWrapper<TeachingExperiment>()
                        .orderByDesc(TeachingExperiment::getStartedAt));
        return pageResult(result, query.getPage(), query.getSize());
    }

    /** 查询时间段内统计（start / end 为 yyyy-MM-dd，闭区间） */
    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam(required = false) String start,
                                        @RequestParam(required = false) String end,
                                        @RequestParam(defaultValue = "TEACHING") String type) {
        LocalDate startDate = parseDate(start, "start");
        LocalDate endDate = parseDate(end, "end");
        if (startDate.isAfter(endDate)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "start 不能晚于 end");
        }

        String normalizedType = type == null ? "TEACHING" : type.trim().toUpperCase();
        if (!"TEACHING".equals(normalizedType) && !"PERFORMANCE".equals(normalizedType)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "type 只能是 TEACHING 或 PERFORMANCE");
        }

        String startTime = startDate.atStartOfDay().format(DATE_TIME);
        String endTime = endDate.atTime(LocalTime.MAX).format(DATE_TIME);

        Map<String, Object> map = new HashMap<>();
        if ("PERFORMANCE".equals(normalizedType)) {
            map.put("data", batchDetailMapper.statsByDateRange(startTime, endTime));
        } else {
            map.put("data", experimentMapper.statsByDateRange(startTime, endTime));
        }
        return map;
    }

    /** 分页查询性能测试批次（学生只能查自己；教师/管理员 userId 为空时查全部） */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceBatches(
            @Valid @ModelAttribute PageQuery query,
            @RequestParam(defaultValue = "") String userId,
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) String currentUserId,
            @RequestAttribute(value = AuthInterceptor.ATTR_ROLE, required = false) String role) {
        Page<PerformanceBatch> p = new Page<>(query.getPage(), query.getSize());
        var wrapper = new LambdaQueryWrapper<PerformanceBatch>()
                .orderByDesc(PerformanceBatch::getCreatedAt);

        String requested = userId == null ? "" : userId.trim();
        if (!isPrivileged(role)) {
            // 学生传任意 userId 都无效，只查自己
            wrapper.eq(PerformanceBatch::getUserId, currentUserId);
        } else if (!requested.isEmpty() && !"all".equalsIgnoreCase(requested)) {
            if (!USER_ID.matcher(requested).matches()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "userId 格式不正确");
            }
            wrapper.eq(PerformanceBatch::getUserId, requested);
        }

        Page<PerformanceBatch> result = performanceBatchMapper.selectPage(p, wrapper);
        return pageResult(result, query.getPage(), query.getSize());
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
        requirePositiveId(batchId, "batchId");
        if (performanceBatchMapper.selectById(batchId) == null) {
            throw new ApiException(ErrorCode.BATCH_NOT_FOUND, "性能批次不存在: " + batchId);
        }
        return batchDetailMapper.selectMaps(
                new LambdaQueryWrapper<BatchDetail>()
                        .eq(BatchDetail::getBatchId, batchId)
                        .orderByAsc(BatchDetail::getRank));
    }

    // ---------- 工具方法 ----------

    private Map<String, Object> pageResult(Page<?> page, int current, int size) {
        Map<String, Object> map = new HashMap<>();
        map.put("records", page.getRecords());
        map.put("total", page.getTotal());
        map.put("page", current);
        map.put("size", size);
        return map;
    }

    private void requirePositiveId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, field + " 必须为正整数");
        }
    }

    private LocalDate parseDate(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new ApiException(ErrorCode.MISSING_PARAMETER, "缺少必要参数: " + field);
        }
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.INVALID_DATE,
                    field + " 格式不正确，应为 yyyy-MM-dd", List.of(Map.of("field", field)));
        }
    }
}
