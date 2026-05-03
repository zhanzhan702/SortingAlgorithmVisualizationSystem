// stores/performance.js
import { defineStore } from 'pinia'

export const usePerformanceStore = defineStore('performance', {
  state: () => ({
    results: [], // 存储每个算法的结果 { algorithm, time, comparisons, swaps }
  }),
  actions: {
    addResult(result) {
      const existing = this.results.find((r) => r.algorithm === result.algorithm)
      if (existing) {
        existing.time = result.time
        existing.comparisons = result.comparisons
        existing.swaps = result.swaps
      } else {
        this.results.push({
          algorithm: result.algorithm,
          time: result.time,
          comparisons: result.comparisons,
          swaps: result.swaps,
        })
      }
    },
    clear() {
      this.results = []
    },
  },
})
