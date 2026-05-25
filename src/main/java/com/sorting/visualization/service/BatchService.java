package com.sorting.visualization.service;

import com.sorting.visualization.entity.BatchDetail;
import com.sorting.visualization.entity.PerformanceBatch;
import com.sorting.visualization.mapper.BatchDetailMapper;
import com.sorting.visualization.mapper.PerformanceBatchMapper;
import com.sorting.visualization.model.response.PerformanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final PerformanceBatchMapper batchMapper;
    private final BatchDetailMapper detailMapper;

    /**
     * 异步保存性能测试批次（含明细）
     */
    @Async
    @Transactional
    public void saveBatch(String userId, Integer dataSize, String distribution, String dataType,
                          List<PerformanceResult> results, List<Long> algoIds) {
        // 保存批次主表
        PerformanceBatch batch = new PerformanceBatch();
        batch.setUserId(userId);
        batch.setDataSize(dataSize);
        batch.setDistribution(distribution);
        batch.setDataType(dataType);
        batchMapper.insert(batch);

        // 按耗时排序确定排名
        List<PerformanceResult> sorted = results.stream()
                .sorted(Comparator.comparing(PerformanceResult::getTime))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            PerformanceResult r = sorted.get(i);
            BatchDetail detail = new BatchDetail();
            detail.setBatchId(batch.getBatchId());
            detail.setAlgoId(algoIds.get(i));
            detail.setComparisons(r.getComparisons());
            detail.setSwaps(r.getSwaps());
            detail.setTimeMicros(r.getTime());
            detail.setRank(i + 1);
            detailMapper.insert(detail);
        }
        log.info("性能批次已保存: batchId={}, detailCount={}", batch.getBatchId(), results.size());
    }
}
