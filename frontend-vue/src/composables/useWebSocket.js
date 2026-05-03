import { ref } from 'vue'
import { useAlgorithmStore } from '../stores/algorithm'
import { useDataStore } from '../stores/data'
import { useUiStore } from '../stores/ui'
import { usePerformanceStore } from '../stores/performance'
import { Utils } from '../utils/helpers'

const socket = ref(null)
const isConnected = ref(false)
let messageQueue = []
let isSending = false

export function useWebSocket() {
  const algorithmStore = useAlgorithmStore()
  const dataStore = useDataStore()
  const uiStore = useUiStore()
  const performanceStore = usePerformanceStore()

  let messageQueue = []
  let isSending = false

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
      uiStore.showErrorModal('WebSocket 错误')
    }
    socket.value.onclose = () => {
      isConnected.value = false
      Utils.logMessage('WebSocket 已断开', 'warning')
      uiStore.showErrorModal('连接已断开')
    }
  }

  const handleMessage = (data) => {
    switch (data.type) {
      case 'STEP_UPDATE':
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
      default:
        console.log('Unknown message type:', data.type)
    }
  }

  const send = (message) => {
    if (!isConnected.value || !socket.value) {
      uiStore.showErrorModal('未连接到服务器')
      return false
    }
    if (isSending) {
      messageQueue.push(message)
      return true
    }
    isSending = true
    socket.value.send(JSON.stringify(message))
    setTimeout(() => {
      isSending = false
      if (messageQueue.length) {
        const next = messageQueue.shift()
        send(next)
      }
    }, 100)
    return true
  }

  const sendSortRequest = (request) => {
    const msg = {
      requestId: crypto.randomUUID(),
      type: 'SORT_REQUEST',
      ...request,
    }
    return send(msg)
  }

  return { isConnected, connect, sendSortRequest }
}
