package com.sorting.visualization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sorting.visualization.entity.BatchDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface BatchDetailMapper extends BaseMapper<BatchDetail> {

    /** 按时间段查询各算法性能统计 */
    @Select("SELECT a.algo_name, COUNT(*) AS count, AVG(bd.comparisons) AS avg_cmp, AVG(bd.swaps) AS avg_swp, AVG(bd.time_micros) AS avg_time " +
            "FROM batch_details bd JOIN algorithms a ON bd.algo_id=a.algo_id " +
            "JOIN performance_batches pb ON bd.batch_id=pb.batch_id " +
            "WHERE pb.created_at BETWEEN #{start} AND #{end} GROUP BY a.algo_id")
    List<Map<String, Object>> statsByDateRange(String start, String end);
}
