-- ============================================================
-- 排序算法可视化教学与实验数据管理平台
-- 存储过程 + 视图 + 触发器
-- ============================================================

USE sorting_visualization;

-- ============================================================
-- 触发器
-- ============================================================

-- 触发器 1：教学实验插入后自动更新算法统计（教学维度）
DROP TRIGGER IF EXISTS trg_after_experiment_insert;
DELIMITER //
CREATE TRIGGER trg_after_experiment_insert
AFTER INSERT ON teaching_experiments
FOR EACH ROW
BEGIN
    INSERT INTO algorithm_stats (
        algo_id, total_experiments,
        avg_exp_comparisons, avg_exp_swaps, avg_exp_time_micros
    ) VALUES (
        NEW.algo_id, 1,
        NEW.comparisons, NEW.swaps, NEW.time_micros
    )
    ON DUPLICATE KEY UPDATE
        total_experiments = total_experiments + 1,
        avg_exp_comparisons = ROUND(
            (avg_exp_comparisons * (total_experiments - 1) + NEW.comparisons) / total_experiments, 2),
        avg_exp_swaps = ROUND(
            (avg_exp_swaps * (total_experiments - 1) + NEW.swaps) / total_experiments, 2),
        avg_exp_time_micros = ROUND(
            (avg_exp_time_micros * (total_experiments - 1) + NEW.time_micros) / total_experiments, 2);
END //
DELIMITER ;

-- 触发器 2：批次明细插入后自动更新算法统计（性能维度）
DROP TRIGGER IF EXISTS trg_after_batch_detail_insert;
DELIMITER //
CREATE TRIGGER trg_after_batch_detail_insert
AFTER INSERT ON batch_details
FOR EACH ROW
BEGIN
    INSERT INTO algorithm_stats (
        algo_id, total_batches,
        avg_batch_comparisons, avg_batch_swaps, avg_batch_time_micros
    ) VALUES (
        NEW.algo_id, 1,
        NEW.comparisons, NEW.swaps, NEW.time_micros
    )
    ON DUPLICATE KEY UPDATE
        total_batches = total_batches + 1,
        avg_batch_comparisons = ROUND(
            (avg_batch_comparisons * (total_batches - 1) + NEW.comparisons) / total_batches, 2),
        avg_batch_swaps = ROUND(
            (avg_batch_swaps * (total_batches - 1) + NEW.swaps) / total_batches, 2),
        avg_batch_time_micros = ROUND(
            (avg_batch_time_micros * (total_batches - 1) + NEW.time_micros) / total_batches, 2);
END //
DELIMITER ;

-- ============================================================
-- 视图 (2个) — 由管理后台"统计"Tab 调用
-- ============================================================

-- 视图 1：算法综合排名（按性能测试平均耗时排序）
DROP VIEW IF EXISTS v_algorithm_ranking;
CREATE VIEW v_algorithm_ranking AS
SELECT
    a.algo_name                     AS algo_name,
    a.category                      AS category,
    a.time_complexity               AS time_complexity,
    a.is_stable                     AS is_stable,
    COALESCE(s.total_experiments, 0) AS teaching_count,
    COALESCE(s.total_batches, 0)    AS performance_count,
    COALESCE(s.avg_batch_time_micros, 0) AS avg_perf_time_us,
    RANK() OVER (ORDER BY COALESCE(s.avg_batch_time_micros, 999999999)) AS speed_rank
FROM algorithms a
LEFT JOIN algorithm_stats s ON a.algo_id = s.algo_id;

-- 视图 2：用户活跃度汇总
DROP VIEW IF EXISTS v_user_activity;
CREATE VIEW v_user_activity AS
SELECT
    u.username,
    u.role,
    COALESCE(te.exp_count, 0)  AS teaching_experiments,
    COALESCE(pb.batch_count, 0) AS performance_batches,
    GREATEST(
        COALESCE(te.last_exp, '1970-01-01'),
        COALESCE(pb.last_batch, '1970-01-01')
    ) AS last_activity
FROM users u
LEFT JOIN (
    SELECT user_id, COUNT(*) AS exp_count, MAX(started_at) AS last_exp
    FROM teaching_experiments GROUP BY user_id
) te ON u.user_id = te.user_id
LEFT JOIN (
    SELECT user_id, COUNT(*) AS batch_count, MAX(created_at) AS last_batch
    FROM performance_batches GROUP BY user_id
) pb ON u.user_id = pb.user_id;

-- ============================================================
-- 存储过程 (1个) — 由管理后台"用户报告"按钮调用
-- ============================================================

DROP PROCEDURE IF EXISTS sp_user_report;
DELIMITER //
CREATE PROCEDURE sp_user_report(IN p_user_id BIGINT)
BEGIN
    -- 结果集1：基础统计
    SELECT
        u.username,
        COUNT(DISTINCT te.exp_id)                               AS total_experiments,
        SUM(CASE WHEN te.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
        SUM(CASE WHEN te.status = 'STOPPED'   THEN 1 ELSE 0 END) AS stopped,
        ROUND(AVG(te.comparisons), 0)                            AS avg_comparisons,
        ROUND(AVG(te.swaps), 0)                                  AS avg_swaps,
        ROUND(AVG(te.time_micros), 0)                            AS avg_time_us
    FROM users u
    LEFT JOIN teaching_experiments te ON u.user_id = te.user_id
    WHERE u.user_id = p_user_id
    GROUP BY u.user_id, u.username;

    -- 结果集2：最常用算法
    SELECT
        a.algo_name AS favorite_algorithm,
        COUNT(*)    AS use_count
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    GROUP BY a.algo_id, a.algo_name
    ORDER BY use_count DESC
    LIMIT 1;

    -- 结果集3：各算法详细统计
    SELECT
        a.algo_name,
        COUNT(*)                     AS times_used,
        ROUND(AVG(te.comparisons),0) AS avg_cmp,
        ROUND(AVG(te.swaps),0)       AS avg_swp,
        ROUND(AVG(te.time_micros),0) AS avg_us
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    GROUP BY a.algo_id, a.algo_name
    ORDER BY times_used DESC;
END //
DELIMITER ;
