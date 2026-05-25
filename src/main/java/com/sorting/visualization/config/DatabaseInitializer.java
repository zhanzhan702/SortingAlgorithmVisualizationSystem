package com.sorting.visualization.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 启动时自动执行 procedures.sql，确保触发器/视图/存储过程始终存在
 */
@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        migrateUserIdToUuid();
        executeProcedures();
    }

    /** 自动迁移 user_id BIGINT → VARCHAR(32) UUID */
    private void migrateUserIdToUuid() {
        try {
            // 检查是否需要迁移（user_id 是否为 BIGINT 类型）
            String colType = jdbcTemplate.queryForObject(
                "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA='sorting_visualization' AND TABLE_NAME='users' AND COLUMN_NAME='user_id'",
                String.class);
            if (!"bigint".equalsIgnoreCase(colType)) {
                log.info("user_id 已是 VARCHAR 类型，跳过迁移");
                return;
            }
            log.info("检测到 BIGINT user_id，执行UUID迁移...");

            // 1. 删外键（MySQL 8.0 不支持 IF EXISTS，逐条尝试）
            try { jdbcTemplate.execute("ALTER TABLE teaching_experiments DROP FOREIGN KEY teaching_experiments_ibfk_1"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_batches DROP FOREIGN KEY performance_batches_ibfk_1"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE algorithm_stats DROP FOREIGN KEY algorithm_stats_ibfk_1"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE datasets DROP FOREIGN KEY datasets_ibfk_1"); } catch (Exception ignored) {}

            // 2. 先改所有外键列类型为 VARCHAR(32)
            jdbcTemplate.execute("ALTER TABLE teaching_experiments MODIFY user_id VARCHAR(32) NOT NULL");
            jdbcTemplate.execute("ALTER TABLE performance_batches MODIFY user_id VARCHAR(32) NOT NULL");
            try { jdbcTemplate.execute("ALTER TABLE datasets MODIFY creator_id VARCHAR(32)"); } catch (Exception ignored) {}

            // 3. 创建映射表并更新FK值
            jdbcTemplate.execute("CREATE TEMPORARY TABLE IF NOT EXISTS user_id_map AS " +
                "SELECT user_id AS old_id, REPLACE(UUID(),'-','') AS new_id FROM users");

            jdbcTemplate.execute("UPDATE teaching_experiments te INNER JOIN user_id_map m ON te.user_id=m.old_id SET te.user_id=m.new_id");
            jdbcTemplate.execute("UPDATE performance_batches pb INNER JOIN user_id_map m ON pb.user_id=m.old_id SET pb.user_id=m.new_id");

            // 4. 改 users 主键列类型并更新值
            jdbcTemplate.execute("ALTER TABLE users MODIFY user_id VARCHAR(32) NOT NULL");
            jdbcTemplate.execute("UPDATE users u INNER JOIN user_id_map m ON u.user_id=m.old_id SET u.user_id=m.new_id");

            // 6. 重建外键
            jdbcTemplate.execute("ALTER TABLE teaching_experiments ADD FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE");
            jdbcTemplate.execute("ALTER TABLE performance_batches ADD FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE");

            // 7. 清理
            jdbcTemplate.execute("DROP TEMPORARY TABLE IF EXISTS user_id_map");

            log.info("✅ UUID迁移完成");
        } catch (Exception e) {
            log.warn("UUID迁移跳过（可能已迁移或首次启动）: {}", e.getMessage());
        }
    }

    /** 执行 procedures.sql 创建触发器/视图/存储过程 */
    private void executeProcedures() {
        try {
            var resource = new ClassPathResource("db/procedures.sql");
            String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 移除 USE 语句和注释头
            sql = sql.replaceAll("(?i)USE\\s+sorting_visualization\\s*;", "");

            // 按 "DROP TRIGGER/VIEW/PROCEDURE" 分割为独立块
            String[] blocks = sql.split("\\n(?=DROP\\s+(TRIGGER|VIEW|PROCEDURE))");
            for (String block : blocks) {
                block = block.trim();
                if (block.isEmpty() || block.startsWith("--")) continue;

                if (block.contains("DELIMITER //")) {
                    // 触发器/存储过程：提取 BEGIN...END 之间的完整语句
                    String clean = block
                        .replace("DELIMITER //", "")
                        .replace("DELIMITER ;", "");
                    // 提取 DROP + CREATE 语句体（包括 BEGIN...END 内的分号）
                    // 策略：找到第一个 DROP，执行它；找到 CREATE...END，整体执行
                    String[] stmts = clean.split(";(?=\\s*(DROP|CREATE)\\s)");
                    for (String stmt : stmts) {
                        stmt = stmt.trim().replace("//", "").trim();
                        if (!stmt.isEmpty()) {
                            try {
                                jdbcTemplate.execute(stmt);
                                String preview = stmt.substring(0, Math.min(60, stmt.length())).replace('\n', ' ');
                                log.info("已执行: {}...", preview);
                            } catch (Exception e) {
                                log.warn("跳过: {}", e.getMessage().substring(0, Math.min(100, e.getMessage().length())));
                            }
                        }
                    }
                } else {
                    // 普通视图 SQL
                    String[] stmts = block.split(";");
                    for (String stmt : stmts) {
                        stmt = stmt.trim();
                        if (!stmt.isEmpty() && !stmt.startsWith("--")) {
                            try { jdbcTemplate.execute(stmt); } catch (Exception e) { log.debug("跳过视图: {}", e.getMessage().substring(0, Math.min(60, e.getMessage().length()))); }
                        }
                    }
                }
            }
            log.info("✅ 数据库触发器/视图/存储过程初始化完成");
        } catch (Exception e) {
            log.warn("数据库初始化失败（非致命）: {}", e.getMessage());
        }
    }
}
