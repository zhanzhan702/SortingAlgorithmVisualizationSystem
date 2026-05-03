<template>
    <section class="control-section" id="stats-section">
        <h3><i class="fas fa-chart-line"></i> 统计信息</h3>
        <div class="stats-display">
            <div class="stat-item">
                <span class="stat-label">比较次数:</span>
                <span class="stat-value">{{ algorithmStore.stats.comparisons }}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">交换次数:</span>
                <span class="stat-value">{{ algorithmStore.stats.swaps }}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">运行时间:</span>
                <span class="stat-value">{{ algorithmStore.stats.time }}ms</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">当前步骤:</span>
                <span class="stat-value">{{ algorithmStore.stats.step }}/{{ algorithmStore.stats.totalSteps }}</span>
            </div>
        </div>
    </section>
</template>

<script setup>
import { useAlgorithmStore } from '../../stores/algorithm'

const algorithmStore = useAlgorithmStore()
</script>