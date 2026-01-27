package com.coderdream.util.nvidia;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MiniMax M2.1 API 工具类测试
 *
 * @author CoderDream
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class MiniMaxUtilTest {

    @Test
    @Order(1)
    @DisplayName("01. 测试 API 连接")
    void testConnection() {
        log.info("\n========【01. 测试 API 连接】========");
        boolean connected = MiniMaxUtil.testConnection();
        assertTrue(connected, "MiniMax API 连接失败");
        log.info("MiniMax API 连接成功");
    }

    @Test
    @Order(2)
    @DisplayName("02. 测试基本翻译功能")
    void testBasicTranslation() {
        log.info("\n========【02. 测试基本翻译功能】========");
        String prompt = "翻译成简体中文：Why are countryside walks no longer so popular?";
        String result = MiniMaxUtil.generateContent(prompt);

        assertNotNull(result, "翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("翻译结果: {}", result);
    }

    @Test
    @Order(3)
    @DisplayName("03. 测试通用翻译方法")
    void testTranslateToChinese() {
        log.info("\n========【03. 测试通用翻译方法】========");
        String englishText = "The quick brown fox jumps over the lazy dog.";
        String result = MiniMaxUtil.translateToChinese(englishText);

        assertNotNull(result, "翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("原文: {}", englishText);
        log.info("翻译: {}", result);
    }

    @Test
    @Order(4)
    @DisplayName("04. 测试字幕翻译功能")
    void testSubtitleTranslation() {
        log.info("\n========【04. 测试字幕翻译功能】========");
        String subtitleText = "Hello and welcome to 6 Minute English.\n" +
                "I'm Neil and joining me today is Sam.\n" +
                "Hello everyone.";
        String result = MiniMaxUtil.translateToChineseForSubtitle(subtitleText);

        assertNotNull(result, "字幕翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("原字幕:\n{}", subtitleText);
        log.info("翻译后:\n{}", result);
    }

    @Test
    @Order(5)
    @DisplayName("05. 测试词汇翻译功能")
    void testVocabularyTranslation() {
        log.info("\n========【05. 测试词汇翻译功能】========");
        String vocText = "cephalopod\n" +
                "the group of animals to which the octopus belongs\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "publicity stunt\n" +
                "something a company might do to grab your attention and promote its products\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n";

        String result = MiniMaxUtil.translateVocabulary(vocText);

        assertNotNull(result, "词汇翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("词汇翻译结果:\n{}", result);
    }

    @Test
    @Order(6)
    @DisplayName("06. 测试脚本翻译功能")
    void testScriptTranslation() {
        log.info("\n========【06. 测试脚本翻译功能】========");
        String scriptText = "Hello and welcome to 6 Minute English.\n" +
                "I'm Neil and joining me today is Sam.\n" +
                "Hello everyone.\n" +
                "Today we're talking about countryside walks.\n" +
                "Why are they no longer so popular?";

        String result = MiniMaxUtil.translateScript(scriptText);

        assertNotNull(result, "脚本翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("脚本翻译结果:\n{}", result);
    }

    @Test
    @Order(7)
    @DisplayName("07. 测试带系统提示的生成")
    void testGenerateWithSystemPrompt() {
        log.info("\n========【07. 测试带系统提示的生成】========");
        String systemPrompt = "你是一个专业的英语教师，请用简洁的中文解释以下英文单词或短语。";
        String userPrompt = "publicity stunt";

        String result = MiniMaxUtil.generateContent(userPrompt, "minimaxai/minimax-m2.1", systemPrompt);

        assertNotNull(result, "生成结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        log.info("单词: {}", userPrompt);
        log.info("解释: {}", result);
    }

    @Test
    @Order(8)
    @DisplayName("08. 测试 think 标签移除")
    void testThinkTagRemoval() {
        log.info("\n========【08. 测试 think 标签移除】========");
        // 这个测试主要验证 API 返回的内容中如果包含 <think> 标签，会被正确移除
        String prompt = "请用一句话回答：什么是人工智能？";
        String result = MiniMaxUtil.generateContent(prompt);

        assertNotNull(result, "生成结果不应为空");
        assertFalse(result.contains("<think>"), "结果不应包含 <think> 标签");
        assertFalse(result.contains("</think>"), "结果不应包含 </think> 标签");
        log.info("回答: {}", result);
    }

    @Test
    @Order(9)
    @DisplayName("09. 测试长文本翻译")
    void testLongTextTranslation() {
        log.info("\n========【09. 测试长文本翻译】========");
        String longText = """
                Hello and welcome to 6 Minute English. I'm Neil and joining me today is Sam.
                Hello everyone.
                Today we're talking about countryside walks and why they're no longer so popular.
                In the past, going for a walk in the countryside was a common weekend activity.
                Families would pack a picnic and head out to enjoy nature.
                But things have changed. Fewer people are taking these walks now.
                There are several reasons for this decline.
                First, people are busier than ever with work and other commitments.
                Second, the rise of technology means many prefer to stay indoors.
                Third, some countryside areas have become less accessible.
                Let's explore this topic further and learn some new vocabulary along the way.
                """;

        String result = MiniMaxUtil.translateToChinese(longText);

        assertNotNull(result, "长文本翻译结果不应为空");
        assertFalse(result.contains("API 调用发生异常"), "API 调用不应发生异常: " + result);
        assertTrue(result.length() > 50, "翻译结果应该有足够的长度");
        log.info("长文本翻译结果:\n{}", result);
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试错误处理 - 空输入")
    void testEmptyInput() {
        log.info("\n========【10. 测试错误处理 - 空输入】========");
        String result = MiniMaxUtil.generateContent("");

        // 空输入应该返回某种结果（可能是错误信息或空响应）
        log.info("空输入的返回结果: {}", result);
        // 不做严格断言，因为 API 对空输入的处理可能不同
    }
}
