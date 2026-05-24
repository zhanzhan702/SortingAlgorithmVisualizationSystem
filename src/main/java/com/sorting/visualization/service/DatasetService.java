package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sorting.visualization.entity.Dataset;
import com.sorting.visualization.mapper.DatasetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetMapper datasetMapper;

    /** 保存数据集 */
    @Transactional
    public Long saveDataset(String name, String dataType, Integer dataSize, String distribution,
                            String dataJson, Long creatorId) {
        Dataset ds = new Dataset();
        ds.setName(name);
        ds.setDataType(dataType);
        ds.setDataSize(dataSize);
        ds.setDistribution(distribution);
        ds.setDataJson(dataJson);
        ds.setCreatorId(creatorId);
        datasetMapper.insert(ds);
        log.info("数据集已保存: datasetId={}, name={}", ds.getDatasetId(), name);
        return ds.getDatasetId();
    }

    /** 分页查询用户的数据集 */
    public Page<Dataset> getUserDatasets(Long userId, int page, int size) {
        Page<Dataset> p = new Page<>(page, size);
        return datasetMapper.selectPage(p,
                new LambdaQueryWrapper<Dataset>()
                        .eq(Dataset::getCreatorId, userId)
                        .orderByDesc(Dataset::getCreatedAt));
    }

    /** 删除数据集 */
    @Transactional
    public void deleteDataset(Long datasetId) {
        datasetMapper.deleteById(datasetId);
    }
}
