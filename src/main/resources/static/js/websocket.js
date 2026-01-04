// WebSocket通信管理器
const WebSocketManager = {
    socket: null,
    reconnectAttempts: 0,
    maxReconnectAttempts: 5,
    reconnectDelay: 3000,
    isConnected: false,
    isSending: false, // 添加发送状态标志
    messageQueue: [], // 添加消息队列
    callbacks: {
        onStepUpdate: null,
        onPerformanceResult: null,
        onError: null,
        onConnect: null,
        onDisconnect: null
    },

    // 初始化WebSocket连接
    init: function (url) {
        if (this.socket) {
            this.socket.close();
        }

        this.updateStatus('connecting');
        Utils.logMessage(`正在连接到服务器: ${url}`, 'info');

        try {
            this.socket = new WebSocket(url);
            this.setupEventHandlers();
        } catch (error) {
            Utils.showError(`无法创建WebSocket连接: ${error.message}`);
            this.updateStatus('disconnected');
        }
    },

    // 设置事件处理器
    setupEventHandlers: function () {
        const self = this;

        this.socket.onopen = function () {
            self.handleConnect();
        };

        this.socket.onmessage = function (event) {
            self.handleMessage(event);
        };

        this.socket.onerror = function (error) {
            self.handleError(error);
        };

        this.socket.onclose = function () {
            self.handleClose();
        };
    },

    // 处理连接成功
    handleConnect: function () {
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.updateStatus('connected');

        // 连接成功后发送队列中的消息
        this.processMessageQueue();

        Utils.logMessage('已连接到服务器', 'success');

        if (this.callbacks.onConnect) {
            this.callbacks.onConnect();
        }
    },

    // 处理收到消息
    handleMessage: function (event) {
        try {
            const data = JSON.parse(event.data);
            Utils.logMessage(`收到消息: ${data.type}`, 'info');

            switch (data.type) {
                case 'STEP_UPDATE':
                    if (this.callbacks.onStepUpdate) {
                        this.callbacks.onStepUpdate(data);
                    }
                    break;

                case 'PERFORMANCE_RESULT':
                    if (this.callbacks.onPerformanceResult) {
                        this.callbacks.onPerformanceResult(data);
                    }
                    break;

                case 'ERROR':
                    Utils.showError(`服务器错误: ${data.message}`);
                    Utils.logMessage(`服务器错误: ${data.message}`, 'error');

                    if (this.callbacks.onError) {
                        this.callbacks.onError(data);
                    }
                    break;

                default:
                    Utils.logMessage(`未知消息类型: ${data.type}`, 'warning');
            }
        } catch (error) {
            Utils.logMessage(`消息解析失败: ${error.message}`, 'error');
        }
    },

    // 处理错误
    handleError: function (error) {
        Utils.logMessage(`WebSocket错误: ${error.type}`, 'error');
        this.updateStatus('disconnected');
    },

    // 处理连接关闭
    handleClose: function () {
        this.isConnected = false;
        this.updateStatus('disconnected');

        Utils.logMessage('连接已关闭', 'warning');

        if (this.callbacks.onDisconnect) {
            this.callbacks.onDisconnect();
        }

        // 尝试重新连接
        if (!this.isConnected && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * this.reconnectAttempts;

            Utils.logMessage(`将在 ${delay / 1000} 秒后尝试重新连接 (尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts})`, 'info');

            setTimeout(() => {
                const url = document.getElementById('server-url').value;
                this.init(url);
            }, delay);
        } else {
            Utils.showError('无法连接到服务器，请检查服务器地址和网络连接');
        }
    },

    // 发送消息（带队列机制）
    send: function (data) {
        // 如果正在发送，将消息加入队列
        if (this.isSending) {
            this.messageQueue.push(data);
            Utils.logMessage(`消息已加入队列: ${data.type}，当前队列长度: ${this.messageQueue.length}`, 'info');
            return false;
        }

        if (!this.isConnected || !this.socket) {
            Utils.showError('未连接到服务器，请先连接');
            return false;
        }

        try {
            this.isSending = true;
            const message = JSON.stringify(data);
            this.socket.send(message);
            Utils.logMessage(`发送消息: ${data.type}`, 'info');

            // 发送完成后，延迟一段时间再允许发送下一条消息
            setTimeout(() => {
                this.isSending = false;
                this.processMessageQueue();
            }, 100); // 100ms 延迟

            return true;
        } catch (error) {
            this.isSending = false;
            Utils.showError(`发送消息失败: ${error.message}`);
            return false;
        }
    },

    // 处理消息队列
    processMessageQueue: function () {
        if (this.messageQueue.length === 0 || this.isSending || !this.isConnected) {
            return;
        }

        // 从队列中取出第一条消息
        const data = this.messageQueue.shift();
        this.send(data);
    },

    // 发送排序请求
    sendSortRequest: function (requestData) {
        const request = {
            requestId: Utils.generateUUID(),
            type: 'SORT_REQUEST',
            ...requestData
        };

        return this.send(request);
    },

    // 更新连接状态显示
    updateStatus: function (status) {
        const indicator = document.getElementById('status-indicator');
        const dot = indicator.querySelector('.status-dot');
        const text = indicator.querySelector('.status-text');

        dot.className = 'status-dot';
        dot.classList.add(status);

        switch (status) {
            case 'connected':
                text.textContent = '已连接';
                break;
            case 'connecting':
                text.textContent = '连接中...';
                break;
            case 'disconnected':
                text.textContent = '未连接';
                break;
        }

        this.isConnected = status === 'connected';
    },

    // 注册回调函数
    on: function (event, callback) {
        if (this.callbacks.hasOwnProperty(event)) {
            this.callbacks[event] = callback;
        } else {
            console.warn(`未知事件: ${event}`);
        }
    },

    // 关闭连接
    close: function () {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        this.isConnected = false;
        this.isSending = false;
        this.messageQueue = [];
        this.updateStatus('disconnected');
    }
};