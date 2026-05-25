-- ============================================================
-- 数据库迁移：user_id BIGINT → VARCHAR(32) UUID
-- 执行前请备份数据库！
-- 用法: mysql -u root -p sorting_visualization < migrate_to_uuid.sql
-- ============================================================

USE sorting_visualization;

-- 1. 删除所有外键（user_id相关）
ALTER TABLE teaching_experiments DROP FOREIGN KEY IF EXISTS teaching_experiments_ibfk_1;
ALTER TABLE performance_batches   DROP FOREIGN KEY IF EXISTS performance_batches_ibfk_1;
ALTER TABLE algorithm_stats       DROP FOREIGN KEY IF EXISTS algorithm_stats_ibfk_1;

-- 2. 创建临时ID映射表
DROP TEMPORARY TABLE IF EXISTS user_id_map;
CREATE TEMPORARY TABLE user_id_map AS
SELECT user_id AS old_id, REPLACE(UUID(), '-', '') AS new_id FROM users;

-- 3. 更新外键表（teaching_experiments）
UPDATE teaching_experiments te
JOIN user_id_map m ON te.user_id = m.old_id
SET te.user_id = m.new_id;

-- 4. 更新外键表（performance_batches）
UPDATE performance_batches pb
JOIN user_id_map m ON pb.user_id = m.old_id
SET pb.user_id = m.new_id;

-- 5. 更新 users 表主键
UPDATE users u JOIN user_id_map m ON u.user_id = m.old_id
SET u.user_id = m.new_id;

-- 6. 修改列类型
ALTER TABLE users                MODIFY user_id VARCHAR(32) NOT NULL;
ALTER TABLE teaching_experiments MODIFY user_id VARCHAR(32) NOT NULL;
ALTER TABLE performance_batches  MODIFY user_id VARCHAR(32) NOT NULL;

-- 7. 重建外键
ALTER TABLE teaching_experiments ADD FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE performance_batches   ADD FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE algorithm_stats       ADD FOREIGN KEY (algo_id)  REFERENCES algorithms(algo_id) ON DELETE CASCADE;

-- 8. 更新存储过程参数类型
DROP PROCEDURE IF EXISTS sp_user_report;
DELIMITER //
CREATE PROCEDURE sp_user_report(IN p_user_id VARCHAR(32))
BEGIN
    SELECT u.username,
        COUNT(DISTINCT te.exp_id) AS total_experiments,
        SUM(CASE WHEN te.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
        SUM(CASE WHEN te.status = 'STOPPED'   THEN 1 ELSE 0 END) AS stopped,
        ROUND(AVG(te.comparisons), 0) AS avg_comparisons,
        ROUND(AVG(te.swaps), 0)       AS avg_swaps,
        ROUND(AVG(te.time_micros), 0) AS avg_time_us
    FROM users u
    LEFT JOIN teaching_experiments te ON u.user_id = te.user_id
    WHERE u.user_id = p_user_id
    GROUP BY u.user_id, u.username;

    SELECT a.algo_name AS favorite_algorithm, COUNT(*) AS use_count
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    GROUP BY a.algo_id, a.algo_name
    ORDER BY use_count DESC LIMIT 1;

    SELECT a.algo_name, COUNT(*) AS times_used,
        ROUND(AVG(te.comparisons),0) AS avg_cmp,
        ROUND(AVG(te.swaps),0) AS avg_swp,
        ROUND(AVG(te.time_micros),0) AS avg_us
    FROM teaching_experiments te
    JOIN algorithms a ON te.algo_id = a.algo_id
    WHERE te.user_id = p_user_id
    GROUP BY a.algo_id, a.algo_name
    ORDER BY times_used DESC;
END //
DELIMITER ;

-- 9. 清理临时表（自动过期）
DROP TEMPORARY TABLE IF EXISTS user_id_map;

SELECT '✅ UUID迁移完成！' AS result;
