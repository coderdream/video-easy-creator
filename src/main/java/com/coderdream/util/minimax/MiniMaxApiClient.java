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
     * MiniMax API 返回的 content 数组可能包含多个块：
     * - type="text"    → 正常文本输出（优先）
     * - type="thinking" → 推理模型的思考过程（备用，当 max_tokens 不足时 text 块不会出现）
     */
    private String extractContent(JSONObject json) {
        // 从 content 数组提取内容
        if (json.containsKey("content")) {
            JSONArray contentArray = json.getJSONArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                String thinkingFallback = null;
                for (int i = 0; i < contentArray.size(); i++) {
                    JSONObject block = contentArray.getJSONObject(i);
                    if (block != null) {
                        String type = block.getStr("type", "");
                        if ("text".equals(type) && block.containsKey("text")) {
                            return block.getStr("text");
                        }
                        // 直接含有 text 字段（无 type 的旧格式）
                        if (block.containsKey("text") && !"thinking".equals(type)) {
                            return block.getStr("text");
                        }
                        // 记录 thinking 内容作为备用
                        if ("thinking".equals(type) && thinkingFallback == null) {
                            thinkingFallback = block.getStr("thinking");
                            log.debug("发现 thinking 块，长度: {} 字符", thinkingFallback != null ? thinkingFallback.length() : 0);
                        }
                    }
                }
                // max_tokens 不足时 thinking 耗尽 token，text 块不出现；返回 thinking 内容
                if (thinkingFallback != null) {
                    log.warn("未找到 type=text 块（stop_reason={}），thinking 块可能已耗尽 max_tokens，返回 thinking 内容。建议增大 max_tokens。",
                            json.getStr("stop_reason"));
                    return "[thinking] " + thinkingFallback;
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
     * 发送消息请求（使用 MiniMaxRequest 对象）
     *
     * @param request 请求对象
     * @return API 响应对象
     */
    public MiniMaxResponse sendMessage(MiniMaxRequest request) {
        // 验证请求参数
        request.validate();

        // 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", request.getModel());

        // 转换消息列表：Anthropic 格式要求 system 放顶层，不能在 messages 数组里
        JSONArray messages = new JSONArray();
        StringBuilder systemContent = new StringBuilder();
        for (MiniMaxMessage msg : request.getMessages()) {
            if ("system".equals(msg.getRole())) {
                if (systemContent.length() > 0) {
                    systemContent.append("\n");
                }
                systemContent.append(msg.getContent());
            } else {
                JSONObject msgObj = new JSONObject();
                msgObj.set("role", msg.getRole());
                msgObj.set("content", msg.getContent());
                messages.add(msgObj);
            }
        }
        if (systemContent.length() > 0) {
            body.set("system", systemContent.toString());
            log.debug("system 消息已提取到顶层，长度: {} 字符", systemContent.length());
        }
        body.set("messages", messages);

        if (request.getMaxTokens() != null) {
            body.set("max_tokens", request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            body.set("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            body.set("top_p", request.getTopP());
        }
        if (request.getStream() != null) {
            body.set("stream", request.getStream());
        }

        // 打印请求详情
        logRequest(body);

        // 执行请求
        String responseStr = executeRequest(body);

        // 解析为 MiniMaxResponse 对象
        return parseToResponse(responseStr);
    }

    /**
     * 解析响应字符串为 MiniMaxResponse 对象
     *
     * @param responseStr 响应字符串
     * @return MiniMaxResponse 对象
     */
    private MiniMaxResponse parseToResponse(String responseStr) {
        log.debug("Raw response ({} chars): {}", responseStr.length(),
                responseStr.substring(0, Math.min(600, responseStr.length())));
        JSONObject json = JSONUtil.parseObj(responseStr);

        MiniMaxResponse response = new MiniMaxResponse();
        response.setId(json.getStr("id"));
        response.setObject(json.getStr("object"));
        response.setCreated(json.getLong("created"));
        response.setModel(json.getStr("model"));
        response.setTimestamp(System.currentTimeMillis());

        // 解析 choices（OpenAI 格式）
        List<MiniMaxResponse.Choice> choices = new ArrayList<>();
        if (json.containsKey("choices")) {
            JSONArray choicesArray = json.getJSONArray("choices");

            for (int i = 0; i < choicesArray.size(); i++) {
                JSONObject choiceObj = choicesArray.getJSONObject(i);
                MiniMaxResponse.Choice choice = new MiniMaxResponse.Choice();
                choice.setIndex(choiceObj.getInt("index"));
                choice.setFinishReason(choiceObj.getStr("finish_reason"));

                // 解析 message
                if (choiceObj.containsKey("message")) {
                    JSONObject msgObj = choiceObj.getJSONObject("message");
                    MiniMaxResponse.Message message = new MiniMaxResponse.Message();
                    message.setSenderType(msgObj.getStr("sender_type", msgObj.getStr("role")));
                    message.setText(msgObj.getStr("text", msgObj.getStr("content")));
                    choice.setMessage(message);
                }

                choices.add(choice);
            }
        }

        // 解析 content（Anthropic 格式：content 数组在响应顶层）
        if (choices.isEmpty() && json.containsKey("content")) {
            JSONArray contentArray = json.getJSONArray("content");
            log.debug("Anthropic 格式响应，content 数组共 {} 个块", contentArray.size());
            String thinkingFallback = null;
            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject block = contentArray.getJSONObject(i);
                log.debug("Block[{}]: {}", i, block);
                if (block != null) {
                    String blockType = block.getStr("type", "");
                    if ("text".equals(blockType)) {
                        // 兼容 "text" 和 "content" 两种字段名
                        String text = block.getStr("text");
                        if (text == null || text.isEmpty()) {
                            text = block.getStr("content");
                        }
                        MiniMaxResponse.Choice choice = new MiniMaxResponse.Choice();
                        choice.setIndex(0);
                        choice.setFinishReason(json.getStr("stop_reason"));
                        MiniMaxResponse.Message message = new MiniMaxResponse.Message();
                        message.setSenderType(json.getStr("role", "assistant"));
                        message.setText(text != null ? text : "");
                        choice.setMessage(message);
                        choices.add(choice);
                        log.debug("提取文本成功，长度: {} 字符", text != null ? text.length() : 0);
                        break;
                    } else if ("thinking".equals(blockType) && thinkingFallback == null) {
                        // M2.5 推理模型会先输出 thinking 块；记录下来以备 max_tokens 耗尽时使用
                        thinkingFallback = block.getStr("thinking");
                        log.debug("发现 thinking 块，长度: {} 字符", thinkingFallback != null ? thinkingFallback.length() : 0);
                    }
                }
            }
            // max_tokens 不足时 thinking 会占满所有 token，text 块不会出现
            // 此时用 thinking 内容作为备用，并警告用户增大 max_tokens
            if (choices.isEmpty() && thinkingFallback != null) {
                log.warn("未找到 type=text 块（stop_reason={}），thinking 块可能已耗尽 max_tokens，以 thinking 内容作为备用。建议增大 max_tokens。",
                        json.getStr("stop_reason"));
                MiniMaxResponse.Choice choice = new MiniMaxResponse.Choice();
                choice.setIndex(0);
                choice.setFinishReason(json.getStr("stop_reason"));
                MiniMaxResponse.Message message = new MiniMaxResponse.Message();
                message.setSenderType(json.getStr("role", "assistant"));
                message.setText("[thinking] " + thinkingFallback);
                choice.setMessage(message);
                choices.add(choice);
            }
        }

        if (!choices.isEmpty()) {
            response.setChoices(choices);
        }

        // 解析 usage
        if (json.containsKey("usage")) {
            JSONObject usageObj = json.getJSONObject("usage");
            MiniMaxResponse.Usage usage = new MiniMaxResponse.Usage();
            // 兼容 OpenAI 格式（total_tokens）和 Anthropic 格式（input_tokens + output_tokens）
            Integer totalTokens = usageObj.getInt("total_tokens");
            if (totalTokens == null) {
                Integer inputTokens = usageObj.getInt("input_tokens", 0);
                Integer outputTokens = usageObj.getInt("output_tokens", 0);
                totalTokens = inputTokens + outputTokens;
            }
            usage.setTotalTokens(totalTokens);
            response.setUsage(usage);
        }

        return response;
    }

    /**
     * 使用高速模型发送消息（MiniMax-M2.5-highspeed，100 token/s）
     *
     * @param prompt 提示词
     * @return 生成的文本内容
     */
    public String sendMessageWithHighspeed(String prompt) {
        String highspeedModel = ConfigUtil.getHighspeedModel();
        log.info("使用高速模型: {}", highspeedModel);
        return sendMessage(prompt, highspeedModel, this.maxTokens);
    }

    /**
     * 使用高速模型发送 MiniMaxRequest
     *
     * @param request 请求对象（model 字段会被强制替换为高速模型）
     * @return API 响应对象
     */
    public MiniMaxResponse sendMessageWithHighspeed(MiniMaxRequest request) {
        String highspeedModel = ConfigUtil.getHighspeedModel();
        log.info("使用高速模型: {}", highspeedModel);
        request.setModel(highspeedModel);
        return sendMessage(request);
    }

    // =====================================================================
    // 模型信息与查询
    // =====================================================================

    /**
     * 模型信息（含使用场景说明）
     */
    public static class ModelInfo {
        private final String id;
        private final String displayName;
        private final String createdAt;
        private final String useCases;
        private final String planRequirement;

        public ModelInfo(String id, String displayName, String createdAt,
                         String useCases, String planRequirement) {
            this.id = id;
            this.displayName = displayName;
            this.createdAt = createdAt;
            this.useCases = useCases;
            this.planRequirement = planRequirement;
        }

        public String getId()             { return id; }
        public String getDisplayName()    { return displayName; }
        public String getCreatedAt()      { return createdAt; }
        public String getUseCases()       { return useCases; }
        public String getPlanRequirement(){ return planRequirement; }

        @Override
        public String toString() {
            return String.format("[%s] %s | 场景: %s | 套餐要求: %s",
                    id, displayName, useCases, planRequirement);
        }
    }

    /**
     * 各模型的使用场景说明（基于官方文档和实测）
     * key = model id, value = {useCases, planRequirement}
     */
    private static final java.util.Map<String, String[]> MODEL_USE_CASES =
            java.util.Map.of(
                "MiniMax-M2.5", new String[]{
                        "高质量翻译、复杂推理、长文档理解（200K上下文）、代码生成、BBC脚本翻译",
                        "Coding Plan 及以上"},
                "MiniMax-M2.5-highspeed", new String[]{
                        "延迟敏感场景、实时对话、批量短文本翻译（100 token/s）",
                        "Pay-as-you-go（错误码2061表示当前套餐不支持）"},
                "MiniMax-M2.1", new String[]{
                        "M2.5的降级备用、普通翻译任务、对质量要求略低时",
                        "Coding Plan 及以上"},
                "MiniMax-M2.1-highspeed", new String[]{
                        "M2.5-highspeed的降级备用（实测当前Coding Plan会路由到M2.5）",
                        "Coding Plan 及以上（实测可用）"},
                "MiniMax-M2", new String[]{
                        "最大兼容性、旧版API调用、简单文本任务兜底",
                        "Coding Plan 及以上"}
            );

    /**
     * 从官方 API 获取当前账号可用的模型列表
     * 调用 GET /anthropic/v1/models（Anthropic 兼容接口）
     *
     * @return 原始 JSON 响应字符串；失败时返回 null
     */
    public String fetchModelsFromApi() {
        String url = getModelsUrl();
        log.info("查询可用模型列表: GET {}", url);
        try {
            String responseStr = cn.hutool.http.HttpRequest.get(url)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("x-api-key", authToken)
                    .header("anthropic-version", apiVersion)
                    .timeout(timeout)
                    .execute()
                    .body();
            log.debug("模型列表响应: {}", responseStr);
            return responseStr;
        } catch (Exception e) {
            log.error("获取模型列表失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前账号可用的模型列表（含使用场景说明）
     * 先尝试调用官方 API，解析失败时回退到本地静态列表
     *
     * @return ModelInfo 列表（按推荐优先级排序）
     */
    public java.util.List<ModelInfo> getAvailableModelsWithUseCases() {
        java.util.List<ModelInfo> result = new java.util.ArrayList<>();

        // 尝试从官方 API 获取
        String rawJson = fetchModelsFromApi();
        if (rawJson != null) {
            try {
                JSONObject json = JSONUtil.parseObj(rawJson);
                if (json.containsKey("data")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        String id          = item.getStr("id", "");
                        String displayName = item.getStr("display_name", id);
                        String createdAt   = item.getStr("created_at", "");
                        String[] cases = MODEL_USE_CASES.getOrDefault(id,
                                new String[]{"通用文本生成", "请参考官方文档"});
                        result.add(new ModelInfo(id, displayName, createdAt, cases[0], cases[1]));
                    }
                    if (!result.isEmpty()) {
                        log.info("从官方 API 获取到 {} 个模型", result.size());
                        return result;
                    }
                }
                // API 返回错误时记录
                if (json.containsKey("error")) {
                    log.warn("API 返回错误: {}", json.getByPath("error.message"));
                }
            } catch (Exception e) {
                log.warn("解析模型列表响应失败，回退到本地静态列表: {}", e.getMessage());
            }
        }

        // 回退到本地静态列表
        log.info("使用本地静态模型列表");
        String[] staticModels = {"MiniMax-M2.5", "MiniMax-M2.5-highspeed",
                                 "MiniMax-M2.1", "MiniMax-M2.1-highspeed", "MiniMax-M2"};
        for (String id : staticModels) {
            String[] cases = MODEL_USE_CASES.getOrDefault(id,
                    new String[]{"通用文本生成", "请参考官方文档"});
            result.add(new ModelInfo(id, id, "", cases[0], cases[1]));
        }
        return result;
    }

    /**
     * 获取当前支持的模型名称列表（Anthropic 兼容接口，静态列表）
     *
     * @return 模型名称列表（按推荐优先级排序）
     */
    public java.util.List<String> getAvailableModels() {
        return java.util.List.of(
                "MiniMax-M2.5",            // 最新，最强，推荐（标准套餐可用）
                "MiniMax-M2.5-highspeed",  // 高速，100 token/s（需升级套餐，错误码 2061 表示不支持）
                "MiniMax-M2.1",            // 降级备用（标准套餐可用）
                "MiniMax-M2.1-highspeed",  // 高速降级备用（需升级套餐）
                "MiniMax-M2"               // 旧版兼容
        );
    }

    /**
     * 获取模型列表 API URL
     */
    private String getModelsUrl() {
        if (baseUrl.endsWith("/anthropic/v1")) {
            return baseUrl + "/models";
        } else if (baseUrl.endsWith("/anthropic")) {
            return baseUrl + "/v1/models";
        } else {
            return baseUrl + "/anthropic/v1/models";
        }
    }

    /**
     * 获取当前配置信息
     */
    public String getConfigInfo() {
        return String.format(
                "MiniMaxApiClient Config: baseUrl=%s, apiVersion=%s, timeout=%d, model=%s, maxTokens=%d, highspeed=%s, fallback=%s",
                baseUrl, apiVersion, timeout, model, maxTokens,
                ConfigUtil.getHighspeedModel(), ConfigUtil.getFallbackModel()
        );
    }

    /**
     * 检查配置是否有效
     */
    public boolean isConfigValid() {
        return StrUtil.isNotBlank(baseUrl) && StrUtil.isNotBlank(authToken);
    }
}
