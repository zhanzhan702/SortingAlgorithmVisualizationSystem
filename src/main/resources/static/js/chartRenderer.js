// 图表渲染器模块
const ChartRenderer = {
    chart: null,
    performanceData: {},

    // 初始化图表
    init: function () {
        const ctx = document.getElementById('performance-chart').getContext('2d');

        // 如果已有图表，销毁它
        if (this.chart) {
            this.chart.destroy();
        }

        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['直接插入', '希尔排序', '冒泡排序', '快速排序', '堆排序', '归并排序'],
                datasets: [
                    {
                        label: '运行时间 (ms)',
                        data: [0, 0, 0, 0, 0, 0],
                        backgroundColor: [
                            'rgba(52, 152, 219, 0.7)',
                            'rgba(155, 89, 182, 0.7)',
                            'rgba(46, 204, 113, 0.7)',
                            'rgba(241, 196, 15, 0.7)',
                            'rgba(230, 126, 34, 0.7)',
                            'rgba(231, 76, 60, 0.7)'
                        ],
                        borderColor: [
                            'rgb(52, 152, 219)',
                            'rgb(155, 89, 182)',
                            'rgb(46, 204, 113)',
                            'rgb(241, 196, 15)',
                            'rgb(230, 126, 34)',
                            'rgb(231, 76, 60)'
                        ],
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false, // 隐藏图例，节省空间
                    },
                    title: {
                        display: true,
                        text: '排序算法性能对比',
                        font: {
                            size: 14 // 减小字体大小
                        },
                        padding: {
                            bottom: 10
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const algorithmNames = {
                                    '直接插入': 'Insertion Sort',
                                    '希尔排序': 'Shell Sort',
                                    '冒泡排序': 'Bubble Sort',
                                    '快速排序': 'Quick Sort',
                                    '堆排序': 'Heap Sort',
                                    '归并排序': 'Merge Sort'
                                };

                                const label = algorithmNames[context.label] || context.label;
                                return `${label}: ${context.raw}ms`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '运行时间 (毫秒)',
                            font: {
                                size: 12
                            }
                        },
                        ticks: {
                            font: {
                                size: 10
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: '排序算法',
                            font: {
                                size: 12
                            }
                        },
                        ticks: {
                            font: {
                                size: 10
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                },
                layout: {
                    padding: {
                        top: 10,
                        bottom: 10,
                        left: 10,
                        right: 10
                    }
                }
            }
        });

        // 初始化性能数据存储
        this.resetPerformanceData();

        Utils.logMessage('图表模块已初始化', 'success');
    },

    // 重置性能数据
    resetPerformanceData: function () {
        this.performanceData = {
            insertion: {time: 0, comparisons: 0, swaps: 0},
            shell: {time: 0, comparisons: 0, swaps: 0},
            bubble: {time: 0, comparisons: 0, swaps: 0},
            quick: {time: 0, comparisons: 0, swaps: 0},
            heap: {time: 0, comparisons: 0, swaps: 0},
            merge: {time: 0, comparisons: 0, swaps: 0}
        };
    },

    // 添加性能结果
    addResult: function (result) {
        const algorithm = result.algorithm.toLowerCase();

        if (this.performanceData[algorithm]) {
            this.performanceData[algorithm] = {
                time: result.time || 0,
                comparisons: result.comparisons || 0,
                swaps: result.swaps || 0
            };

            this.updateChart();
            this.updateComparisonTable();
        }
    },

    // 更新图表
    updateChart: function () {
        if (!this.chart) return;

        const data = [
            this.performanceData.insertion.time,
            this.performanceData.shell.time,
            this.performanceData.bubble.time,
            this.performanceData.quick.time,
            this.performanceData.heap.time,
            this.performanceData.merge.time
        ];

        this.chart.data.datasets[0].data = data;
        this.chart.update();
    },

    // 更新对比表格
    updateComparisonTable: function () {
        // 创建或更新对比表格
        let table = document.getElementById('performance-table');

        // 确保表格容器存在
        let tableContainer = document.getElementById('performance-table-container');
        if (!tableContainer) {
            // 如果在中间栏找不到，尝试在右侧栏找
            tableContainer = document.querySelector('#performance-chart-section .chart-container');
            if (tableContainer) {
                tableContainer.insertAdjacentHTML('afterend', '<div id="performance-table-container" class="performance-table-container"></div>');
                tableContainer = document.getElementById('performance-table-container');
            }
        }

        if (!table) {
            table = document.createElement('table');
            table.id = 'performance-table';
            table.className = 'performance-table';
            table.innerHTML = `
            <thead>
                <tr>
                    <th>算法</th>
                    <th>时间 (ms)</th>
                    <th>比较次数</th>
                    <th>交换次数</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>直接插入排序</td>
                    <td id="insertion-time">0</td>
                    <td id="insertion-comparisons">0</td>
                    <td id="insertion-swaps">0</td>
                </tr>
                <tr>
                    <td>希尔排序</td>
                    <td id="shell-time">0</td>
                    <td id="shell-comparisons">0</td>
                    <td id="shell-swaps">0</td>
                </tr>
                <tr>
                    <td>冒泡排序</td>
                    <td id="bubble-time">0</td>
                    <td id="bubble-comparisons">0</td>
                    <td id="bubble-swaps">0</td>
                </tr>
                <tr>
                    <td>快速排序</td>
                    <td id="quick-time">0</td>
                    <td id="quick-comparisons">0</td>
                    <td id="quick-swaps">0</td>
                </tr>
                <tr>
                    <td>堆排序</td>
                    <td id="heap-time">0</td>
                    <td id="heap-comparisons">0</td>
                    <td id="heap-swaps">0</td>
                </tr>
                <tr>
                    <td>归并排序</td>
                    <td id="merge-time">0</td>
                    <td id="merge-comparisons">0</td>
                    <td id="merge-swaps">0</td>
                </tr>
            </tbody>
        `;

            if (tableContainer) {
                tableContainer.innerHTML = '';
                tableContainer.appendChild(table);
            } else {
                // 如果还是找不到容器，默认添加到图表后面
                const chartSection = document.querySelector('#performance-chart-section .chart-container');
                if (chartSection) {
                    chartSection.parentNode.insertBefore(table, chartSection.nextSibling);
                }
            }
        }

        // 更新表格数据
        const algorithms = ['insertion', 'shell', 'bubble', 'quick', 'heap', 'merge'];
        algorithms.forEach(algo => {
            const data = this.performanceData[algo];
            document.getElementById(`${algo}-time`).textContent = data.time;
            document.getElementById(`${algo}-comparisons`).textContent = data.comparisons;
            document.getElementById(`${algo}-swaps`).textContent = data.swaps;
        });
    },


    // 清除性能数据
    clear: function () {
        this.resetPerformanceData();
        if (this.chart) {
            this.chart.data.datasets[0].data = [0, 0, 0, 0, 0, 0];
            this.chart.update();
        }

        // 清除表格
        const tableContainer = document.getElementById('performance-table-container');
        if (tableContainer) {
            tableContainer.innerHTML = '';
        }

        Utils.logMessage('性能数据已清除', 'info');
    }
};