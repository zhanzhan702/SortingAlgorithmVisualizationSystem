<template>
    <div class="main-visualization">
        <div class="visualization-header">
            <h3><i class="fas fa-chalkboard-teacher"></i> 排序过程可视化</h3>
            <div class="color-legend">
                <div class="legend-item"><span class="legend-color normal"></span><span>正常</span></div>
                <div class="legend-item"><span class="legend-color comparing"></span><span>比较中</span></div>
                <div class="legend-item"><span class="legend-color swapping"></span><span>交换中</span></div>
                <div class="legend-item"><span class="legend-color pivot"></span><span>基准元素</span></div>
                <div class="legend-item"><span class="legend-color heap"></span><span>建堆中</span></div>
                <div class="legend-item"><span class="legend-color sorted"></span><span>已排序</span></div>
            </div>
        </div>
        <div class="visualization-container">
            <svg id="sort-visualization" width="100%" height="100%"></svg>
            <div v-if="dataStore.rawData.length === 0" class="no-data-message">
                <i class="fas fa-chart-bar fa-3x"></i>
                <p>请先生成或上传数据</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useDataStore } from '../../stores/data'
import { useComparatorStore } from '../../stores/comparator'
import { useVisualizer } from '../../composables/useVisualizer'

const dataStore = useDataStore()
const comparatorStore = useComparatorStore()
const { update, resize } = useVisualizer('sort-visualization')

// 监听数据变化（原始数据或显示数据）
watch(() => dataStore.displayData, (newData) => {
    if (newData && newData.length) update(newData, dataStore.highlight || {})
}, { deep: true })

// 同样监听 highlight 变化
watch(() => dataStore.highlight, () => {
    if (dataStore.displayData.length) {
        update(dataStore.displayData, dataStore.highlight || {})
    }
}, { deep: true })

// 监听排序字段变化（仅对Person数据有用）
watch(() => comparatorStore.structField, () => {
    if (dataStore.displayData.length) update(dataStore.displayData)
})

onMounted(() => {
    if (dataStore.displayData.length) update(dataStore.displayData)
    window.addEventListener('resize', resize)
})

onUnmounted(() => {
    window.removeEventListener('resize', resize)
})
</script>