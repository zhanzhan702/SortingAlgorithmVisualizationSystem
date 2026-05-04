import { defineStore } from 'pinia'
import DataGenerator from '../utils/dataGenerator'

export const useDataStore = defineStore('data', {
  state: () => ({
    rawData: [],
    displayData: [],
    highlight: {},
    dataType: 'int',
    dataDistribution: 'random',
    dataSize: 20,
    minValue: 10,
    maxValue: 100,
  }),
  actions: {
    generateNewData() {
      let newData
      if (this.dataType === 'Person') {
        newData = DataGenerator.generatePersonData(this.dataSize, 'score')
      } else {
        newData = DataGenerator.generateData(
          this.dataSize,
          this.dataType,
          this.dataDistribution,
          this.minValue,
          this.maxValue,
        )
      }
      this.rawData = newData
      this.displayData = [...newData]
      this.highlight = {} // 重置高亮状态
    },
    setData(data, type) {
      this.rawData = data
      this.displayData = [...data]
      this.dataType = type
      this.highlight = {} // 重置高亮状态
    },
    updateDisplayData(data, highlight = {}) {
      this.displayData = data
      this.highlight = highlight
    },
  },
})
