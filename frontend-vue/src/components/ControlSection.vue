<template>
    <section class="control-section">
        <h3><i class="fas fa-play-circle"></i> 控制</h3>
        <div class="control-buttons">
            <button class="btn success-btn" @click="startSort"
                :disabled="algorithmStore.isSorting || !dataStore.rawData.length">
                <i class="fas fa-play"></i> 开始排序
            </button>
            <button v-if="algorithmStore.isSorting && !uiStore.isPaused" class="btn warning-btn" @click="pauseSort">
                <i class="fas fa-pause"></i> 暂停
            </button>
            <button v-if="uiStore.isPaused" class="btn info-btn" @click="stepForward">
                <i class="fas fa-step-forward"></i> 单步
            </button>
            <button v-if="uiStore.isPaused" class="btn success-btn" @click="resumeSort">
                <i class="fas fa-play"></i> 继续
            </button>
            <button v-if="algorithmStore.isSorting" class="btn danger-btn" @click="stopSort">
                <i class="fas fa-stop"></i> 停止
            </button>
            <button v-if="!algorithmStore.isSorting" class="btn danger-btn" @click="resetSort">
                <i class="fas fa-redo"></i> 重置
            </button>
        </div>
        <div class="interval-control">
            <label>步进间隔</label>
            <input type="range" v-model.number="interval" min="100" max="2000" step="100"
                :disabled="algorithmStore.isSorting && !uiStore.isPaused" />
            <span>{{ (interval / 1000).toFixed(1) }}s</span>
        </div>
        <div class="save-replay" v-if="uiStore.currentMode === 'teaching'">
            <label class="checkbox-label">
                <input type="checkbox" v-model="saveReplay" /> 保存回放（步骤快照）
            </label>
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
const { sendSortRequest, sendControl } = useWebSocket()
const interval = ref(1000)
const saveReplay = ref(false)

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
        interval: interval.value,
        dataType: dataStore.dataType.toUpperCase(),
        distribution: 'RANDOM',
        ascending: comparatorStore.direction === 'ascending',
        saveReplay: saveReplay.value,
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

const pauseSort = () => {
    Utils.logMessage('暂停排序', 'info')
    sendControl('PAUSE')
}

const resumeSort = () => {
    Utils.logMessage('继续排序', 'info')
    sendControl('RESUME', { interval: interval.value })
}

const stopSort = () => {
    Utils.logMessage('停止排序', 'info')
    sendControl('STOP')
}

const stepForward = () => {
    Utils.logMessage('单步执行', 'info')
    sendControl('STEP_FORWARD')
}

const resetSort = () => {
    Utils.logMessage('排序已重置', 'info')
    algorithmStore.resetSort()
    dataStore.updateDisplayData(dataStore.rawData)
    uiStore.setPaused(false)
    uiStore.hideLoading()
}
</script>

<style scoped>
/* Control section styles can be added here if needed */
</style>