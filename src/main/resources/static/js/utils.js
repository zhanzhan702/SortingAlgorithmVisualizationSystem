// 工具函数模块
const Utils = {
    // 生成UUID
    generateUUID: function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },

    // 格式化时间
    formatTime: function (ms) {
        if (ms < 1000) return `${ms}ms`;
        const seconds = (ms / 1000).toFixed(2);
        return `${seconds}s`;
    },

    // 格式化数字
    formatNumber: function (num) {
        if (num >= 1000000) return `${(num / 1000000).toFixed(2)}M`;
        if (num >= 1000) return `${(num / 1000).toFixed(2)}K`;
        return num.toString();
    },

    // 深拷贝对象
    deepClone: function (obj) {
        return JSON.parse(JSON.stringify(obj));
    },

    // 数组洗牌（Fisher-Yates算法）
    shuffleArray: function (array) {
        const newArray = [...array];
        for (let i = newArray.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [newArray[i], newArray[j]] = [newArray[j], newArray[i]];
        }
        return newArray;
    },

    // 创建正态分布数据
    createNormalDistribution: function (count, mean, stdDev) {
        const data = [];
        for (let i = 0; i < count; i++) {
            let u = 0, v = 0;
            while (u === 0) u = Math.random();
            while (v === 0) v = Math.random();
            const num = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
            data.push(Math.max(0, Math.min(100, mean + num * stdDev)));
        }
        return data;
    },

    // 检查是否为有效数字
    isValidNumber: function (value) {
        return !isNaN(parseFloat(value)) && isFinite(value);
    },

    // 限制数值在范围内
    clamp: function (value, min, max) {
        return Math.min(Math.max(value, min), max);
    },

    // 显示错误消息
    showError: function (message) {
        const modal = document.getElementById('error-modal');
        const errorMessage = document.getElementById('error-message');
        errorMessage.textContent = message;
        modal.classList.add('active');
    },

    // 隐藏错误消息
    hideError: function () {
        const modal = document.getElementById('error-modal');
        modal.classList.remove('active');
    },

    // 显示加载指示器
    showLoading: function (message = '正在处理...') {
        const overlay = document.getElementById('loading-overlay');
        const loadingMessage = document.getElementById('loading-message');
        loadingMessage.textContent = message;
        overlay.style.display = 'flex';
    },

    // 隐藏加载指示器
    hideLoading: function () {
        const overlay = document.getElementById('loading-overlay');
        overlay.style.display = 'none';
    },

    // 添加日志消息
    logMessage: function (message, type = 'info') {
        const logContainer = document.getElementById('log-messages');
        const logEntry = document.createElement('div');
        logEntry.className = `log-message ${type}`;

        const timestamp = new Date().toLocaleTimeString();
        logEntry.textContent = `[${timestamp}] ${message}`;

        logContainer.appendChild(logEntry);
        logContainer.scrollTop = logContainer.scrollHeight;
    },

    // 清空日志
    clearLog: function () {
        const logContainer = document.getElementById('log-messages');
        logContainer.innerHTML = '';
    }
};

// 错误模态框事件处理
document.addEventListener('DOMContentLoaded', function () {
    const modal = document.getElementById('error-modal');
    const closeBtn = modal.querySelector('.close-modal');
    const confirmBtn = document.getElementById('confirm-error');

    closeBtn.addEventListener('click', Utils.hideError);
    confirmBtn.addEventListener('click', Utils.hideError);

    // 点击模态框背景关闭
    modal.addEventListener('click', function (e) {
        if (e.target === modal) {
            Utils.hideError();
        }
    });
});