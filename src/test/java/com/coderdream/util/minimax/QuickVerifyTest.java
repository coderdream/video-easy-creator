package com.coderdream.util.minimax;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 快速验证测试（硬编码配置，无需重启 IDEA）
 *
 * @author Claude Code
 * @since 2026-03-04
 */
@Slf4j
public class QuickVerifyTest {

    // TODO: 请在这里填写你的实际配置
    private static final String BASE_URL = "https://api.minimaxi.com";
    private static final String API_KEY = "sk-cp-g_oaJaWZTmTc0rL1HWfWavKaQ8g0hHXIuAfoeuM0rN6XeW_bI-8hPjfhU-vl8dh-X3EnOJS8FpQy6H1NmZO4O0tnJW26eKdN3QrSQ-zezpjMO0674Qww5m8"; // 请替换为你的完整 API Key

    @Test
    public void test01VerifyConfig() {
        log.info("========== 配置验证 ==========");
        log.info("BASE_URL: {}", BASE_URL);
        log.info("API_KEY 前缀: {}", API_KEY.substring(0, Math.min(10, API_KEY.length())));
        log.info("API_KEY 后缀: {}", API_KEY.substring(Math.max(0, API_KEY.length() - 10)));

        boolean baseUrlOk = "https://api.minimaxi.com".equals(BASE_URL);
        boolean apiKeyOk = API_KEY.startsWith("sk-cp-g") && API_KEY.endsWith("ww5m8");

        log.info("BASE_URL 正确: {}", baseUrlOk ? "✓" : "✗");
        log.info("API_KEY 格式正确: {}", apiKeyOk ? "✓" : "✗");
    }

    @Test
    public void test02DirectApiCall() {
        log.info("========== 直接 API 调用测试 ==========");

        String fullUrl = BASE_URL + "/anthropic/v1/messages";
        log.info("完整 URL: {}", fullUrl);

        // 构造请求体
        JSONObject body = new JSONObject();
        body.set("model", "MiniMax-M2.1");
        body.set("max_tokens", 1024);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", "你好，请确认你是否在线。");
        messages.add(message);
        body.set("messages", messages);

        log.info("请求体: {}", JSONUtil.toJsonPrettyStr(body));

        try {
            log.info("发送请求...");
            String responseStr = HttpRequest.post(fullUrl)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("x-api-key", API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(60000)
                    .execute()
                    .body();

            log.info("========== 响应 ==========");
            log.info("响应长度: {} 字符", responseStr.length());

            try {
                JSONObject json = JSONUtil.parseObj(responseStr);
                log.info("响应内容: {}", JSONUtil.toJsonPrettyStr(json));

                if (json.containsKey("error")) {
                    log.error("API 返回错误: {}", json.getJSONObject("error"));
                } else {
                    log.info("✓ API 调用成功！");

                    // 提取响应内容
                    if (json.containsKey("content")) {
                        JSONArray content = json.getJSONArray("content");
                        for (int i = 0; i < content.size(); i++) {
                            JSONObject block = content.getJSONObject(i);
                            if ("text".equals(block.getStr("type"))) {
                                log.info("响应文本: {}", block.getStr("text"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析响应失败: {}", e.getMessage());
                log.info("原始响应: {}", responseStr);
            }

        } catch (Exception e) {
            log.error("请求失败: {}", e.getMessage(), e);
        }
    }
}
