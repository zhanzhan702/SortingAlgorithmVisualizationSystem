import { ref, onMounted, onUnmounted } from 'vue'
import { useComparatorStore } from '../stores/comparator'

export function useVisualizer(svgId) {
  const svg = ref(null)
  const width = ref(0)
  const height = ref(0)
  const margin = { top: 30, right: 20, bottom: 50, left: 40 }
  const colors = {
    normal: '#3498db',
    comparing: '#e74c3c',
    swapping: '#27ae60',
    sorted: '#9b59b6',
    heap: '#f39c12',
    pivot: '#1abc9c',
  }
  const comparatorStore = useComparatorStore()

  const init = () => {
    svg.value = document.getElementById(svgId)
    if (!svg.value) return
    const rect = svg.value.getBoundingClientRect()
    width.value = rect.width
    height.value = rect.height
    svg.value.innerHTML = ''
    const bgRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
    bgRect.setAttribute('width', '100%')
    bgRect.setAttribute('height', '100%')
    bgRect.setAttribute('fill', '#f9f9f9')
    bgRect.setAttribute('rx', '8')
    svg.value.appendChild(bgRect)
  }

  const drawAxes = (dataCount, maxValue, yAxisLabel = '元素值') => {
    // X轴
    const xAxis = document.createElementNS('http://www.w3.org/2000/svg', 'line')
    xAxis.setAttribute('x1', margin.left)
    xAxis.setAttribute('y1', height.value - margin.bottom)
    xAxis.setAttribute('x2', width.value - margin.right)
    xAxis.setAttribute('y2', height.value - margin.bottom)
    xAxis.setAttribute('stroke', '#95a5a6')
    xAxis.setAttribute('stroke-width', '2')
    svg.value.appendChild(xAxis)

    // Y轴
    const yAxis = document.createElementNS('http://www.w3.org/2000/svg', 'line')
    yAxis.setAttribute('x1', margin.left)
    yAxis.setAttribute('y1', margin.top)
    yAxis.setAttribute('x2', margin.left)
    yAxis.setAttribute('y2', height.value - margin.bottom)
    yAxis.setAttribute('stroke', '#95a5a6')
    yAxis.setAttribute('stroke-width', '2')
    svg.value.appendChild(yAxis)

    // X轴标签
    const xLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text')
    xLabel.setAttribute('x', width.value / 2)
    xLabel.setAttribute('y', height.value - 10)
    xLabel.setAttribute('text-anchor', 'middle')
    xLabel.setAttribute('font-size', '12px')
    xLabel.setAttribute('fill', '#7f8c8d')
    xLabel.textContent = `元素索引 (共 ${dataCount} 个元素)`
    svg.value.appendChild(xLabel)

    // Y轴标签
    const yLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text')
    yLabel.setAttribute('x', 15)
    yLabel.setAttribute('y', height.value / 2)
    yLabel.setAttribute('text-anchor', 'middle')
    yLabel.setAttribute('font-size', '12px')
    yLabel.setAttribute('fill', '#7f8c8d')
    yLabel.setAttribute('transform', `rotate(-90, 15, ${height.value / 2})`)
    yLabel.textContent = yAxisLabel
    svg.value.appendChild(yLabel)

    // Y轴刻度
    const tickCount = 5
    for (let i = 0; i <= tickCount; i++) {
      const value = (i / tickCount) * maxValue
      const y =
        height.value -
        margin.bottom -
        (value / maxValue) * (height.value - margin.top - margin.bottom)

      const tick = document.createElementNS('http://www.w3.org/2000/svg', 'line')
      tick.setAttribute('x1', margin.left - 5)
      tick.setAttribute('y1', y)
      tick.setAttribute('x2', margin.left)
      tick.setAttribute('y2', y)
      tick.setAttribute('stroke', '#95a5a6')
      tick.setAttribute('stroke-width', '1')
      svg.value.appendChild(tick)

      const tickLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text')
      tickLabel.setAttribute('x', margin.left - 8)
      tickLabel.setAttribute('y', y + 3)
      tickLabel.setAttribute('text-anchor', 'end')
      tickLabel.setAttribute('font-size', '10px')
      tickLabel.setAttribute('fill', '#7f8c8d')
      tickLabel.textContent = Math.round(value)
      svg.value.appendChild(tickLabel)
    }
  }

  const update = (data, highlight = {}) => {
    if (!svg.value) init()
    if (!data || data.length === 0) return

    // 清除除背景外的所有元素
    const children = Array.from(svg.value.childNodes)
    children.forEach((child) => {
      if (child.tagName !== 'rect' || child.getAttribute('fill') !== '#f9f9f9') {
        svg.value.removeChild(child)
      }
    })

    const sortedIndices = highlight.sorted || []
    const isPersonData =
      data.length > 0 &&
      typeof data[0] === 'object' &&
      data[0] !== null &&
      ('id' in data[0] || 'score' in data[0])

    let values = data
    let yAxisLabel = '元素值'
    if (isPersonData) {
      const field = comparatorStore.structField || 'score'
      values = data.map((item) => {
        const val = item[field]
        return typeof val === 'number' ? val : parseFloat(val) || 0
      })
      yAxisLabel = `${field}值`
    }

    const maxValue = Math.max(...values, 1)
    const scaleY = (height.value - margin.top - margin.bottom) / maxValue
    const barWidth = Math.max(3, (width.value - margin.left - margin.right) / data.length - 2)

    data.forEach((item, idx) => {
      const value = isPersonData ? values[idx] : item
      const barHeight = value * scaleY
      const x = margin.left + idx * (barWidth + 2)
      const y = height.value - margin.bottom - barHeight

      let color = colors.normal
      if (highlight.swap && highlight.swap.includes(idx)) color = colors.swapping
      else if (highlight.compare && highlight.compare.includes(idx)) color = colors.comparing
      else if (highlight.heap && highlight.heap.includes(idx)) color = colors.heap
      else if (highlight.pivot && highlight.pivot.includes(idx)) color = colors.pivot
      else if (sortedIndices.includes(idx)) color = colors.sorted

      const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
      rect.setAttribute('x', x)
      rect.setAttribute('y', y)
      rect.setAttribute('width', barWidth)
      rect.setAttribute('height', barHeight)
      rect.setAttribute('fill', color)
      rect.setAttribute('stroke', '#fff')
      rect.setAttribute('stroke-width', '1')
      rect.setAttribute('rx', '2')
      svg.value.appendChild(rect)

      if (data.length <= 30) {
        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text')
        text.setAttribute('x', x + barWidth / 2)
        text.setAttribute('y', y - 5)
        text.setAttribute('text-anchor', 'middle')
        text.setAttribute('font-size', '10px')
        text.setAttribute('fill', '#333')
        text.textContent = Math.round(value)
        svg.value.appendChild(text)
      }
    })

    drawAxes(data.length, maxValue, yAxisLabel)
  }

  const resize = () => {
    init()
  }

  onMounted(() => {
    init()
    window.addEventListener('resize', resize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', resize)
  })

  return { update, resize }
}
