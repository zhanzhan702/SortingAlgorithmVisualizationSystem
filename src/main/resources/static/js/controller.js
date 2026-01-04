// 控制面板管理器
const Controller = {
    currentMode: 'teaching',
    currentAlgorithm: 'bubble',
    currentData: [],
    isSorting: false,
    sortInterval: null,
    currentStep: 0,
    totalSteps: 0,
    performanceQueue: [], // 添加性能测试队列
    performanceResults: {}, // 添加性能测试结果存储
    currentPerformanceTest: null, // 当前正在测试的算法
    stats: {
        comparisons: 0,
        swaps: 0,
        time: 0
    },

    // 初始化控制器
    init: function () {
        this.setupEventListeners();
        this.generateAlgorithmButtons();
        this.updateAlgorithmInfo();
        this.updatePseudoCode();
        this.updateStatsDisplay();

        // 初始化比较器管理器
        ComparisonManager.init();

        // 初始化WebSocket回调
        WebSocketManager.on('onStepUpdate', this.handleStepUpdate.bind(this));
        WebSocketManager.on('onPerformanceResult', this.handlePerformanceResult.bind(this));
        WebSocketManager.on('onError', this.handleError.bind(this));
        WebSocketManager.on('onConnect', this.handleConnect.bind(this));
        WebSocketManager.on('onDisconnect', this.handleDisconnect.bind(this));

        // 确保重置按钮初始状态为禁用
        document.getElementById('start-sort').disabled = false;
        document.getElementById('reset-sort').disabled = true;

        Utils.logMessage('控制器已初始化', 'success');
    },

    // 设置事件监听器
    setupEventListeners: function () {
        // 模式选择
        document.querySelectorAll('.mode-btn').forEach(btn => {
            btn.addEventListener('click', this.handleModeChange.bind(this));
        });

        // 控制按钮
        document.getElementById('start-sort').addEventListener('click', this.startSort.bind(this));
        document.getElementById('reset-sort').addEventListener('click', this.resetSort.bind(this));

        // 速度控制
        const speedControl = document.getElementById('speed-control');
        speedControl.addEventListener('input', this.handleSpeedChange.bind(this));

        // 连接按钮
        document.getElementById('connect-btn').addEventListener('click', this.connectToServer.bind(this));

        // 清空日志
        document.getElementById('clear-log').addEventListener('click', Utils.clearLog);

        // 性能测试
        document.getElementById('run-performance-test').addEventListener('click', this.runPerformanceTest.bind(this));

        // 数据生成
        document.getElementById('generate-data').addEventListener('click', this.handleGenerateData.bind(this));

        // 文件上传
        document.getElementById('file-input').addEventListener('change', this.handleFileSelect.bind(this));
        document.getElementById('parse-file').addEventListener('click', this.handleFileParse.bind(this));

        // 手动输入
        document.getElementById('parse-manual').addEventListener('click', this.handleManualInput.bind(this));

        // 标签页切换
        document.querySelectorAll('.method-tab').forEach(tab => {
            tab.addEventListener('click', this.handleMethodTabChange.bind(this));
        });

        // 数据大小滑块
        const dataSize = document.getElementById('data-size');
        dataSize.addEventListener('input', function () {
            document.getElementById('size-value').textContent = this.value;
        });

        // 数据类型改变时显示/隐藏结构体字段选择器
        document.getElementById('data-type').addEventListener('change', (e) => {
            const isPerson = e.target.value === 'Person';
            document.getElementById('struct-field-container').style.display = isPerson ? 'block' : 'none';

            // 如果是Person类型，更新比较器
            if (isPerson) {
                ComparisonManager.generateStructFieldSelector('Person');
            }
        });

        // 窗口大小改变时重新初始化可视化
        window.addEventListener('resize', this.handleResize.bind(this));
        // 性能模式标签页切换（确保在初始化时绑定）
        setTimeout(() => {
            document.querySelectorAll('.performance-tab').forEach(tab => {
                tab.addEventListener('click', this.handlePerformanceTabChange.bind(this));
            });

            // 性能模式文件选择
            const perfFileInput = document.getElementById('performance-file-input');
            if (perfFileInput) {
                perfFileInput.addEventListener('change', this.handlePerformanceFileSelect.bind(this));
            }

            // 性能模式文件解析
            const parsePerfFile = document.getElementById('parse-performance-file');
            if (parsePerfFile) {
                parsePerfFile.addEventListener('click', this.parsePerformanceFile.bind(this));
            }
        }, 100);
    },

    // 生成算法按钮
    generateAlgorithmButtons: function () {
        const algorithms = [
            {id: 'insertion', name: '直接插入排序', complexity: 'O(n²)'},
            {id: 'shell', name: '希尔排序', complexity: 'O(n log n)'},
            {id: 'bubble', name: '冒泡排序', complexity: 'O(n²)'},
            {id: 'quick', name: '快速排序', complexity: 'O(n log n)'},
            {id: 'heap', name: '堆排序', complexity: 'O(n log n)'},
            {id: 'merge', name: '二路归并排序', complexity: 'O(n log n)'}
        ];

        const container = document.getElementById('algorithm-selector');
        container.innerHTML = '';

        algorithms.forEach(algo => {
            const button = document.createElement('button');
            button.className = `algorithm-btn ${algo.id === this.currentAlgorithm ? 'active' : ''}`;
            button.dataset.algorithm = algo.id;
            button.innerHTML = `
                <div>${algo.name}</div>
                <small>${algo.complexity}</small>
            `;

            button.addEventListener('click', () => {
                this.selectAlgorithm(algo.id);
            });

            container.appendChild(button);
        });
    },

    // 选择算法
    selectAlgorithm: function (algorithmId) {
        // 更新按钮状态
        document.querySelectorAll('.algorithm-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.algorithm === algorithmId) {
                btn.classList.add('active');
            }
        });

        // 更新当前算法
        this.currentAlgorithm = algorithmId;

        // 更新算法信息
        this.updateAlgorithmInfo();

        // 更新伪代码
        this.updatePseudoCode();

        Utils.logMessage(`已选择算法: ${algorithmId}`, 'info');
    },

    // 更新算法信息
    updateAlgorithmInfo: function () {
        const algorithmInfo = {
            insertion: {
                name: '直接插入排序',
                description: '通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应位置并插入。',
                timeComplexity: '最坏: O(n²), 平均: O(n²), 最好: O(n)',
                spaceComplexity: 'O(1)',
                stability: '稳定',
                advantages: '实现简单，对小规模数据或基本有序数据效率高'
            },
            shell: {
                name: '希尔排序',
                description: '是插入排序的一种更高效的改进版本，通过将原始数据分成多个子序列分别进行插入排序。',
                timeComplexity: '取决于增量序列，通常 O(n log n)',
                spaceComplexity: 'O(1)',
                stability: '不稳定',
                advantages: '比直接插入排序快，适用于中等规模数据'
            },
            bubble: {
                name: '冒泡排序',
                description: '重复地走访过要排序的数列，一次比较两个元素，如果它们的顺序错误就把它们交换过来。',
                timeComplexity: '最坏: O(n²), 平均: O(n²), 最好: O(n)',
                spaceComplexity: 'O(1)',
                stability: '稳定',
                advantages: '实现简单，适合教学演示'
            },
            quick: {
                name: '快速排序',
                description: '采用分治的思想，通过一趟排序将待排记录分隔成独立的两部分，其中一部分记录的关键字均比另一部分的关键字小。',
                timeComplexity: '最坏: O(n²), 平均: O(n log n), 最好: O(n log n)',
                spaceComplexity: 'O(log n)',
                stability: '不稳定',
                advantages: '平均性能最好，是实际应用中最常用的排序算法'
            },
            heap: {
                name: '堆排序',
                description: '利用堆这种数据结构所设计的一种排序算法，通过构建最大堆或最小堆来实现排序。',
                timeComplexity: 'O(n log n)',
                spaceComplexity: 'O(1)',
                stability: '不稳定',
                advantages: '时间复杂度稳定为O(n log n)，适合大数据排序'
            },
            merge: {
                name: '二路归并排序',
                description: '采用分治法，将已有序的子序列合并，得到完全有序的序列。',
                timeComplexity: 'O(n log n)',
                spaceComplexity: 'O(n)',
                stability: '稳定',
                advantages: '稳定排序，时间复杂度稳定，适合链表排序'
            }
        };

        const info = algorithmInfo[this.currentAlgorithm];
        const container = document.getElementById('algorithm-info');

        container.innerHTML = `
            <h4>${info.name}</h4>
            <p>${info.description}</p>
            <ul>
                <li><strong>时间复杂度:</strong> ${info.timeComplexity}</li>
                <li><strong>空间复杂度:</strong> ${info.spaceComplexity}</li>
                <li><strong>稳定性:</strong> ${info.stability}</li>
                <li><strong>优点:</strong> ${info.advantages}</li>
            </ul>
        `;
    },

    // 更新伪代码
    updatePseudoCode: function () {
        const pseudoCodes = {
            insertion: `function insertionSort(arr):
    for i = 1 to n-1:
        key = arr[i]
        j = i-1
        while j >= 0 and arr[j] > key:
            arr[j+1] = arr[j]
            j = j-1
        arr[j+1] = key`,

            bubble: `function bubbleSort(arr):
    for i = 0 to n-1:
        for j = 0 to n-i-2:
            if arr[j] > arr[j+1]:
                swap(arr[j], arr[j+1])`,

            quick: `function quickSort(arr, low, high):
    if low < high:
        pi = partition(arr, low, high)
        quickSort(arr, low, pi-1)
        quickSort(arr, pi+1, high)

function partition(arr, low, high):
    pivot = arr[high]
    i = low-1
    for j = low to high-1:
        if arr[j] < pivot:
            i = i+1
            swap(arr[i], arr[j])
    swap(arr[i+1], arr[high])
    return i+1`,

            shell: `function shellSort(arr):
    n = length(arr)
    gap = n/2
    while gap > 0:
        for i = gap to n-1:
            temp = arr[i]
            j = i
            while j >= gap and arr[j-gap] > temp:
                arr[j] = arr[j-gap]
                j = j-gap
            arr[j] = temp
        gap = gap/2`,

            heap: `function heapSort(arr):
    buildMaxHeap(arr)
    for i = n-1 downto 1:
        swap(arr[0], arr[i])
        heapSize = heapSize-1
        heapify(arr, 0, heapSize)

function buildMaxHeap(arr):
    heapSize = n
    for i = floor(n/2) downto 0:
        heapify(arr, i, heapSize)

function heapify(arr, i, heapSize):
    largest = i
    left = 2*i+1
    right = 2*i+2
    if left < heapSize and arr[left] > arr[largest]:
        largest = left
    if right < heapSize and arr[right] > arr[largest]:
        largest = right
    if largest != i:
        swap(arr[i], arr[largest])
        heapify(arr, largest, heapSize)`,

            merge: `function mergeSort(arr, left, right):
    if left < right:
        mid = floor((left+right)/2)
        mergeSort(arr, left, mid)
        mergeSort(arr, mid+1, right)
        merge(arr, left, mid, right)

function merge(arr, left, mid, right):
    n1 = mid-left+1
    n2 = right-mid
    create L[0..n1] and R[0..n2]
    for i=0 to n1-1:
        L[i] = arr[left+i]
    for j=0 to n2-1:
        R[j] = arr[mid+1+j]
    i=0, j=0, k=left
    while i<n1 and j<n2:
        if L[i] <= R[j]:
            arr[k] = L[i]
            i = i+1
        else:
            arr[k] = R[j]
            j = j+1
        k = k+1
    while i < n1:
        arr[k] = L[i]
        i = i+1
        k = k+1
    while j < n2:
        arr[k] = R[j]
        j = j+1
        k = k+1`
        };

        const pseudoCodeElement = document.getElementById('pseudo-code');
        const code = pseudoCodes[this.currentAlgorithm];

        if (code) {
            // 清空现有内容
            pseudoCodeElement.innerHTML = '';

            // 分割代码行并添加行号
            const lines = code.split('\n');
            let lineNumber = 1;

            lines.forEach(line => {
                const lineElement = document.createElement('div');
                lineElement.className = 'code-line';
                lineElement.dataset.line = lineNumber;

                const highlightedLine = this.highlightSyntax(line);
                lineElement.innerHTML = highlightedLine;

                pseudoCodeElement.appendChild(lineElement);
                lineNumber++;
            });
        } else {
            pseudoCodeElement.innerHTML = '<div class="code-placeholder">伪代码暂未提供</div>';
        }
    },

    // 添加语法高亮函数
    highlightSyntax: function (line) {
        // 先转义HTML特殊字符
        let escapedLine = line
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');

        // 高亮控制结构（使用不区分大小写的匹配）
        escapedLine = escapedLine.replace(
            /\b(function|for|while|if|else|return|downto|to|and|create)\b/gi,
            '<span class="code-keyword">$&</span>'
        );

        // 高亮函数名
        escapedLine = escapedLine.replace(
            /\b(insertionSort|shellSort|bubbleSort|quickSort|heapSort|mergeSort|partition|buildMaxHeap|heapify|merge|swap|floor|length)\b/gi,
            '<span class="code-function">$&</span>'
        );

        // 高亮变量名
        escapedLine = escapedLine.replace(
            /\b(arr|key|j|i|n|gap|temp|low|high|pivot|pi|heapSize|largest|left|right|mid|L|R|k|n1|n2)\b/gi,
            '<span class="code-variable">$&</span>'
        );

        // 高亮数字
        escapedLine = escapedLine.replace(
            /\b\d+\b/g,
            '<span class="code-number">$&</span>'
        );

        // 处理缩进：将制表符转换为4个空格
        escapedLine = escapedLine.replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');

        return escapedLine;
    },
    // 处理模式变化
    handleModeChange: function (event) {
        const mode = event.currentTarget.dataset.mode;

        // 更新按钮状态
        document.querySelectorAll('.mode-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        event.currentTarget.classList.add('active');

        // 更新当前模式
        this.currentMode = mode;

        // 切换中间栏显示
        if (mode === 'performance') {
            document.getElementById('teaching-visualization').style.display = 'none';
            document.getElementById('performance-visualization').style.display = 'block';

            // 确保右侧面板显示性能图表（如果需要）
            document.getElementById('performance-chart-section').style.display = 'block';

            // 初始化性能模式事件监听器
            this.setupPerformanceEventListeners();

            document.title = '排序算法性能对比';
        } else {
            document.getElementById('teaching-visualization').style.display = 'block';
            document.getElementById('performance-visualization').style.display = 'none';

            // 隐藏右侧性能图表（如果需要）
            document.getElementById('performance-chart-section').style.display = 'none';

            document.title = '排序算法可视化系统';
        }

        // 重置状态
        this.resetSort();

        // 更新按钮状态
        document.getElementById('reset-sort').disabled = !this.isSorting && this.currentStep === 0;

        Utils.logMessage(`已切换到${mode === 'teaching' ? '教学' : '性能'}模式`, 'info');
    },

    // 设置性能模式事件监听器
    setupPerformanceEventListeners: function () {
        // 性能模式标签切换
        document.querySelectorAll('.performance-tab').forEach(tab => {
            tab.addEventListener('click', this.handlePerformanceTabChange.bind(this));
        });

        // 性能模式文件选择
        document.getElementById('performance-file-input').addEventListener('change', this.handlePerformanceFileSelect.bind(this));

        // 性能模式文件解析
        document.getElementById('parse-performance-file').addEventListener('click', this.parsePerformanceFile.bind(this));
    },

    // 处理性能模式标签切换
    handlePerformanceTabChange: function (event) {
        const source = event.currentTarget.dataset.source;

        // 更新标签状态
        document.querySelectorAll('.performance-tab').forEach(tab => {
            tab.classList.remove('active');
        });
        event.currentTarget.classList.add('active');

        // 显示对应的面板
        document.querySelectorAll('.performance-panel').forEach(panel => {
            panel.classList.remove('active');
        });
        document.getElementById(`performance-${source}-panel`).classList.add('active');
    },

    // 处理性能模式文件选择
    handlePerformanceFileSelect: function (event) {
        const file = event.target.files[0];
        if (!file) return;

        // 检查文件大小（≤1MB）
        if (file.size > 1024 * 1024) {
            Utils.showError('文件大小超过1MB限制');
            event.target.value = ''; // 清空选择
            return;
        }

        const fileInfo = document.getElementById('performance-file-info');
        fileInfo.innerHTML = `
        <div><strong>${file.name}</strong></div>
        <div>大小: ${(file.size / 1024).toFixed(1)} KB</div>
        <div>类型: ${file.type || '文本文件'}</div>
    `;

        document.getElementById('parse-performance-file').disabled = false;
    },

    // 解析性能测试文件
    parsePerformanceFile: function () {
        const fileInput = document.getElementById('performance-file-input');
        const file = fileInput.files[0];

        if (!file) {
            Utils.showError('请先选择文件');
            return;
        }

        Utils.showLoading('正在解析性能测试数据...');

        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const content = e.target.result;
                // 简化解析逻辑
                const lines = content.split('\n');
                const testData = [];

                for (let line of lines) {
                    line = line.trim();
                    if (!line || line.startsWith('//') || line.startsWith('#')) continue;

                    // 尝试解析为数字
                    const num = parseFloat(line);
                    if (!isNaN(num)) {
                        testData.push(num);
                    }
                }

                if (testData.length < 10) {
                    Utils.showError(`数据量不足: 只找到${testData.length}个有效数据，至少需要10个`);
                    return;
                }

                // 限制数据量
                const maxSize = 10000;
                const finalData = testData.length > maxSize ? testData.slice(0, maxSize) : testData;

                if (testData.length > maxSize) {
                    Utils.logMessage(`数据量过大，已截取前${maxSize}个数据`, 'warning');
                }

                Utils.logMessage(`成功导入${finalData.length}个测试数据`, 'success');

                // 运行性能测试
                this.runPerformanceTestWithData(finalData);

            } catch (error) {
                Utils.showError(`文件解析失败: ${error.message}`);
            } finally {
                Utils.hideLoading();
            }
        };

        reader.onerror = () => {
            Utils.showError('读取文件失败');
            Utils.hideLoading();
        };

        reader.readAsText(file, 'UTF-8');
    },

    // 处理速度变化
    handleSpeedChange: function () {
        const speedControl = document.getElementById('speed-control');
        const speedValue = document.getElementById('speed-value');
        const value = parseInt(speedControl.value);

        // 显示速度值
        speedValue.textContent = (value / 1000).toFixed(1) + 's';
    },

    // 开始排序
    startSort: function () {
        if (this.currentData.length === 0) {
            Utils.showError('请先生成或上传数据');
            return;
        }

        if (this.currentData.length > 100) {
            Utils.showError('可视化最多支持100个数据，当前有' + this.currentData.length + '个');
            return;
        }

        if (!WebSocketManager.isConnected) {
            Utils.showError('请先连接到服务器');
            return;
        }

        if (this.isSorting) {
            return; // 已经在排序中
        }

        this.isSorting = true;

        // 更新按钮状态
        document.getElementById('start-sort').disabled = true;
        document.getElementById('reset-sort').disabled = false;

        // 获取排序参数
        const speed = document.getElementById('speed-control').value;
        const dataType = document.getElementById('data-type').value;

        // 获取比较器信息
        const comparatorInfo = {
            direction: ComparisonManager.currentDirection,
            method: ComparisonManager.currentMethod,
            description: ComparisonManager.getComparatorDescription()
        };

        // 如果是Person类型，获取排序字段
        if (dataType === 'Person') {
            comparatorInfo.structField = ComparisonManager.structField;
        }

        const requestData = {
            mode: this.currentMode.toUpperCase(),
            algorithm: this.currentAlgorithm.toUpperCase(),
            data: this.currentData,
            interval: parseInt(speed),
            dataType: dataType.toUpperCase() ? dataType.toUpperCase() : "PERSON",
            distribution: 'RANDOM',
            ascending: ComparisonManager.currentDirection === 'ascending',
            comparatorInfo: comparatorInfo
        };

        const success = WebSocketManager.sendSortRequest(requestData);

        if (success) {
            Utils.logMessage(`开始${this.currentAlgorithm}排序 (${comparatorInfo.description})`, 'info');
        } else {
            this.isSorting = false;
            document.getElementById('start-sort').disabled = false;
            document.getElementById('pause-sort').disabled = true;
        }
    },

    // 重置排序
    resetSort: function () {
        // 清除定时器
        if (this.sortInterval) {
            clearInterval(this.sortInterval);
            this.sortInterval = null;
        }

        // 重置状态
        this.isSorting = false;
        this.currentStep = 0;
        this.totalSteps = 0;

        // 重置统计
        this.stats = {
            comparisons: 0,
            swaps: 0,
            time: 0
        };

        // 更新按钮状态：启用开始按钮，禁用重置按钮
        document.getElementById('start-sort').disabled = false;
        document.getElementById('reset-sort').disabled = true;

        // 更新显示
        this.updateStatsDisplay();

        // 如果存在数据，重新显示原始数据
        if (this.currentData.length > 0) {
            Visualizer.update(this.currentData);
        }

        Utils.logMessage('排序已重置', 'info');
    },

    // 处理步骤更新
    handleStepUpdate: function (data) {
        if (!this.isSorting) return;

        this.currentStep = data.step || 0;
        this.totalSteps = data.totalSteps || 0;

        // 更新数据
        if (data.data && Array.isArray(data.data)) {
            this.currentData = data.data;

            // 对于Person结构体数据，需要特殊处理
            const isPersonData = data.data.length > 0 &&
                typeof data.data[0] === 'object' &&
                data.data[0].score !== undefined;

            if (isPersonData) {
                // 复制数据以避免直接修改
                const displayData = data.data.map(person => {
                    const field = ComparisonManager.structField || 'score';
                    const value = person[field];
                    // 如果值是字符串，转换为数字用于可视化
                    if (typeof value === 'string') {
                        return value.length * 10; // 简单的哈希
                    }
                    return typeof value === 'number' ? value : 0;
                });
                Visualizer.update(displayData, data.highlight || {});
            } else {
                Visualizer.update(this.currentData, data.highlight || {});
            }
        }

        // 更新统计
        if (data.stats) {
            this.stats.comparisons = data.stats.comparisons || 0;
            this.stats.swaps = data.stats.swaps || 0;
            this.stats.time = data.stats.time || 0;
        }

        // 更新显示
        this.updateStatsDisplay();

        // 如果是最后一步，完成排序
        if (this.currentStep >= this.totalSteps) {
            this.completeSort();
        }
    },

    // 处理性能结果
    handlePerformanceResult: function (data) {
        Utils.hideLoading();

        // 更新性能图表
        ChartRenderer.addResult(data);

        const algorithm = data.algorithm.toLowerCase();
        Utils.logMessage(`${algorithm}排序完成: ${data.time}ms, 比较: ${data.comparisons}, 交换: ${data.swaps}`, 'success');

        // 如果正在进行性能测试队列处理，继续下一个
        if (this.performanceQueue && this.performanceQueue.length > 0) {
            // 延迟1秒后继续下一个测试，给图表更新和显示留出时间
            setTimeout(() => {
                const testSize = document.getElementById('test-size').value;
                const distribution = document.getElementById('test-distribution').value;

                // 重新生成相同的数据（确保所有算法使用相同的数据）
                const testData = DataGenerator.generateData(
                    parseInt(testSize),
                    'int',
                    distribution,
                    1,
                    1000
                );

                this.processPerformanceQueue(testData, distribution);
            }, 1000);
        }
    },

    // 处理错误
    handleError: function (data) {
        Utils.hideLoading();
        this.resetSort();
    },

    // 处理连接
    handleConnect: function () {
        Utils.logMessage('WebSocket连接成功', 'success');
    },

    // 处理断开连接
    handleDisconnect: function () {
        if (this.isSorting) {
            this.resetSort();
            Utils.showError('与服务器的连接已断开');
        }
    },

    // 完成排序
    completeSort: function () {
        this.isSorting = false;

        // 更新按钮状态
        document.getElementById('start-sort').disabled = false;
        document.getElementById('reset-sort').disabled = false;

        // 显示完成消息
        Utils.logMessage('排序完成!', 'success');

        // 高亮所有已排序的元素
        const sortedHighlight = {};
        for (let i = 0; i < this.currentData.length; i++) {
            if (!sortedHighlight.sorted) sortedHighlight.sorted = [];
            sortedHighlight.sorted.push(i);
        }

        Visualizer.update(this.currentData, sortedHighlight);
    },

    // 更新统计显示
    updateStatsDisplay: function () {
        document.getElementById('comparisons-count').textContent = this.stats.comparisons;
        document.getElementById('swaps-count').textContent = this.stats.swaps;
        document.getElementById('time-count').textContent = this.stats.time + 'ms';
        document.getElementById('current-step').textContent = `${this.currentStep}/${this.totalSteps}`;
    },

    // 连接到服务器
    connectToServer: function () {
        const url = document.getElementById('server-url').value;

        if (!url) {
            Utils.showError('请输入服务器地址');
            return;
        }

        WebSocketManager.init(url);
    },

    // 运行性能测试
    runPerformanceTest: function () {
        if (!WebSocketManager.isConnected) {
            Utils.showError('请先连接到服务器');
            return;
        }

        const testSize = document.getElementById('test-size').value;
        const distribution = document.getElementById('test-distribution').value;

        Utils.showLoading('正在运行性能测试...');

        // 生成测试数据
        const testData = DataGenerator.generateData(
            parseInt(testSize),
            'int',
            distribution,
            1,
            1000
        );

        // 运行性能测试
        this.runPerformanceTestWithData(testData, distribution);
    },

    // 使用指定数据运行性能测试
    runPerformanceTestWithData: function (testData, distribution = 'custom') {
        if (!WebSocketManager.isConnected) {
            Utils.showError('请先连接到服务器');
            return;
        }

        // 重置性能数据
        ChartRenderer.clear();
        ChartRenderer.resetPerformanceData();

        // 获取所有算法
        const algorithms = ['insertion', 'shell', 'bubble', 'quick', 'heap', 'merge'];

        // 创建请求队列
        this.performanceQueue = algorithms.slice();
        this.performanceResults = {};
        this.currentPerformanceTest = null;

        // 存储测试数据
        this.performanceTestData = testData;

        // 开始处理队列
        this.processPerformanceQueue(testData, distribution);
    },

    // 处理性能测试队列
    processPerformanceQueue: function (testData, distribution) {
        if (this.performanceQueue.length === 0) {
            // 所有测试完成
            Utils.hideLoading();
            Utils.logMessage('性能测试完成！', 'success');
            return;
        }

        const algorithm = this.performanceQueue.shift();

        Utils.logMessage(`开始测试 ${algorithm} 排序...`, 'info');

        const requestData = {
            mode: 'PERFORMANCE',
            algorithm: algorithm.toUpperCase(),
            data: testData,
            dataType: 'INTEGER',
            distribution: distribution.toUpperCase()
        };

        // 设置当前测试的算法
        this.currentPerformanceTest = algorithm;

        // 延迟发送请求，确保前一个请求已处理完成
        setTimeout(() => {
            const success = WebSocketManager.sendSortRequest(requestData);

            if (!success) {
                Utils.logMessage(`${algorithm} 排序请求发送失败，跳过该测试`, 'error');
                // 继续处理队列
                setTimeout(() => {
                    this.processPerformanceQueue(testData, distribution);
                }, 500);
            }
        }, 300); // 300ms 延迟，确保服务器有足够时间处理
    },

    // 处理数据生成
    handleGenerateData: function () {
        const size = parseInt(document.getElementById('data-size').value);
        const type = document.getElementById('data-type').value;
        const distribution = document.getElementById('data-distribution').value;
        const min = parseInt(document.getElementById('min-value').value);
        const max = parseInt(document.getElementById('max-value').value);

        if (size > 100) {
            Utils.showError('可视化最多支持100个数据');
            return;
        }

        if (min >= max) {
            Utils.showError('最小值必须小于最大值');
            return;
        }

        // 根据类型生成数据
        if (type === 'Person') {
            const sortField = ComparisonManager.structField || 'score';
            this.currentData = DataGenerator.generatePersonData(size, sortField);
        } else {
            this.currentData = DataGenerator.generateData(size, type, distribution, min, max);
        }

        // 更新可视化
        Visualizer.update(this.currentData);

        // 重置排序状态
        this.resetSort();

        // 重置按钮应禁用，因为还没有开始排序
        document.getElementById('reset-sort').disabled = true;

        // 显示比较器信息
        const comparatorDesc = ComparisonManager.getComparatorDescription();
        Utils.logMessage(
            `已生成${size}个${type}类型数据 (${distribution}分布, ${comparatorDesc})`,
            'success'
        );

        // 如果是Person类型，显示字段选择器
        if (type === 'Person') {
            document.getElementById('struct-field-container').style.display = 'block';
        }
    },

    // 处理文件选择
    handleFileSelect: function (event) {
        const file = event.target.files[0];
        if (!file) return;

        const fileInfo = document.getElementById('file-info');
        fileInfo.innerHTML = `
            <div>文件名: ${file.name}</div>
            <div>文件大小: ${(file.size / 1024).toFixed(2)} KB</div>
        `;

        document.getElementById('parse-file').disabled = false;
    },

    // 处理文件解析
    handleFileParse: function () {
        const fileInput = document.getElementById('file-input');
        const file = fileInput.files[0];

        if (!file) {
            Utils.showError('请先选择文件');
            return;
        }
        FileParser.parseFile(file)
            .then(result => {
                if (result.error) {
                    Utils.showError(result.error);
                    return;
                }

                if (result.data.length > 100) {
                    Utils.showError(`文件包含${result.data.length}个数据，可视化最多支持100个`);
                    return;
                }

                this.currentData = result.data;

                // 更新数据类型选择
                document.getElementById('data-type').value = result.dataType;

                // 如果是Person类型，显示字段选择器
                if (result.dataType === 'person') {
                    document.getElementById('struct-field-container').style.display = 'block';
                }

                // 更新可视化
                Visualizer.update(this.currentData);

                // 重置排序状态
                this.resetSort();

                // 重置按钮应禁用
                document.getElementById('reset-sort').disabled = true;

                Utils.logMessage(`已从文件加载${result.data.length}个${result.dataType}类型数据`, 'success');
            })
            .catch(error => {
                Utils.showError(`文件解析失败: ${error.message}`);
            });
    },

    // 处理手动输入
    handleManualInput: function () {
        const inputText = document.getElementById('manual-data').value;
        const type = document.getElementById('manual-type').value;

        if (!inputText.trim()) {
            Utils.showError('请输入数据');
            return;
        }

        const lines = inputText.trim().split('\n').map(line => line.trim()).filter(line => line);

        if (lines.length > 100) {
            Utils.showError(`输入了${lines.length}个数据，可视化最多支持100个`);
            return;
        }

        const data = [];
        let hasError = false;

        lines.forEach((line, index) => {
            if (type === 'int') {
                const num = parseInt(line);
                if (isNaN(num)) {
                    Utils.showError(`第${index + 1}行不是有效的整数: ${line}`);
                    hasError = true;
                } else {
                    data.push(num);
                }
            } else if (type === 'double') {
                const num = parseFloat(line);
                if (isNaN(num)) {
                    Utils.showError(`第${index + 1}行不是有效的浮点数: ${line}`);
                    hasError = true;
                } else {
                    data.push(num);
                }
            }
        });

        if (hasError) return;

        this.currentData = data;

        // 更新数据类型选择
        document.getElementById('data-type').value = type;

        // 更新可视化
        Visualizer.update(this.currentData);

        // 重置排序状态
        this.resetSort();

        // 重置按钮应禁用
        document.getElementById('reset-sort').disabled = true;

        Utils.logMessage(`已从手动输入加载${data.length}个${type}类型数据`, 'success');
    },

    // 处理标签页切换
    handleMethodTabChange: function (event) {
        const method = event.currentTarget.dataset.method;

        // 更新标签状态
        document.querySelectorAll('.method-tab').forEach(tab => {
            tab.classList.remove('active');
        });
        event.currentTarget.classList.add('active');

        // 显示对应的面板
        document.querySelectorAll('.method-panel').forEach(panel => {
            panel.classList.remove('active');
        });
        document.getElementById(`${method}-panel`).classList.add('active');
    },

    // 处理窗口大小变化
    handleResize: function () {
        Visualizer.handleResize();

        // 重新绘制当前数据
        if (this.currentData.length > 0 && this.currentMode === 'teaching') {
            Visualizer.update(this.currentData);
        }

        // 如果是在性能模式，调整图表大小
        if (this.currentMode === 'performance' && ChartRenderer.chart) {
            ChartRenderer.chart.resize();
        }
    }
};