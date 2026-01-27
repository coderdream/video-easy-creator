package com.coderdream.util.claudecode.prompt;

import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.claudecode.ClaudeApiClient;
import com.coderdream.util.claudecode.ClaudeRequest;
import com.coderdream.util.claudecode.ClaudeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Claude 翻译工具
 * 基于资深翻译官 System Prompt 的高质量翻译
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeTranslator {

    /**
     * API 客户端
     */
    private ClaudeApiClient apiClient;

    /**
     * 翻译场景
     */
    public enum TranslationScenario {
        TECHNICAL("技术文档"),
        LITERATURE("文学作品"),
        BUSINESS("商业文案"),
        GENERAL("通用翻译");

        private final String description;

        TranslationScenario(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 构造函数
     *
     * @param apiClient API 客户端
     */
    public ClaudeTranslator(ClaudeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 翻译文本（通用）
     *
     * @param englishText 英文文本
     * @return 中文翻译
     */
    public String translate(String englishText) {
        return translate(englishText, TranslationScenario.GENERAL);
    }

    /**
     * 翻译文本（指定场景）
     *
     * @param englishText 英文文本
     * @param scenario    翻译场景
     * @return 中文翻译
     */
    public String translate(String englishText, TranslationScenario scenario) {
        log.info("正在翻译文本 for scenario: {}", scenario.getDescription());

        String systemPrompt = buildSystemPrompt(scenario);

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                .setMaxTokens(CdConstants.CLAUDE_MAX_TOKENS)
                .setSystemPrompt(systemPrompt)
                .enableThinking(CdConstants.CLAUDE_THINKING_BUDGET)
                .addUserMessage(englishText);

        try {
            ClaudeResponse response = apiClient.sendMessage(request);
            String translation = response.getTextContent();
            log.info("翻译完成 successfully");
            return translation;
        } catch (Exception e) {
            log.error("翻译失败", e);
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 翻译技术文档
     *
     * @param englishText 英文文本
     * @return 中文翻译
     */
    public String translateTechnical(String englishText) {
        return translate(englishText, TranslationScenario.TECHNICAL);
    }

    /**
     * 翻译文学作品
     *
     * @param englishText 英文文本
     * @return 中文翻译
     */
    public String translateLiterature(String englishText) {
        return translate(englishText, TranslationScenario.LITERATURE);
    }

    /**
     * 翻译商业文案
     *
     * @param englishText 英文文本
     * @return 中文翻译
     */
    public String translateBusiness(String englishText) {
        return translate(englishText, TranslationScenario.BUSINESS);
    }

    /**
     * 构建系统提示词
     *
     * @param scenario 翻译场景
     * @return 系统提示词
     */
    private String buildSystemPrompt(TranslationScenario scenario) {
        String basePrompt = "你是一位拥有20年经验的资深中英文翻译官，擅长将复杂的英文内容翻译成地道、优美且准确的中文。\n" +
                "你追求\"信（Faithfulness）、达（Expressiveness）、雅（Elegance）\"的翻译境界。\n\n" +
                "翻译原则：\n" +
                "1. 信（Faithfulness）：准确传达原文意思，不遗漏任何信息\n" +
                "2. 达（Expressiveness）：符合中文表达习惯，流畅自然\n" +
                "3. 雅（Elegance）：优化词汇和句式，提升文学性\n\n" +
                "避免翻译腔：\n" +
                "- 避免过度使用被动语态\n" +
                "- 避免长串定语，拆分为短句\n" +
                "- 避免机械翻译\n" +
                "- 避免过多的\"被\"、\"基于\"、\"的一系列\"等词汇\n\n" +
                "中英文排版规范：\n" +
                "- 在中英文之间增加半角空格\n" +
                "- 保持原文的格式和结构\n\n" +
                "输出格式：直接输出最终翻译后的中文文本，除非用户要求，否则不要输出多余的解释说明。\n\n";

        switch (scenario) {
            case TECHNICAL:
                return basePrompt +
                        "当前语境是 IT 技术文档翻译。\n" +
                        "特殊要求：\n" +
                        "- 代码块和代码注释不需要翻译，保持原样\n" +
                        "- 变量名、函数名、类名等保持原样\n" +
                        "- 使用行业标准术语（如 Interface 译为 \"接口\" 而非 \"界面\"）\n" +
                        "- 对于不确定的术语，保留英文并在括号内标注\n";

            case LITERATURE:
                return basePrompt +
                        "当前语境是文学作品翻译。\n" +
                        "特殊要求：\n" +
                        "- 多使用四字成语或骈句，增加文学色彩\n" +
                        "- 保留原文的修辞手法和意象\n" +
                        "- 注重节奏感和韵律感\n" +
                        "- 传达原文的情感和意境\n";

            case BUSINESS:
                return basePrompt +
                        "当前语境是商业文案翻译。\n" +
                        "特殊要求：\n" +
                        "- 翻译应极具感染力和说服力\n" +
                        "- 突出产品或服务的价值主张\n" +
                        "- 使用简洁有力的表达\n" +
                        "- 符合中文商业文案的习惯用语\n";

            default:
                return basePrompt + "进行通用翻译，平衡准确性和可读性。\n";
        }
    }

    /**
     * 获取 API 客户端
     *
     * @return API 客户端
     */
    public ClaudeApiClient getApiClient() {
        return this.apiClient;
    }
}
