package com.sorting.visualization.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sorting.visualization.entity.Dataset;
import com.sorting.visualization.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    /** 保存数据集 */
    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        Long id = datasetService.saveDataset(
                (String) body.get("name"),
                (String) body.get("dataType"),
                (Integer) body.get("dataSize"),
                (String) body.get("distribution"),
                (String) body.get("dataJson"),
                Long.valueOf(body.get("userId").toString()));
        return Map.of("success", true, "datasetId", id);
    }

    /** 查询用户数据集 */
    @GetMapping
    public Map<String, Object> list(@RequestParam Long userId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        Page<Dataset> result = datasetService.getUserDatasets(userId, page, size);
        return Map.of("records", result.getRecords(), "total", result.getTotal());
    }

    /** 删除数据集 */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        datasetService.deleteDataset(id);
        return Map.of("success", true);
    }
}
