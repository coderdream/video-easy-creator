package com.coderdream.util.claudecode;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Claude API 客户端
 * 负责与 Anthropic Messages API 的通信
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeApiClient {

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
     * 代理主机（已禁用，不使用代理）
     */
    private String proxyHost;

    /**
     * 代理端口（已禁用）
     */
    private int proxyPort;

    /**
     * 构造函数 - 从环境变量读取配置
     */
    public ClaudeApiClient() {
        this.baseUrl = System.getenv("ANTHROPIC_BASE_URL");
        this.authToken = System.getenv("ANTHROPIC_AUTH_TOKEN");
        this.apiVersion = CdConstants.CLAUDE_API_VERSION;
        this.timeout = CdConstants.CLAUDE_API_TIMEOUT;
        // 禁用代理，直接连接
        this.proxyHost = null;
        this.proxyPort = 0;

        log.info("ClaudeApiClient 已初始化，baseUrl: {}", baseUrl);
        log.info("代理已禁用，将直接连接");
    }

    /**
     * 构造函数 - 自定义配置
     *
     * @param baseUrl   API 基础 URL
     * @param authToken API 认证令牌
     */
    public ClaudeApiClient(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.apiVersion = CdConstants.CLAUDE_API_VERSION;
        this.timeout = CdConstants.CLAUDE_API_TIMEOUT;
    }

    /**
     * 发送消息请求
     *
     * @param request 请求对象
     * @return 响应对象
     * @throws RuntimeException 如果请求失败
     */
    public ClaudeResponse sendMessage(ClaudeRequest request) {
        request.validate();
        JSONObject body = buildRequestBody(request);
        return executeRequest(body);
    }

    /**
     * 发送带工具定义的消息请求
     *
     * @param request 请求对象
     * @param tools   工具定义列表
     * @return 响应对象
     * @throws RuntimeException 如果请求失败
     */
    public ClaudeResponse sendMessageWithTools(ClaudeRequest request, List<JSONObject> tools) {
        request.validate();
        JSONObject body = buildRequestBody(request);

        if (tools != null && !tools.isEmpty()) {
            JSONArray toolsArray = new JSONArray();
            for (JSONObject tool : tools) {
                toolsArray.add(tool);
            }
            body.set("tools", toolsArray);
            log.debug("已添加 {} 个工具到请求", tools.size());
        }

        return executeRequest(body);
    }

    /**
     * 构建请求体
     *
     * @param request 请求对象
     * @return JSON 请求体
     */
    private JSONObject buildRequestBody(ClaudeRequest request) {
        JSONObject body = new JSONObject();

        // 基础参数
        body.set("model", request.getModel());
        body.set("max_tokens", request.getMaxTokens());

        // 可选参数
        if (request.getTemperature() != null) {
            body.set("temperature", request.getTemperature());
        }

        if (request.getEffort() != null) {
            body.set("effort", request.getEffort());
        }

        // 系统提示词
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            body.set("system", request.getSystemPrompt());
        }

        // 思考模式
        if (request.isThinkingEnabled()) {
            JSONObject thinking = new JSONObject();
            thinking.set("type", "enabled");
            thinking.set("budget_tokens", request.getThinkingBudget());
            body.set("thinking", thinking);
            log.debug("思考模式已启用，预算: {}", request.getThinkingBudget());
        }

        // 消息列表
        JSONArray messages = new JSONArray();
        for (ClaudeMessage msg : request.getMessages()) {
            messages.add(msg.toJson());
        }
        body.set("messages", messages);

        log.debug("请求体已构建，包含 {} 条消息", request.getMessages().size());
        return body;
    }

    /**
     * 执行 HTTP 请求
     *
     * @param body 请求体
     * @return 响应对象
     * @throws RuntimeException 如果请求失败
     */
    private ClaudeResponse executeRequest(JSONObject body) {
        try {
            String url = baseUrl + "/v1/messages";
            log.debug("发送请求到: {}", url);

            HttpRequest httpRequest = HttpRequest.post(url)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("x-api-key", authToken)
                    .header("anthropic-version", apiVersion)
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(timeout);

            // 配置代理
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                httpRequest.setHttpProxy(proxyHost, proxyPort);
                log.debug("使用代理: {}:{}", proxyHost, proxyPort);
            }

            String responseStr = httpRequest.execute().body();
            log.debug("API 响应已接收，长度: {}", responseStr.length());

            return parseResponse(responseStr);
        } catch (Exception e) {
            log.error("API 请求失败", e);
            throw new RuntimeException("Failed to call Anthropic API: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 API 响应
     *
     * @param responseStr 响应字符串
     * @return 解析后的响应对象
     * @throws RuntimeException 如果响应包含错误
     */
    private ClaudeResponse parseResponse(String responseStr) {
        JSONObject json = JSONUtil.parseObj(responseStr);

        // 检查错误
        if (json.containsKey("error")) {
            String errorMsg = json.getByPath("error.message").toString();
            log.error("API 错误: {}", errorMsg);
            throw new RuntimeException("API Error: " + errorMsg);
        }

        ClaudeResponse response = new ClaudeResponse();
        response.setId(json.getStr("id"));
        response.setModel(json.getStr("model"));
        response.setStopReason(json.getStr("stop_reason"));
        response.setTimestamp(System.currentTimeMillis());

        // 解析内容块
        JSONArray contentArray = json.getJSONArray("content");
        List<ClaudeResponse.ContentBlock> contentBlocks = new ArrayList<>();

        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject block = contentArray.getJSONObject(i);
            ClaudeResponse.ContentBlock cb = new ClaudeResponse.ContentBlock();
            cb.setType(block.getStr("type"));

            if ("text".equals(cb.getType())) {
                cb.setText(block.getStr("text"));
            } else if ("tool_use".equals(cb.getType())) {
                cb.setToolUseId(block.getStr("id"));
                cb.setToolName(block.getStr("name"));
                cb.setToolInput(block.getJSONObject("input"));
            } else if ("thinking".equals(cb.getType())) {
                cb.setThinking(block.getStr("thinking"));
            }

            contentBlocks.add(cb);
        }
        response.setContent(contentBlocks);

        // 解析 Token 使用统计
        JSONObject usage = json.getJSONObject("usage");
        ClaudeResponse.Usage usageObj = new ClaudeResponse.Usage();
        usageObj.setInputTokens(usage.getInt("input_tokens"));
        usageObj.setOutputTokens(usage.getInt("output_tokens"));

        if (usage.containsKey("cache_creation_input_tokens")) {
            usageObj.setCacheCreationInputTokens(usage.getInt("cache_creation_input_tokens"));
        }
        if (usage.containsKey("cache_read_input_tokens")) {
            usageObj.setCacheReadInputTokens(usage.getInt("cache_read_input_tokens"));
        }

        response.setUsage(usageObj);

        log.info("响应解析成功。停止原因：{}，总 Token 数：{}",
                response.getStopReason(), usageObj.getTotalTokens());

        return response;
    }

    /**
     * 设置代理（已禁用，此方法不再生效）
     *
     * @param host 代理主机
     * @param port 代理端口
     * @return 当前客户端实例（支持链式调用）
     * @deprecated 代理功能已禁用，此方法不再生效
     */
    @Deprecated
    public ClaudeApiClient setProxy(String host, int port) {
        log.warn("代理功能已禁用，忽略代理设置: {}:{}", host, port);
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间（毫秒）
     * @return 当前客户端实例（支持链式调用）
     */
    public ClaudeApiClient setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 获取当前配置信息
     *
     * @return 配置信息字符串
     */
    public String getConfigInfo() {
        return String.format(
                "ClaudeApiClient Config: baseUrl=%s, apiVersion=%s, timeout=%d, proxy=disabled",
                baseUrl, apiVersion, timeout
        );
    }
}
