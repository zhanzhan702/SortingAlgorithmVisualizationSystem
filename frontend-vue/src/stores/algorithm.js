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
      const codes = {
        insertion: `function insertionSort(arr):\n    for i = 1 to n-1:\n        key = arr[i]\n        j = i-1\n        while j >= 0 and arr[j] > key:\n            arr[j+1] = arr[j]\n            j = j-1\n        arr[j+1] = key`,
        bubble: `function bubbleSort(arr):\n    for i = 0 to n-1:\n        for j = 0 to n-i-2:\n            if arr[j] > arr[j+1]:\n                swap(arr[j], arr[j+1])`,
        quick: `function quickSort(arr, low, high):\n    if low < high:\n        pi = partition(arr, low, high)\n        quickSort(arr, low, pi-1)\n        quickSort(arr, pi+1, high)\n\nfunction partition(arr, low, high):\n    pivot = arr[high]\n    i = low-1\n    for j = low to high-1:\n        if arr[j] < pivot:\n            i = i+1\n            swap(arr[i], arr[j])\n    swap(arr[i+1], arr[high])\n    return i+1`,
        shell: `function shellSort(arr):\n    n = length(arr)\n    gap = n/2\n    while gap > 0:\n        for i = gap to n-1:\n            temp = arr[i]\n            j = i\n            while j >= gap and arr[j-gap] > temp:\n                arr[j] = arr[j-gap]\n                j = j-gap\n            arr[j] = temp\n        gap = gap/2`,
        heap: `function heapSort(arr):\n    buildMaxHeap(arr)\n    for i = n-1 downto 1:\n        swap(arr[0], arr[i])\n        heapSize = heapSize-1\n        heapify(arr, 0, heapSize)\n\nfunction buildMaxHeap(arr):\n    heapSize = n\n    for i = floor(n/2) downto 0:\n        heapify(arr, i, heapSize)\n\nfunction heapify(arr, i, heapSize):\n    largest = i\n    left = 2*i+1\n    right = 2*i+2\n    if left < heapSize and arr[left] > arr[largest]:\n        largest = left\n    if right < heapSize and arr[right] > arr[largest]:\n        largest = right\n    if largest != i:\n        swap(arr[i], arr[largest])\n        heapify(arr, largest, heapSize)`,
        merge: `function mergeSort(arr, left, right):\n    if left < right:\n        mid = floor((left+right)/2)\n        mergeSort(arr, left, mid)\n        mergeSort(arr, mid+1, right)\n        merge(arr, left, mid, right)\n\nfunction merge(arr, left, mid, right):\n    n1 = mid-left+1\n    n2 = right-mid\n    create L[0..n1] and R[0..n2]\n    for i=0 to n1-1:\n        L[i] = arr[left+i]\n    for j=0 to n2-1:\n        R[j] = arr[mid+1+j]\n    i=0, j=0, k=left\n    while i<n1 and j<n2:\n        if L[i] <= R[j]:\n            arr[k] = L[i]\n            i = i+1\n        else:\n            arr[k] = R[j]\n            j = j+1\n        k = k+1\n    while i < n1:\n        arr[k] = L[i]\n        i = i+1\n        k = k+1\n    while j < n2:\n        arr[k] = R[j]\n        j = j+1\n        k = k+1`,
      }
      this.pseudocode = codes[this.currentAlgorithm] || ''
    },
  },
})
