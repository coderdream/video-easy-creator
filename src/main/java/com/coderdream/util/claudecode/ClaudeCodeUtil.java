package com.coderdream.util.claudecode;

import cn.hutool.core.util.StrUtil;
import com.coderdream.util.cd.CdConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Claude API 工具门面类
 * <p>
 * 封装所有与 Claude API 的交互，提供统一的调用入口：
 * <ul>
 *   <li>{@link #callWithFallback(String)} - 带模型降级链的文本生成（主入口）</li>
 *   <li>{@link #callWithFallback(String, String)} - 指定系统提示词版本</li>
 *   <li>{@link #getAvailableModels()} - 获取支持的模型列表</li>
 *   <li>{@link #getConfigInfo()} - 当前配置信息</li>
 *   <li>{@link #isConfigValid()} - 配置有效性检查</li>
 * </ul>
 * <p>
 * 降级策略（{@link #callWithFallback}）：
 * <pre>
 *   主力模型 (Claude Sonnet 4.5)
 *     → 连续失败 {@value #SWITCH_THRESHOLD} 次 → 降级模型 (Claude Haiku 4.5)
 *     → 降级模型再失败 → 返回异常描述字符串
 * </pre>
 *
 * @author Claude Code
 * @since 2026-03-13
 */
@Slf4j
public class ClaudeCodeUtil {

    /** 单次模型最大重试次数，超过后切换下一级模型 */
    private static final int SWITCH_THRESHOLD = 3;

    /** 重试间隔（毫秒） */
    private static final int RETRY_INTERVAL_MS = 5000;

    /** 异常返回值前缀，业务层用此判断调用是否成功 */
    public static final String ERROR_PREFIX = "Claude API 调用发生异常";

    /** 主力模型 - Claude Sonnet 4.6 (最新版本，改进的编码能力) */
    private static final String PRIMARY_MODEL = CdConstants.CLAUDE_MODEL_SONNET_46;

    /** 降级模型 - Claude Haiku 4.5 (快速响应，低成本) */
    private static final String FALLBACK_MODEL = CdConstants.CLAUDE_MODEL_HAIKU_45;

    /** 最强模型 - Claude Opus 4.6 (最强能力，复杂任务专用) */
    private static final String PREMIUM_MODEL = CdConstants.CLAUDE_MODEL_OPUS_46;

    /** 默认最大 Token 数 */
    private static final int DEFAULT_MAX_TOKENS = 4096;

    /** API 客户端单例 */
    private static final ClaudeApiClient CLIENT = createClient();

    /**
     * 支持的模型信息
     */
    public static class ModelInfo {
        private final String id;
        private final String name;
        private final String useCase;
        private final String pricing;

        public ModelInfo(String id, String name, String useCase, String pricing) {
            this.id = id;
            this.name = name;
            this.useCase = useCase;
            this.pricing = pricing;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUseCase() { return useCase; }
        public String getPricing() { return pricing; }

        @Override
        public String toString() {
            return String.format("%s (%s) - %s [%s]", name, id, useCase, pricing);
        }
    }

    /** 支持的模型列表（按推荐优先级排序） */
    private static final List<ModelInfo> AVAILABLE_MODELS = Arrays.asList(
        new ModelInfo(
            CdConstants.CLAUDE_MODEL_SONNET_45,
            "Claude Sonnet 4.5",
            "平衡性能与成本，适合大多数翻译和文本生成任务",
            "$3/$15 per Mtok"
        ),
        new ModelInfo(
            CdConstants.CLAUDE_MODEL_HAIKU_45,
            "Claude Haiku 4.5",
            "快速响应，低成本，适合简单翻译和批量处理",
            "$1/$5 per Mtok"
        ),
        new ModelInfo(
            CdConstants.CLAUDE_MODEL_OPUS_45,
            "Claude Opus 4.5",
            "最强能力，适合复杂推理和高质量内容生成",
            "$5/$25 per Mtok"
        )
    );

    private static ClaudeApiClient createClient() {
        String baseUrl = System.getenv("ANTHROPIC_BASE_URL");
        String authToken = System.getenv("ANTHROPIC_AUTH_TOKEN");

        if (StrUtil.isBlank(baseUrl) || StrUtil.isBlank(authToken)) {
            log.error("Claude API 配置无效！请设置环境变量 ANTHROPIC_BASE_URL 和 ANTHROPIC_AUTH_TOKEN");
            throw new RuntimeException("Claude API 配置无效");
        }

        log.info("初始化 ClaudeCodeUtil，baseUrl={}", baseUrl);
        return new ClaudeApiClient(baseUrl, authToken);
    }

    // =========================================================================
    // 文本生成（带降级链）
    // =========================================================================

    /**
     * 使用模型降级链调用 Claude API 生成文本
     * <p>
     * 降级链：主力模型（Sonnet 4.5）→ 降级模型（Haiku 4.5），每级最多重试 {@value #SWITCH_THRESHOLD} 次。
     *
     * @param userPrompt 用户提示词
     * @return 生成的文本；失败时返回以 {@value #ERROR_PREFIX} 开头的描述字符串
     */
    public static String callWithFallback(String userPrompt) {
        return callWithFallback(userPrompt, null);
    }

    /**
     * 使用模型降级链调用 Claude API 生成文本（带系统提示词）
     * <p>
     * 降级链：主力模型（Sonnet 4.5）→ 降级模型（Haiku 4.5），每级最多重试 {@value #SWITCH_THRESHOLD} 次。
     *
     * @param userPrompt   用户提示词
     * @param systemPrompt 系统提示词（可选，为 null 时不设置）
     * @return 生成的文本；失败时返回以 {@value #ERROR_PREFIX} 开头的描述字符串
     */
    public static String callWithFallback(String userPrompt, String systemPrompt) {
        String[] modelChain = {PRIMARY_MODEL, FALLBACK_MODEL};

        for (String model : modelChain) {
            int errorCount = 0;
            while (errorCount < SWITCH_THRESHOLD) {
                try {
                    ClaudeRequest request = new ClaudeRequest()
                        .setModel(model)
                        .setMaxTokens(DEFAULT_MAX_TOKENS)
                        .setSystemPrompt(systemPrompt)
                        .addUserMessage(userPrompt);

                    ClaudeResponse response = CLIENT.sendMessage(request);
                    String result = response.getTextContent();

                    if (!PRIMARY_MODEL.equals(model)) {
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
     * 使用最强模型（Opus 4.5）调用 Claude API
     * <p>
     * 注意：此方法不使用降级链，失败时直接返回错误。适用于对质量要求极高的场景。
     *
     * @param userPrompt   用户提示词
     * @param systemPrompt 系统提示词（可选）
     * @return 生成的文本；失败时返回以 {@value #ERROR_PREFIX} 开头的描述字符串
     */
    public static String callWithPremiumModel(String userPrompt, String systemPrompt) {
        try {
            log.info("使用最强模型 {} 调用 Claude API", PREMIUM_MODEL);

            ClaudeRequest request = new ClaudeRequest()
                .setModel(PREMIUM_MODEL)
                .setMaxTokens(DEFAULT_MAX_TOKENS)
                .setSystemPrompt(systemPrompt)
                .addUserMessage(userPrompt);

            ClaudeResponse response = CLIENT.sendMessage(request);
            return response.getTextContent();
        } catch (Exception e) {
            log.error("最强模型 {} 调用失败: {}", PREMIUM_MODEL, e.getMessage());
            return ERROR_PREFIX + ": " + e.getMessage();
        }
    }

    // =========================================================================
    // 模型查询
    // =========================================================================

    /**
     * 获取支持的模型列表（含使用场景说明）
     *
     * @return ModelInfo 列表，按推荐优先级排序
     */
    public static List<ModelInfo> getAvailableModels() {
        return AVAILABLE_MODELS;
    }

    /**
     * 获取主力模型 ID
     *
     * @return 主力模型 ID
     */
    public static String getPrimaryModel() {
        return PRIMARY_MODEL;
    }

    /**
     * 获取降级模型 ID
     *
     * @return 降级模型 ID
     */
    public static String getFallbackModel() {
        return FALLBACK_MODEL;
    }

    /**
     * 获取最强模型 ID
     *
     * @return 最强模型 ID
     */
    public static String getPremiumModel() {
        return PREMIUM_MODEL;
    }

    // =========================================================================
    // 配置与诊断
    // =========================================================================

    /**
     * 获取当前 Claude 客户端的配置信息（用于日志诊断）
     *
     * @return 配置摘要字符串
     */
    public static String getConfigInfo() {
        return CLIENT.getConfigInfo() + String.format(
            "\n降级链: %s → %s (每级最多重试 %d 次)",
            PRIMARY_MODEL, FALLBACK_MODEL, SWITCH_THRESHOLD
        );
    }

    /**
     * 检查当前 Claude 配置是否有效（baseUrl 和 authToken 非空）
     *
     * @return true 表示配置有效
     */
    public static boolean isConfigValid() {
        String baseUrl = System.getenv("ANTHROPIC_BASE_URL");
        String authToken = System.getenv("ANTHROPIC_AUTH_TOKEN");
        return StrUtil.isNotBlank(baseUrl) && StrUtil.isNotBlank(authToken);
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

    /**
     * 打印所有支持的模型信息（用于调试）
     */
    public static void printAvailableModels() {
        log.info("========== Claude API 支持的模型 ==========");
        for (ModelInfo model : AVAILABLE_MODELS) {
            log.info(model.toString());
        }
        log.info("==========================================");
    }
}
