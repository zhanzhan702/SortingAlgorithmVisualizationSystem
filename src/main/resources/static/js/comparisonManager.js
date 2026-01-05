// 比较器管理模块
const ComparisonManager = {
    currentDirection: 'ascending',
    currentMethod: 'numeric',
    customFunction: null,
    structField: 'score',

    // 初始化
    init: function () {
        this.setupEventListeners();
        this.loadDefaultComparators();
    },

    // 设置事件监听器
    setupEventListeners: function () {
        // 排序方向
        document.querySelectorAll('input[name="sort-direction"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.currentDirection = e.target.value;
                Utils.logMessage(`排序方向设置为: ${this.currentDirection === 'ascending' ? '升序' : '降序'}`, 'info');
            });
        });

        // 比较方法
        document.getElementById('comparison-method').addEventListener('change', (e) => {
            this.currentMethod = e.target.value;
            Utils.logMessage(`比较方法设置为: ${this.getMethodDisplayName()}`, 'info');
        });

        // Person结构体字段选择
        const structFieldSelect = document.getElementById('struct-field');
        if (structFieldSelect) {
            structFieldSelect.addEventListener('change', (e) => {
                this.structField = e.target.value;
                Utils.logMessage(`Person排序字段设置为: ${e.target.value}`, 'info');
            });
        }
    },

    // 加载默认比较器
    loadDefaultComparators: function () {
        // 这些是预定义的比较函数
        this.defaultComparators = {
            // 数值比较
            numeric: {
                ascending: (a, b) => a < b,
                descending: (a, b) => a > b
            },
            // 绝对值比较
            absolute: {
                ascending: (a, b) => Math.abs(a) < Math.abs(b),
                descending: (a, b) => Math.abs(a) > Math.abs(b)
            },
            // 反向比较（用于测试）
            reverse: {
                ascending: (a, b) => a > b,  // 实际上是降序
                descending: (a, b) => a < b  // 实际上是升序
            }
        };
    },

    // 获取当前比较器
    getComparator: function (dataType) {
        // 对于结构体类型，使用特殊的比较器
        if (dataType === 'Person') {
            return this.getPersonComparator();
        }

        // 对于数值类型，使用预定义的比较器
        const comparatorSet = this.defaultComparators[this.currentMethod] || this.defaultComparators.numeric;
        return comparatorSet[this.currentDirection];
    },

    // 获取Person结构体比较器
    getPersonComparator: function () {
        const field = this.structField;
        const direction = this.currentDirection;

        return (a, b) => {
            const valueA = a[field];
            const valueB = b[field];

            // 对于字符串字段，使用localeCompare
            if (typeof valueA === 'string' && typeof valueB === 'string') {
                if (direction === 'ascending') {
                    return valueA.localeCompare(valueB) < 0;
                } else {
                    return valueA.localeCompare(valueB) > 0;
                }
            }

            // 对于数值字段
            if (direction === 'ascending') {
                return valueA < valueB;
            } else {
                return valueA > valueB;
            }
        };
    },

    // 获取比较器描述
    getComparatorDescription: function () {
        const directionText = this.currentDirection === 'ascending' ? '升序' : '降序';
        const methodText = this.getMethodDisplayName();

        return `${directionText} - ${methodText}`;
    },

    // 获取方法显示名称
    getMethodDisplayName: function () {
        const names = {
            'numeric': '数值比较',
            'absolute': '绝对值比较',
            'reverse': '反向比较'
        };

        return names[this.currentMethod] || this.currentMethod;
    },

    // 为结构体数据生成字段选择器
    generateStructFieldSelector: function (structType) {
        if (structType !== 'Person') return;

        const container = document.getElementById('struct-field-container');
        if (!container) return;

        container.style.display = 'block';

        // 添加事件监听（如果尚未添加）
        const selector = document.getElementById('struct-field');
        if (selector && !selector.hasListener) {
            selector.addEventListener('change', (e) => {
                this.structField = e.target.value;
                Utils.logMessage(`Person排序字段设置为: ${e.target.value}`, 'info');
            });
            selector.hasListener = true;
        }
    },

    // 重置为默认值
    reset: function () {
        this.currentDirection = 'ascending';
        this.currentMethod = 'numeric';
        this.customFunction = null;
        this.structField = 'score';

        // 更新UI
        document.querySelector('input[name="sort-direction"][value="ascending"]').checked = true;
        document.getElementById('comparison-method').value = 'numeric';
        document.getElementById('struct-field-container').style.display = 'none';

        Utils.logMessage('比较器已重置为默认值', 'info');
    }
};