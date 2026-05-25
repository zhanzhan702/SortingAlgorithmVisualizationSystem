<template>
  <div class="container">
    <header class="header">
      <h1><i class="fas fa-shield-alt"></i> 管理后台</h1>
      <button class="btn secondary-btn" @click="$router.push('/')" style="width:auto">返回</button>
    </header>

    <div class="tab-bar">
      <button :class="['tab', { active: activeTab === 'stats' }]" @click="activeTab = 'stats'">算法统计</button>
      <button :class="['tab', { active: activeTab === 'ranking' }]" @click="activeTab = 'ranking'">综合排名</button>
      <button :class="['tab', { active: activeTab === 'activity' }]" @click="loadActivity(); activeTab = 'activity'">用户活跃</button>
      <button :class="['tab', { active: activeTab === 'report' }]" @click="loadReport(); activeTab = 'report'">用户报告</button>
      <button :class="['tab', { active: activeTab === 'backup' }]" @click="activeTab = 'backup'" v-if="authStore.isAdmin">数据库备份</button>
    </div>

    <div class="admin-content">
      <!-- 算法统计 -->
      <section v-if="activeTab === 'stats'">
        <table class="data-table" v-if="stats.length">
          <thead>
            <tr><th>算法ID</th><th>教学次数</th><th>教学均比</th><th>教学均换</th><th>教学均时(µs)</th><th>性能次数</th><th>性能均比</th><th>性能均换</th><th>性能均时(µs)</th></tr>
          </thead>
          <tbody>
            <tr v-for="s in stats" :key="s.algoId">
              <td>{{ algoMap[s.algoId] || s.algoId }}</td>
              <td>{{ s.totalExperiments }}</td>
              <td>{{ s.avgExpComparisons }}</td>
              <td>{{ s.avgExpSwaps }}</td>
              <td>{{ s.avgExpTimeMicros }}</td>
              <td>{{ s.totalBatches }}</td>
              <td>{{ s.avgBatchComparisons }}</td>
              <td>{{ s.avgBatchSwaps }}</td>
              <td>{{ s.avgBatchTimeMicros }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else>暂无统计数据（运行教学/性能模式后自动生成）</p>
      </section>

      <!-- 综合排名 -->
      <section v-if="activeTab === 'ranking'">
        <table class="data-table" v-if="ranking.length">
          <thead>
            <tr><th>算法</th><th>类别</th><th>时间复杂度</th><th>稳定</th><th>教学次数</th><th>性能次数</th><th>均时(µs)</th><th>排名</th></tr>
          </thead>
          <tbody>
            <tr v-for="r in ranking" :key="r.algo_name">
              <td>{{ r.algo_name }}</td>
              <td>{{ r.category }}</td>
              <td>{{ r.time_complexity }}</td>
              <td>{{ r.is_stable ? '是' : '否' }}</td>
              <td>{{ r.teaching_count }}</td>
              <td>{{ r.performance_count }}</td>
              <td>{{ r.avg_perf_time_us }}</td>
              <td><strong>#{{ r.speed_rank }}</strong></td>
            </tr>
          </tbody>
        </table>
        <p v-else>暂无排名数据</p>
      </section>

      <!-- 用户活跃度（视图） -->
      <section v-if="activeTab === 'activity'">
        <table class="data-table" v-if="activity.length">
          <thead><tr><th>用户名</th><th>角色</th><th>教学次数</th><th>性能批次</th><th>最后活动</th></tr></thead>
          <tbody>
            <tr v-for="a in activity" :key="a.username">
              <td>{{ a.username }}</td><td>{{ a.role }}</td>
              <td>{{ a.teaching_experiments }}</td><td>{{ a.performance_batches }}</td>
              <td>{{ a.last_activity }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else>暂无活跃数据</p>
      </section>

      <!-- 用户报告（存储过程） -->
      <section v-if="activeTab === 'report'">
        <div style="display:flex;gap:10px;align-items:center;margin-bottom:15px">
          <label>用户ID:</label>
          <input v-model="reportUserId" type="text" style="width:220px;padding:4px" placeholder="UUID"/>
          <button class="btn primary-btn" @click="loadReport()" style="width:auto">生成报告</button>
        </div>
        <div v-if="reportSummary" class="admin-section">
          <h4>📊 {{ reportSummary.username }} 的实验报告</h4>
          <p>总实验: <strong>{{ reportSummary.total_experiments }}</strong> | 完成: {{ reportSummary.completed }} | 停止: {{ reportSummary.stopped }}</p>
          <p>平均比较: {{ reportSummary.avg_comparisons }} | 平均交换: {{ reportSummary.avg_swaps }} | 平均耗时: {{ reportSummary.avg_time_us }}µs</p>
        </div>
        <p v-else style="color:#999">输入用户ID后点击"生成报告"</p>
      </section>

      <!-- 备份 -->
      <section v-if="activeTab === 'backup'" class="admin-section">
        <h3>数据库备份</h3>
        <button class="btn primary-btn" @click="doBackup" :disabled="backingUp" style="width:auto">
          {{ backingUp ? '备份中...' : '立即备份' }}
        </button>
        <p v-if="backupMsg" :class="backupMsg.includes('成功') ? 'msg-ok' : 'msg-err'">{{ backupMsg }}</p>
        <h4 style="margin-top:20px">备份文件列表</h4>
        <ul v-if="files.length"><li v-for="f in files" :key="f">{{ f }}</li></ul>
        <p v-else>暂无备份</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const activeTab = ref('stats')
const algoMap = { 1:'冒泡排序',2:'快速排序',3:'直接插入排序',4:'希尔排序',5:'堆排序',6:'归并排序' }

// Stats
const stats = ref([])
const ranking = ref([])

// Activity & Report
const activity = ref([])
const reportUserId = ref('a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6')
const reportSummary = ref(null)
const reportFavorite = ref(null)
const reportDetails = ref([])

// Backup
const backingUp = ref(false)
const backupMsg = ref('')
const files = ref([])

const loadStats = async () => {
  try {
    const res = await fetch('/api/admin/stats'); stats.value = await res.json() || []
    const res2 = await fetch('/api/admin/ranking'); ranking.value = await res2.json() || []
  } catch(e) {}
}

const loadActivity = async () => {
  try { const res = await fetch('/api/admin/activity'); activity.value = await res.json() || [] } catch(e) {}
}

const loadReport = async () => {
  try {
    const res = await fetch(`/api/admin/report?userId=${reportUserId.value}`)
    const data = await res.json()
    if (data && data.length > 0) {
      reportSummary.value = data[0] || null
    }
  } catch(e) { console.error(e) }
}

const doBackup = async () => {
  backingUp.value = true
  try {
    const res = await fetch('/api/admin/backup', { method: 'POST' })
    const data = await res.json()
    backupMsg.value = data.success ? `备份成功: ${data.filename}` : `备份失败: ${data.message}`
    if (data.success) loadFiles()
  } catch (e) { backupMsg.value = '备份请求失败' }
  backingUp.value = false
}

const loadFiles = async () => {
  try {
    const res = await fetch('/api/admin/backups'); const data = await res.json()
    files.value = data.files || []
  } catch(e) {}
}

onMounted(() => { loadStats(); loadFiles() })
</script>

<style scoped>
.tab-bar { display: flex; gap: 0; margin: 0 20px 0; }
.tab {
  padding: 10px 24px; border: 1px solid #ddd; background: #f8f9fa;
  cursor: pointer; font-size: 0.9rem; border-bottom: none; border-radius: 8px 8px 0 0;
}
.tab.active { background: white; color: #2c3e50; font-weight: 600; border-color: #3498db; }
.tab:hover:not(.active) { background: #e9ecef; }
.admin-content { padding: 20px; max-width: 1000px; margin: 0 auto; }
.data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 0 8px 8px 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.data-table th, .data-table td { padding: 8px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 0.82rem; }
.data-table th { background: #f8f9fa; font-weight: 600; color: #2c3e50; }
.admin-section { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.admin-section h3 { margin-bottom: 15px; }
.msg-ok { margin-top: 10px; color: #27ae60; }
.msg-err { margin-top: 10px; color: #e74c3c; }
</style>
