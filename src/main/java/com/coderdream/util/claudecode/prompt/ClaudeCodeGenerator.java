package com.coderdream.util.claudecode.prompt;

import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.claudecode.ClaudeApiClient;
import com.coderdream.util.claudecode.ClaudeRequest;
import com.coderdream.util.claudecode.ClaudeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Claude 代码生成工具
 * 利用 Claude 的编码能力进行代码生成、审查和修复
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeCodeGenerator {

    /**
     * API 客户端
     */
    private ClaudeApiClient apiClient;

    /**
     * 代码生成任务类型
     */
    public enum CodeTaskType {
        GENERATE("代码生成"),
        REVIEW("代码审查"),
        FIX_BUG("Bug 修复"),
        REFACTOR("代码重构"),
        OPTIMIZE("性能优化");

        private final String description;

        CodeTaskType(String description) {
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
    public ClaudeCodeGenerator(ClaudeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 生成代码
     *
     * @param requirement 需求描述
     * @return 生成的代码
     */
    public String generateCode(String requirement) {
        return generateCode(requirement, "java");
    }

    /**
     * 生成代码（指定语言）
     *
     * @param requirement 需求描述
     * @param language    编程语言
     * @return 生成的代码
     */
    public String generateCode(String requirement, String language) {
        log.info("Generating {} code for requirement: {}", language, requirement.substring(0, Math.min(50, requirement.length())));

        String systemPrompt = buildSystemPrompt(CodeTaskType.GENERATE, language);
        String prompt = String.format("请用 %s 编写以下功能：\n%s", language, requirement);

        return executeCodeTask(prompt, systemPrompt);
    }

    /**
     * 审查代码
     *
     * @param code 代码内容
     * @return 审查意见
     */
    public String reviewCode(String code) {
        log.info("Reviewing code");

        String systemPrompt = buildSystemPrompt(CodeTaskType.REVIEW, "java");
        String prompt = "请审查以下代码，指出潜在的问题、改进建议和最佳实践：\n\n" + code;

        return executeCodeTask(prompt, systemPrompt);
    }

    /**
     * 修复 Bug
     *
     * @param code      有问题的代码
     * @param bugReport Bug 描述
     * @return 修复后的代码
     */
    public String fixBug(String code, String bugReport) {
        log.info("Fixing bug: {}", bugReport.substring(0, Math.min(50, bugReport.length())));

        String systemPrompt = buildSystemPrompt(CodeTaskType.FIX_BUG, "java");
        String prompt = String.format(
                "以下代码存在问题：%s\n\n代码：\n%s\n\n请修复这个问题并解释修复方案。",
                bugReport, code
        );

        return executeCodeTask(prompt, systemPrompt);
    }

    /**
     * 重构代码
     *
     * @param code 原始代码
     * @return 重构后的代码
     */
    public String refactorCode(String code) {
        log.info("Refactoring code");

        String systemPrompt = buildSystemPrompt(CodeTaskType.REFACTOR, "java");
        String prompt = "请重构以下代码，使其更加清晰、高效和易于维护：\n\n" + code;

        return executeCodeTask(prompt, systemPrompt);
    }

    /**
     * 优化代码性能
     *
     * @param code 原始代码
     * @return 优化后的代码
     */
    public String optimizeCode(String code) {
        log.info("Optimizing code performance");

        String systemPrompt = buildSystemPrompt(CodeTaskType.OPTIMIZE, "java");
        String prompt = "请优化以下代码的性能，并解释优化策略：\n\n" + code;

        return executeCodeTask(prompt, systemPrompt);
    }

    /**
     * 执行代码任务
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @return 任务结果
     */
    private String executeCodeTask(String prompt, String systemPrompt) {
        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_MODEL_OPUS_45)  // 使用最强的模型
                .setMaxTokens(CdConstants.CLAUDE_MAX_TOKENS)
                .setSystemPrompt(systemPrompt)
                .enableThinking(CdConstants.CLAUDE_THINKING_BUDGET)
                .setEffortHigh()  // 使用高努力程度
                .addUserMessage(prompt);

        try {
            ClaudeResponse response = apiClient.sendMessage(request);
            String result = response.getTextContent();
            log.info("Code task completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Code task failed", e);
            throw new RuntimeException("Code task failed: " + e.getMessage(), e);
        }
    }

    /**
     * 构建系统提示词
     *
     * @param taskType 任务类型
     * @param language 编程语言
     * @return 系统提示词
     */
    private String buildSystemPrompt(CodeTaskType taskType, String language) {
        String basePrompt = "你是一位资深的软件工程师，拥有深厚的编程知识和最佳实践经验。\n" +
                "你精通多种编程语言，包括 Java、Python、JavaScript、Go 等。\n" +
                "你的代码遵循以下原则：\n" +
                "1. 清晰易读 - 使用有意义的变量名和函数名\n" +
                "2. 高效可靠 - 考虑性能和边界情况\n" +
                "3. 易于维护 - 遵循设计模式和最佳实践\n" +
                "4. 完整文档 - 提供必要的注释和说明\n\n";

        switch (taskType) {
            case GENERATE:
                return basePrompt +
                        "当前任务是代码生成。\n" +
                        "要求：\n" +
                        "- 生成完整、可运行的代码\n" +
                        "- 包含必要的错误处理\n" +
                        "- 提供清晰的代码注释\n" +
                        "- 遵循 " + language + " 的最佳实践\n";

            case REVIEW:
                return basePrompt +
                        "当前任务是代码审查。\n" +
                        "要求：\n" +
                        "- 指出代码中的潜在问题\n" +
                        "- 提供改进建议\n" +
                        "- 指出违反最佳实践的地方\n" +
                        "- 建议性能优化机会\n";

            case FIX_BUG:
                return basePrompt +
                        "当前任务是 Bug 修复。\n" +
                        "要求：\n" +
                        "- 准确定位问题根源\n" +
                        "- 提供完整的修复方案\n" +
                        "- 解释修复的原理\n" +
                        "- 建议如何防止类似问题\n";

            case REFACTOR:
                return basePrompt +
                        "当前任务是代码重构。\n" +
                        "要求：\n" +
                        "- 改进代码结构和可读性\n" +
                        "- 消除代码重复\n" +
                        "- 应用适当的设计模式\n" +
                        "- 保持功能不变\n";

            case OPTIMIZE:
                return basePrompt +
                        "当前任务是性能优化。\n" +
                        "要求：\n" +
                        "- 识别性能瓶颈\n" +
                        "- 提供优化策略\n" +
                        "- 解释优化的效果\n" +
                        "- 权衡可读性和性能\n";

            default:
                return basePrompt;
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
