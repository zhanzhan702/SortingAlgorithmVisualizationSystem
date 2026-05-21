import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    currentMode: 'teaching', // 'teaching' | 'performance'
    isLoading: false,
    loadingMessage: '正在处理...',
    errorMessage: null,
    showError: false,
    isPaused: false,
  }),
  actions: {
    switchMode(mode) {
      this.currentMode = mode
    },
    showLoading(msg) {
      this.isLoading = true
      this.loadingMessage = msg
    },
    hideLoading() {
      this.isLoading = false
    },
    showErrorModal(msg) {
      this.errorMessage = msg
      this.showError = true
    },
    hideErrorModal() {
      this.showError = false
      this.errorMessage = null
    },
    setPaused(paused) {
      this.isPaused = paused
    },
  },
})
