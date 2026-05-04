<template>
    <section class="control-section">
        <h3><i class="fas fa-play-circle"></i> 控制</h3>
        <div class="control-buttons">
            <button class="btn success-btn" @click="startSort"
                :disabled="algorithmStore.isSorting || !dataStore.rawData.length">
                <i class="fas fa-play"></i> 开始排序
            </button>
            <button class="btn danger-btn" @click="resetSort">
                <i class="fas fa-redo"></i> 重置
            </button>
        </div>
        <div class="speed-control">
            <label>动画速度</label>
            <input type="range" v-model.number="speed" min="100" max="2000" step="100" />
            <span>{{ (speed / 1000).toFixed(1) }}s</span>
        </div>
    </section>
</template>

<script setup>
import { ref } from 'vue'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'
import { useComparatorStore } from '../stores/comparator'
import { useWebSocket } from '../composables/useWebSocket'
import { useUiStore } from '../stores/ui'
import { Utils } from '../utils/helpers'

const uiStore = useUiStore()
const algorithmStore = useAlgorithmStore()
const dataStore = useDataStore()
const comparatorStore = useComparatorStore()
const { sendSortRequest } = useWebSocket()
const speed = ref(1000)

const startSort = () => {
    Utils.logMessage(`开始排序: ${algorithmStore.currentAlgorithm}`, 'info')
    if (!dataStore.rawData.length) {
        uiStore.showErrorModal('请先生成或上传数据')
        return
    }
    if (dataStore.rawData.length > 100) {
        uiStore.showErrorModal('教学模式最多支持100个数据')
        return
    }
    const request = {
        mode: 'TEACHING',
        algorithm: algorithmStore.currentAlgorithm.toUpperCase(),
        data: dataStore.rawData,
        interval: speed.value,
        dataType: dataStore.dataType.toUpperCase(),
        distribution: 'RANDOM',
        ascending: comparatorStore.direction === 'ascending',
        comparatorInfo: {
            direction: comparatorStore.direction,
            method: comparatorStore.method,
            description: comparatorStore.comparatorDescription,
            structField: dataStore.dataType === 'Person' ? comparatorStore.structField : undefined
        }
    }
    sendSortRequest(request)
    algorithmStore.startSort()
}

const resetSort = () => {
    Utils.logMessage('排序已重置', 'info')
    algorithmStore.resetSort()
    dataStore.updateDisplayData(dataStore.rawData) // 恢复原始数据
}
</script>

<style scoped>
/* Control section styles can be added here if needed */
</style>