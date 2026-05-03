import { defineStore } from 'pinia'

export const useComparatorStore = defineStore('comparator', {
  state: () => ({
    direction: 'ascending',
    method: 'numeric',
    structField: 'score',
  }),
  getters: {
    comparatorDescription() {
      const dirText = this.direction === 'ascending' ? '升序' : '降序'
      const methodText =
        { numeric: '数值比较', absolute: '绝对值比较', reverse: '反向比较' }[this.method] ||
        this.method
      return `${dirText} - ${methodText}`
    },
    getComparator() {
      // 返回比较函数（供后端使用）
      return (a, b) => {
        if (this.direction === 'ascending') return a < b
        else return a > b
      }
    },
  },
  actions: {
    setDirection(dir) {
      this.direction = dir
    },
    setMethod(method) {
      this.method = method
    },
    setStructField(field) {
      this.structField = field
    },
  },
})
