<template>
    <section class="control-section">
        <h3><i class="fas fa-network-wired"></i> 连接状态</h3>
        <div class="connection-status">
            <div class="status-indicator" id="status-indicator">
                <span class="status-dot" :class="{ connected: isConnected, disconnected: !isConnected }"></span>
                <span class="status-text">{{ isConnected ? '已连接' : '未连接' }}</span>
            </div>
            <button class="btn secondary-btn" @click="connectToServer">
                <i class="fas fa-plug"></i> 连接服务器
            </button>
        </div>
        <div class="server-url">
            <label>服务器地址</label>
            <input type="text" v-model="serverUrl" />
        </div>
    </section>
</template>

<script setup>
import { ref } from 'vue'
import { useWebSocket } from '../../composables/useWebSocket'
import { useAuthStore } from '../../stores/auth'

const { isConnected, connect } = useWebSocket()
const authStore = useAuthStore()
const serverUrl = ref('ws://localhost:8080/websocket')

const connectToServer = () => {
    if (isConnected.value) {
        alert('已经连接到服务器')   // 或使用 uiStore.showErrorModal
        return
    }
    // 必须带上令牌：否则后端解析不到 userId，本次实验与性能数据不会入库
    connect(serverUrl.value, authStore.token)
}
</script>