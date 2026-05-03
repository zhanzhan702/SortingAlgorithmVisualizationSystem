<template>
    <div class="container">
        <header class="header">
            <h1>
                <span><i class="fas fa-sort-amount-down"></i> 排序算法可视化系统</span>
                <span><i class="fas fa-balance-scale"></i> 制作团队：六序天平</span>
            </h1>
            <p class="subtitle">可视化演示多种排序算法的执行过程与性能对比</p>
        </header>

        <div class="main-content">
            <!-- 左侧控制面板 -->
            <div class="control-panel">
                <ModeSelector />
                <AlgorithmSelector />
                <DataInput />
                <SortOptions />
                <ConnectionStatus />

                <!-- 控制按钮（新增） -->
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

                <StatsPanel />
            </div>

            <!-- 中间可视化区 -->
            <div class="visualization-area">
                <BarChart v-if="uiStore.currentMode === 'teaching'" />
                <PerformanceView v-else />
            </div>

            <!-- 右侧信息面板 -->
            <div class="info-panel">
                <PseudoCode />
                <AlgorithmInfo />
                <SystemLog />
            </div>
        </div>

        <footer class="footer">
            <div class="footer-content">
                <div class="footer-item">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span>注意：教学模式最多支持100个数据 | 性能模式最多支持1000个数据</span>
                </div>
                <div class="footer-item">
                    <i class="fas fa-cog"></i>
                    <span>教学模式：展示排序过程 | 性能模式：对比算法效率</span>
                </div>
                <div class="footer-item">
                    <i class="fas fa-code"></i>
                    <span>后端：Spring Boot + WebSocket | 前端：Vue 3 + SVG</span>
                </div>
            </div>
        </footer>

        <Modal />
        <LoadingOverlay />
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { usePerformanceStore } from '../stores/performance'
import { useUiStore } from '../stores/ui'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'
import { useComparatorStore } from '../stores/comparator'
import { useWebSocket } from '../composables/useWebSocket'
import ModeSelector from '../components/ControlPanel/ModeSelector.vue'
import AlgorithmSelector from '../components/ControlPanel/AlgorithmSelector.vue'
import DataInput from '../components/ControlPanel/DataInput.vue'
import SortOptions from '../components/ControlPanel/SortOptions.vue'
import ConnectionStatus from '../components/ControlPanel/ConnectionStatus.vue'
import StatsPanel from '../components/ControlPanel/StatsPanel.vue'
import BarChart from '../components/Visualization/BarChart.vue'
import PerformanceView from '../components/Visualization/PerformanceView.vue'
import PseudoCode from '../components/InfoPanel/PseudoCode.vue'
import AlgorithmInfo from '../components/InfoPanel/AlgorithmInfo.vue'
import SystemLog from '../components/InfoPanel/SystemLog.vue'
import Modal from '../components/Common/Modal.vue'
import LoadingOverlay from '../components/Common/LoadingOverlay.vue'

const performanceStore = usePerformanceStore()
const uiStore = useUiStore()
const algorithmStore = useAlgorithmStore()
const dataStore = useDataStore()
const comparatorStore = useComparatorStore()
const { connect, sendSortRequest } = useWebSocket()
const speed = ref(1000)

const startSort = () => {
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
    algorithmStore.resetSort()
    dataStore.updateDisplayData(dataStore.rawData)
}

watch(() => uiStore.currentMode, (newMode) => {
    if (newMode === 'teaching') {
        performanceStore.clear()
    }
})

onMounted(() => {
    connect('ws://localhost:8080/websocket')
})
</script>