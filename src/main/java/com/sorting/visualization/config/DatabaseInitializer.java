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
