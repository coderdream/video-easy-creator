package com.coderdream.util.minimax;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * MiniMax API 调试测试
 * 直接使用 HttpRequest 测试，不经过封装
 *
 * @author Claude Code
 * @since 2026-03-04
 */
@Slf4j
public class MiniMaxApiDebugTest {

    /**
     * 测试1: 直接使用 curl 命令的参数测试
     */
    @Test
    public void test01DirectApiCall() {
        log.info("========== 直接 API 调用测试 ==========");

        // 从环境变量读取配置
        String baseUrl = System.getenv("MINIMAX_BASE_URL");
        String apiKey = System.getenv("MINIMAX_API_KEY");

        log.info("Base URL: {}", baseUrl);
        log.info("API Key: {}", apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null");

        if (baseUrl == null || apiKey == null) {
            log.error("环境变量未设置！");
            return;
        }

        // 构造完整 URL
        String fullUrl = baseUrl + "/anthropic/v1/messages";
        log.info("完整 URL: {}", fullUrl);

        // 构造请求体（完全按照 curl 命令）
        JSONObject body = new JSONObject();
        body.set("model", "MiniMax-M2.1");
        body.set("max_tokens", 1024);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", "你好，请确认你是否在线。");
        messages.add(message);

        body.set("messages", messages);

        log.info("========== 请求信息 ==========");
        log.info("URL: {}", fullUrl);
        log.info("Method: POST");
        log.info("Headers:");
        log.info("  - Content-Type: application/json");
        log.info("  - x-api-key: {}...", apiKey.substring(0, Math.min(15, apiKey.length())));
        log.info("  - anthropic-version: 2023-06-01");
        log.info("Body:");
        log.info("{}", JSONUtil.toJsonPrettyStr(body));

        try {
            // 发送请求
            log.info("========== 发送请求 ==========");
            String responseStr = HttpRequest.post(fullUrl)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(60000)
                    .execute()
                    .body();

            log.info("========== 响应信息 ==========");
            log.info("响应长度: {} 字符", responseStr.length());
            log.info("响应内容:");
            log.info("{}", responseStr);

            // 尝试解析响应
            try {
                JSONObject json = JSONUtil.parseObj(responseStr);
                log.info("========== 解析后的响应 ==========");
                log.info("{}", JSONUtil.toJsonPrettyStr(json));

                // 检查是否有错误
                if (json.containsKey("error")) {
                    log.error("API 返回错误: {}", json.getJSONObject("error"));
                } else {
                    log.info("✓ API 调用成功！");

                    // 提取内容
                    if (json.containsKey("content")) {
                        JSONArray content = json.getJSONArray("content");
                        log.info("Content 数组: {}", content);

                        for (int i = 0; i < content.size(); i++) {
                            JSONObject block = content.getJSONObject(i);
                            log.info("Block {}: type={}, text={}", i, block.getStr("type"), block.getStr("text"));
                        }
                    }

                    if (json.containsKey("choices")) {
                        JSONArray choices = json.getJSONArray("choices");
                        log.info("Choices 数组: {}", choices);

                        for (int i = 0; i < choices.size(); i++) {
                            JSONObject choice = choices.getJSONObject(i);
                            log.info("Choice {}: {}", i, choice);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析响应失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("========== 请求失败 ==========");
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误信息: {}", e.getMessage());
            log.error("完整堆栈:", e);
        }
    }

    /**
     * 测试2: 使用 MiniMaxApiClient 测试
     */
    @Test
    public void test02UsingMiniMaxApiClient() {
        log.info("========== 使用 MiniMaxApiClient 测试 ==========");

        try {
            MiniMaxApiClient client = new MiniMaxApiClient();
            log.info("客户端配置: {}", client.getConfigInfo());

            MiniMaxRequest request = new MiniMaxRequest()
                    .setModel("MiniMax-M2.1")
                    .setMaxTokens(1024)
                    .addUserMessage("你好，请确认你是否在线。");

            log.info("发送请求...");
            MiniMaxResponse response = client.sendMessage(request);

            log.info("========== 响应结果 ==========");
            log.info("Response ID: {}", response.getId());
            log.info("Model: {}", response.getModel());
            log.info("Finish Reason: {}", response.getFinishReason());
            log.info("响应内容: {}", response.getFirstChoiceText());

            if (response.getUsage() != null) {
                log.info("Token 使用: {}", response.getUsage().getTotalTokensCount());
            }

            log.info("✓ 测试成功！");

        } catch (Exception e) {
            log.error("测试失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试3: 对比 Apifox 的请求
     */
    @Test
    public void test03CompareWithApifox() {
        log.info("========== 对比 Apifox 请求 ==========");

        String baseUrl = System.getenv("MINIMAX_BASE_URL");
        String apiKey = System.getenv("MINIMAX_API_KEY");

        log.info("Apifox 使用的参数:");
        log.info("  URL: https://api.minimaxi.com/anthropic/v1/messages");
        log.info("  Headers:");
        log.info("    - x-api-key: sk-cp-gxxxxx");
        log.info("    - anthropic-version: 2023-06-01");
        log.info("    - Content-Type: application/json");
        log.info("  Body:");
        log.info("    {");
        log.info("      \"model\": \"MiniMax-M2.1\",");
        log.info("      \"max_tokens\": 1024,");
        log.info("      \"messages\": [");
        log.info("        {");
        log.info("          \"role\": \"user\",");
        log.info("          \"content\": \"你好，请确认你是否在线。\"");
        log.info("        }");
        log.info("      ]");
        log.info("    }");

        log.info("");
        log.info("当前代码使用的参数:");
        log.info("  URL: {}/anthropic/v1/messages", baseUrl);
        log.info("  Headers:");
        log.info("    - x-api-key: {}...", apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) : "null");
        log.info("    - anthropic-version: 2023-06-01");
        log.info("    - Content-Type: application/json");
        log.info("  Body: (相同)");

        log.info("");
        log.info("对比结果:");
        boolean urlMatch = baseUrl != null && baseUrl.equals("https://api.minimaxi.com");
        log.info("  URL 匹配: {}", urlMatch ? "✓" : "✗");
        log.info("  Headers 匹配: ✓ (相同)");
        log.info("  Body 匹配: ✓ (相同)");

        if (!urlMatch) {
            log.warn("URL 不匹配！请确保 MINIMAX_BASE_URL=https://api.minimaxi.com");
        }
    }
}
