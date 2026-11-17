package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.mapper.PerformanceBatchMapper;
import com.sorting.visualization.mapper.TeachingExperimentMapper;
import com.sorting.visualization.model.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * W4：分页查询业务。
 *
 * <p>说明：MyBatis-Plus 3.5.9 起把 {@code PaginationInnerInterceptor} 拆到了
 * 独立的 mybatis-plus-jsqlparser 模块，本项目 pom 未引入该依赖，无法直接使用
 * 分页插件。这里改用 {@code selectCount} + {@code LIMIT offset,size} 手写分页，
 * 全部为本任务新增文件，不改动原有 Controller / Service。</p>
 */
@Service
@RequiredArgsConstructor
public class PaginationService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final TeachingExperimentMapper experimentMapper;
    private final PerformanceBatchMapper batchMapper;

    /** 教学实验分页：userId 为空或 all 时查询全部用户 */
    public PageResult<TeachingExperiment> pageExperiments(String userId, int page, int size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);

        LambdaQueryWrapper<TeachingExperiment> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isBlank() && !"all".equals(userId)) {
            wrapper.eq(TeachingExperiment::getUserId, userId);
        }

        long total = experimentMapper.selectCount(wrapper);
        wrapper.orderByDesc(TeachingExperiment::getStartedAt)
                .last("LIMIT " + offset(p, s) + "," + s);
        List<TeachingExperiment> records = experimentMapper.selectList(wrapper);

        return new PageResult<>(records, total, p, s);
    }

    /** 性能批次分页：userId 为空或 all 时查询全部用户 */
    public PageResult<PerformanceBatch> pagePerformanceBatches(String userId, int page, int size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);

        LambdaQueryWrapper<PerformanceBatch> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isBlank() && !"all".equals(userId)) {
            wrapper.eq(PerformanceBatch::getUserId, userId);
        }

        long total = batchMapper.selectCount(wrapper);
        wrapper.orderByDesc(PerformanceBatch::getCreatedAt)
                .last("LIMIT " + offset(p, s) + "," + s);
        List<PerformanceBatch> records = batchMapper.selectList(wrapper);

        return new PageResult<>(records, total, p, s);
    }

    private static int normalizePage(int page) {
        return page < 1 ? 1 : page;
    }

    private static int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private static long offset(int page, int size) {
        return (long) (page - 1) * size;
    }
}
