<template>
  <div class="container">
    <header class="header">
      <h1 @click="$router.push('/')" style="cursor:pointer">
        <i class="fas fa-chart-bar"></i> 排序算法可视化 — 实验历史
      </h1>
      <button class="btn secondary-btn" @click="$router.push('/')" style="width:auto">返回可视化</button>
    </header>
    <div class="history-content">
      <table class="history-table" v-if="historyStore.experiments.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>算法</th>
            <th>数据量</th>
            <th>总步数</th>
            <th>比较次数</th>
            <th>交换次数</th>
            <th>耗时(µs)</th>
            <th>间隔(ms)</th>
            <th>状态</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="exp in historyStore.experiments" :key="exp.expId">
            <td>{{ exp.expId }}</td>
            <td>{{ exp.algoId }}</td>
            <td>{{ exp.dataSize }}</td>
            <td>{{ exp.totalSteps }}</td>
            <td>{{ exp.comparisons }}</td>
            <td>{{ exp.swaps }}</td>
            <td>{{ exp.timeMicros }}</td>
            <td>{{ exp.intervalMs }}</td>
            <td>{{ exp.status }}</td>
            <td>{{ formatTime(exp.startedAt) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else-if="historyStore.loading">加载中...</p>
      <p v-else>暂无实验记录，请先进行排序实验。</p>
      <div class="pagination" v-if="historyStore.total > historyStore.pageSize">
        <button :disabled="historyStore.page <= 1" @click="prevPage">上一页</button>
        <span>第 {{ historyStore.page }} 页 / 共 {{ Math.ceil(historyStore.total / historyStore.pageSize) }} 页</span>
        <button :disabled="historyStore.page * historyStore.pageSize >= historyStore.total" @click="nextPage">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useHistoryStore } from '../stores/history'
import { useAuthStore } from '../stores/auth'

const historyStore = useHistoryStore()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.userId) historyStore.fetchExperiments(authStore.userId)
})

const prevPage = () => historyStore.fetchExperiments(authStore.userId, historyStore.page - 1)
const nextPage = () => historyStore.fetchExperiments(authStore.userId, historyStore.page + 1)
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''
</script>

<style scoped>
.history-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.history-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.history-table th, .history-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 0.85rem;
}
.history-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}
.pagination button {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}
.pagination button:disabled {
  opacity: 0.5;
  cursor: default;
}
</style>
