package com.sorting.visualization.controller;

import com.sorting.visualization.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BackupService backupService;

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
}
