package com.coderdream.util.minimax;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MiniMaxUtil 门面类测试
 * <p>
 * 覆盖所有公开方法，按无 API 调用（纯配置/本地）→ 有 API 调用的顺序排列。
 *
 * @author Claude Code
 * @since 2026-03-11
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MiniMaxUtilTest {

    // =========================================================================
    // 纯配置/本地方法（无 API 调用）
    // =========================================================================

    /**
     * 测试1: 配置有效性检查
     */
    @Test
    @Order(1)
    public void test01IsConfigValid() {
        log.info("========== 测试1: 配置有效性检查 ==========");

        boolean valid = MiniMaxUtil.isConfigValid();
        log.info("配置是否有效: {}", valid);
        assertTrue(valid, "MiniMax 配置应有效（检查 config.properties 中的 baseUrl 和 apiKey）");
    }

    /**
     * 测试2: 配置信息输出
     */
    @Test
    @Order(2)
    public void test02GetConfigInfo() {
        log.info("========== 测试2: 配置信息输出 ==========");

        String info = MiniMaxUtil.getConfigInfo();
        log.info("配置信息: {}", info);

        assertNotNull(info, "配置信息不能为 null");
        assertFalse(info.isBlank(), "配置信息不能为空字符串");
        assertTrue(info.contains("MiniMaxApiClient Config"), "配置信息应包含 'MiniMaxApiClient Config'");
    }

    /**
     * 测试3: 静态模型 ID 列表
     */
    @Test
    @Order(3)
    public void test03GetModelIds() {
        log.info("========== 测试3: 静态模型 ID 列表 ==========");

        List<String> ids = MiniMaxUtil.getModelIds();
        log.info("模型 ID 列表（共 {} 个）:", ids.size());
        ids.forEach(id -> log.info("  - {}", id));

        assertNotNull(ids, "模型列表不能为 null");
        assertFalse(ids.isEmpty(), "模型列表不能为空");
        assertTrue(ids.contains("MiniMax-M2.5"), "列表应包含 MiniMax-M2.5");
        assertTrue(ids.contains("MiniMax-M2.1"), "列表应包含 MiniMax-M2.1");
        assertTrue(ids.contains("MiniMax-M2"),   "列表应包含 MiniMax-M2");
    }

    /**
     * 测试4: isFailed 辅助方法的各种边界条件
     */
    @Test
    @Order(4)
    public void test04IsFailed() {
        log.info("========== 测试4: isFailed 辅助方法 ==========");

        // 应判断为失败的情况
        assertTrue(MiniMaxUtil.isFailed(null),                             "null 应判断为失败");
        assertTrue(MiniMaxUtil.isFailed(""),                               "空字符串应判断为失败");
        assertTrue(MiniMaxUtil.isFailed("   "),                            "纯空白应判断为失败");
        assertTrue(MiniMaxUtil.isFailed(MiniMaxUtil.ERROR_PREFIX),         "ERROR_PREFIX 自身应判断为失败");
        assertTrue(MiniMaxUtil.isFailed(MiniMaxUtil.ERROR_PREFIX + ": 模型不可用"), "带后缀的错误信息应判断为失败");

        // 应判断为成功的情况
        assertFalse(MiniMaxUtil.isFailed("早上好"),          "正常译文应判断为成功");
        assertFalse(MiniMaxUtil.isFailed("Good morning."),   "英文内容应判断为成功");
        assertFalse(MiniMaxUtil.isFailed("[thinking] xxx"),  "thinking 备用内容也算有内容");

        log.info("isFailed 所有边界条件验证通过");
    }

    // =========================================================================
    // 需要 API 调用的方法
    // =========================================================================

    /**
     * 测试5: callWithFallback(String) — 简单文本生成
     */
    @Test
    @Order(5)
    public void test05CallWithFallbackSimple() {
        log.info("========== 测试5: callWithFallback(String) 简单文本生成 ==========");

        String prompt = "请用一句话介绍你自己。";
        log.info("Prompt: {}", prompt);

        String result = MiniMaxUtil.callWithFallback(prompt);
        log.info("结果: {}", result);

        assertNotNull(result, "结果不能为 null");
        assertFalse(MiniMaxUtil.isFailed(result), "结果不应以错误前缀开头，当前: " + result);
        log.info("test05 通过，结果长度: {} 字符", result.length());
    }

    /**
     * 测试6: callWithFallback(String) — 英译中翻译
     */
    @Test
    @Order(6)
    public void test06CallWithFallbackTranslation() {
        log.info("========== 测试6: callWithFallback(String) 英译中翻译 ==========");

        String prompt = "请将以下英文翻译成中文，只输出译文，不加解释：\n\nGood morning, everyone! Welcome to BBC Six Minute English.";
        log.info("Prompt: {}", prompt);

        String result = MiniMaxUtil.callWithFallback(prompt);
        log.info("翻译结果: {}", result);

        assertNotNull(result, "翻译结果不能为 null");
        assertFalse(MiniMaxUtil.isFailed(result), "翻译结果不应包含错误前缀，当前: " + result);
        // 应包含中文字符
        assertTrue(result.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "翻译结果应包含中文字符");
        log.info("test06 通过，译文长度: {} 字符", result.length());
    }

    /**
     * 测试7: callWithFallback(MiniMaxRequest) — 请求对象版本
     */
    @Test
    @Order(7)
    public void test07CallWithFallbackRequest() {
        log.info("========== 测试7: callWithFallback(MiniMaxRequest) 请求对象版本 ==========");

        MiniMaxRequest request = new MiniMaxRequest()
                .setMaxTokens(500)
                .addSystemMessage("你是一个专业的英译中翻译助手，只输出译文，不加解释。")
                .addUserMessage("Translate to Chinese: The weather is nice today.");

        log.info("发送 MiniMaxRequest，系统提示词 + 用户消息");
        MiniMaxResponse response = MiniMaxUtil.callWithFallback(request);

        assertNotNull(response, "响应对象不能为 null");
        String text = response.getFirstChoiceText();
        log.info("响应文本: {}", text);
        assertNotNull(text, "响应文本不能为 null");
        assertFalse(text.isBlank(), "响应文本不能为空");
        log.info("test07 通过，响应模型: {}, 文本长度: {} 字符",
                response.getModel(), text.length());
    }

    /**
     * 测试8: fetchModelsRaw() — 官方 GET /models 原始响应
     */
    @Test
    @Order(8)
    public void test08FetchModelsRaw() {
        log.info("========== 测试8: fetchModelsRaw() 官方 API 模型列表 ==========");

        String raw = MiniMaxUtil.fetchModelsRaw();
        log.info("原始响应: {}", raw != null ? raw : "(null，接口可能不支持)");

        // 该测试不强制要求 API 支持 /models 端点，仅输出结果供参考
        if (raw != null) {
            assertFalse(raw.isBlank(), "原始响应不应为空字符串");
            log.info("test08: 官方 /models 接口响应长度 {} 字符", raw.length());
        } else {
            log.warn("test08: 官方 /models 接口返回 null（接口不支持或网络异常），跳过内容断言");
        }
    }

    /**
     * 测试9: getModelsWithUseCases() — 含使用场景的模型列表
     */
    @Test
    @Order(9)
    public void test09GetModelsWithUseCases() {
        log.info("========== 测试9: getModelsWithUseCases() 模型及使用场景 ==========");

        List<MiniMaxApiClient.ModelInfo> models = MiniMaxUtil.getModelsWithUseCases();

        log.info("==================== 模型及使用场景 ====================");
        for (MiniMaxApiClient.ModelInfo m : models) {
            log.info("  {}", m);
        }
        log.info("=========================================================");

        assertNotNull(models, "模型列表不能为 null");
        assertFalse(models.isEmpty(), "模型列表不能为空（静态列表应保底）");

        for (MiniMaxApiClient.ModelInfo m : models) {
            assertNotNull(m.getId(),            "模型 id 不能为 null");
            assertFalse(m.getId().isBlank(),    "模型 id 不能为空");
            assertNotNull(m.getUseCases(),       "使用场景不能为 null");
            assertNotNull(m.getPlanRequirement(),"套餐要求不能为 null");
        }

        log.info("test09 通过，共 {} 个模型", models.size());
    }

    /**
     * 测试10: BBC 六分钟英语场景 — 完整对话段落翻译
     */
    @Test
    @Order(10)
    public void test10BbcDialogTranslation() {
        log.info("========== 测试10: BBC 六分钟英语对话段落翻译 ==========");

        String dialog =
                "Neil: Hello and welcome to 6 Minute English, the programme where we explore an interesting topic and teach you some related vocabulary.\n" +
                "Sam: I'm Sam, and today we're talking about the science of sleep.\n" +
                "Neil: That's right. Have you ever wondered why we dream?\n" +
                "Sam: It's a fascinating question. Scientists still don't fully understand it.";

        String prompt = "请将以下BBC六分钟英语对话翻译成中文，保持原来的格式（主持人名字: 内容），只输出译文：\n\n" + dialog;
        log.info("原文:\n{}", dialog);

        String result = MiniMaxUtil.callWithFallback(prompt);
        log.info("译文:\n{}", result);

        assertNotNull(result, "译文不能为 null");
        assertFalse(MiniMaxUtil.isFailed(result), "译文不应为错误信息，当前: " + result);
        assertTrue(result.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "译文应包含中文字符");

        log.info("test10 通过，译文长度: {} 字符", result.length());
    }
}
