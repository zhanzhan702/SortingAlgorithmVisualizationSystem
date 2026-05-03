<template>
    <section class="info-section">
        <h3><i class="fas fa-terminal"></i> 系统日志</h3>
        <div class="log-container">
            <div id="log-messages">
                <div v-for="(log, idx) in logs" :key="idx" :class="['log-message', log.type]">
                    [{{ log.time }}] {{ log.message }}
                </div>
            </div>
        </div>
        <div class="log-controls">
            <button class="btn secondary-btn" @click="clearLogs">
                <i class="fas fa-trash"></i> 清空日志
            </button>
        </div>
    </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const logs = ref([])

const addLog = (detail) => {
    const time = new Date().toLocaleTimeString()
    logs.value.push({ time, message: detail.message, type: detail.type })
    setTimeout(() => {
        const container = document.querySelector('.log-container')
        if (container) container.scrollTop = container.scrollHeight
    }, 0)
}

const clearLogs = () => {
    logs.value = []
}

const handleAddLog = (event) => addLog(event.detail)
const handleClearLog = () => clearLogs()

onMounted(() => {
    window.addEventListener('add-log', handleAddLog)
    window.addEventListener('clear-log', handleClearLog)
})

onUnmounted(() => {
    window.removeEventListener('add-log', handleAddLog)
    window.removeEventListener('clear-log', handleClearLog)
})
</script>