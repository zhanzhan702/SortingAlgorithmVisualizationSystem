package com.sorting.visualization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final JdbcTemplate jdbcTemplate;
    private static final String BACKUP_DIR = "backups/";

    /** 导出完整数据库为标准 SQL（含列名+正确转义，可直接导入恢复） */
    public String exportToSql() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = BACKUP_DIR + "sorting_visualization_backup_" + timestamp + ".sql";
        new File(BACKUP_DIR).mkdirs();

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            w.write("-- 恢复: mysql -u root -p --default-character-set=utf8mb4 sorting_visualization < " + filename + "\n\n");
            w.write("USE sorting_visualization;\n");
            w.write("SET NAMES utf8mb4;\n");
            w.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");
            for (String t : new String[]{"users", "algorithms", "teaching_experiments",
                    "experiment_steps", "performance_batches", "batch_details", "algorithm_stats"}) {
                exportTable(w, t);
            }
            w.write("SET FOREIGN_KEY_CHECKS = 1;\n");
        }
        log.info("数据库备份完成: {}", filename);
        return filename;
    }

    private void exportTable(BufferedWriter w, String table) throws IOException {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + table);
        // 恢复时先清空表，避免主键冲突
        w.write("TRUNCATE TABLE " + table + ";\n");
        if (rows.isEmpty()) { w.write("-- " + table + ": 0行\n\n"); return; }
        String cols = rows.get(0).keySet().stream()
                .map(c -> "`" + c + "`").collect(Collectors.joining(", "));
        w.write("-- " + table + " (" + rows.size() + "行)\n");
        for (Map<String, Object> row : rows) {
            StringBuilder vals = new StringBuilder();
            for (String col : rows.get(0).keySet()) {
                if (vals.length() > 0) vals.append(", ");
                Object v = row.get(col);
                if (v == null) vals.append("NULL");
                else if (v instanceof Number || v instanceof Boolean) vals.append(v);
                else vals.append("'").append(v.toString().replace("\\","\\\\").replace("'","\\'")).append("'");
            }
            w.write("INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ");\n");
        }
        w.write("\n");
    }

    /** 清空实验数据（保留users和algorithms） */
    public void truncateExperimentData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE experiment_steps");
        jdbcTemplate.execute("TRUNCATE TABLE batch_details");
        jdbcTemplate.execute("TRUNCATE TABLE teaching_experiments");
        jdbcTemplate.execute("TRUNCATE TABLE performance_batches");
        jdbcTemplate.execute("TRUNCATE TABLE algorithm_stats");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        log.info("实验数据已清空（users和algorithms保留）");
    }

    public String[] listBackups() {
        File dir = new File(BACKUP_DIR);
        return dir.exists() ? dir.list((d, n) -> n.endsWith(".sql")) : new String[0];
    }
}
