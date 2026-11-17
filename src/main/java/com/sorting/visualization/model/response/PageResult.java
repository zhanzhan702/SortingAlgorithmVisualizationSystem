package com.sorting.visualization.model.response;

import lombok.Data;

import java.util.List;

/**
 * W4：统一分页响应结构。
 *
 * <p>字段与原有 HistoryController 返回的 records / total / page 保持兼容，
 * 额外补充 size 与 pages（总页数），便于前端直接渲染分页条。</p>
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private long page;
    /** 每页条数 */
    private long size;
    /** 总页数 */
    private long pages;

    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }
}
