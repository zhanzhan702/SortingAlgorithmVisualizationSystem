// 排序可视化模块
const Visualizer = {
    svg: null,
    width: 0,
    height: 0,
    margin: {top: 30, right: 20, bottom: 50, left: 40},
    data: [],
    colors: {
        normal: '#3498db',
        comparing: '#e74c3c',
        swapping: '#27ae60',
        sorted: '#9b59b6',
        heap: '#f39c12',
        pivot: '#1abc9c'
    },
    isInitialized: false,

    // 初始化可视化
    init: function () {
        this.svg = document.getElementById('sort-visualization');
        this.width = this.svg.clientWidth;
        this.height = this.svg.clientHeight;

        // 清空SVG
        this.svg.innerHTML = '';

        // 创建背景矩形
        const bgRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
        bgRect.setAttribute('width', '100%');
        bgRect.setAttribute('height', '100%');
        bgRect.setAttribute('fill', '#f9f9f9');
        bgRect.setAttribute('rx', '8');
        bgRect.setAttribute('ry', '8');
        this.svg.appendChild(bgRect);

        this.isInitialized = true;
        Utils.logMessage('可视化模块已初始化', 'success');
    },

    // 更新数据并绘制
    update: function (data, highlight = {}) {
        if (!this.isInitialized) {
            this.init();
        }

        this.data = data;

        // 清空之前的图形（保留背景）
        const children = Array.from(this.svg.childNodes);
        children.forEach(child => {
            if (child.tagName !== 'rect' || child.getAttribute('fill') !== '#f9f9f9') {
                this.svg.removeChild(child);
            }
        });

        if (data.length === 0) {
            document.getElementById('no-data-message').style.display = 'block';
            return;
        }

        document.getElementById('no-data-message').style.display = 'none';

        // Person结构体判断
        const isPersonData = data.length > 0 &&
            typeof data[0] === 'object' &&
            data[0] !== null &&
            ('id' in data[0] || 'name' in data[0] || 'score' in data[0] || 'age' in data[0]);

        // highlight中没有sorted，就不要显示已排序颜色
        // 只有当highlight.sorted存在时才使用它
        const sortedIndices = highlight.sorted || [];

        // 提取数值用于可视化
        let values;
        if (isPersonData) {
            // 对于Person数据，根据当前选择的排序字段获取值
            const field = ComparisonManager.structField || 'score';
            values = data.map(person => {
                const value = person[field];
                if (typeof value === 'string') {
                    // 字符串使用哈希值
                    return this.stringToHash(value);
                }
                return typeof value === 'number' ? value : 0;
            });
        } else {
            // 对于普通数值数据，直接使用
            values = data;
        }

        const maxValue = Math.max(...values, 1); // 防止除零
        const scaleY = (this.height - this.margin.top - this.margin.bottom) / maxValue;
        const barWidth = Math.max(3, (this.width - this.margin.left - this.margin.right) / data.length - 2);

        // 绘制柱状图
        data.forEach((item, index) => {
            const value = isPersonData ? values[index] : item;
            const barHeight = value * scaleY;
            const x = this.margin.left + index * (barWidth + 2);
            const y = this.height - this.margin.bottom - barHeight;

            // 正确的颜色优先级
            // 1. 先检查当前元素是否是sorted
            const isSorted = sortedIndices.includes(index);

            // 2. 确定颜色
            let color = this.colors.normal;
            let highlightClass = '';

            // 动态操作优先级高于静态状态
            if (highlight.swap && highlight.swap.includes(index)) {
                color = this.colors.swapping;
                highlightClass = 'highlight-swap';
            } else if (highlight.compare && highlight.compare.includes(index)) {
                color = this.colors.comparing;
                highlightClass = 'highlight-compare';
            } else if (highlight.heap && highlight.heap.includes(index)) {
                color = this.colors.heap;
                highlightClass = 'highlight-heap';
            } else if (highlight.pivot && highlight.pivot.includes(index)) {
                color = this.colors.pivot;
                highlightClass = 'highlight-pivot';
            } else if (sortedIndices.includes(index)) {
                color = this.colors.sorted;
                highlightClass = 'highlight-sorted';
            } else {
                color = this.colors.normal;
            }

            // 创建柱状条
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', x);
            rect.setAttribute('y', y);
            rect.setAttribute('width', barWidth);
            rect.setAttribute('height', barHeight);
            rect.setAttribute('fill', color);
            rect.setAttribute('stroke', '#fff');
            rect.setAttribute('stroke-width', '1');
            rect.setAttribute('rx', '2');
            rect.setAttribute('ry', '2');
            rect.setAttribute('class', `bar-element ${highlightClass}`);
            rect.setAttribute('data-index', index);

            // 添加悬停效果
            rect.addEventListener('mouseover', function () {
                this.setAttribute('opacity', '0.8');
            });

            rect.addEventListener('mouseout', function () {
                this.setAttribute('opacity', '1');
            });

            this.svg.appendChild(rect);

            // 显示数值（仅当数据量较少时）
            if (data.length <= 30) {
                const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                text.setAttribute('x', x + barWidth / 2);
                text.setAttribute('y', y - 5);
                text.setAttribute('text-anchor', 'middle');
                text.setAttribute('font-size', '10px');
                text.setAttribute('fill', '#333');

                if (isPersonData) {
                    const displayValue = Math.round(value);
                    text.textContent = displayValue;

                    // 为Person数据添加额外信息
                    if (data.length <= 15 && item.name) {
                        const nameText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                        nameText.setAttribute('x', x + barWidth / 2);
                        nameText.setAttribute('y', y - 20);
                        nameText.setAttribute('text-anchor', 'middle');
                        nameText.setAttribute('font-size', '8px');
                        nameText.setAttribute('fill', '#666');
                        nameText.textContent = item.name.substring(0, 3);
                        this.svg.appendChild(nameText);
                    }
                } else {
                    text.textContent = Math.round(value);
                }

                this.svg.appendChild(text);
            }
        });

        // 绘制坐标轴
        this.drawAxes(data.length, maxValue);

        // 添加图例说明
        if (isPersonData) {
            const legend = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            legend.setAttribute('x', this.width - this.margin.right);
            legend.setAttribute('y', this.margin.top - 10);
            legend.setAttribute('text-anchor', 'end');
            legend.setAttribute('font-size', '10px');
            legend.setAttribute('fill', '#7f8c8d');
            legend.textContent = `按 ${ComparisonManager.structField || 'score'} 排序`;
            this.svg.appendChild(legend);
        }
    },

    stringToHash: function (str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // 转换为32位整数
        }
        return Math.abs(hash % 100); // 返回0-99之间的值
    },

    // 绘制坐标轴
    drawAxes: function (dataCount, maxValue) {
        // X轴
        const xAxis = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        xAxis.setAttribute('x1', this.margin.left);
        xAxis.setAttribute('y1', this.height - this.margin.bottom);
        xAxis.setAttribute('x2', this.width - this.margin.right);
        xAxis.setAttribute('y2', this.height - this.margin.bottom);
        xAxis.setAttribute('stroke', '#95a5a6');
        xAxis.setAttribute('stroke-width', '2');
        this.svg.appendChild(xAxis);

        // Y轴
        const yAxis = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        yAxis.setAttribute('x1', this.margin.left);
        yAxis.setAttribute('y1', this.margin.top);
        yAxis.setAttribute('x2', this.margin.left);
        yAxis.setAttribute('y2', this.height - this.margin.bottom);
        yAxis.setAttribute('stroke', '#95a5a6');
        yAxis.setAttribute('stroke-width', '2');
        this.svg.appendChild(yAxis);

        // X轴标签
        const xLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        xLabel.setAttribute('x', this.width / 2);
        xLabel.setAttribute('y', this.height - 10);
        xLabel.setAttribute('text-anchor', 'middle');
        xLabel.setAttribute('font-size', '12px');
        xLabel.setAttribute('fill', '#7f8c8d');
        xLabel.textContent = `元素索引 (共 ${dataCount} 个元素)`;
        this.svg.appendChild(xLabel);

        // Y轴标签
        const yLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        yLabel.setAttribute('x', 15);
        yLabel.setAttribute('y', this.height / 2);
        yLabel.setAttribute('text-anchor', 'middle');
        yLabel.setAttribute('font-size', '12px');
        yLabel.setAttribute('fill', '#7f8c8d');
        yLabel.setAttribute('transform', `rotate(-90, 15, ${this.height / 2})`);
        yLabel.textContent = '元素值';
        this.svg.appendChild(yLabel);

        // Y轴刻度
        const tickCount = 5;
        for (let i = 0; i <= tickCount; i++) {
            const value = (i / tickCount) * maxValue;
            const y = this.height - this.margin.bottom - (value / maxValue) * (this.height - this.margin.top - this.margin.bottom);

            // 刻度线
            const tick = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            tick.setAttribute('x1', this.margin.left - 5);
            tick.setAttribute('y1', y);
            tick.setAttribute('x2', this.margin.left);
            tick.setAttribute('y2', y);
            tick.setAttribute('stroke', '#95a5a6');
            tick.setAttribute('stroke-width', '1');
            this.svg.appendChild(tick);

            // 刻度标签
            const tickLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            tickLabel.setAttribute('x', this.margin.left - 8);
            tickLabel.setAttribute('y', y + 3);
            tickLabel.setAttribute('text-anchor', 'end');
            tickLabel.setAttribute('font-size', '10px');
            tickLabel.setAttribute('fill', '#7f8c8d');
            tickLabel.textContent = Math.round(value);
            this.svg.appendChild(tickLabel);
        }
    },

    // 清除可视化
    clear: function () {
        if (this.svg) {
            this.svg.innerHTML = '';
            document.getElementById('no-data-message').style.display = 'block';
        }
    },

    // 窗口大小改变时重新初始化
    handleResize: function () {
        if (this.isInitialized) {
            this.init();
            if (this.data.length > 0) {
                this.update(this.data);
            }
        }
    }
};