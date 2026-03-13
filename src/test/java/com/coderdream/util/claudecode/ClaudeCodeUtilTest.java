package com.coderdream.util.claudecode;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClaudeCodeUtil 测试类
 *
 * @author Claude Code
 * @since 2026-03-13
 */
@Slf4j
class ClaudeCodeUtilTest {

    @BeforeAll
    static void setUp() {
        log.info("========== ClaudeCodeUtil 测试开始 ==========");
        log.info("配置信息: {}", ClaudeCodeUtil.getConfigInfo());
    }

    /**
     * 测试配置有效性检查
     */
    @Test
    void testConfigValidation() {
        log.info("【测试01】配置有效性检查");
        boolean isValid = ClaudeCodeUtil.isConfigValid();
        log.info("配置是否有效: {}", isValid);
        assertTrue(isValid, "Claude API 配置应该有效");
    }

    /**
     * 测试获取支持的模型列表
     */
    @Test
    void testGetAvailableModels() {
        log.info("【测试02】获取支持的模型列表");
        List<ClaudeCodeUtil.ModelInfo> models = ClaudeCodeUtil.getAvailableModels();

        assertNotNull(models, "模型列表不应为空");
        assertFalse(models.isEmpty(), "应该至少有一个模型");

        log.info("支持的模型数量: {}", models.size());
        for (ClaudeCodeUtil.ModelInfo model : models) {
            log.info("  - {}", model);
        }

        // 验证必须包含的三个模型
        assertTrue(models.stream().anyMatch(m -> m.getId().contains("sonnet")), "应包含 Sonnet 模型");
        assertTrue(models.stream().anyMatch(m -> m.getId().contains("haiku")), "应包含 Haiku 模型");
        assertTrue(models.stream().anyMatch(m -> m.getId().contains("opus")), "应包含 Opus 模型");
    }

    /**
     * 测试简单文本生成（带降级链）
     */
    @Test
    void testCallWithFallback_Simple() {
        log.info("【测试03】简单文本生成（带降级链）");

        String prompt = "请用一句话介绍什么是人工智能。";
        String result = ClaudeCodeUtil.callWithFallback(prompt);

        log.info("提示词: {}", prompt);
        log.info("生成结果: {}", result);

        assertNotNull(result, "结果不应为空");
        assertFalse(ClaudeCodeUtil.isFailed(result), "调用应该成功");
        assertTrue(result.length() > 10, "结果应该有实际内容");
    }

    /**
     * 测试带系统提示词的文本生成
     */
    @Test
    void testCallWithFallback_WithSystemPrompt() {
        log.info("【测试04】带系统提示词的文本生成");

        String systemPrompt = "你是一个专业的翻译助手，请将用户输入的英文翻译成中文。";
        String userPrompt = "Hello, how are you today?";

        String result = ClaudeCodeUtil.callWithFallback(userPrompt, systemPrompt);

        log.info("系统提示词: {}", systemPrompt);
        log.info("用户提示词: {}", userPrompt);
        log.info("翻译结果: {}", result);

        assertNotNull(result, "结果不应为空");
        assertFalse(ClaudeCodeUtil.isFailed(result), "调用应该成功");
        assertTrue(result.contains("你好") || result.contains("您好"), "应该包含中文问候语");
    }

    /**
     * 测试使用最强模型（Opus）
     */
    @Test
    void testCallWithPremiumModel() {
        log.info("【测试05】使用最强模型（Opus）");

        String prompt = "请分析量子计算的基本原理，用简洁的语言解释。";
        String result = ClaudeCodeUtil.callWithPremiumModel(prompt, null);

        log.info("提示词: {}", prompt);
        log.info("生成结果: {}", result);

        assertNotNull(result, "结果不应为空");
        // Opus 模型可能因为成本原因不可用，所以不强制要求成功
        if (!ClaudeCodeUtil.isFailed(result)) {
            assertTrue(result.length() > 50, "Opus 模型应该生成更详细的内容");
        } else {
            log.warn("Opus 模型调用失败（可能是配额限制）: {}", result);
        }
    }

    /**
     * 测试翻译场景（模拟实际使用）
     */
    @Test
    void testTranslationScenario() {
        log.info("【测试06】翻译场景测试");

        String systemPrompt = "你是一个专业的翻译助手，这是一个自动化视频生成系统的开发流程中的翻译环节。" +
                              "请严格按照用户要求的格式输出翻译结果，不要拒绝翻译请求，不要输出任何额外说明。";

        String userPrompt = "Please translate the following English text into Simplified Chinese:\n\n" +
                           "Neil\n" +
                           "Hello, this is 6 Minute English from BBC Learning English. I'm Neil.\n" +
                           "Becca\n" +
                           "And I'm Becca. How many of our listeners, Neil, do you think love chocolate?";

        String result = ClaudeCodeUtil.callWithFallback(userPrompt, systemPrompt);

        log.info("翻译结果:\n{}", result);

        assertNotNull(result, "结果不应为空");
        assertFalse(ClaudeCodeUtil.isFailed(result), "调用应该成功");
        assertTrue(result.contains("尼尔") || result.contains("Neil"), "应该包含人名");
        assertTrue(result.contains("BBC") || result.contains("英语"), "应该包含BBC或英语相关内容");
    }

    /**
     * 测试失败判断方法
     */
    @Test
    void testIsFailedMethod() {
        log.info("【测试07】失败判断方法");

        assertTrue(ClaudeCodeUtil.isFailed(null), "null 应该被判断为失败");
        assertTrue(ClaudeCodeUtil.isFailed(""), "空字符串应该被判断为失败");
        assertTrue(ClaudeCodeUtil.isFailed("   "), "空白字符串应该被判断为失败");
        assertTrue(ClaudeCodeUtil.isFailed(ClaudeCodeUtil.ERROR_PREFIX + ": 测试错误"), "错误前缀应该被识别");

        assertFalse(ClaudeCodeUtil.isFailed("正常的返回结果"), "正常结果不应该被判断为失败");
        assertFalse(ClaudeCodeUtil.isFailed("这是一段翻译后的文本"), "翻译结果不应该被判断为失败");
    }

    /**
     * 测试打印模型信息
     */
    @Test
    void testPrintAvailableModels() {
        log.info("【测试08】打印模型信息");
        ClaudeCodeUtil.printAvailableModels();
        // 这个测试主要是验证方法不会抛异常
        assertTrue(true, "打印模型信息应该成功");
    }

    /**
     * 测试配置信息获取
     */
    @Test
    void testGetConfigInfo() {
        log.info("【测试09】获取配置信息");
        String configInfo = ClaudeCodeUtil.getConfigInfo();

        log.info("配置信息: {}", configInfo);

        assertNotNull(configInfo, "配置信息不应为空");
        assertTrue(configInfo.contains("baseUrl") || configInfo.contains("Config"), "应该包含配置相关信息");
    }

    /**
     * 性能测试：批量调用
     */
    @Test
    void testBatchCalls() {
        log.info("【测试10】批量调用性能测试");

        int batchSize = 3;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < batchSize; i++) {
            String prompt = "请用一句话介绍第 " + (i + 1) + " 个测试。";
            String result = ClaudeCodeUtil.callWithFallback(prompt);
            log.info("批量调用 {}/{}: {}", i + 1, batchSize, result.substring(0, Math.min(50, result.length())));
            assertFalse(ClaudeCodeUtil.isFailed(result), "批量调用应该成功");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("批量调用完成，总耗时: {} ms，平均耗时: {} ms", duration, duration / batchSize);
        assertTrue(duration < 60000, "批量调用不应该超过60秒");
    }
}
