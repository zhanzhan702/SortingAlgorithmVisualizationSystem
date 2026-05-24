package com.sorting.visualization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sorting.visualization.entity.TeachingExperiment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeachingExperimentMapper extends BaseMapper<TeachingExperiment> {

    /** 按时间段查询各算法统计 */
    @Select("SELECT a.algo_name, COUNT(*) AS count, AVG(te.comparisons) AS avg_cmp, AVG(te.swaps) AS avg_swp " +
            "FROM teaching_experiments te JOIN algorithms a ON te.algo_id=a.algo_id " +
            "WHERE te.started_at BETWEEN #{start} AND #{end} GROUP BY a.algo_id")
    List<Map<String, Object>> statsByDateRange(String start, String end);
}
