package com.sorting.visualization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sorting.visualization.entity.Dataset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {
}
