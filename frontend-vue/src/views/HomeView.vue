<template>
    <div class="container">
        <!-- 头部 -->
        <Header />
        <!-- 主要内容 -->
        <div class="main-content">
            <!-- 左侧控制面板 -->
            <div class="control-panel">
                <!-- 模式选择 -->
                <ModeSelector />
                <!-- 排序算法选择 -->
                <AlgorithmSelector />
                <!-- 数据输入 -->
                <DataInput />
                <!-- 排序选项 -->
                <SortOptions />
                <!-- 连接状态 -->
                <ConnectionStatus />
                <!-- 控制选项 -->
                <ControlSection />
                <!-- 统计信息 -->
                <StatsPanel />
            </div>

            <!-- 中间可视化区 -->
            <div class="visualization-area">
                <!-- 教学模式 -->
                <BarChart v-if="uiStore.currentMode === 'teaching'" />
                <!-- 性能模式 -->
                <PerformanceView v-else />
            </div>

            <!-- 右侧信息面板 -->
            <div class="info-panel">
                <!-- 算法伪代码 -->
                <PseudoCode />
                <!-- 算法信息 -->
                <AlgorithmInfo />
                <!-- 系统日志 -->
                <SystemLog />
            </div>
        </div>
        <!-- 页脚 -->
        <Footer />

        <Modal />
        <LoadingOverlay />
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUiStore } from '../stores/ui'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'
import { useComparatorStore } from '../stores/comparator'
import { useWebSocket } from '../composables/useWebSocket'
import { useAuthStore } from '../stores/auth'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import ControlSection from '../components/ControlSection.vue'
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

const uiStore = useUiStore()
const { connect } = useWebSocket()
const algorithmStore = useAlgorithmStore()
const dataStore = useDataStore()
const comparatorStore = useComparatorStore()
const authStore = useAuthStore()

onMounted(() => {
    algorithmStore.updatePseudocode()
    connect(`ws://${window.location.host}/websocket`, authStore.token)
})
</script>