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

-- 视图 1：算法综合排名（纯性能数据，按每元素耗时排序）
DROP VIEW IF EXISTS v_algorithm_ranking;
CREATE VIEW v_algorithm_ranking AS
SELECT
    a.algo_name,
    a.category,
    a.time_complexity,
    a.is_stable,
    COALESCE(s.total_experiments, 0) AS teaching_count,
    COALESCE(s.total_batches, 0)    AS perf_count,
    COALESCE(s.avg_exp_time_micros, 0)   AS teach_avg_time_us,
    COALESCE(s.avg_batch_time_micros, 0) AS perf_avg_time_us,
    COALESCE(ds.avg_data_size, 0)   AS perf_avg_data_size,
    CASE WHEN ds.avg_data_size > 0 AND s.avg_batch_time_micros > 0
         THEN ROUND(s.avg_batch_time_micros / ds.avg_data_size, 2)
         ELSE NULL END               AS perf_time_per_element_us,
    RANK() OVER (
        ORDER BY CASE WHEN ds.avg_data_size > 0 AND s.avg_batch_time_micros > 0
                  THEN s.avg_batch_time_micros / ds.avg_data_size
                  ELSE 999999999 END
    ) AS speed_rank
FROM algorithms a
LEFT JOIN algorithm_stats s ON a.algo_id = s.algo_id
LEFT JOIN (
    SELECT bd.algo_id, ROUND(AVG(pb.data_size), 0) AS avg_data_size
    FROM batch_details bd
    JOIN performance_batches pb ON bd.batch_id = pb.batch_id
    GROUP BY bd.algo_id
) ds ON a.algo_id = ds.algo_id;

-- 视图 2：用户活跃度汇总
DROP VIEW IF EXISTS v_user_activity;
CREATE VIEW v_user_activity AS
SELECT
    u.user_id,
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
CREATE PROCEDURE sp_user_report(IN p_user_id VARCHAR(32))
BEGIN
    -- 结果集1：综合统计（教学模式 + 性能模式，子查询避免笛卡尔积）
    SELECT
        u.username,
        u.role,
        COALESCE(ts.teach_total, 0)      AS teach_total,
        COALESCE(ts.teach_completed, 0)   AS teach_completed,
        COALESCE(ts.teach_stopped, 0)     AS teach_stopped,
        COALESCE(ts.teach_avg_cmp, 0)     AS teach_avg_cmp,
        COALESCE(ts.teach_avg_swp, 0)     AS teach_avg_swp,
        COALESCE(ts.teach_avg_us, 0)      AS teach_avg_us,
        COALESCE(ps.perf_batches, 0)      AS perf_batches,
        COALESCE(ps.perf_details, 0)      AS perf_details,
        COALESCE(ps.perf_avg_us, 0)       AS perf_avg_us,
        COALESCE(ps.perf_avg_cmp, 0)      AS perf_avg_cmp,
        COALESCE(ps.perf_avg_swp, 0)      AS perf_avg_swp
    FROM users u
    LEFT JOIN (
        SELECT user_id,
            COUNT(DISTINCT exp_id)                                AS teach_total,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS teach_completed,
            SUM(CASE WHEN status = 'STOPPED'   THEN 1 ELSE 0 END) AS teach_stopped,
            ROUND(AVG(comparisons), 0)                            AS teach_avg_cmp,
            ROUND(AVG(swaps), 0)                                  AS teach_avg_swp,
            ROUND(AVG(time_micros), 0)                            AS teach_avg_us
        FROM teaching_experiments
        GROUP BY user_id
    ) ts ON u.user_id = ts.user_id
    LEFT JOIN (
        SELECT pb.user_id,
            COUNT(DISTINCT pb.batch_id)    AS perf_batches,
            COUNT(bd.detail_id)            AS perf_details,
            ROUND(AVG(bd.time_micros), 0)  AS perf_avg_us,
            ROUND(AVG(bd.comparisons), 0)  AS perf_avg_cmp,
            ROUND(AVG(bd.swaps), 0)        AS perf_avg_swp
        FROM performance_batches pb
        JOIN batch_details bd ON pb.batch_id = bd.batch_id
        GROUP BY pb.user_id
    ) ps ON u.user_id = ps.user_id
    WHERE u.user_id = p_user_id;

    -- 结果集2：教学模式最常用算法
    SELECT
        a.algo_name AS favorite_algorithm,
        COUNT(*)    AS use_count
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    GROUP BY a.algo_id, a.algo_name
    ORDER BY use_count DESC
    LIMIT 1;

    -- 结果集3：各算法综合统计（教学 + 性能）
    SELECT
        a.algo_name,
        -- 教学维度
        COALESCE(ts.teach_times, 0)        AS teach_times,
        COALESCE(ts.teach_avg_cmp, 0)      AS teach_avg_cmp,
        COALESCE(ts.teach_avg_swp, 0)      AS teach_avg_swp,
        COALESCE(ts.teach_avg_us, 0)       AS teach_avg_us,
        -- 性能维度
        COALESCE(ps.perf_times, 0)         AS perf_times,
        COALESCE(ps.perf_avg_us, 0)        AS perf_avg_us,
        COALESCE(ps.perf_avg_cmp, 0)       AS perf_avg_cmp,
        COALESCE(ps.perf_avg_swp, 0)       AS perf_avg_swp,
        -- 性价比
        CASE WHEN ts.teach_avg_us > 0 AND ps.perf_avg_us > 0
             THEN ROUND(ps.perf_avg_us / ts.teach_avg_us, 2)
             ELSE NULL END                   AS perf_vs_teach_ratio
    FROM algorithms a
    LEFT JOIN (
        SELECT algo_id,
            COUNT(*)                     AS teach_times,
            ROUND(AVG(comparisons), 0)   AS teach_avg_cmp,
            ROUND(AVG(swaps), 0)         AS teach_avg_swp,
            ROUND(AVG(time_micros), 0)   AS teach_avg_us
        FROM teaching_experiments
        WHERE user_id = p_user_id
        GROUP BY algo_id
    ) ts ON a.algo_id = ts.algo_id
    LEFT JOIN (
        SELECT bd.algo_id,
            COUNT(*)                     AS perf_times,
            ROUND(AVG(bd.time_micros), 0) AS perf_avg_us,
            ROUND(AVG(bd.comparisons), 0) AS perf_avg_cmp,
            ROUND(AVG(bd.swaps), 0)       AS perf_avg_swp
        FROM batch_details bd
        JOIN performance_batches pb ON bd.batch_id = pb.batch_id
        WHERE pb.user_id = p_user_id
        GROUP BY bd.algo_id
    ) ps ON a.algo_id = ps.algo_id
    WHERE ts.teach_times > 0 OR ps.perf_times > 0
    ORDER BY teach_times DESC, perf_times DESC;
END //
DELIMITER ;
