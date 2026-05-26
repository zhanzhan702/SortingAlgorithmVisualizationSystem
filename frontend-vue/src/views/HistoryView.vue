<template>
  <div class="container">
    <header class="header">
      <h1 @click="$router.push('/')" style="cursor:pointer">
        <i class="fas fa-chart-bar"></i> 排序算法可视化 — 实验历史
      </h1>
      <button class="btn secondary-btn" @click="$router.push('/')" style="width:auto">返回可视化</button>
    </header>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button :class="['tab', { active: activeTab === 'teaching' }]" @click="switchTab('teaching')">教学记录</button>
      <button :class="['tab', { active: activeTab === 'performance' }]" @click="switchTab('performance')">性能记录</button>
      <label v-if="authStore.isTeacher" class="all-toggle" style="margin-left:auto;display:flex;align-items:center;gap:6px;font-size:0.85rem;cursor:pointer">
        <input type="checkbox" v-model="showAll" @change="fetchTeachingData" /> 查看全部用户实验
      </label>
    </div>

    <div class="history-content">
      <!-- 教学记录表格 -->
      <table class="history-table" v-if="activeTab === 'teaching' && historyStore.experiments.length">
        <thead>
          <tr>
            <th>ID</th><th>算法</th><th>数据量</th><th>总步数</th>
            <th>比较次数</th><th>交换次数</th><th>耗时(µs)</th>
            <th>间隔(ms)</th><th>状态</th><th>时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="exp in historyStore.experiments" :key="exp.expId">
            <td>{{ exp.expId }}</td>
            <td>{{ getAlgoName(exp.algoId) }}</td>
            <td>{{ exp.dataSize }}</td>
            <td>{{ exp.totalSteps }}</td>
            <td>{{ exp.comparisons }}</td>
            <td>{{ exp.swaps }}</td>
            <td>{{ exp.timeMicros }}</td>
            <td>{{ exp.intervalMs }}</td>
            <td><span :class="['status-badge', exp.status]">{{ statusLabel(exp.status) }}</span></td>
            <td>{{ formatTime(exp.startedAt) }}</td>
            <td class="action-cell">
              <button class="btn small-btn info-btn" @click="viewDetail(exp)">详情</button>
              <button class="btn small-btn success-btn" @click="replayExp(exp)">回放</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 性能记录表格 -->
      <table class="history-table" v-if="activeTab === 'performance' && perfBatches.length">
        <thead>
          <tr>
            <th>批次ID</th><th>数据量</th><th>分布</th><th>类型</th><th>算法数</th><th>最优算法</th><th>时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="batch in perfBatches" :key="batch.batchId">
            <td>{{ batch.batchId }}</td>
            <td>{{ batch.dataSize }}</td>
            <td>{{ batch.distribution }}</td>
            <td>{{ batch.dataType }}</td>
            <td>{{ batch.detailCount || '-' }}</td>
            <td>{{ batch.bestAlgo || '-' }}</td>
            <td>{{ formatTime(batch.createdAt) }}</td>
            <td class="action-cell">
              <button class="btn small-btn info-btn" @click="viewPerfDetail(batch)">详情</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-if="(activeTab === 'teaching' && historyStore.loading && !historyStore.experiments.length) || (activeTab === 'performance' && loading && !perfBatches.length)">加载中...</p>
      <p v-else-if="activeTab === 'teaching' && !historyStore.experiments.length">暂无教学实验记录</p>
      <p v-else-if="activeTab === 'performance' && !perfBatches.length">暂无性能测试记录</p>

      <!-- 教学记录分页 -->
      <div class="pagination" v-if="activeTab === 'teaching' && historyStore.total > historyStore.pageSize">
        <button :disabled="historyStore.page <= 1" @click="prevPage">上一页</button>
        <span>第 {{ historyStore.page }} 页 / 共 {{ Math.ceil(historyStore.total / historyStore.pageSize) }} 页</span>
        <button :disabled="historyStore.page * historyStore.pageSize >= historyStore.total" @click="nextPage">下一页</button>
      </div>
    </div>

    <!-- 教学详情弹窗 -->
    <div class="hist-modal-overlay" v-if="showDetail" @click.self="showDetail = false">
      <div class="hist-modal">
        <div class="hist-modal-header">
          <h3>教学实验详情 #{{ detailExp?.expId }}</h3>
          <button class="hist-close-btn" @click="showDetail = false">&times;</button>
        </div>
        <div class="hist-modal-body" v-if="detailExp">
          <div class="detail-grid">
            <div><strong>算法：</strong>{{ getAlgoName(detailExp.algoId) }}</div>
            <div><strong>数据量：</strong>{{ detailExp.dataSize }}</div>
            <div><strong>总步数：</strong>{{ detailExp.totalSteps }}</div>
            <div><strong>比较次数：</strong>{{ detailExp.comparisons }}</div>
            <div><strong>交换次数：</strong>{{ detailExp.swaps }}</div>
            <div><strong>耗时：</strong>{{ detailExp.timeMicros }} µs</div>
            <div><strong>步进间隔：</strong>{{ detailExp.intervalMs }} ms</div>
            <div><strong>状态：</strong>{{ statusLabel(detailExp.status) }}</div>
            <div><strong>开始：</strong>{{ formatTime(detailExp.startedAt) }}</div>
            <div><strong>结束：</strong>{{ formatTime(detailExp.finishedAt) }}</div>
          </div>
          <div class="detail-steps" v-if="detailSteps.length">
            <h4>步骤快照 ({{ detailSteps.length }} 步)</h4>
            <div class="steps-scroll">
              <div v-for="step in detailSteps" :key="step.stepNumber" class="step-item">
                <span class="step-num">#{{ step.stepNumber }}</span>
                <span class="step-desc">{{ step.description || '步骤 ' + step.stepNumber }}</span>
                <button class="btn tiny-btn" @click="replayFromStep(step.stepNumber)">回放</button>
              </div>
            </div>
          </div>
          <p v-else-if="detailLoading" style="text-align:center;color:#999;padding:20px">加载步骤中...</p>
          <p v-else style="text-align:center;color:#999;padding:20px">该实验未保存步骤快照（排序时需勾选"保存回放"）</p>
        </div>
      </div>
    </div>

    <!-- 性能详情弹窗 -->
    <div class="hist-modal-overlay" v-if="showPerfDetail" @click.self="showPerfDetail = false">
      <div class="hist-modal">
        <div class="hist-modal-header">
          <h3>性能批次详情 #{{ perfDetailBatch?.batchId }}</h3>
          <button class="hist-close-btn" @click="showPerfDetail = false">&times;</button>
        </div>
        <div class="hist-modal-body" v-if="perfDetailBatch">
          <div class="detail-grid">
            <div><strong>数据量：</strong>{{ perfDetailBatch.dataSize }}</div>
            <div><strong>分布：</strong>{{ perfDetailBatch.distribution }}</div>
            <div><strong>数据类型：</strong>{{ perfDetailBatch.dataType }}</div>
            <div><strong>时间：</strong>{{ formatTime(perfDetailBatch.createdAt) }}</div>
          </div>
          <h4 style="margin-top:12px">算法排名</h4>
          <table class="perf-detail-table" v-if="perfDetails.length">
            <thead>
              <tr><th>排名</th><th>算法</th><th>耗时(µs)</th><th>比较次数</th><th>交换次数</th></tr>
            </thead>
            <tbody>
              <tr v-for="d in perfDetails" :key="d.detail_id || d.rank">
                <td>#{{ d.rank }}</td>
                <td>{{ getAlgoName(d.algo_id) }}</td>
                <td>{{ d.time_micros }}</td>
                <td>{{ d.comparisons }}</td>
                <td>{{ d.swaps }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else style="text-align:center;color:#999;padding:20px">加载明细中...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHistoryStore } from '../stores/history'
import { useAuthStore } from '../stores/auth'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'

const router = useRouter()
const historyStore = useHistoryStore()
const authStore = useAuthStore()
const algorithmStore = useAlgorithmStore()
const dataStore = useDataStore()

const algoIdMap = { 1: '冒泡排序', 2: '快速排序', 3: '直接插入排序', 4: '希尔排序', 5: '堆排序', 6: '归并排序' }
const algoCodeMap = { 1: 'bubble', 2: 'quick', 3: 'insertion', 4: 'shell', 5: 'heap', 6: 'merge' }

const activeTab = ref('teaching')
const loading = ref(false)
const showAll = ref(false)

// --- 教学记录 ---
const showDetail = ref(false)
const detailExp = ref(null)
const detailSteps = ref([])
const detailLoading = ref(false)

// --- 性能记录 ---
const perfBatches = ref([])
const showPerfDetail = ref(false)
const perfDetailBatch = ref(null)
const perfDetails = ref([])

const getAlgoName = (id) => algoIdMap[id] || `算法#${id}`
const statusLabel = (s) => ({ COMPLETED: '已完成', STOPPED: '已停止', ERROR: '异常' }[s] || s)
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''

const fetchTeachingData = () => {
  if (showAll.value) fetchAllExperiments()
  else if (authStore.userId) historyStore.fetchExperiments(authStore.userId)
}

const fetchAllExperiments = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/history/experiments/all?page=1&size=20')
    const data = await res.json()
    historyStore.experiments = data.records || []
    historyStore.total = data.total || 0
    historyStore.page = 1
  } catch(e) { console.error(e) }
  loading.value = false
}

onMounted(() => {
  fetchTeachingData()
  fetchPerfBatches()
})

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'performance') fetchPerfBatches()
  else fetchTeachingData()
}

const prevPage = () => {
  if (showAll.value) return
  historyStore.fetchExperiments(authStore.userId, historyStore.page - 1)
}
const nextPage = () => {
  if (showAll.value) return
  historyStore.fetchExperiments(authStore.userId, historyStore.page + 1)
}

// 教学详情
const viewDetail = async (exp) => {
  detailExp.value = exp; detailSteps.value = []; showDetail.value = true; detailLoading.value = true
  try { detailSteps.value = await historyStore.fetchSteps(exp.expId) || [] }
  finally { detailLoading.value = false }
}

const replayExp = async (exp) => {
  // 加载步骤数据，提取初始数据用于回放
  try {
    const steps = await historyStore.fetchSteps(exp.expId)
    if (steps && steps.length > 0) {
      // 使用第一步的数据作为初始数据
      const firstData = JSON.parse(steps[0].dataJson || '[]')
      dataStore.setData(firstData, 'int')
    }
    // 设置算法并跳转
    algorithmStore.selectAlgorithm(algoCodeMap[exp.algoId] || 'bubble')
    router.push('/')
  } catch (e) {
    console.error('回放失败:', e)
    algorithmStore.selectAlgorithm(algoCodeMap[exp.algoId] || 'bubble')
    router.push('/')
  }
}

const replayFromStep = async (stepNum) => {
  if (!detailExp.value) return
  showDetail.value = false
  try {
    const steps = await historyStore.fetchSteps(detailExp.value.expId)
    const step = steps.find(s => s.stepNumber === stepNum)
    if (step && step.dataJson) {
      const stepData = JSON.parse(step.dataJson || '[]')
      dataStore.setData(stepData, 'int')
    } else if (steps.length > 0) {
      const firstData = JSON.parse(steps[0].dataJson || '[]')
      dataStore.setData(firstData, 'int')
    }
    algorithmStore.selectAlgorithm(algoCodeMap[detailExp.value.algoId] || 'bubble')
    router.push('/')
  } catch (e) {
    console.error('回放失败:', e)
    algorithmStore.selectAlgorithm(algoCodeMap[detailExp.value.algoId] || 'bubble')
    router.push('/')
  }
}

// 性能记录
const fetchPerfBatches = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/history/performance?userId=${authStore.userId || ''}`)
    const data = await res.json()
    // 补充每个批次的算法数和最优算法
    const batches = data.records || []
    for (const b of batches) {
      try {
        const dRes = await fetch(`/api/history/performance/${b.batchId}/details`)
        const details = await dRes.json()
        b.detailCount = details.length
        if (details.length) {
          const best = details[0]
          b.bestAlgo = getAlgoName(best.algo_id)
        }
      } catch (e) { /* ignore */ }
    }
    perfBatches.value = batches
  } catch (e) {
    console.error('获取性能记录失败:', e)
  } finally {
    loading.value = false
  }
}

const viewPerfDetail = async (batch) => {
  perfDetailBatch.value = batch; perfDetails.value = []; showPerfDetail.value = true
  try {
    const res = await fetch(`/api/history/performance/${batch.batchId}/details`)
    perfDetails.value = await res.json() || []
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.tab-bar { display: flex; gap: 0; margin: 0 20px; }
.tab {
  padding: 10px 24px; border: 1px solid #ddd; background: #f8f9fa;
  cursor: pointer; font-size: 0.9rem; border-bottom: none; border-radius: 8px 8px 0 0;
}
.tab.active { background: white; color: #3498db; font-weight: 600; border-color: #3498db; }
.tab:hover:not(.active) { background: #e9ecef; }

.history-content { padding: 0 20px 20px; max-width: 1300px; margin: 0 auto; }
.history-table { width: 100%; border-collapse: collapse; background: white; border-radius: 0 8px 8px 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.history-table th, .history-table td { padding: 8px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 0.82rem; }
.history-table th { background: #f8f9fa; font-weight: 600; color: #2c3e50; }
.history-table tbody tr:hover { background: #f0f4ff; }
.status-badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem; font-weight: 600; }
.status-badge.COMPLETED { background: #d4edda; color: #155724; }
.status-badge.STOPPED { background: #fff3cd; color: #856404; }
.status-badge.ERROR { background: #f8d7da; color: #721c24; }
.action-cell { display: flex; gap: 4px; white-space: nowrap; }
.small-btn { padding: 3px 10px; font-size: 0.75rem; border: none; border-radius: 4px; cursor: pointer; color: white; }
.small-btn.info-btn { background: #3498db; } .small-btn.info-btn:hover { background: #2980b9; }
.small-btn.success-btn { background: #27ae60; } .small-btn.success-btn:hover { background: #219a52; }
.tiny-btn { padding: 2px 8px; font-size: 0.7rem; border: 1px solid #3498db; border-radius: 3px; background: white; color: #3498db; cursor: pointer; }
.tiny-btn:hover { background: #3498db; color: white; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 15px; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: default; }

/* 弹窗 - 使用 hist-* 前缀避免全局CSS冲突 */
.hist-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.hist-modal { background: white; border-radius: 12px; width: 90%; max-width: 650px; max-height: 80vh; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.hist-modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #eee; }
.hist-modal-header h3 { margin: 0; color: #2c3e50; }
.hist-close-btn { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #999; }
.hist-close-btn:hover { color: #333; }
.hist-modal-body { padding: 20px; overflow-y: auto; max-height: 65vh; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 12px; }
.detail-grid div { font-size: 0.9rem; }
.detail-grid strong { color: #2c3e50; }
.steps-scroll { max-height: 300px; overflow-y: auto; border: 1px solid #eee; border-radius: 6px; }
.step-item { display: flex; align-items: center; gap: 8px; padding: 6px 12px; border-bottom: 1px solid #f0f0f0; font-size: 0.82rem; }
.step-item:last-child { border-bottom: none; } .step-item:hover { background: #f8f9fa; }
.step-num { font-weight: 600; color: #3498db; min-width: 40px; }
.step-desc { flex: 1; color: #555; }

.perf-detail-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
.perf-detail-table th, .perf-detail-table td { padding: 6px 10px; border-bottom: 1px solid #eee; font-size: 0.82rem; text-align: left; }
.perf-detail-table th { background: #f8f9fa; }
</style>
