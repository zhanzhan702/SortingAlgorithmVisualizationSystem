import { defineStore } from 'pinia'

export const useAlgorithmStore = defineStore('algorithm', {
  state: () => ({
    currentAlgorithm: 'bubble',
    availableAlgorithms: [
      { id: 'insertion', name: '直接插入排序', complexity: 'O(n²)' },
      { id: 'shell', name: '希尔排序', complexity: 'O(n log n)' },
      { id: 'bubble', name: '冒泡排序', complexity: 'O(n²)' },
      { id: 'quick', name: '快速排序', complexity: 'O(n log n)' },
      { id: 'heap', name: '堆排序', complexity: 'O(n log n)' },
      { id: 'merge', name: '二路归并排序', complexity: 'O(n log n)' },
    ],
    isSorting: false,
    stats: { comparisons: 0, swaps: 0, time: 0, step: 0, totalSteps: 0 },
    pseudocode: '',
  }),
  actions: {
    selectAlgorithm(id) {
      this.currentAlgorithm = id
      this.updatePseudocode()
    },
    startSort() {
      this.isSorting = true
    },
    resetSort() {
      this.isSorting = false
      this.stats = { comparisons: 0, swaps: 0, time: 0, step: 0, totalSteps: 0 }
    },
    updateStats(newStats) {
      this.stats = { ...this.stats, ...newStats }
    },
    updatePseudocode() {
      const algo = this.currentAlgorithm.toUpperCase()
      if (!algo) {
        this.pseudocode = ''
        return
      }
      // 尝试从缓存获取
      if (this._pseudocodeCache?.[algo]) {
        this.pseudocode = this.highlightSyntax(this._pseudocodeCache[algo])
        return
      }
      // 从后端 API 获取伪代码
      fetch('/api/algorithms')
        .then(res => res.json())
        .then(data => {
          const details = data?.algorithmDetails?.[algo]
          if (details?.pseudocode) {
            const lines = Array.isArray(details.pseudocode)
              ? details.pseudocode.join('\n')
              : details.pseudocode
            if (!this._pseudocodeCache) this._pseudocodeCache = {}
            this._pseudocodeCache[algo] = lines
            this.pseudocode = this.highlightSyntax(lines)
          } else {
            this.pseudocode = ''
          }
        })
        .catch(err => {
          console.error('获取伪代码失败:', err)
          this.pseudocode = ''
        })
    },
    // 添加语法高亮方法
    highlightSyntax(text) {
      // 转义 HTML 特殊字符
      let escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      // 关键字
      escaped = escaped.replace(
        /\b(function|for|while|if|else|return|downto|to|and|create)\b/gi,
        '<span class="code-keyword">$&</span>',
      )
      // 函数名
      escaped = escaped.replace(
        /\b(insertionSort|shellSort|bubbleSort|quickSort|heapSort|mergeSort|partition|buildMaxHeap|heapify|merge|swap|floor|length)\b/gi,
        '<span class="code-function">$&</span>',
      )
      // 变量名
      escaped = escaped.replace(
        /\b(arr|key|j|i|n|gap|temp|low|high|pivot|pi|heapSize|largest|left|right|mid|L|R|k|n1|n2)\b/gi,
        '<span class="code-variable">$&</span>',
      )
      // 数字
      escaped = escaped.replace(/\b\d+\b/g, '<span class="code-number">$&</span>')
      // 保留换行
      escaped = escaped.replace(/\n/g, '<br>')
      return escaped
    },
  },
})
