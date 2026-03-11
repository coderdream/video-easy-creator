package com.coderdream.util.minimax;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * MiniMax API 客户端简单测试
 * 用于快速验证配置和连接
 *
 * @author Claude Code
 * @since 2026-03-04
 */
@Slf4j
public class MiniMaxApiClientSimpleTest {

    /**
     * 测试1: 检查环境变量
     */
    @Test
    public void test01CheckEnvironmentVariables() {
        log.info("========== 检查环境变量 ==========");

        String baseUrl = System.getenv("MINIMAX_BASE_URL");
        String apiKey = System.getenv("MINIMAX_API_KEY");

        log.info("MINIMAX_BASE_URL: {}", baseUrl);
        log.info("MINIMAX_API_KEY: {}", apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null");

        if (baseUrl == null || apiKey == null) {
            log.error("环境变量未设置！");
            log.error("请在系统环境变量中设置:");
            log.error("  MINIMAX_BASE_URL=https://api.minimaxi.com");
            log.error("  MINIMAX_API_KEY=sk-cp-gxxxxx");
        } else {
            log.info("✓ 环境变量已设置");
        }
    }

    /**
     * 测试2: 检查 ConfigUtil 读取
     */
    @Test
    public void test02CheckConfigUtil() {
        log.info("========== 检查 ConfigUtil 读取 ==========");

        try {
            String baseUrl = ConfigUtil.getMiniMaxBaseUrl();
            String apiKey = ConfigUtil.getMiniMaxApiKey();
            String version = ConfigUtil.getMiniMaxApiVersion();
            int timeout = ConfigUtil.getMiniMaxTimeout();
            String model = ConfigUtil.getClaudeModel();
            int maxTokens = ConfigUtil.getClaudeMaxTokens();

            log.info("Base URL: {}", baseUrl);
            log.info("API Key: {}", apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null");
            log.info("API Version: {}", version);
            log.info("Timeout: {}ms", timeout);
            log.info("Model: {}", model);
            log.info("Max Tokens: {}", maxTokens);

            boolean isValid = ConfigUtil.isConfigValid();
            log.info("配置有效性: {}", isValid ? "✓ 有效" : "✗ 无效");

            if (!isValid) {
                log.error("配置无效！请检查环境变量或 config.properties 文件");
            }
        } catch (Exception e) {
            log.error("读取配置失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试3: 测试 API 连接
     */
    @Test
    public void test03TestApiConnection() {
        log.info("========== 测试 API 连接 ==========");

        try {
            MiniMaxApiClient client = new MiniMaxApiClient();
            log.info("客户端配置: {}", client.getConfigInfo());

            // 构造简单的测试请求
            MiniMaxRequest request = new MiniMaxRequest()
                    .setModel("MiniMax-M2.1")
                    .setMaxTokens(100)
                    .addUserMessage("你好，请确认你是否在线。");

            log.info("========== 发送测试请求 ==========");
            log.info("模型: {}", request.getModel());
            log.info("Max Tokens: {}", request.getMaxTokens());
            log.info("消息数量: {}", request.getMessages().size());

            MiniMaxResponse response = client.sendMessage(request);

            log.info("========== 响应结果 ==========");
            log.info("Response ID: {}", response.getId());
            log.info("Model: {}", response.getModel());
            log.info("Finish Reason: {}", response.getFinishReason());

            String result = response.getFirstChoiceText();
            log.info("✓ API 调用成功！");
            log.info("响应内容: {}", result);

            if (response.getUsage() != null) {
                log.info("Token 使用: {}", response.getUsage().getTotalTokensCount());
            }

        } catch (Exception e) {
            log.error("========== API 调用失败 ==========");
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误信息: {}", e.getMessage());

            if (e.getCause() != null) {
                log.error("原因: {}", e.getCause().getMessage());
            }

            log.error("完整堆栈:", e);

            log.error("========== 排查建议 ==========");
            log.error("1. 检查 MINIMAX_BASE_URL 是否正确: https://api.minimaxi.com");
            log.error("2. 检查 MINIMAX_API_KEY 是否有效");
            log.error("3. 检查网络连接是否正常");
            log.error("4. 检查 API Key 是否有权限访问该模型");
            log.error("5. 尝试使用 curl 命令测试 API 是否可用");
        }
    }

    /**
     * 测试4: 使用旧版 API 方法测试
     */
    @Test
    public void test04TestLegacyApiMethod() {
        log.info("========== 测试旧版 API 方法 ==========");

        try {
            MiniMaxApiClient client = new MiniMaxApiClient();

            String result = client.sendMessage("你好，请确认你是否在线。");

            log.info("✓ API 调用成功！");
            log.info("响应内容: {}", result);

        } catch (Exception e) {
            log.error("API 调用失败: {}", e.getMessage(), e);
        }
    }
}
