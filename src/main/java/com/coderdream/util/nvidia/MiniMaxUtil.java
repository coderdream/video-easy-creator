package com.coderdream.util.nvidia;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdDateUtil;
import com.coderdream.util.proxy.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MiniMax M2.1 API 工具类
 * 通过 NVIDIA NIM 平台调用 MiniMax M2.1 模型
 * 使用 OpenAI 兼容模式
 *
 * @author CoderDream
 */
@Slf4j
public class MiniMaxUtil {

    /**
     * NVIDIA NIM API 基础地址
     */
    private static final String BASE_API_URL = "https://integrate.api.nvidia.com/v1";

    /**
     * Chat Completions 端点
     */
    private static final String CHAT_COMPLETIONS_URL = BASE_API_URL + "/chat/completions";

    /**
     * API Key
     */
    private static final String API_KEY = CdConstants.NVIDIA_API_KEY;

    /**
     * 默认模型 ID
     */
    private static final String DEFAULT_MODEL = "minimaxai/minimax-m2.1";

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 2000;

    /**
     * 默认超时时间（毫秒）- 120秒，因为 MiniMax 可能需要较长时间处理
     */
    private static final int DEFAULT_TIMEOUT_MS = 120000;

    /**
     * 用于匹配 <think>...</think> 标签的正则表达式
     */
    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);

    /**
     * 调用 MiniMax API 生成内容（使用默认模型）
     *
     * @param prompt 用户输入的提示文本
     * @return API 返回的结果文本，或者错误信息
     */
    public static String generateContent(String prompt) {
        return generateContent(prompt, DEFAULT_MODEL);
    }

    /**
     * 调用 MiniMax API 生成内容（可指定模型）
     *
     * @param prompt    用户输入的提示文本
     * @param modelName 要使用的模型名称
     * @return API 返回的结果文本，或者错误信息
     */
    public static String generateContent(String prompt, String modelName) {
        return generateContent(prompt, modelName, null);
    }

    /**
     * 调用 MiniMax API 生成内容（可指定模型和系统提示）
     *
     * @param prompt       用户输入的提示文本
     * @param modelName    要使用的模型名称
     * @param systemPrompt 系统提示（可选）
     * @return API 返回的结果文本，或者错误信息
     */
    public static String generateContent(String prompt, String modelName, String systemPrompt) {
        Instant startTime = Instant.now();
        String lastErrorMessage = "";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    log.info("正在进行第 {} 次尝试 (模型: {})...", attempt, modelName);
                } else {
                    log.info("MiniMax API 请求 URL: {}", CHAT_COMPLETIONS_URL);
                    log.info("MiniMax API Key (部分隐藏): {}", maskApiKey(API_KEY));
                    log.info("使用模型: {}", modelName);
                }

                // 构建请求体
                JSONObject requestBody = buildRequestBody(prompt, modelName, systemPrompt);
                log.debug("MiniMax API 请求体: {}", requestBody);

                // 发送请求
                String result = sendRequest(requestBody);

                if (StrUtil.isBlank(result)) {
                    lastErrorMessage = "API 响应为空";
                    log.warn("模型 {} 的 API 响应为空", modelName);
                    Thread.sleep(RETRY_DELAY_MS);
                    continue;
                }

                log.debug("MiniMax API 原始响应: {}", result);

                // 检查是否包含错误信息
                if (result.contains("\"error\"")) {
                    JSONObject resultObject = JSONUtil.parseObj(result);
                    if (resultObject.containsKey("error")) {
                        JSONObject errorObj = resultObject.getJSONObject("error");
                        String message = errorObj.getStr("message");
                        Integer code = errorObj.getInt("code");

                        lastErrorMessage = "API 错误: " + message;
                        log.warn("MiniMax API 返回错误 (代码: {}, 信息: {})", code, message);

                        // 503 Overloaded: 稍后重试
                        if (code != null && code == 503) {
                            log.warn("检测到模型过载 (503)，准备重试...");
                            Thread.sleep(RETRY_DELAY_MS);
                            continue;
                        }

                        // 429 Rate Limit: 稍后重试
                        if (code != null && code == 429) {
                            log.warn("检测到速率限制 (429)，准备重试...");
                            Thread.sleep(RETRY_DELAY_MS * 2); // 等待更长时间
                            continue;
                        }

                        // 其他错误
                        return "API 调用发生异常: " + message;
                    }
                }

                // 解析响应
                String extractedText = parseResponse(result);
                if (StrUtil.isNotBlank(extractedText)) {
                    // 移除 <think> 标签
                    extractedText = removeThinkTags(extractedText);

                    Instant endTime = Instant.now();
                    long duration = Duration.between(startTime, endTime).toMillis();
                    log.info("MiniMax API (模型: {}) 调用成功，耗时: {}", modelName, CdDateUtil.formatDurationHMSS(duration));

                    return extractedText;
                } else {
                    lastErrorMessage = "未返回有效内容";
                    log.error("模型 {} 未返回有效的内容", modelName);
                    Thread.sleep(RETRY_DELAY_MS);
                }

            } catch (Exception e) {
                lastErrorMessage = e.getMessage();
                log.error("MiniMax API (模型: {}) 调用发生异常: {}", modelName, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "API 调用被中断";
                }
            }
        }

        // 所有尝试都失败
        Instant endTime = Instant.now();
        long duration = Duration.between(startTime, endTime).toMillis();
        String finalErrorMessage = String.format(
                "MiniMax API 调用失败，已尝试 %d 次，耗时: %s, 最后错误: %s",
                MAX_RETRIES, CdDateUtil.formatDurationHMSS(duration), lastErrorMessage);
        log.error(finalErrorMessage);
        return "API 调用发生异常: " + lastErrorMessage;
    }

    /**
     * 调用 MiniMax API 进行翻译（专用于字幕翻译）
     *
     * @param text 要翻译的文本
     * @return 翻译后的文本
     */
    public static String translateToChineseForSubtitle(String text) {
        String systemPrompt = "你是一个专业的影视字幕翻译专家。我将提供一段英文字幕，请将其翻译成中文。" +
                "不要输出任何解释性文字，直接输出翻译后的内容。" +
                "请确保翻译符合中文表达习惯，结合上下文语境。";
        return generateContent(text, DEFAULT_MODEL, systemPrompt);
    }

    /**
     * 调用 MiniMax API 进行翻译（通用翻译）
     *
     * @param text 要翻译的文本
     * @return 翻译后的文本
     */
    public static String translateToChinese(String text) {
        String prompt = "请将以下英文翻译成中文，只返回翻译结果，不要任何解释：\n" + text;
        return generateContent(prompt);
    }

    /**
     * 调用 MiniMax API 进行词汇翻译
     *
     * @param vocText 词汇文本（按照 CdConstants.VOC_CN_PREFIX 格式）
     * @return 翻译后的词汇文本
     */
    public static String translateVocabulary(String vocText) {
        String prompt = CdConstants.VOC_CN_PREFIX + vocText;
        return generateContent(prompt);
    }

    /**
     * 调用 MiniMax API 进行脚本翻译
     *
     * @param scriptText 脚本文本
     * @return 翻译后的脚本文本
     */
    public static String translateScript(String scriptText) {
        String prompt = CdConstants.SRC_TRANSLATE_PREFIX + scriptText;
        return generateContent(prompt);
    }

    /**
     * 构建请求体
     *
     * @param prompt       用户提示
     * @param modelName    模型名称
     * @param systemPrompt 系统提示（可选）
     * @return 请求体 JSON 对象
     */
    private static JSONObject buildRequestBody(String prompt, String modelName, String systemPrompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelName);

        JSONArray messages = new JSONArray();

        // 添加系统提示（如果有）
        if (StrUtil.isNotBlank(systemPrompt)) {
            JSONObject systemMessage = new JSONObject();
            systemMessage.set("role", "system");
            systemMessage.set("content", systemPrompt);
            messages.add(systemMessage);
        }

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);

        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.3);  // 较低的温度以获得更稳定的翻译结果
        requestBody.set("top_p", 0.7);
        requestBody.set("max_tokens", 4096);
        requestBody.set("stream", false);  // 使用非流式响应，便于处理

        return requestBody;
    }

    /**
     * 发送 HTTP 请求
     *
     * @param requestBody 请求体
     * @return 响应字符串
     */
    private static String sendRequest(JSONObject requestBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + API_KEY);

        try (HttpResponse response = HttpRequest.post(CHAT_COMPLETIONS_URL)
                .addHeaders(headers)
                .body(requestBody.toString())
                .setHttpProxy(CdConstants.PROXY_HOST, OperatingSystem.getProxyPort())
                .timeout(DEFAULT_TIMEOUT_MS)
                .execute()) {

            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST 请求失败，URL: {}，错误信息: {}", CHAT_COMPLETIONS_URL, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析 API 响应
     *
     * @param response 原始响应字符串
     * @return 提取的文本内容
     */
    private static String parseResponse(String response) {
        try {
            JSONObject resultObject = JSONUtil.parseObj(response);

            // 检查是否有 choices 数组
            JSONArray choices = resultObject.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("响应中未找到 'choices' 字段或为空");
                return null;
            }

            // 获取第一个 choice
            JSONObject firstChoice = choices.getJSONObject(0);
            if (firstChoice == null) {
                log.error("无法获取第一个 choice");
                return null;
            }

            // 获取 message
            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null) {
                // 尝试获取 delta（流式响应格式）
                message = firstChoice.getJSONObject("delta");
            }

            if (message == null) {
                log.error("无法获取 message 或 delta");
                return null;
            }

            // 获取 content
            String content = message.getStr("content");
            if (StrUtil.isBlank(content)) {
                log.error("content 为空");
                return null;
            }

            return content;
        } catch (Exception e) {
            log.error("解析响应失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 移除 <think>...</think> 标签及其内容
     *
     * @param text 原始文本
     * @return 移除标签后的文本
     */
    private static String removeThinkTags(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        Matcher matcher = THINK_PATTERN.matcher(text);
        String result = matcher.replaceAll("");
        // 清理多余的空行
        result = result.replaceAll("\\n{3,}", "\n\n").trim();
        return result;
    }

    /**
     * 隐藏 API 密钥的中间部分
     *
     * @param apiKey 原始 API 密钥
     * @return 部分隐藏的 API 密钥
     */
    private static String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey) || apiKey.length() < 12) {
            return "API Key is too short to be masked or is blank.";
        }
        return apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 测试 API 连接
     *
     * @return 是否连接成功
     */
    public static boolean testConnection() {
        String result = generateContent("Hello, please respond with 'OK' if you can read this.");
        return StrUtil.isNotBlank(result) && !result.contains("API 调用发生异常");
    }

    public static void main(String[] args) {
        // 测试基本功能
        String prompt = "翻译成简体中文：Why are countryside walks no longer so popular?";
        String result = generateContent(prompt);
        System.out.println("翻译结果: " + result);
    }
}
