<template>
    <div class="main-visualization">
        <div class="visualization-header">
            <h3><i class="fas fa-tachometer-alt"></i> 性能对比测试</h3>
        </div>
        <div class="performance-container">
            <!-- 图表与表格共用滚动区域 -->
            <div class="performance-chart-section" ref="chartSection">
                <canvas ref="chartCanvas" class="performance-canvas"></canvas>
                <div v-if="performanceStore.results.length > 0" class="performance-table-container"
                    ref="tableContainer">
                    <table class="performance-table">
                        <thead>
                            <tr>
                                <th>算法</th>
                                <th>时间(µs)</th>
                                <th>比较次数</th>
                                <th>交换次数</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="algo in performanceStore.results" :key="algo.algorithm">
                                <td>{{ algo.algorithm }}</td>
                                <td>{{ algo.time }}</td>
                                <td>{{ algo.comparisons }}</td>
                                <td>{{ algo.swaps }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- 控制面板 （保持不变） -->
            <div class="performance-controls">
                <div class="performance-tabs">
                    <button :class="['performance-tab', { active: activeTab === 'generate' }]"
                        @click="activeTab = 'generate'">
                        生成数据
                    </button>
                    <button :class="['performance-tab', { active: activeTab === 'file' }]" @click="activeTab = 'file'">
                        导入文件
                    </button>
                </div>
                <div v-show="activeTab === 'generate'" class="performance-panel">
                    <div class="form-group">
                        <label>测试数据规模</label>
                        <select v-model="testSize">
                            <option value="100">100</option>
                            <option value="200">200</option>
                            <option value="500">500</option>
                            <option value="1000">1000</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>数据分布</label>
                        <select v-model="testDistribution">
                            <option value="random">随机数据</option>
                            <option value="sorted">有序数据</option>
                            <option value="reverse">逆序数据</option>
                            <option value="duplicate">重复数据</option>
                        </select>
                    </div>
                    <button class="btn primary-btn" @click="runTestFromGenerate">运行测试</button>
                </div>
                <div v-show="activeTab === 'file'" class="performance-panel">
                    <div class="form-group">
                        <label>选择文件</label>
                        <input type="file" accept=".txt" @change="onFileSelect" />
                        <div class="file-info compact" v-if="selectedFile">
                            <div>{{ selectedFile.name }}</div>
                            <div>{{ (selectedFile.size / 1024).toFixed(1) }} KB</div>
                        </div>
                    </div>
                    <button class="btn primary-btn" @click="parsePerformanceFile" :disabled="!selectedFile">
                        导入并测试
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { usePerformanceStore } from '../../stores/performance'
import { useWebSocket } from '../../composables/useWebSocket'
import { useUiStore } from '../../stores/ui'
import { Utils } from '../../utils/helpers'
import DataGenerator from '../../utils/dataGenerator'
import Chart from 'chart.js/auto'

const performanceStore = usePerformanceStore()
const { sendSortRequest, isConnected } = useWebSocket()
const uiStore = useUiStore()

const activeTab = ref('generate')
const testSize = ref('100')
const testDistribution = ref('random')
const selectedFile = ref(null)
let chart = null
const chartCanvas = ref(null)
const chartSection = ref(null)
const tableContainer = ref(null)

// 更新图表（增量更新，不再 destroy + recreate）
function updateChart() {
    if (!chartCanvas.value) return
    const labels = performanceStore.results.map(r => r.algorithm)
    const times = performanceStore.results.map(r => r.time)
    if (chart) {
        // 增量更新：只改 data，不重建 Chart 实例
        chart.data.labels = labels
        chart.data.datasets[0].data = times
        chart.update()
    } else {
        const ctx = chartCanvas.value.getContext('2d')
        chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: '运行时间 (µs)',
                    data: times,
                    backgroundColor: 'rgba(52,152,219,0.7)',
                    borderColor: 'rgb(52,152,219)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'top' } }
            }
        })
    }
}

watch(() => performanceStore.results, updateChart, { deep: true })

let currentTableElement = null
function handleTableWheel(e) {
    if (!chartSection.value) return
    if (Math.abs(e.deltaY) > 0) {
        chartSection.value.scrollTop += e.deltaY
        e.preventDefault()
    }
}

watch(tableContainer, (newEl, oldEl) => {
    if (oldEl) oldEl.removeEventListener('wheel', handleTableWheel)
    if (newEl) newEl.addEventListener('wheel', handleTableWheel, { passive: false })
    currentTableElement = newEl
})

onUnmounted(() => {
    if (currentTableElement) {
        currentTableElement.removeEventListener('wheel', handleTableWheel)
    }
})

// 运行测试（生成数据）
function runTestFromGenerate() {
    const size = parseInt(testSize.value)
    if (size > 1000) {
        uiStore.showErrorModal('性能模式最多支持1000个数据')
        return
    }

    // 先检查连接
    if (!isConnected.value) {
        uiStore.showErrorModal('请先连接到服务器')
        return
    }

    Utils.logMessage('开始性能测试（生成数据）', 'info')
    uiStore.showLoading('正在运行性能测试...')
    const testData = DataGenerator.generateData(
        size,
        'int',
        testDistribution.value,
        1,
        1000
    )
    runAllAlgorithms(testData, testDistribution.value)
}

// 文件导入后运行测试
function parsePerformanceFile() {
    if (!selectedFile.value) {
        uiStore.showErrorModal('请先选择文件')
        return
    }
    if (selectedFile.value.size > 1024 * 1024) { // 1MB 限制
        uiStore.showErrorModal('文件大小不能超过1MB')
        return
    }

    uiStore.showLoading('正在解析文件...')
    const reader = new FileReader()
    reader.onload = (e) => {
        const content = e.target.result
        const lines = content.split(/\r?\n/).filter(l => l.trim())
        const numbers = []
        for (let line of lines) {
            const num = parseFloat(line.trim())
            if (!isNaN(num)) numbers.push(num)
        }
        if (numbers.length < 10) {
            uiStore.showErrorModal('数据量不足（至少10个）')
            uiStore.hideLoading()
            return
        }
        if (numbers.length > 1000) {
            uiStore.showErrorModal('数据量超过1000，请使用更小的文件')
            uiStore.hideLoading()
            return
        }
        // 解析成功，运行性能测试
        runAllAlgorithms(numbers, 'custom')
    }
    reader.onerror = () => {
        uiStore.showErrorModal('文件读取失败')
        uiStore.hideLoading()
    }
    reader.readAsText(selectedFile.value)
}

function onFileSelect(e) {
    selectedFile.value = e.target.files[0]
}

// 依次请求6个算法
const algorithms = ['insertion', 'shell', 'bubble', 'quick', 'heap', 'merge']
let pendingQueue = []      // 待发送的算法队列
let currentTestData = null
let currentDistribution = 'random'
let isRunning = false
// 监听性能结果，触发下一个请求
watch(() => performanceStore.results.length, (newLen, oldLen) => {
    if (isRunning && newLen > oldLen && pendingQueue.length > 0) {
        sendNext()
    } else if (isRunning && pendingQueue.length === 0) {
        // 全部完成
        isRunning = false
        uiStore.hideLoading()
    }
})

function runAllAlgorithms(testData, distribution) {
    if (!isConnected.value) {
        uiStore.showErrorModal('请先连接到服务器')
        uiStore.hideLoading()
        return
    }
    performanceStore.clear()
    pendingQueue = [...algorithms]
    currentTestData = testData
    currentDistribution = distribution
    isRunning = true
    uiStore.showLoading('正在运行性能测试...')
    sendNext()
}

function sendNext() {
    if (!isConnected.value) {
        uiStore.showErrorModal('连接已断开')
        uiStore.hideLoading()
        isRunning = false
        pendingQueue = []
        return
    }
    if (pendingQueue.length === 0) return

    const algo = pendingQueue.shift()
    const request = {
        mode: 'PERFORMANCE',
        algorithm: algo.toUpperCase(),
        data: currentTestData,
        dataType: 'INT',
        distribution: currentDistribution.toUpperCase()
    }
    Utils.logMessage(`请求算法: ${algo}`, 'info')
    sendSortRequest(request)
    // 不在这里立即发送下一个，等 PERFORMANCE_RESULT 触发 watch
}
</script>