-- ============================================================
-- 排序算法可视化教学与实验数据管理平台 — DDL
-- MySQL 8.0 + InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS sorting_visualization
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE sorting_visualization;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE users (
    user_id       VARCHAR(32)  PRIMARY KEY COMMENT 'UUID(32位无横线)',
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('student','teacher','admin') DEFAULT 'student',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 算法元数据表
-- ----------------------------
CREATE TABLE algorithms (
    algo_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    algo_code        VARCHAR(20)  NOT NULL COMMENT '内部代码: BUBBLE/QUICK/...',
    algo_name        VARCHAR(50)  NOT NULL,
    category         VARCHAR(20)  NOT NULL COMMENT 'exchange/insertion/selection/merge/other',
    time_complexity  VARCHAR(50)  COMMENT '时间复杂度',
    space_complexity VARCHAR(20)  COMMENT '空间复杂度',
    is_stable        BOOLEAN      COMMENT '是否稳定',
    pseudocode       TEXT         COMMENT '伪代码（多行文本）',
    description      VARCHAR(500) COMMENT '算法简介',
    advantages       VARCHAR(500) COMMENT '优点',
    UNIQUE KEY uk_algo_code (algo_code),
    UNIQUE KEY uk_algo_name (algo_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排序算法元数据';

-- ----------------------------
-- 3. 教学实验记录表（摘要）
-- ----------------------------
CREATE TABLE teaching_experiments (
    exp_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(32) NOT NULL,
    algo_id      BIGINT NOT NULL,
    data_size    INT    NOT NULL,
    total_steps  INT    DEFAULT 0,
    comparisons  INT    DEFAULT 0,
    swaps        INT    DEFAULT 0,
    time_micros  BIGINT DEFAULT 0  COMMENT '总耗时（微秒）',
    interval_ms  INT    DEFAULT 1000 COMMENT '步进间隔（毫秒）',
    status       ENUM('COMPLETED','STOPPED','ERROR') DEFAULT 'COMPLETED',
    started_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at  TIMESTAMP NULL,
    FOREIGN KEY (user_id)    REFERENCES users(user_id)      ON DELETE CASCADE,
    FOREIGN KEY (algo_id)    REFERENCES algorithms(algo_id) ON DELETE RESTRICT,
    INDEX idx_exp_user_time (user_id, started_at),
    INDEX idx_exp_algo_status (algo_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学实验摘要';

-- ----------------------------
-- 5. 实验步骤快照表（可选保存）
-- ----------------------------
CREATE TABLE experiment_steps (
    step_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    exp_id         BIGINT NOT NULL,
    step_number    INT    NOT NULL,
    data_json      JSON   NOT NULL COMMENT '数组快照',
    highlight_json JSON   COMMENT '高亮信息',
    description    VARCHAR(200),
    FOREIGN KEY (exp_id) REFERENCES teaching_experiments(exp_id) ON DELETE CASCADE,
    INDEX idx_step_exp (exp_id, step_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学实验步骤快照（可选）';

-- ----------------------------
-- 6. 性能测试批次主表
-- ----------------------------
CREATE TABLE performance_batches (
    batch_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(32) NOT NULL,
    data_size    INT    NOT NULL,
    distribution VARCHAR(30),
    data_type    ENUM('INTEGER','DOUBLE','PERSON') DEFAULT 'INTEGER',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_batch_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='性能测试批次主表';

-- ----------------------------
-- 7. 批次明细表（一个批次多个算法）
-- ----------------------------
CREATE TABLE batch_details (
    detail_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id     BIGINT NOT NULL,
    algo_id      BIGINT NOT NULL,
    comparisons  INT    DEFAULT 0,
    swaps        INT    DEFAULT 0,
    time_micros  BIGINT DEFAULT 0,
    `rank`       INT    COMMENT '批次内排名（按耗时升序）',
    FOREIGN KEY (batch_id) REFERENCES performance_batches(batch_id) ON DELETE CASCADE,
    FOREIGN KEY (algo_id)  REFERENCES algorithms(algo_id)         ON DELETE RESTRICT,
    INDEX idx_detail_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次明细';

-- ----------------------------
-- 8. 算法统计表（触发器自动维护）
-- ----------------------------
CREATE TABLE algorithm_stats (
    stat_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    algo_id               BIGINT NOT NULL UNIQUE,
    total_experiments     INT DEFAULT 0 COMMENT '教学实验次数',
    avg_exp_comparisons   DECIMAL(12,2) DEFAULT 0 COMMENT '教学实验平均比较次数',
    avg_exp_swaps         DECIMAL(12,2) DEFAULT 0 COMMENT '教学实验平均交换次数',
    avg_exp_time_micros   DECIMAL(12,2) DEFAULT 0 COMMENT '教学实验平均耗时(µs)',
    total_batches         INT DEFAULT 0 COMMENT '性能测试批次数',
    avg_batch_comparisons DECIMAL(12,2) DEFAULT 0 COMMENT '性能测试平均比较次数',
    avg_batch_swaps       DECIMAL(12,2) DEFAULT 0 COMMENT '性能测试平均交换次数',
    avg_batch_time_micros DECIMAL(12,2) DEFAULT 0 COMMENT '性能测试平均耗时(µs)',
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (algo_id) REFERENCES algorithms(algo_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='算法统计表（触发器维护）';
