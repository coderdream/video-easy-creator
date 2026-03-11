package com.coderdream.util.minimax;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * MiniMax 工具门面类
 * <p>
 * 封装所有与 MiniMax API 的交互，提供统一的调用入口：
 * <ul>
 *   <li>{@link #callWithFallback(String)} - 带模型降级链的文本生成（主入口）</li>
 *   <li>{@link #callWithFallback(MiniMaxRequest)} - 请求对象版本</li>
 *   <li>{@link #fetchModelsRaw()} - 从官方 API 获取模型列表原始 JSON</li>
 *   <li>{@link #getModelsWithUseCases()} - 模型列表（含使用场景说明）</li>
 *   <li>{@link #getModelIds()} - 仅返回模型 ID 列表</li>
 *   <li>{@link #getConfigInfo()} - 当前配置信息</li>
 *   <li>{@link #isConfigValid()} - 配置有效性检查</li>
 * </ul>
 * <p>
 * 降级策略（{@link #callWithFallback}）：
 * <pre>
 *   主力模型 (MiniMax-M2.5)
 *     → 连续失败 {@value #SWITCH_THRESHOLD} 次 → 降级模型 (MiniMax-M2.1)
 *     → 降级模型再失败 → 返回异常描述字符串
 * </pre>
 *
 * @author Claude Code
 * @since 2026-03-11
 */
@Slf4j
public class MiniMaxUtil {

    /** 单次模型最大重试次数，超过后切换下一级模型 */
    private static final int SWITCH_THRESHOLD = 3;

    /** 重试间隔（毫秒） */
    private static final int RETRY_INTERVAL_MS = 5000;

    /** 异常返回值前缀，业务层用此判断调用是否成功 */
    public static final String ERROR_PREFIX = "API 调用发生异常";

    /** API 客户端单例 */
    private static final MiniMaxApiClient CLIENT = createClient();

    private static MiniMaxApiClient createClient() {
        if (!ConfigUtil.isConfigValid()) {
            log.error("MiniMax 配置无效！请检查 config.properties 或环境变量");
            throw new RuntimeException("MiniMax 配置无效");
        }
        log.info("初始化 MiniMaxUtil，baseUrl={}", ConfigUtil.getMiniMaxBaseUrl());
        return new MiniMaxApiClient();
    }

    // =========================================================================
    // 文本生成（带降级链）
    // =========================================================================

    /**
     * 使用模型降级链调用 MiniMax API 生成文本
     * <p>
     * 降级链：主力模型（M2.5）→ 降级模型（M2.1），每级最多重试 {@value #SWITCH_THRESHOLD} 次。
     *
     * @param prompt 提示词
     * @return 生成的文本；失败时返回以 {@value #ERROR_PREFIX} 开头的描述字符串
     */
    public static String callWithFallback(String prompt) {
        final String primaryModel  = ConfigUtil.getClaudeModel();
        final String fallbackModel = ConfigUtil.getFallbackModel();
        final int    maxTokens     = ConfigUtil.getClaudeMaxTokens();

        String[] modelChain = {primaryModel, fallbackModel};

        for (String model : modelChain) {
            int errorCount = 0;
            while (errorCount < SWITCH_THRESHOLD) {
                try {
                    String result = CLIENT.sendMessage(prompt, model, maxTokens);
                    if (!primaryModel.equals(model)) {
                        log.info("降级模型 {} 调用成功", model);
                    }
                    return result;
                } catch (Exception e) {
                    errorCount++;
                    log.warn("模型 {} 调用失败 ({}/{}): {}", model, errorCount, SWITCH_THRESHOLD, e.getMessage());
                    if (errorCount < SWITCH_THRESHOLD) {
                        sleep(RETRY_INTERVAL_MS);
                    }
                }
            }
            log.warn("模型 {} 连续失败 {} 次，切换下一级模型", model, SWITCH_THRESHOLD);
        }

        log.error("所有模型均失败（{}），放弃本次调用", String.join(" → ", modelChain));
        return ERROR_PREFIX + ": 所有模型均不可用";
    }

    /**
     * 使用模型降级链调用 MiniMax API（请求对象版本）
     * <p>
     * 请求对象中的 model 字段会被降级链覆盖；max_tokens 若未设置则使用配置默认值。
     *
     * @param request 请求对象（model 会被降级链覆盖）
     * @return API 响应对象；失败时抛出 RuntimeException
     */
    public static MiniMaxResponse callWithFallback(MiniMaxRequest request) {
        final String primaryModel  = ConfigUtil.getClaudeModel();
        final String fallbackModel = ConfigUtil.getFallbackModel();

        if (request.getMaxTokens() == null) {
            request.setMaxTokens(ConfigUtil.getClaudeMaxTokens());
        }

        String[] modelChain = {primaryModel, fallbackModel};

        for (String model : modelChain) {
            int errorCount = 0;
            while (errorCount < SWITCH_THRESHOLD) {
                try {
                    request.setModel(model);
                    MiniMaxResponse response = CLIENT.sendMessage(request);
                    if (!primaryModel.equals(model)) {
                        log.info("降级模型 {} 调用成功", model);
                    }
                    return response;
                } catch (Exception e) {
                    errorCount++;
                    log.warn("模型 {} 调用失败 ({}/{}): {}", model, errorCount, SWITCH_THRESHOLD, e.getMessage());
                    if (errorCount < SWITCH_THRESHOLD) {
                        sleep(RETRY_INTERVAL_MS);
                    }
                }
            }
            log.warn("模型 {} 连续失败 {} 次，切换下一级模型", model, SWITCH_THRESHOLD);
        }

        throw new RuntimeException(ERROR_PREFIX + ": 所有模型均不可用（" + String.join(" → ", modelChain) + "）");
    }

    // =========================================================================
    // 模型查询
    // =========================================================================

    /**
     * 从官方 GET /anthropic/v1/models 接口获取当前账号可用模型列表的原始 JSON
     *
     * @return 原始 JSON 字符串；接口不支持或网络异常时返回 null
     */
    public static String fetchModelsRaw() {
        return CLIENT.fetchModelsFromApi();
    }

    /**
     * 获取当前账号可用的模型列表（含使用场景说明）
     * <p>
     * 优先从官方 API 获取；失败时回退到本地静态列表。
     *
     * @return ModelInfo 列表，按推荐优先级排序
     */
    public static List<MiniMaxApiClient.ModelInfo> getModelsWithUseCases() {
        return CLIENT.getAvailableModelsWithUseCases();
    }

    /**
     * 获取支持的模型 ID 列表（静态列表，不调用 API）
     *
     * @return 模型 ID 列表，按推荐优先级排序
     */
    public static List<String> getModelIds() {
        return CLIENT.getAvailableModels();
    }

    // =========================================================================
    // 配置与诊断
    // =========================================================================

    /**
     * 获取当前 MiniMax 客户端的配置信息（用于日志诊断）
     *
     * @return 配置摘要字符串
     */
    public static String getConfigInfo() {
        return CLIENT.getConfigInfo();
    }

    /**
     * 检查当前 MiniMax 配置是否有效（baseUrl 和 authToken 非空）
     *
     * @return true 表示配置有效
     */
    public static boolean isConfigValid() {
        return ConfigUtil.isConfigValid();
    }

    /**
     * 判断调用结果是否表示失败
     *
     * @param result callWithFallback 的返回值
     * @return true 表示调用失败
     */
    public static boolean isFailed(String result) {
        return StrUtil.isBlank(result) || result.startsWith(ERROR_PREFIX);
    }

    // =========================================================================
    // 内部工具
    // =========================================================================

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
