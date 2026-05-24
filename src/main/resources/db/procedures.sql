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
-- 视图
-- ============================================================

-- 视图 1：各类算法实验统计汇总
DROP VIEW IF EXISTS v_category_stats;
CREATE VIEW v_category_stats AS
SELECT
    a.category                         AS 算法类别,
    COUNT(DISTINCT te.exp_id)          AS 教学实验次数,
    COUNT(DISTINCT pb.batch_id)        AS 性能测试批次,
    COALESCE(AVG(te.time_micros), 0)   AS 教学平均耗时_us,
    COALESCE(AVG(bd.time_micros), 0)   AS 性能平均耗时_us
FROM algorithms a
LEFT JOIN teaching_experiments te ON a.algo_id = te.algo_id
LEFT JOIN batch_details bd         ON a.algo_id = bd.algo_id
LEFT JOIN performance_batches pb   ON bd.batch_id = pb.batch_id
GROUP BY a.category;

-- 视图 2：算法综合排名（按性能测试平均耗时排序）
DROP VIEW IF EXISTS v_algorithm_ranking;
CREATE VIEW v_algorithm_ranking AS
SELECT
    a.algo_name                     AS 算法名称,
    a.category                      AS 类别,
    a.time_complexity               AS 时间复杂度,
    a.is_stable                     AS 是否稳定,
    COALESCE(s.total_experiments, 0) AS 教学实验次数,
    COALESCE(s.total_batches, 0)    AS 性能测试次数,
    COALESCE(s.avg_batch_time_micros, 0) AS 平均性能耗时_us,
    RANK() OVER (ORDER BY COALESCE(s.avg_batch_time_micros, 999999999)) AS 速度排名
FROM algorithms a
LEFT JOIN algorithm_stats s ON a.algo_id = s.algo_id;

-- ============================================================
-- 存储过程
-- ============================================================

-- 存储过程 1：查询某段时间内各算法的实验统计
DROP PROCEDURE IF EXISTS sp_algorithm_stats;
DELIMITER //
CREATE PROCEDURE sp_algorithm_stats(
    IN start_date DATE,
    IN end_date   DATE,
    IN stat_type  VARCHAR(20)   -- 'TEACHING' 或 'PERFORMANCE'
)
BEGIN
    IF stat_type = 'TEACHING' THEN
        SELECT
            a.algo_name                   AS 算法,
            COUNT(*)                      AS 实验次数,
            ROUND(AVG(te.comparisons), 0) AS 平均比较次数,
            ROUND(AVG(te.swaps), 0)       AS 平均交换次数,
            ROUND(AVG(te.time_micros), 0) AS 平均耗时_us,
            SUM(CASE WHEN te.status = 'COMPLETED' THEN 1 ELSE 0 END) AS 完成次数,
            SUM(CASE WHEN te.status = 'STOPPED'   THEN 1 ELSE 0 END) AS 停止次数
        FROM teaching_experiments te
        JOIN algorithms a ON te.algo_id = a.algo_id
        WHERE te.started_at BETWEEN start_date AND DATE_ADD(end_date, INTERVAL 1 DAY)
        GROUP BY a.algo_id, a.algo_name
        ORDER BY 实验次数 DESC;
    ELSEIF stat_type = 'PERFORMANCE' THEN
        SELECT
            a.algo_name                   AS 算法,
            COUNT(*)                      AS 测试次数,
            ROUND(AVG(bd.comparisons), 0) AS 平均比较次数,
            ROUND(AVG(bd.swaps), 0)       AS 平均交换次数,
            ROUND(AVG(bd.time_micros), 0) AS 平均耗时_us
        FROM batch_details bd
        JOIN performance_batches pb ON bd.batch_id = pb.batch_id
        JOIN algorithms a           ON bd.algo_id = a.algo_id
        WHERE pb.created_at BETWEEN start_date AND DATE_ADD(end_date, INTERVAL 1 DAY)
        GROUP BY a.algo_id, a.algo_name
        ORDER BY 平均耗时_us ASC;
    ELSE
        SELECT '错误：stat_type 必须为 TEACHING 或 PERFORMANCE' AS 提示;
    END IF;
END //
DELIMITER ;

-- 存储过程 2：查询某用户的实验历史（分页用 LIMIT offset, size 在 Service 层拼接）
DROP PROCEDURE IF EXISTS sp_user_experiments;
DELIMITER //
CREATE PROCEDURE sp_user_experiments(
    IN p_user_id BIGINT,
    IN p_offset  INT,
    IN p_limit   INT
)
BEGIN
    SELECT
        te.exp_id,
        a.algo_name,
        te.data_size,
        te.total_steps,
        te.comparisons,
        te.swaps,
        te.time_micros,
        te.interval_ms,
        te.status,
        te.started_at,
        (SELECT COUNT(*) FROM experiment_steps WHERE exp_id = te.exp_id) AS step_count
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    ORDER BY te.started_at DESC
    LIMIT p_offset, p_limit;
END //
DELIMITER ;
