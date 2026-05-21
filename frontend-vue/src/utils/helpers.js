// 工具函数模块（原 utils.js 迁移）
export const Utils = {
  generateUUID: function () {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = (Math.random() * 16) | 0
      const v = c === 'x' ? r : (r & 0x3) | 0x8
      return v.toString(16)
    })
  },

  formatTime: function (ms) {
    if (ms < 1000) return `${ms}ms`
    return `${(ms / 1000).toFixed(2)}s`
  },

  formatNumber: function (num) {
    if (num >= 1e6) return `${(num / 1e6).toFixed(2)}M`
    if (num >= 1e3) return `${(num / 1e3).toFixed(2)}K`
    return num.toString()
  },

  deepClone: function (obj) {
    return JSON.parse(JSON.stringify(obj))
  },

  shuffleArray: function (array) {
    const newArray = [...array]
    for (let i = newArray.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[newArray[i], newArray[j]] = [newArray[j], newArray[i]]
    }
    return newArray
  },

  createNormalDistribution: function (count, mean, stdDev) {
    const data = []
    for (let i = 0; i < count; i++) {
      let u = 0,
        v = 0
      while (u === 0) u = Math.random()
      while (v === 0) v = Math.random()
      const num = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v)
      data.push(Math.max(0, Math.min(100, mean + num * stdDev)))
    }
    return data
  },

  isValidNumber: function (value) {
    return !isNaN(parseFloat(value)) && isFinite(value)
  },

  clamp: function (value, min, max) {
    return Math.min(Math.max(value, min), max)
  },

  logMessage: function (message, type = 'info') {
    console.log(`[${type.toUpperCase()}]`, message)
    window.dispatchEvent(new CustomEvent('add-log', { detail: { message, type } }))
  },

  clearLog: function () {
    window.dispatchEvent(new CustomEvent('clear-log'))
  },
}

// 单独导出常用函数便于按需导入
export const {
  generateUUID,
  formatTime,
  formatNumber,
  deepClone,
  shuffleArray,
  logMessage,
  clearLog,
} = Utils
