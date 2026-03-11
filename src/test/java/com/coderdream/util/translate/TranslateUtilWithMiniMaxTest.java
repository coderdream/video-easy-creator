package com.coderdream.util.translate;

import com.coderdream.util.minimax.ConfigUtil;
import com.coderdream.util.minimax.MiniMaxApiClient;
import com.coderdream.util.minimax.MiniMaxRequest;
import com.coderdream.util.minimax.MiniMaxResponse;
import com.coderdream.util.minimax.TranslateUtilWithMiniMax;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TranslateUtilWithMiniMax 基础能力测试
 *
 * @author Claude Code
 * @since 2026-03-11
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TranslateUtilWithMiniMaxTest {

    /**
     * 测试1: 验证 MiniMax 配置读取
     * 不发起 API 调用，仅验证配置是否可读
     */
    @Test
    @Order(1)
    public void test01ConfigRead() {
        log.info("========== 测试1: 验证 MiniMax 配置读取 ==========");

        String baseUrl = ConfigUtil.getMiniMaxBaseUrl();
        String apiKey = ConfigUtil.getMiniMaxApiKey();
        String apiVersion = ConfigUtil.getMiniMaxApiVersion();
        int timeout = ConfigUtil.getMiniMaxTimeout();
        String model = ConfigUtil.getClaudeModel();
        int maxTokens = ConfigUtil.getClaudeMaxTokens();

        log.info("BaseUrl:        {}", baseUrl);
        log.info("ApiKey:         {}", apiKey != null ? "***已配置(" + apiKey.length() + "位)***" : "未配置");
        log.info("ApiVersion:     {}", apiVersion);
        log.info("Timeout:        {}ms", timeout);
        log.info("Primary Model:  {}", model);
        log.info("Highspeed Model:{}", ConfigUtil.getHighspeedModel());
        log.info("Fallback Model: {}", ConfigUtil.getFallbackModel());
        log.info("MaxTokens:      {}", maxTokens);

        boolean isValid = ConfigUtil.isConfigValid();
        log.info("配置有效性: {}", isValid ? "有效" : "无效");

        assertNotNull(baseUrl, "BaseUrl 不能为空");
        assertNotNull(model, "Model 不能为空");
        assertTrue(maxTokens > 0, "MaxTokens 必须大于 0");
        assertTrue(timeout > 0, "Timeout 必须大于 0");
        assertTrue(isValid, "MiniMax API 配置无效，请检查 config.properties 中的 MINIMAX_API_KEY / MINIMAX_BASE_URL");
    }

    /**
     * 测试2: 验证 MiniMaxApiClient 初始化
     */
    @Test
    @Order(2)
    public void test02ClientInit() {
        log.info("========== 测试2: 验证 MiniMaxApiClient 初始化 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();
        log.info("配置信息: {}", client.getConfigInfo());

        assertTrue(client.isConfigValid(), "客户端配置无效");
        log.info("客户端初始化成功");
    }

    /**
     * 测试3: MiniMaxRequest 参数校验逻辑（不发起 API 调用）
     */
    @Test
    @Order(3)
    public void test03RequestValidation() {
        log.info("========== 测试3: MiniMaxRequest 参数校验 ==========");

        // 3.1 空消息列表应抛异常
        MiniMaxRequest emptyMsgRequest = new MiniMaxRequest()
                .setModel(ConfigUtil.getClaudeModel())
                .setMaxTokens(100);
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                emptyMsgRequest::validate,
                "消息列表为空时应抛出 IllegalArgumentException"
        );
        log.info("空消息校验通过: {}", ex1.getMessage());

        // 3.2 temperature 超出范围应抛异常
        MiniMaxRequest badTempRequest = new MiniMaxRequest()
                .setModel(ConfigUtil.getClaudeModel())
                .setMaxTokens(100)
                .setTemperature(1.5)
                .addUserMessage("test");
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                badTempRequest::validate,
                "temperature=1.5 时应抛出 IllegalArgumentException"
        );
        log.info("temperature 超范围校验通过: {}", ex2.getMessage());

        // 3.3 合法请求不应抛异常
        MiniMaxRequest validRequest = new MiniMaxRequest()
                .setModel(ConfigUtil.getClaudeModel())
                .setMaxTokens(100)
                .setTemperature(0.7)
                .addSystemMessage("你是一个翻译助手。")
                .addUserMessage("Hello");
        assertDoesNotThrow(validRequest::validate, "合法请求不应抛出异常");
        log.info("合法请求校验通过");
    }

    /**
     * 测试4: 直接调用 MiniMaxApiClient.sendMessage(String) — 最基础的 API 调用
     */
    @Test
    @Order(4)
    public void test04SimpleApiCall() {
        log.info("========== 测试4: 最基础的 API 调用 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();
        String result = client.sendMessage("请用一句话介绍你自己。");

        log.info("API 返回内容: {}", result);

        assertNotNull(result, "返回结果不能为空");
        assertFalse(result.isBlank(), "返回结果不能为空字符串");
    }

    /**
     * 测试5: 使用 MiniMaxRequest 对象调用，验证 MiniMaxResponse 结构
     */
    @Test
    @Order(5)
    public void test05RequestObjectCall() {
        log.info("========== 测试5: 使用 MiniMaxRequest 对象调用 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();
        MiniMaxRequest request = new MiniMaxRequest()
                .setModel(ConfigUtil.getClaudeModel())
                .setMaxTokens(500)  // M2.5 是推理模型，会先输出 thinking 块，需要足够的 token 空间
                .addSystemMessage("你是一个专业的英译中翻译助手，只输出译文，不加解释。")
                .addUserMessage("Translate to Chinese: Good morning.");

        MiniMaxResponse response = client.sendMessage(request);

        assertNotNull(response, "Response 对象不能为空");

        String text = response.getFirstChoiceText();
        log.info("翻译结果: {}", text);
        assertNotNull(text, "翻译结果文本不能为空");
        assertFalse(text.isBlank(), "翻译结果不能为空字符串");

        String finishReason = response.getFinishReason();
        log.info("完成原因: {}", finishReason);

        if (response.getUsage() != null) {
            log.info("Token 使用: {}", response.getUsage().getTotalTokensCount());
            assertTrue(response.getUsage().getTotalTokensCount() > 0, "Token 数应大于 0");
        }
    }

    /**
     * 测试6: TranslateUtilWithMiniMax.generateContent — 通用内容生成
     */
    @Test
    @Order(6)
    public void test06GenerateContent() {
        log.info("========== 测试6: generateContent 通用内容生成 ==========");

        String prompt = "请将以下英文翻译成简体中文，只输出译文：\n"
                + "Welcome to BBC Learning English. Today's topic is climate change.";

        String result = TranslateUtilWithMiniMax.generateContent(prompt);

        log.info("生成结果:\n{}", result);

        assertNotNull(result, "结果不能为空");
        assertFalse(result.contains("API 调用发生异常"), "不应包含异常信息: " + result);
        assertFalse(result.isBlank(), "结果不能为空字符串");
    }

    /**
     * 测试7: generateContent — 多条文本批量翻译
     */
    @Test
    @Order(7)
    public void test07BatchTranslation() throws InterruptedException {
        log.info("========== 测试7: 批量翻译（多条，带间隔避免限流） ==========");

        String[] texts = {
                "Good morning.",
                "How are you today?",
                "Let's start our English lesson.",
        };

        long start = System.currentTimeMillis();
        for (int i = 0; i < texts.length; i++) {
            String prompt = "Translate to Chinese (output only): " + texts[i];
            String result = TranslateUtilWithMiniMax.generateContent(prompt);

            log.info("[{}] {} → {}", i + 1, texts[i], result);
            assertNotNull(result, "第 " + (i + 1) + " 条结果不能为空");
            assertFalse(result.contains("API 调用发生异常"), "第 " + (i + 1) + " 条发生异常");

            if (i < texts.length - 1) {
                Thread.sleep(1000); // 避免触发速率限制
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("批量翻译完成，共 {} 条，总耗时 {}ms，平均 {}ms/条",
                texts.length, elapsed, elapsed / texts.length);
    }

    /**
     * 测试8: 高速模型调用（MiniMax-M2.5-highspeed，100 token/s）
     * 注意：高速模型需要更高级别的 API 套餐，若套餐不支持则跳过而非失败
     */
    @Test
    @Order(8)
    public void test08HighspeedModel() {
        log.info("========== 测试8: 高速模型调用 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();
        String highspeedModel = ConfigUtil.getHighspeedModel();
        log.info("高速模型: {}", highspeedModel);

        try {
            long start = System.currentTimeMillis();
            String result = client.sendMessageWithHighspeed(
                    "Translate to Chinese (output only): The future is bright.");
            long elapsed = System.currentTimeMillis() - start;

            log.info("高速模型翻译结果: {}", result);
            log.info("高速模型耗时: {}ms", elapsed);

            assertNotNull(result, "高速模型结果不能为空");
            assertFalse(result.isBlank(), "高速模型结果不能为空字符串");

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            // 错误码 2061 = 当前套餐不支持该模型，跳过而非失败
            if (msg != null && (msg.contains("not support model") || msg.contains("2061"))) {
                log.warn("当前 API 套餐不支持高速模型 {}，跳过测试。如需使用，请升级套餐。", highspeedModel);
                log.warn("错误详情: {}", msg);
                // 套餐限制不属于代码缺陷，测试视为通过
            } else {
                // 其他错误仍需暴露
                fail("高速模型调用发生非预期异常: " + msg);
            }
        }
    }

    /**
     * 测试9: 模型链信息验证（无 API 调用）
     */
    @Test
    @Order(9)
    public void test09ModelChainInfo() {
        log.info("========== 测试9: 模型链信息验证 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();

        // 验证模型层级配置
        String primary   = ConfigUtil.getClaudeModel();
        String highspeed = ConfigUtil.getHighspeedModel();
        String fallback  = ConfigUtil.getFallbackModel();

        log.info("模型降级链: {} → {} → {}", primary, fallback, highspeed);
        log.info("完整配置:   {}", client.getConfigInfo());

        // 验证可用模型列表
        java.util.List<String> models = client.getAvailableModels();
        log.info("支持的模型列表 ({} 个):", models.size());
        models.forEach(m -> log.info("  - {}", m));

        assertNotNull(primary,   "primary model 不能为空");
        assertNotNull(highspeed, "highspeed model 不能为空");
        assertNotNull(fallback,  "fallback model 不能为空");

        // 三者不能相同
        assertNotEquals(primary, fallback,  "primary 和 fallback 模型应不同");
        assertNotEquals(primary, highspeed, "primary 和 highspeed 模型应不同");

        assertTrue(models.contains(primary),   "可用列表应包含 primary model");
        assertTrue(models.contains(highspeed), "可用列表应包含 highspeed model");
        assertTrue(models.contains(fallback),  "可用列表应包含 fallback model");

        assertTrue(models.size() >= 3, "至少应有 3 个可用模型");

        log.info("模型链验证通过");
    }

    /**
     * 测试10: 逐个探测 5 个模型的可用性（当前 API Key 套餐）
     * 结果仅做记录，不影响测试通过/失败
     */
    @Test
    @Order(10)
    public void test10ModelSurvey() throws InterruptedException {
        log.info("========== 测试10: 逐个探测模型可用性 ==========");

        // Anthropic 兼容接口支持的全部 5 个模型（按官方文档）
        String[] allModels = {
                "MiniMax-M2.5",           // 推荐，标准套餐可用
                "MiniMax-M2.5-highspeed", // 需 Pay-as-you-go key
                "MiniMax-M2.1",           // 旧版，标准套餐可用
                "MiniMax-M2.1-highspeed", // 需 Pay-as-you-go key
                "MiniMax-M2"              // 最旧版兼容
        };

        MiniMaxApiClient client = new MiniMaxApiClient();
        String prompt = "Reply with exactly one Chinese character: 好";

        java.util.List<String> available   = new java.util.ArrayList<>();
        java.util.List<String> unavailable = new java.util.ArrayList<>();

        for (String model : allModels) {
            log.info("------- 测试模型: {} -------", model);
            try {
                String result = client.sendMessage(prompt, model, 10);
                log.info("[OK ] {} => {}", model, result);
                available.add(model);
            } catch (RuntimeException e) {
                String msg = e.getMessage() != null ? e.getMessage() : "unknown";
                // 截取关键错误信息
                String brief = msg.length() > 120 ? msg.substring(0, 120) + "..." : msg;
                log.warn("[ERR] {} => {}", model, brief);
                unavailable.add(model + " (" + brief + ")");
            }
            // 每次请求间隔 1s，避免限流
            Thread.sleep(1000);
        }

        // 汇总报告
        log.info("==================== 探测结果 ====================");
        log.info("可用模型 ({} 个):", available.size());
        available.forEach(m -> log.info("  [OK ] {}", m));
        log.info("不可用模型 ({} 个):", unavailable.size());
        unavailable.forEach(m -> log.info("  [ERR] {}", m));
        log.info("===================================================");

        // 至少有 1 个模型可用，否则说明配置本身有问题
        assertFalse(available.isEmpty(), "没有任何模型可用，请检查 API Key 和网络");
    }

    /**
     * 测试11: 调用官方 GET /anthropic/v1/models 接口，获取当前账号可用模型及使用场景
     */
    @Test
    @Order(11)
    public void test11ModelListWithUseCases() {
        log.info("========== 测试11: 官方 API 查询可用模型及使用场景 ==========");

        MiniMaxApiClient client = new MiniMaxApiClient();

        // 1. 先输出原始 API 响应（便于调试）
        String rawJson = client.fetchModelsFromApi();
        log.info("官方 API 原始响应:\n{}", rawJson != null ? rawJson : "(null，接口不支持或网络异常)");

        // 2. 获取含使用场景的模型列表（自动回退到本地静态列表）
        java.util.List<MiniMaxApiClient.ModelInfo> models = client.getAvailableModelsWithUseCases();

        log.info("==================== 模型及使用场景 ====================");
        log.info("共 {} 个模型:", models.size());
        for (MiniMaxApiClient.ModelInfo m : models) {
            log.info("  {}", m);
        }
        log.info("=========================================================");

        // 无论 API 是否支持，静态列表保底不为空
        assertFalse(models.isEmpty(), "模型列表不能为空");

        // 验证每个 ModelInfo 的必填字段不为空
        for (MiniMaxApiClient.ModelInfo m : models) {
            assertNotNull(m.getId(),       "模型 id 不能为空");
            assertNotNull(m.getUseCases(), "模型使用场景不能为空");
            assertNotNull(m.getPlanRequirement(), "套餐要求不能为空");
            log.info("  验证通过: {}", m.getId());
        }

        log.info("模型列表验证通过，共 {} 个模型", models.size());
    }
}
