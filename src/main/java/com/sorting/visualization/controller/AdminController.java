package com.sorting.visualization.controller;

import com.sorting.visualization.entity.AlgorithmStat;
import com.sorting.visualization.mapper.AlgorithmStatMapper;
import com.sorting.visualization.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BackupService backupService;
    private final AlgorithmStatMapper algorithmStatMapper;

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

    /** 生成用户实验报告（存储过程） */
    @GetMapping("/report")
    public List<Map<String, Object>> getReport(@RequestParam String userId) {
        return algorithmStatMapper.callUserReport(userId);
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
