package com.sorting.visualization.websocket;

import com.sorting.visualization.algorithm.ComparatorFactory;
import com.sorting.visualization.algorithm.SortingAlgorithm;
import com.sorting.visualization.algorithm.impl.*;
import com.sorting.visualization.model.request.ControlRequest;
import com.sorting.visualization.model.request.SortRequest;
import com.sorting.visualization.model.response.ErrorResponse;
import com.sorting.visualization.model.response.PerformanceResult;
import com.sorting.visualization.model.response.SortComplete;
import com.sorting.visualization.model.response.StepUpdate;
import com.sorting.visualization.service.SortService;
import com.sorting.visualization.service.ExperimentService;
import com.sorting.visualization.service.BatchService;
import com.sorting.visualization.mapper.AlgorithmMapper;
import com.sorting.visualization.entity.AlgorithmEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sorting.visualization.util.DataValidator;
import com.sorting.visualization.util.JsonUtil;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MessageHandler {

    // 算法实例缓存
    private final Map<String, SortingAlgorithm<?>> algorithmCache = new ConcurrentHashMap<>();
    @Autowired
    private WebSocketSessionManager sessionManager;
    @Autowired
    private SortService sortService;
    @Autowired
    private DataValidator dataValidator;
    @Autowired
    private ExperimentService experimentService;
    @Autowired
    private BatchService batchService;
    @Autowired
    private AlgorithmMapper algorithmMapper;

    public MessageHandler() {
        // 初始化算法实例
        algorithmCache.put("BUBBLE", new BubbleSort<>());
        algorithmCache.put("INSERTION", new InsertionSort<>());
        algorithmCache.put("SHELL", new ShellSort<>());
        algorithmCache.put("QUICK", new QuickSort<>());
        algorithmCache.put("HEAP", new HeapSort<>());
        algorithmCache.put("MERGE", new MergeSort<>());
    }

    /**
     * 处理接收到的消息
     */
    public void handleMessage(String sessionId, Session session, String message) {
        try {
            // 解析消息类型
            Map<String, Object> messageMap = JsonUtil.fromJson(message, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
            String type = null;
            if (messageMap != null) {
                type = (String) messageMap.get("type");
            }

            if (type != null) {
                switch (type) {
                    case "SORT_REQUEST":
                        handleSortRequest(sessionId, session, message);
                        break;

                    case "CONTROL":
                        handleControlRequest(sessionId, session, message);
                        break;

                    default:
                        log.warn("未知消息类型: {}", type);
                        sendError(sessionId, "UNKNOWN_MESSAGE_TYPE", "未知消息类型: " + type, null);
                }
            }
        } catch (Exception e) {
            log.error("处理消息失败: sessionId={}, message={}, error={}",
                    sessionId, message, e.getMessage(), e);
            sendError(sessionId, "INTERNAL_ERROR", "处理消息失败: " + e.getMessage(), null);
        }
    }

    /**
     * 处理排序请求
     */
    @SuppressWarnings("unchecked")
    private void handleSortRequest(String sessionId, Session session, String message) {
        SortRequest request = JsonUtil.fromJson(message, SortRequest.class);

        if (request == null) {
            sendError(sessionId, "VALIDATION_ERROR", "无法解析排序请求", null);
            return;
        }

        try {
            // 验证请求
            dataValidator.validateSortRequest(request);

            // 检查会话是否正在处理其他请求
            if (sessionManager.isProcessing(sessionId)) {
                sendError(sessionId, "VALIDATION_ERROR",
                        "当前会话正在处理其他请求，请等待完成或停止当前请求", request.getRequestId());
                return;
            }

            // 转换数据类型（这里会处理INT和INTEGER的兼容性）
            List<Object> convertedData = dataValidator.convertData(request.getData(), request.getDataType());

            // 获取算法实例
            SortingAlgorithm<?> algorithm = algorithmCache.get(request.getAlgorithm().toUpperCase());
            if (algorithm == null) {
                sendError(sessionId, "UNSUPPORTED_ALGORITHM",
                        "不支持的算法: " + request.getAlgorithm(), request.getRequestId());
                return;
            }

            // 创建比较器
            Comparator<Object> comparator = ComparatorFactory.createComparator(
                    request.getDataType(), request.getComparatorInfo());

            // 根据模式处理
            if ("TEACHING".equals(request.getMode())) {
                handleTeachingMode(sessionId, request, convertedData, algorithm, comparator);
            } else if ("PERFORMANCE".equals(request.getMode())) {
                handlePerformanceMode(sessionId, request, convertedData, algorithm, comparator);
            } else {
                sendError(sessionId, "VALIDATION_ERROR",
                        "无效的模式: " + request.getMode(), request.getRequestId());
            }

        } catch (DataValidator.ValidationException e) {
            sendError(sessionId, e.getCode(), e.getMessage(), request.getRequestId());
        } catch (Exception e) {
            log.error("处理排序请求失败: sessionId={}, requestId={}, error={}",
                    sessionId, request.getRequestId(), e.getMessage(), e);
            sendError(sessionId, "INTERNAL_ERROR", "处理排序请求失败: " + e.getMessage(), request.getRequestId());
        }
    }

    /**
     * 处理教学模式
     */
    @SuppressWarnings("unchecked")
    private void handleTeachingMode(String sessionId, SortRequest request, List<Object> data,
                                    SortingAlgorithm<?> algorithm, Comparator<Object> comparator) {
        log.info("开始教学模式处理: sessionId={}, requestId={}, algorithm={}, dataSize={}",
                sessionId, request.getRequestId(), request.getAlgorithm(), data.size());

        // 标记会话开始处理（存储初始 interval 和 dataSize）
        boolean saveReplay = request.getSaveReplay() != null && request.getSaveReplay();
        sessionManager.startProcessing(sessionId, request.getRequestId(),
                request.getAlgorithm(), request.getMode(),
                request.getInterval() != null ? request.getInterval() : 1000,
                data.size(), saveReplay);

        // 异步执行排序
        sessionManager.getExecutorService().submit(() -> {
            try {
                // 执行排序算法
                SortingAlgorithm<Object> algo = (SortingAlgorithm<Object>) algorithm;
                SortingAlgorithm.TeachingResult<Object> result = algo.teach(data, comparator);

                // 发送步骤更新
                sendTeachingSteps(sessionId, request.getRequestId(), result.getSteps(),
                        request.getInterval(), result);

                // 标记会话处理完成
                sessionManager.stopProcessing(sessionId);

            } catch (Exception e) {
                log.error("教学模式排序失败: sessionId={}, requestId={}, error={}",
                        sessionId, request.getRequestId(), e.getMessage(), e);
                sendError(sessionId, "ALGORITHM_ERROR", "排序算法执行失败: " + e.getMessage(), request.getRequestId());
                sessionManager.stopProcessing(sessionId);
            }
        });
    }

    /**
     * 发送教学步骤
     */
    private void sendTeachingSteps(String sessionId, String requestId,
                                   List<StepUpdate> steps, int interval,
                                   SortingAlgorithm.TeachingResult<?> result) {
        try {
            // 先发送初始状态
            StepUpdate firstStep = steps.get(0);
            firstStep.setRequestId(requestId);
            firstStep.setTimestamp(System.currentTimeMillis());
            sessionManager.sendMessage(sessionId, firstStep);

            // 按间隔发送后续步骤
            for (int i = 1; i < steps.size(); i++) {
                // 使用事件驱动等待替代忙等轮询（零 CPU 开销）
                SessionState state = sessionManager.getSessionState(sessionId);
                if (state != null) {
                    state.waitIfPaused();
                }

                // 检查是否停止
                if (!sessionManager.isProcessing(sessionId)) {
                    log.info("排序被停止: sessionId={}, requestId={}", sessionId, requestId);
                    // 停止时保存部分结果
                    saveTeachingToDbOnStop(sessionId, state, result);
                    return;
                }

                // 从会话状态动态读取间隔（支持暂停时调整）
                int currentInterval = (state != null) ? state.getInterval() : interval;
                Thread.sleep(currentInterval);

                StepUpdate step = steps.get(i);
                step.setRequestId(requestId);
                step.setTimestamp(System.currentTimeMillis());

                // 更新会话步骤
                state = sessionManager.getSessionState(sessionId);
                if (state != null) {
                    state.updateStep(step.getStep());
                }

                sessionManager.sendMessage(sessionId, step);
            }

            // 发送完成消息
            sendSortComplete(sessionId, requestId, result);

            // 保存教学实验到数据库
            saveTeachingToDb(sessionId, result);

        } catch (InterruptedException e) {
            log.info("排序被中断: sessionId={}, requestId={}", sessionId, requestId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("发送教学步骤失败: sessionId={}, requestId={}, error={}",
                    sessionId, requestId, e.getMessage(), e);
        }
    }

    /**
     * 处理性能模式
     */
    @SuppressWarnings("unchecked")
    private void handlePerformanceMode(String sessionId, SortRequest request,
                                       List<Object> data, SortingAlgorithm<?> algorithm,
                                       Comparator<Object> comparator) {
        log.info("开始性能模式处理: sessionId={}, requestId={}, algorithm={}, dataSize={}",
                sessionId, request.getRequestId(), request.getAlgorithm(), data.size());

        // 异步执行排序
        sessionManager.getExecutorService().submit(() -> {
            try {
                // 执行排序算法
                SortingAlgorithm<Object> algo = (SortingAlgorithm<Object>) algorithm;
                SortingAlgorithm.PerformanceResult<Object> result = algo.perform(data, comparator);

                // 发送性能结果
                sendPerformanceResult(sessionId, request, result);

            } catch (Exception e) {
                log.error("性能模式排序失败: sessionId={}, requestId={}, error={}",
                        sessionId, request.getRequestId(), e.getMessage(), e);
                sendError(sessionId, "ALGORITHM_ERROR", "排序算法执行失败: " + e.getMessage(), request.getRequestId());
            }
        });
    }

    /**
     * 发送性能结果
     */
    private void sendPerformanceResult(String sessionId, SortRequest request,
                                       SortingAlgorithm.PerformanceResult<Object> result) {
        PerformanceResult response = new PerformanceResult();
        response.setRequestId(request.getRequestId());
        response.setAlgorithm(request.getAlgorithm());
        response.setTime(result.getTime());
        response.setComparisons(result.getComparisons());
        response.setSwaps(result.getSwaps());
        response.setDataSize(request.getData().size());
        response.setDistribution(request.getDistribution());
        response.setSortedData((List<Object>) result.getSortedData());
        response.setSorted(true);
        response.setTimestamp(System.currentTimeMillis());

        sessionManager.sendMessage(sessionId, response);

        // 保存性能结果到数据库
        try {
            SessionState state = sessionManager.getSessionState(sessionId);
            if (state != null) {
                Long userId = state.getUserId() != null ? state.getUserId() : 1L;
                Long algoId = getAlgoId(request.getAlgorithm());
                if (algoId != null) {
                    // 规范化数据类型（INT -> INTEGER 适配数据库 ENUM）
                    String dt = request.getDataType();
                    if ("INT".equalsIgnoreCase(dt)) dt = "INTEGER";
                    else if (dt != null) dt = dt.toUpperCase();
                    // 规范化分布字段
                    String dist = request.getDistribution();
                    if (dist != null) dist = dist.toUpperCase();
                    else dist = "RANDOM";
                    batchService.saveBatch(userId, request.getData().size(),
                            dist, dt,
                            List.of(response), List.of(algoId));
                }
            }
        } catch (Exception e) {
            log.error("保存性能结果失败: sessionId={}", sessionId, e);
        }

        log.info("性能模式完成: sessionId={}, requestId={}, algorithm={}, time={}ms, comparisons={}, swaps={}",
                sessionId, request.getRequestId(), request.getAlgorithm(),
                result.getTime(), result.getComparisons(), result.getSwaps());
    }

    /**
     * 发送排序完成消息
     */
    private void sendSortComplete(String sessionId, String requestId,
                                  SortingAlgorithm.TeachingResult<?> result) {
        SortComplete response = new SortComplete();
        response.setRequestId(requestId);
        response.setMessage("排序完成");

        SortComplete.FinalStatistics stats = new SortComplete.FinalStatistics();
        stats.setTotalComparisons(result.getTotalComparisons());
        stats.setTotalSwaps(result.getTotalSwaps());
        stats.setTotalTime(result.getTotalTime());
        stats.setAlgorithm("排序算法");
        response.setFinalStats(stats);

        response.setTimestamp(System.currentTimeMillis());

        sessionManager.sendMessage(sessionId, response);

        log.info("教学模式完成: sessionId={}, requestId={}, totalSteps={}, totalTime={}ms, comparisons={}, swaps={}",
                sessionId, requestId, result.getSteps().size(),
                result.getTotalTime(), result.getTotalComparisons(), result.getTotalSwaps());
    }

    /**
     * 处理控制请求（发送确认消息给前端）
     */
    private void handleControlRequest(String sessionId, Session session, String message) {
        ControlRequest request = JsonUtil.fromJson(message, ControlRequest.class);

        if (request == null) {
            sendError(sessionId, "VALIDATION_ERROR", "无法解析控制请求", null);
            return;
        }

        String action = request.getAction();
        String requestId = request.getRequestId();

        switch (action.toUpperCase()) {
            case "PAUSE":
                sessionManager.pauseProcessing(sessionId);
                sendStatusMessage(sessionId, "PAUSED", "排序已暂停");
                log.info("暂停排序: sessionId={}, requestId={}", sessionId, requestId);
                break;

            case "RESUME":
                // 如果有新间隔值，先更新再恢复（Phase 4：暂停时调参）
                if (request.getInterval() != null && request.getInterval() >= 100 && request.getInterval() <= 5000) {
                    SessionState st = sessionManager.getSessionState(sessionId);
                    if (st != null) {
                        st.setInterval(request.getInterval());
                        log.info("更新步进间隔: sessionId={}, interval={}ms", sessionId, request.getInterval());
                    }
                }
                sessionManager.resumeProcessing(sessionId);
                sendStatusMessage(sessionId, "RESUMED", "排序已继续");
                log.info("恢复排序: sessionId={}, requestId={}", sessionId, requestId);
                break;

            case "STOP":
                sessionManager.stopProcessing(sessionId);
                sendStatusMessage(sessionId, "STOPPED", "排序已停止");
                log.info("停止排序: sessionId={}, requestId={}", sessionId, requestId);
                break;

            case "STEP_FORWARD":
                sessionManager.stepForward(sessionId);
                log.info("单步执行: sessionId={}, requestId={}", sessionId, requestId);
                break;

            default:
                log.warn("未知控制动作: {}", action);
                sendError(sessionId, "VALIDATION_ERROR", "未知控制动作: " + action, requestId);
        }
    }

    /**
     * 发送状态消息（PAUSED/RESUMED/STOPPED）
     */
    private void sendStatusMessage(String sessionId, String type, String message) {
        Map<String, Object> statusMsg = new java.util.HashMap<>();
        statusMsg.put("type", type);
        statusMsg.put("message", message);
        statusMsg.put("timestamp", System.currentTimeMillis());
        sessionManager.sendMessage(sessionId, statusMsg);
    }

    /**
     * 发送错误消息
     */
    private void sendError(String sessionId, String code, String message, String requestId) {
        ErrorResponse error = new ErrorResponse();
        error.setRequestId(requestId);
        error.setMessage(message);
        error.setCode(code);
        error.setTimestamp(System.currentTimeMillis());

        sessionManager.sendMessage(sessionId, error);

        log.warn("发送错误消息: sessionId={}, code={}, message={}", sessionId, code, message);
    }

    /** 保存教学实验到数据库 */
    private void saveTeachingToDb(String sessionId, SortingAlgorithm.TeachingResult<?> result) {
        try {
            SessionState state = sessionManager.getSessionState(sessionId);
            if (state == null) return;
            Long userId = state.getUserId() != null ? state.getUserId() : 1L;
            String algoName = state.getCurrentAlgorithm();
            Long algoId = getAlgoId(algoName);
            if (algoId == null) return;

            if (state.getSaveReplay() != null && state.getSaveReplay()) {
                // 保存实验 + 步骤快照
                experimentService.saveExperimentWithSteps(
                        userId, algoId, state.getDataSize(),
                        result.getSteps(), state.getInterval(), "COMPLETED");
            } else {
                experimentService.saveExperiment(
                        userId, algoId,
                        state.getDataSize() != null ? state.getDataSize() : 0,
                        result.getSteps().size(),
                        result.getTotalComparisons(),
                        result.getTotalSwaps(),
                        result.getTotalTime(),
                        state.getInterval(),
                        "COMPLETED");
            }
        } catch (Exception e) {
            log.error("保存教学实验失败: sessionId={}", sessionId, e);
        }
    }

    /** 根据算法名称查询 algo_id */
    private Long getAlgoId(String algoName) {
        try {
            AlgorithmEntity algo = algorithmMapper.selectOne(
                    new LambdaQueryWrapper<AlgorithmEntity>().eq(AlgorithmEntity::getAlgoCode, algoName));
            return algo != null ? algo.getAlgoId() : null;
        } catch (Exception e) {
            return null;
        }
    }
    /** 停止时保存教学实验（部分结果） */
    private void saveTeachingToDbOnStop(String sessionId, SessionState s, SortingAlgorithm.TeachingResult<?> result) {
        try {
            if (s == null) return;
            String algoName = s.getCurrentAlgorithm();
            Long algoId = getAlgoId(algoName);
            if (algoId == null) return;
            Long userId = s.getUserId() != null ? s.getUserId() : 1L;
            int dataSize = s.getDataSize() != null ? s.getDataSize() : 0;
            experimentService.saveExperiment(
                    userId, algoId,
                    dataSize,
                    s.getCurrentStep(),
                    result.getTotalComparisons(),
                    result.getTotalSwaps(),
                    result.getTotalTime(),
                    s.getInterval(),
                    "STOPPED");
            log.info("已保存停止的实验: sessionId={}, steps={}", sessionId, s.getCurrentStep());
        } catch (Exception e) {
            log.error("保存停止实验失败: sessionId={}", sessionId, e);
        }
    }}