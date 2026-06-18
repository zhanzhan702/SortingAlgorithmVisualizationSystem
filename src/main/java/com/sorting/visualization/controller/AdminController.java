package com.sorting.visualization.controller;

import com.sorting.visualization.entity.AlgorithmStat;
import com.sorting.visualization.mapper.AlgorithmStatMapper;
import com.sorting.visualization.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BackupService backupService;
    private final AlgorithmStatMapper algorithmStatMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 获取算法统计数据（触发器自动维护） */
    @GetMapping("/stats")
    public List<AlgorithmStat> getStats() {
        return algorithmStatMapper.selectList(null);
    }

    /** 获取算法综合排名（视图） */
    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        return algorithmStatMapper.selectRanking();
    }

    /** 获取用户活跃度（视图） */
    @GetMapping("/activity")
    public List<Map<String, Object>> getActivity() {
        return algorithmStatMapper.selectUserActivity();
    }

    /** 生成用户实验报告（存储过程，返回3个结果集） */
    @GetMapping("/report")
    public List<List<Map<String, Object>>> getReport(@RequestParam String userId) {
        List<List<Map<String, Object>>> allResults = new ArrayList<>();
        jdbcTemplate.execute((ConnectionCallback<Void>) con -> {
            var cs = con.prepareCall("{CALL sp_user_report(?)}");
            cs.setString(1, userId);
            boolean hasResults = cs.execute();
            // 遍历所有结果集
            while (true) {
                if (hasResults) {
                    var rs = cs.getResultSet();
                    var columns = rs.getMetaData().getColumnCount();
                    var rows = new ArrayList<Map<String, Object>>();
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columns; i++) {
                            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                        }
                        rows.add(row);
                    }
                    allResults.add(rows);
                }
                if (cs.getMoreResults()) {
                    hasResults = true;
                } else if (cs.getUpdateCount() == -1) {
                    break;
                } else {
                    hasResults = false;
                }
            }
            return null;
        });
        return allResults;
    }

    /** 触发备份 */
    @PostMapping("/backup")
    public Map<String, Object> backup() {
        try {
            String filename = backupService.exportToSql();
            return Map.of("success", true, "filename", filename);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /** 获取备份列表 */
    @GetMapping("/backups")
    public Map<String, Object> listBackups() {
        return Map.of("files", backupService.listBackups());
    }

    /** 清空实验数据（保留用户和算法） */
    @PostMapping("/cleanup")
    public Map<String, Object> cleanup() {
        try {
            backupService.truncateExperimentData();
            return Map.of("success", true, "message", "实验数据已清空，users和algorithms保留");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
