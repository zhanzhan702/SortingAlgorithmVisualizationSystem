<template>
  <div class="container">
    <header class="header">
      <h1><i class="fas fa-shield-alt"></i> 管理后台</h1>
      <button class="btn secondary-btn" @click="$router.push('/')" style="width:auto">返回</button>
    </header>

    <div class="tab-bar">
      <button :class="['tab', { active: activeTab === 'stats' }]" @click="activeTab = 'stats'">算法统计</button>
      <button :class="['tab', { active: activeTab === 'activity' }]" @click="loadActivity(); activeTab = 'activity'">用户活跃</button>
      <button :class="['tab', { active: activeTab === 'report' }]" @click="loadReport(); activeTab = 'report'">用户报告</button>
      <button :class="['tab', { active: activeTab === 'backup' }]" @click="activeTab = 'backup'" v-if="authStore.isAdmin">数据库备份</button>
    </div>

    <div class="admin-content">
      <!-- 算法统计（含排名） -->
      <section v-if="activeTab === 'stats'">
        <table class="data-table" v-if="mergedStats.length">
          <thead>
            <tr>
              <th>算法</th>
              <th>类别</th>
              <th>复杂度</th>
              <th>稳定</th>
              <th colspan="3">📖 教学维度</th>
              <th colspan="3">⚡ 性能维度</th>
              <th>时/元素(µs)</th>
              <th>排名</th>
            </tr>
            <tr>
              <th></th><th></th><th></th><th></th>
              <th>次数</th><th>均比较</th><th>均时(µs)</th>
              <th>次数</th><th>均比较</th><th>均时(µs)</th>
              <th></th><th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in mergedStats" :key="m.algoId || m.algo_name">
              <td>{{ m.algo_name || algoMap[m.algoId] }}</td>
              <td>{{ catMap[m.category] || m.category || '-' }}</td>
              <td>{{ m.time_complexity || '-' }}</td>
              <td>{{ m.is_stable != null ? (m.is_stable ? '是' : '否') : '-' }}</td>
              <td>{{ m.total_experiments ?? m.teaching_count ?? 0 }}</td>
              <td>{{ m.avg_exp_comparisons ?? '-' }}</td>
              <td>{{ m.avg_exp_time_micros ?? m.teach_avg_time_us ?? 0 }}</td>
              <td>{{ m.total_batches ?? m.perf_count ?? 0 }}</td>
              <td>{{ m.avg_batch_comparisons ?? '-' }}</td>
              <td>{{ m.avg_batch_time_micros ?? m.perf_avg_time_us ?? 0 }}</td>
              <td>{{ m.perf_time_per_element_us ?? '-' }}</td>
              <td><strong>{{ m.speed_rank ? '#' + m.speed_rank : '-' }}</strong></td>
            </tr>
          </tbody>
        </table>
        <p v-else>暂无统计数据（运行教学/性能模式后由触发器自动生成）</p>
      </section>

      <!-- 用户活跃度（视图） -->
      <section v-if="activeTab === 'activity'">
        <table class="data-table" v-if="activity.length">
          <thead><tr><th>用户ID</th><th>用户名</th><th>角色</th><th>教学次数</th><th>性能批次</th><th>最后活动</th></tr></thead>
          <tbody>
            <tr v-for="a in activity" :key="a.user_id">
              <td style="font-size:0.7rem;font-family:monospace">{{ a.user_id }}</td>
              <td>{{ a.username }}</td>
              <td>{{ a.role }}</td>
              <td>{{ a.teaching_experiments }}</td>
              <td>{{ a.performance_batches }}</td>
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
          <h4>📊 {{ reportSummary.username }}（{{ reportSummary.role }}）的综合报告</h4>

          <h5 style="margin-top:16px">📖 教学模式</h5>
          <p>总实验: <strong>{{ reportSummary.teach_total || 0 }}</strong>
            | 完成: {{ reportSummary.teach_completed || 0 }}
            | 停止: {{ reportSummary.teach_stopped || 0 }}</p>
          <p>平均比较: {{ reportSummary.teach_avg_cmp || '-' }}
            | 平均交换: {{ reportSummary.teach_avg_swp || '-' }}
            | 平均耗时: {{ reportSummary.teach_avg_us || '-' }} µs</p>

          <h5 style="margin-top:16px">⚡ 性能模式</h5>
          <p>测试批次: <strong>{{ reportSummary.perf_batches || 0 }}</strong>
            | 明细记录: {{ reportSummary.perf_details || 0 }}</p>
          <p>平均比较: {{ reportSummary.perf_avg_cmp || '-' }}
            | 平均交换: {{ reportSummary.perf_avg_swp || '-' }}
            | 平均耗时: {{ reportSummary.perf_avg_us || '-' }} µs</p>
        </div>
        <p v-else style="color:#999">输入用户ID后点击"生成报告"</p>

        <!-- 算法明细表 -->
        <table class="data-table" v-if="reportDetails.length" style="margin-top:16px">
          <thead>
            <tr>
              <th>算法</th>
              <th colspan="3">📖 教学</th>
              <th colspan="3">⚡ 性能</th>
              <th>性能/教学</th>
            </tr>
            <tr>
              <th></th>
              <th>次数</th><th>均比较</th><th>均时(µs)</th>
              <th>次数</th><th>均比较</th><th>均时(µs)</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in reportDetails" :key="d.algo_name">
              <td>{{ d.algo_name }}</td>
              <td>{{ d.teach_times || 0 }}</td>
              <td>{{ d.teach_avg_cmp || '-' }}</td>
              <td>{{ d.teach_avg_us || '-' }}</td>
              <td>{{ d.perf_times || 0 }}</td>
              <td>{{ d.perf_avg_cmp || '-' }}</td>
              <td>{{ d.perf_avg_us || '-' }}</td>
              <td>{{ d.perf_vs_teach_ratio ?? '-' }}</td>
            </tr>
          </tbody>
        </table>
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
const catMap = { insertion:'插入类', exchange:'交换类', selection:'选择类', merge:'归并类' }

// Stats (merged from /api/admin/stats + /api/admin/ranking)
const mergedStats = ref([])

// Activity & Report
const activity = ref([])
const reportUserId = ref(authStore.userId || '')
const reportSummary = ref(null)
const reportFavorite = ref(null)
const reportDetails = ref([])

// Backup
const backingUp = ref(false)
const backupMsg = ref('')
const files = ref([])

const loadStats = async () => {
  try {
    const [sRes, rRes] = await Promise.all([
      fetch('/api/admin/stats'),
      fetch('/api/admin/ranking')
    ])
    const statsArr = await sRes.json() || []
    const rankingArr = await rRes.json() || []

    // Merge: ranking view has algo_name, stats table has algoId; match by name via algoMap
    const rankByName = {}
    for (const r of rankingArr) {
      rankByName[r.algo_name] = r
    }

    mergedStats.value = statsArr.map(s => {
      const name = algoMap[s.algoId] || `算法#${s.algoId}`
      const rank = rankByName[name] || {}
      return {
        ...s,
        algo_name: name,
        category: rank.category || null,
        time_complexity: rank.time_complexity || null,
        is_stable: rank.is_stable,
        teaching_count: rank.teaching_count,
        teach_avg_time_us: rank.teach_avg_time_us,
        perf_count: rank.perf_count,
        perf_avg_time_us: rank.perf_avg_time_us,
        perf_time_per_element_us: rank.perf_time_per_element_us,
        speed_rank: rank.speed_rank,
      }
    })
  } catch(e) { console.error(e) }
}

const loadActivity = async () => {
  try { const res = await fetch('/api/admin/activity'); activity.value = await res.json() || [] } catch(e) {}
}

const loadReport = async () => {
  try {
    const res = await fetch(`/api/admin/report?userId=${reportUserId.value}`)
    const data = await res.json()  // [[rs1_rows], [rs2_rows], [rs3_rows]]
    if (data && data.length > 0 && data[0] && data[0].length > 0) {
      reportSummary.value = data[0][0] || null           // 结果集1: 综合统计（取第一行）
      reportFavorite.value = (data.length > 1 && data[1].length) ? data[1][0] : null  // 结果集2: 最常用算法
      reportDetails.value = data.length > 2 ? data[2] : []  // 结果集3: 算法明细列表
    } else {
      reportSummary.value = null
      reportDetails.value = []
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
