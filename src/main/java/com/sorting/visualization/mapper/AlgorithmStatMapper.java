package com.sorting.visualization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sorting.visualization.entity.AlgorithmStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlgorithmStatMapper extends BaseMapper<AlgorithmStat> {

    /** 查询算法综合排名视图 */
    @Select("SELECT * FROM v_algorithm_ranking")
    List<Map<String, Object>> selectRanking();

    /** 查询用户活跃度视图 */
    @Select("SELECT * FROM v_user_activity")
    List<Map<String, Object>> selectUserActivity();

    /** 调用存储过程生成用户报告（返回第一个结果集：基础统计） */
    @Select("CALL sp_user_report(#{userId})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callUserReport(String userId);
}
