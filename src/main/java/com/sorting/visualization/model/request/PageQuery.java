package com.sorting.visualization.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数。
 *
 * <p>原实现直接接收 {@code page} / {@code size} 且无上下限，{@code size=100000}
 * 会触发一次大查询。这里的上限与 {@code ExperimentService} 的分页语义保持一致。</p>
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "page 必须大于等于 1")
    private int page = 1;

    @Min(value = 1, message = "size 必须大于等于 1")
    @Max(value = 100, message = "size 不能超过 100")
    private int size = 20;
}
