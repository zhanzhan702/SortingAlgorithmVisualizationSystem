<template>
  <div class="container">
    <header class="header">
      <h1><i class="fas fa-shield-alt"></i> 管理后台</h1>
      <button class="btn secondary-btn" @click="$router.push('/')" style="width:auto">返回</button>
    </header>
    <div class="admin-content">
      <section class="admin-section">
        <h3>数据库备份</h3>
        <button class="btn primary-btn" @click="doBackup" :disabled="backingUp" style="width:auto">
          {{ backingUp ? '备份中...' : '立即备份' }}
        </button>
        <p v-if="backupMsg" class="msg">{{ backupMsg }}</p>
      </section>
      <section class="admin-section">
        <h3>备份文件列表</h3>
        <ul v-if="files.length">
          <li v-for="f in files" :key="f">{{ f }}</li>
        </ul>
        <p v-else>暂无备份</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const backingUp = ref(false)
const backupMsg = ref('')
const files = ref([])

const doBackup = async () => {
  backingUp.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/backup', { method: 'POST' })
    const data = await res.json()
    backupMsg.value = data.success ? `备份成功: ${data.filename}` : `备份失败: ${data.message}`
    if (data.success) loadFiles()
  } catch (e) {
    backupMsg.value = '备份请求失败'
  }
  backingUp.value = false
}

const loadFiles = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/admin/backups')
    const data = await res.json()
    files.value = data.files || []
  } catch (e) {}
}

onMounted(loadFiles)
</script>

<style scoped>
.admin-content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.admin-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.admin-section h3 {
  margin-bottom: 15px;
}
.msg {
  margin-top: 10px;
  color: #27ae60;
}
</style>
