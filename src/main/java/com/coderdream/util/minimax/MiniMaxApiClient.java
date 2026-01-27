package com.coderdream.util.minimax;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * MiniMax API 客户端
 * 负责与 MiniMax Messages API 的通信
 *
 * @author Claude Code
 * @since 2026-01-27
 */
@Slf4j
public class MiniMaxApiClient {

    /**
     * API 基础 URL
     */
    private String baseUrl;

    /**
     * API 认证令牌
     */
    private String authToken;

    /**
     * API 版本
     */
    private String apiVersion;

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 最大 Token 数
     */
    private int maxTokens;

    /**
     * 构造函数 - 从 ConfigUtil 读取配置
     */
    public MiniMaxApiClient() {
        this.baseUrl = ConfigUtil.getMiniMaxBaseUrl();
        this.authToken = ConfigUtil.getMiniMaxApiKey();
        this.apiVersion = ConfigUtil.getMiniMaxApiVersion();
        this.timeout = ConfigUtil.getMiniMaxTimeout();
        this.model = ConfigUtil.getClaudeModel();
        this.maxTokens = ConfigUtil.getClaudeMaxTokens();

        log.info("MiniMaxApiClient 已初始化，baseUrl: {}", baseUrl);
        log.info("使用模型: {}，maxTokens: {}", model, maxTokens);
    }

    /**
     * 构造函数 - 自定义配置
     *
     * @param baseUrl   API 基础 URL
     * @param authToken API 认证令牌
     * @param model     模型名称
     * @param maxTokens 最大 Token 数
     */
    public MiniMaxApiClient(String baseUrl, String authToken, String model, int maxTokens) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.apiVersion = "2023-06-01";
        this.timeout = 60000;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    /**
     * 发送消息请求
     *
     * @param prompt 提示词
     * @return 生成的文本内容
     */
    public String sendMessage(String prompt) {
        return sendMessage(prompt, this.model, this.maxTokens);
    }

    /**
     * 发送消息请求（自定义模型）
     *
     * @param prompt     提示词
     * @param model      模型名称
     * @param maxTokens  最大 Token 数
     * @return 生成的文本内容
     */
    public String sendMessage(String prompt, String model, int maxTokens) {
        // 构建请求体
        JSONObject body = buildRequestBody(prompt, model, maxTokens);

        // 打印请求详情
        logRequest(body);

        // 执行请求
        String responseStr = executeRequest(body);

        // 解析响应
        return parseResponse(responseStr);
    }

    /**
     * 构建请求体
     */
    private JSONObject buildRequestBody(String prompt, String model, int maxTokens) {
        JSONObject body = new JSONObject();

        body.set("model", model);
        body.set("max_tokens", maxTokens);

        // 消息列表
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", prompt);
        messages.add(message);

        body.set("messages", messages);

        log.debug("请求体已构建，包含 1 条消息");
        return body;
    }

    /**
     * 获取完整的 API URL
     * 兼容两种格式：
     * 1. https://api.minimaxi.com (需要添加 /anthropic/v1/messages)
     * 2. https://api.minimaxi.com/anthropic/v1 (只需要添加 /messages)
     */
    private String getFullUrl() {
        if (baseUrl.endsWith("/anthropic/v1")) {
            return baseUrl + "/messages";
        } else if (baseUrl.endsWith("/anthropic")) {
            return baseUrl + "/v1/messages";
        } else {
            // 默认添加完整的 anthropic/v1/messages 路径
            return baseUrl + "/anthropic/v1/messages";
        }
    }

    /**
     * 打印请求详情
     */
    private void logRequest(JSONObject body) {
        String url = getFullUrl();

        log.info("========== MiniMax API 请求详情 ==========");
        log.info("【URL】: {}", url);
        log.info("【Method】: POST");
        log.info("【Headers】:");
        log.info("  - Content-Type: application/json");
        log.info("  - x-api-key: {}", authToken != null ? authToken.substring(0, Math.min(10, authToken.length())) + "..." : "null");
        log.info("  - anthropic-version: {}", apiVersion);
        log.info("【Request Body】:");
        log.info("{}", JSONUtil.toJsonStr(body));
        log.info("==========================================");
    }

    /**
     * 执行 HTTP 请求
     */
    private String executeRequest(JSONObject body) {
        try {
            String url = getFullUrl();

            HttpRequest httpRequest = HttpRequest.post(url)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("x-api-key", authToken)
                    .header("anthropic-version", apiVersion)
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(timeout);

            log.info("正在发送请求...");
            String responseStr = httpRequest.execute().body();

            log.info("响应长度: {} 字符", responseStr != null ? responseStr.length() : 0);
            return responseStr;

        } catch (Exception e) {
            log.error("API 请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call MiniMax API: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 API 响应
     */
    private String parseResponse(String responseStr) {
        // 检查响应是否为空
        if (StrUtil.isBlank(responseStr)) {
            log.error("API 响应为空");
            throw new RuntimeException("API 响应为空");
        }

        // 先尝试解析 JSON（避免字符串误匹配）
        JSONObject json;
        try {
            json = JSONUtil.parseObj(responseStr);
        } catch (Exception e) {
            log.error("JSON解析失败，响应内容: {}", responseStr.substring(0, Math.min(500, responseStr.length())));
            throw new RuntimeException("JSON解析失败: " + e.getMessage() + ", 响应: " + responseStr);
        }

        // 检查是否是错误响应（有 error 字段且没有 content 字段）
        if (json.containsKey("error")) {
            String errorMsg = json.getByPath("error.message") != null ?
                    json.getByPath("error.message").toString() : "未知错误";
            log.error("API 错误: {}", errorMsg);
            throw new RuntimeException("API Error: " + errorMsg);
        }

        // 检查是否包含 429 错误（Too Many Requests）
        if (json.containsKey("error") ||
            (json.containsKey("base_resp") && json.getJSONObject("base_resp").getInt("status_code", -1) != 0)) {
            log.warn("API 返回错误信息: {}", responseStr.substring(0, Math.min(200, responseStr.length())));
            throw new RuntimeException("API Error: " + responseStr);
        }

        // 提取内容
        String result = extractContent(json);
        log.info("成功提取内容，长度: {} 字符", result != null ? result.length() : 0);
        return result;
    }

    /**
     * 从响应中提取文本内容
     * MiniMax API 返回的 content 数组可能包含多个块，需要找 type="text" 的块
     */
    private String extractContent(JSONObject json) {
        // 从 content 数组提取 type="text" 的块
        if (json.containsKey("content")) {
            JSONArray contentArray = json.getJSONArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                // 遍历 content 数组，找到 type="text" 的块
                for (int i = 0; i < contentArray.size(); i++) {
                    JSONObject block = contentArray.getJSONObject(i);
                    if (block != null) {
                        // 检查是否是 text 类型的块
                        String type = block.getStr("type", "");
                        if ("text".equals(type) && block.containsKey("text")) {
                            return block.getStr("text");
                        }
                        // 也支持直接有 text 字段的情况
                        if (block.containsKey("text")) {
                            return block.getStr("text");
                        }
                    }
                }
            }
        }

        // 备选：从 text 字段提取
        if (json.containsKey("text")) {
            return json.getStr("text");
        }

        // 备选：从 message 字段提取
        if (json.containsKey("message")) {
            JSONObject message = json.getJSONObject("message");
            if (message != null && message.containsKey("content")) {
                return message.getStr("content");
            }
        }

        log.error("无法从响应中提取内容: {}", json.toString().substring(0, Math.min(200, json.toString().length())));
        throw new RuntimeException("无法从响应中提取内容");
    }

    /**
     * 获取当前配置信息
     */
    public String getConfigInfo() {
        return String.format(
                "MiniMaxApiClient Config: baseUrl=%s, apiVersion=%s, timeout=%d, model=%s, maxTokens=%d",
                baseUrl, apiVersion, timeout, model, maxTokens
        );
    }

    /**
     * 检查配置是否有效
     */
    public boolean isConfigValid() {
        return StrUtil.isNotBlank(baseUrl) && StrUtil.isNotBlank(authToken);
    }
}
