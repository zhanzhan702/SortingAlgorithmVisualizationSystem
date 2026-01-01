package com.sorting.visualization.websocket;

import jakarta.websocket.Session;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class SessionState {
    private String sessionId;
    private Session session;
    private String currentRequestId;
    private String currentAlgorithm;
    private String currentMode;
    private AtomicBoolean isProcessing;
    private AtomicBoolean isPaused;
    private AtomicInteger currentStep;
    private Long startTime;
    private ExecutorService processingThread;

    public SessionState(String sessionId, Session session) {
        this.sessionId = sessionId;
        this.session = session;
        this.isProcessing = new AtomicBoolean(false);
        this.isPaused = new AtomicBoolean(false);
        this.currentStep = new AtomicInteger(0);
        this.startTime = null;
    }

    /**
     * 开始处理
     */
    public void startProcessing(String requestId, String algorithm, String mode) {
        this.currentRequestId = requestId;
        this.currentAlgorithm = algorithm;
        this.currentMode = mode;
        this.isProcessing.set(true);
        this.isPaused.set(false);
        this.currentStep.set(0);
        this.startTime = System.currentTimeMillis();
        log.info("开始处理请求: sessionId={}, requestId={}, algorithm={}, mode={}",
                sessionId, requestId, algorithm, mode);
    }

    /**
     * 暂停处理
     */
    public void pauseProcessing() {
        if (isProcessing.get()) {
            isPaused.set(true);
            log.info("暂停处理: sessionId={}, requestId={}", sessionId, currentRequestId);
        }
    }

    /**
     * 恢复处理
     */
    public void resumeProcessing() {
        if (isProcessing.get() && isPaused.get()) {
            isPaused.set(false);
            log.info("恢复处理: sessionId={}, requestId={}", sessionId, currentRequestId);
        }
    }

    /**
     * 停止处理
     */
    public void stopProcessing() {
        if (isProcessing.get()) {
            isProcessing.set(false);
            isPaused.set(false);
            if (processingThread != null && !processingThread.isShutdown()) {
                processingThread.shutdownNow();
            }
            log.info("停止处理: sessionId={}, requestId={}", sessionId, currentRequestId);
        }
    }

    /**
     * 检查是否正在处理
     */
    public boolean isProcessing() {
        return isProcessing.get();
    }

    /**
     * 检查是否已暂停
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /**
     * 更新步骤
     */
    public void updateStep(int step) {
        this.currentStep.set(step);
    }

    /**
     * 获取当前步骤
     */
    public int getCurrentStep() {
        return currentStep.get();
    }

    /**
     * 获取运行时间
     */
    public long getRunningTime() {
        if (startTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 清理状态
     */
    public void clear() {
        stopProcessing();
        this.currentRequestId = null;
        this.currentAlgorithm = null;
        this.currentMode = null;
        this.startTime = null;
        this.currentStep.set(0);
    }
}