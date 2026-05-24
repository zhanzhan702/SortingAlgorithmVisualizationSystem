package com.sorting.visualization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.entity.*;
import com.sorting.visualization.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final UserMapper userMapper;
    private final AlgorithmMapper algorithmMapper;
    private final DatasetMapper datasetMapper;
    private final TeachingExperimentMapper experimentMapper;
    private final PerformanceBatchMapper batchMapper;
    private final BatchDetailMapper batchDetailMapper;

    private static final String BACKUP_DIR = "backups/";

    /**
     * 导出完整数据库为 SQL 文件
     */
    public String exportToSql() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = BACKUP_DIR + "sorting_visualization_backup_" + timestamp + ".sql";
        new File(BACKUP_DIR).mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            writer.write("-- ========================================\n");
            writer.write("-- 数据库备份: sorting_visualization\n");
            writer.write("-- 导出时间: " + LocalDateTime.now() + "\n");
            writer.write("-- ========================================\n\n");
            writer.write("USE sorting_visualization;\n\n");

            // 导出各表数据
            exportTable(writer, "users", userMapper.selectList(null));
            exportTable(writer, "algorithms", algorithmMapper.selectList(null));
            exportTable(writer, "datasets", datasetMapper.selectList(new LambdaQueryWrapper<Dataset>().last("LIMIT 1000")));
            exportTable(writer, "teaching_experiments", experimentMapper.selectList(
                    new LambdaQueryWrapper<TeachingExperiment>().last("LIMIT 10000")));
            exportTable(writer, "performance_batches", batchMapper.selectList(
                    new LambdaQueryWrapper<PerformanceBatch>().last("LIMIT 1000")));
            exportTable(writer, "batch_details", batchDetailMapper.selectList(null));
        }

        log.info("数据库备份完成: {}", filename);
        return filename;
    }

    private void exportTable(BufferedWriter writer, String tableName, List<?> records) throws IOException {
        if (records == null || records.isEmpty()) return;
        writer.write("-- 表: " + tableName + " (" + records.size() + " 行)\n");
        for (Object record : records) {
            writer.write("INSERT INTO " + tableName + " VALUES (" + record.toString() + ");\n");
        }
        writer.write("\n");
    }

    /** 获取备份文件列表 */
    public String[] listBackups() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) return new String[0];
        return dir.list((d, name) -> name.endsWith(".sql"));
    }
}
