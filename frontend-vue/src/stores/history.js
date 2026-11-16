import { defineStore } from 'pinia'
import { apiFetch } from '../utils/api'

export const useHistoryStore = defineStore('history', {
  state: () => ({
    experiments: [],
    total: 0,
    page: 1,
    pageSize: 20,
    loading: false,
  }),
  actions: {
    async fetchExperiments(userId, page = 1) {
      this.loading = true
      try {
        const res = await apiFetch(
          `/api/history/experiments?userId=${userId}&page=${page}&size=${this.pageSize}`
        )
        const data = await res.json()
        this.experiments = data.records || []
        this.total = data.total || 0
        this.page = page
      } catch (e) {
        console.error('获取历史失败:', e)
      } finally {
        this.loading = false
      }
    },
    async fetchSteps(expId) {
      try {
        const res = await apiFetch(`/api/history/experiments/${expId}/steps`)
        return await res.json()
      } catch (e) {
        console.error('获取步骤失败:', e)
        return []
      }
    },
  },
})
