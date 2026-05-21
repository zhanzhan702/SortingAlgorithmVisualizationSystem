import { ref } from 'vue'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'
import { useUiStore } from '../stores/ui'
import { usePerformanceStore } from '../stores/performance'
import { Utils } from '../utils/helpers'

const socket = ref(null)
const isConnected = ref(false)
let messageQueue = []
let silentClose = false

export function useWebSocket() {
  const algorithmStore = useAlgorithmStore()
  const dataStore = useDataStore()
  const uiStore = useUiStore()
  const performanceStore = usePerformanceStore()

  const connect = (url) => {
    if (socket.value) {
      silentClose = true // 标记为静默关闭
      socket.value.close()
    }
    uiStore.showLoading('连接服务器中...')
    socket.value = new WebSocket(url)
    socket.value.onopen = () => {
      isConnected.value = true
      Utils.logMessage('WebSocket 已连接', 'success')
      uiStore.hideLoading()
      console.log('WebSocket connected')
    }
    socket.value.onmessage = (event) => {
      const data = JSON.parse(event.data)
      handleMessage(data)
    }
    socket.value.onerror = () => {
      uiStore.hideLoading()
      uiStore.showErrorModal('WebSocket 错误')
    }
    socket.value.onclose = () => {
      isConnected.value = false
      uiStore.hideLoading()
      if (!silentClose) {
        Utils.logMessage('WebSocket 已断开', 'warning')
        uiStore.showErrorModal('连接已断开')
      }
      silentClose = false
    }
  }

  const handleMessage = (data) => {
    switch (data.type) {
      case 'STEP_UPDATE':
        if (!algorithmStore.isSorting) return
        Utils.logMessage(`收到步骤更新: ${data.step}/${data.totalSteps}`, 'info')
        algorithmStore.updateStats({
          comparisons: data.stats?.comparisons || 0,
          swaps: data.stats?.swaps || 0,
          time: data.stats?.time || 0,
          step: data.step || 0,
          totalSteps: data.totalSteps || 0,
        })
        dataStore.updateDisplayData(data.data, data.highlight || {})
        if (data.step >= data.totalSteps) {
          algorithmStore.resetSort()
          uiStore.hideLoading()
        }
        break
      case 'PERFORMANCE_RESULT':
        Utils.logMessage(`${data.algorithm} 完成: ${data.time}ms`, 'success')
        performanceStore.addResult(data)
        break
      case 'ERROR':
        Utils.logMessage(`服务器错误: ${data.message}`, 'error')
        uiStore.showErrorModal(data.message)
        break
      case 'CONNECTED':
        Utils.logMessage('服务器确认连接成功', 'success')
        break
      case 'PAUSED':
        Utils.logMessage('排序已暂停', 'info')
        uiStore.setPaused(true)
        break
      case 'RESUMED':
        Utils.logMessage('排序已继续', 'info')
        uiStore.setPaused(false)
        break
      case 'STOPPED':
        Utils.logMessage('排序已停止', 'info')
        algorithmStore.resetSort()
        uiStore.setPaused(false)
        uiStore.hideLoading()
        break
      case 'SORT_COMPLETE':
        Utils.logMessage('排序完成', 'success')
        algorithmStore.resetSort()
        uiStore.hideLoading()
        if (data.finalStats) {
          algorithmStore.updateStats({
            comparisons: data.finalStats.totalComparisons || 0,
            swaps: data.finalStats.totalSwaps || 0,
            time: data.finalStats.totalTime || 0,
          })
        }
        break
      default:
        console.log('Unknown message type:', data.type)
    }
  }

  const send = (message) => {
    if (!isConnected.value || !socket.value) {
      uiStore.showErrorModal('未连接到服务器')
      return false
    }
    messageQueue.push(message)
    flushQueue()
    return true
  }

  const flushQueue = () => {
    if (socket.value.readyState !== WebSocket.OPEN) return
    while (messageQueue.length > 0) {
      try {
        socket.value.send(JSON.stringify(messageQueue.shift()))
      } catch (e) {
        console.error('发送消息失败:', e)
        break
      }
    }
  }

  const sendSortRequest = (request) => {
    const msg = {
      requestId: crypto.randomUUID(),
      type: 'SORT_REQUEST',
      ...request,
    }
    return send(msg)
  }

  const sendControl = (action, extra = {}) => {
    const msg = {
      type: 'CONTROL',
      action: action,
      requestId: crypto.randomUUID(),
      timestamp: Date.now(),
      ...extra,
    }
    return send(msg)
  }

  return { isConnected, socket, connect, sendSortRequest, sendControl, send }
}
