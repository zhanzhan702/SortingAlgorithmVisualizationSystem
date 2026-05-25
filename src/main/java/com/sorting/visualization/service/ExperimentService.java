package com.sorting.visualization.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sorting.visualization.entity.ExperimentStep;
import com.sorting.visualization.entity.TeachingExperiment;
import com.sorting.visualization.mapper.ExperimentStepMapper;
import com.sorting.visualization.mapper.TeachingExperimentMapper;
import com.sorting.visualization.model.response.StepUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final TeachingExperimentMapper experimentMapper;
    private final ExperimentStepMapper stepMapper;

    /**
     * 异步保存教学实验摘要
     */
    @Async
    @Transactional
    public void saveExperiment(Long userId, Long algoId, Integer dataSize, Integer totalSteps,
                               Integer comparisons, Integer swaps, Long timeMicros,
                               Integer intervalMs, String status) {
        TeachingExperiment exp = new TeachingExperiment();
        exp.setUserId(userId);
        exp.setAlgoId(algoId);
        exp.setDataSize(dataSize);
        exp.setTotalSteps(totalSteps);
        exp.setComparisons(comparisons);
        exp.setSwaps(swaps);
        exp.setTimeMicros(timeMicros);
        exp.setIntervalMs(intervalMs);
        exp.setStatus(status);
        exp.setFinishedAt(LocalDateTime.now());
        experimentMapper.insert(exp);
        log.info("教学实验已保存: expId={}, algoId={}, status={}", exp.getExpId(), algoId, status);
    }

    /**
     * 异步保存实验 + 步骤快照（用户勾选"保存回放"时）
     */
    @Async
    @Transactional
    public void saveExperimentWithSteps(Long userId, Long algoId, Integer dataSize,
                                         List<StepUpdate> steps, Integer intervalMs, String status) {
        if (steps == null || steps.isEmpty()) return;

        StepUpdate last = steps.get(steps.size() - 1);
        TeachingExperiment exp = new TeachingExperiment();
        exp.setUserId(userId);
        exp.setAlgoId(algoId);
        exp.setDataSize(dataSize);
        exp.setTotalSteps(steps.size());
        exp.setComparisons(last.getStats() != null ? last.getStats().getComparisons() : 0);
        exp.setSwaps(last.getStats() != null ? last.getStats().getSwaps() : 0);
        exp.setTimeMicros(last.getStats() != null ? last.getStats().getTime() : 0L);
        exp.setIntervalMs(intervalMs);
        exp.setStatus(status);
        exp.setFinishedAt(LocalDateTime.now());
        experimentMapper.insert(exp);

        // 批量插入步骤快照
        List<ExperimentStep> stepEntities = new ArrayList<>();
        for (StepUpdate step : steps) {
            ExperimentStep es = new ExperimentStep();
            es.setExpId(exp.getExpId());
            es.setStepNumber(step.getStep());
            es.setDataJson(toJson(step.getData()));
            es.setHighlightJson(toJson(step.getHighlight()));
            es.setDescription(step.getDescription());
            stepEntities.add(es);
        }
        stepMapper.insert(stepEntities, stepEntities.size()); // 批量插入
        log.info("教学实验+步骤已保存: expId={}, steps={}", exp.getExpId(), steps.size());
    }

    private static String toJson(Object obj) {
        return com.sorting.visualization.util.JsonUtil.toJson(obj);
    }

    /** 分页查询用户实验历史 */
    public Page<TeachingExperiment> getUserExperiments(Long userId, int page, int size) {
        Page<TeachingExperiment> p = new Page<>(page, size);
        return experimentMapper.selectPage(p,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeachingExperiment>()
                        .eq(TeachingExperiment::getUserId, userId)
                        .orderByDesc(TeachingExperiment::getStartedAt));
    }

    /** 查询实验的步骤快照 */
    public List<ExperimentStep> getExperimentSteps(Long expId) {
        return stepMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExperimentStep>()
                        .eq(ExperimentStep::getExpId, expId)
                        .orderByAsc(ExperimentStep::getStepNumber));
    }
}
