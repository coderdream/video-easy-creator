package com.coderdream.util.claudecode;

import com.coderdream.util.cd.CdConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude API 客户端测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeApiClientTest {

    private ClaudeApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new ClaudeApiClient();
        log.info("测试设置完成");
        log.info(apiClient.getConfigInfo());
    }

    /**
     * 测试简单消息调用
     */
    @Test
    public void testSimpleMessage() {
        log.info("========== 测试：简单消息调用 ==========");

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                .setMaxTokens(1024)
                .setSystemPrompt("You are a helpful assistant. Answer in Chinese.")
                .addUserMessage("你好，请自我介绍一下");

        try {
            ClaudeResponse response = apiClient.sendMessage(request);

            assertNotNull(response, "响应不应为空");
            assertNotNull(response.getId(), "响应 ID 不应为空");
            assertFalse(response.getContent().isEmpty(), "内容不应为空");
            assertNotNull(response.getTextContent(), "文本内容不应为空");

            log.info("响应 ID：{}", response.getId());
            log.info("停止原因：{}", response.getStopReason());
            log.info("总 Token 数：{}", response.getUsage().getTotalTokens());
            log.info("响应文本：\n{}", response.getTextContent());

            assertTrue(response.isComplete(), "响应应为完成状态");
        } catch (Exception e) {
            log.error("测试失败", e);
            fail("测试不应抛出异常：" + e.getMessage());
        }
    }

    /**
     * 测试带思考模式的消息
     */
    @Test
    public void testMessageWithThinking() {
        log.info("========== 测试：带思考模式的消息 ==========");

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_MODEL_OPUS_45)
                .setMaxTokens(4096)
                .enableThinking(2048)
                .setSystemPrompt("You are an expert programmer.")
                .addUserMessage("解释一下快速排序算法的原理");

        try {
            ClaudeResponse response = apiClient.sendMessage(request);

            assertNotNull(response, "响应不应为空");
            assertFalse(response.getContent().isEmpty(), "内容不应为空");

            log.info("响应 ID：{}", response.getId());
            log.info("是否有思考内容：{}", response.hasThinking());
            log.info("总 Token 数：{}", response.getUsage().getTotalTokens());

            if (response.hasThinking()) {
                log.info("思考内容：\n{}", response.getThinkingContent());
            }

            log.info("响应文本：\n{}", response.getTextContent());
        } catch (Exception e) {
            log.error("测试失败", e);
            fail("测试不应抛出异常：" + e.getMessage());
        }
    }

    /**
     * 测试不同模型
     */
    @Test
    public void testDifferentModels() {
        log.info("========== 测试：不同模型 ==========");

        String[] models = {
                CdConstants.CLAUDE_MODEL_SONNET_45,
                CdConstants.CLAUDE_MODEL_HAIKU_45
        };

        for (String model : models) {
            log.info("测试模型：{}", model);

            ClaudeRequest request = new ClaudeRequest()
                    .setModel(model)
                    .setMaxTokens(512)
                    .addUserMessage("简单介绍一下你自己");

            try {
                ClaudeResponse response = apiClient.sendMessage(request);

                assertNotNull(response, "响应不应为空");
                assertEquals(model, response.getModel(), "模型应匹配");
                log.info("模型 {} - Token 数：{}", model, response.getUsage().getTotalTokens());
            } catch (Exception e) {
                log.error("模型测试失败：{}", model, e);
                fail("模型 " + model + " 测试不应抛出异常");
            }
        }
    }

    /**
     * 测试努力程度参数
     */
    @Test
    public void testEffortParameter() {
        log.info("========== 测试：努力程度参数 ==========");

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_MODEL_OPUS_45)
                .setMaxTokens(2048)
                .setEffortHigh()
                .addUserMessage("分析这个代码片段的性能问题：\n" +
                        "for (int i = 0; i < list.size(); i++) {\n" +
                        "    for (int j = 0; j < list.size(); j++) {\n" +
                        "        if (list.get(i).equals(list.get(j))) {\n" +
                        "            // do something\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        try {
            ClaudeResponse response = apiClient.sendMessage(request);

            assertNotNull(response, "响应不应为空");
            log.info("高努力程度响应 - Token 数：{}", response.getUsage().getTotalTokens());
            log.info("响应：\n{}", response.getTextContent());
        } catch (Exception e) {
            log.error("测试失败", e);
            fail("测试不应抛出异常：" + e.getMessage());
        }
    }

    /**
     * 测试多轮对话
     */
    @Test
    public void testMultiTurnConversation() {
        log.info("========== 测试：多轮对话 ==========");

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                .setMaxTokens(1024)
                .setSystemPrompt("You are a helpful assistant.");

        try {
            // 第一轮
            request.addUserMessage("什么是 Java？");
            ClaudeResponse response1 = apiClient.sendMessage(request);
            assertNotNull(response1, "第一轮响应不应为空");
            log.info("第一轮响应：\n{}", response1.getTextContent());

            // 第二轮
            request.addAssistantMessage(response1.getTextContent());
            request.addUserMessage("Java 有什么优点？");
            ClaudeResponse response2 = apiClient.sendMessage(request);
            assertNotNull(response2, "第二轮响应不应为空");
            log.info("第二轮响应：\n{}", response2.getTextContent());

            // 第三轮
            request.addAssistantMessage(response2.getTextContent());
            request.addUserMessage("如何学习 Java？");
            ClaudeResponse response3 = apiClient.sendMessage(request);
            assertNotNull(response3, "第三轮响应不应为空");
            log.info("第三轮响应：\n{}", response3.getTextContent());

        } catch (Exception e) {
            log.error("测试失败", e);
            fail("测试不应抛出异常：" + e.getMessage());
        }
    }

    /**
     * 测试请求验证
     */
    @Test
    public void testRequestValidation() {
        log.info("========== 测试：请求验证 ==========");

        // 测试缺少模型
        ClaudeRequest invalidRequest = new ClaudeRequest()
                .addUserMessage("test");

        try {
            invalidRequest.validate();
            fail("缺少模型时应抛出异常");
        } catch (IllegalArgumentException e) {
            log.info("正确捕获验证错误：{}", e.getMessage());
        }

        // 测试缺少消息
        ClaudeRequest invalidRequest2 = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL);

        try {
            invalidRequest2.validate();
            fail("缺少消息时应抛出异常");
        } catch (IllegalArgumentException e) {
            log.info("正确捕获验证错误：{}", e.getMessage());
        }
    }

    /**
     * 测试代理配置
     */
    @Test
    public void testProxyConfiguration() {
        log.info("========== 测试：代理配置 ==========");

        ClaudeApiClient clientWithProxy = new ClaudeApiClient()
                .setProxy("127.0.0.1", 7890)
                .setTimeout(60000);

        log.info(clientWithProxy.getConfigInfo());
        assertTrue(clientWithProxy.getConfigInfo().contains("127.0.0.1"), "配置应包含代理信息");
    }

    /**
     * 测试温度参数
     */
    @Test
    public void testTemperatureParameter() {
        log.info("========== 测试：温度参数 ==========");

        ClaudeRequest request = new ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                .setMaxTokens(512)
                .setTemperature(0.1f)  // 低温度 - 更确定的输出
                .addUserMessage("2+2等于多少？");

        try {
            ClaudeResponse response = apiClient.sendMessage(request);
            assertNotNull(response, "响应不应为空");
            log.info("低温度响应：\n{}", response.getTextContent());
        } catch (Exception e) {
            log.error("测试失败", e);
            fail("测试不应抛出异常：" + e.getMessage());
        }
    }
}
