package com.coderdream.util.nvidia;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.ChineseCharacterUtil;
import com.coderdream.util.proxy.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class NvidiaTranslateUtil {

    private static final String API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL_NAME = "minimaxai/minimax-m2.1";
    private static final String MODEL_NAME_FALLBACK = "minimax/minimax-m2.1";
    private static final double TEMPERATURE = 0.3;
    private static final double TOP_P = 1.0;
    private static final int MAX_TOKENS = 4096;
    private static final String SYSTEM_PROMPT = "Do not include <think> tags in the response.";
    private static volatile String activeModel = MODEL_NAME;
    private static final int CHUNK_SIZE = 20;
    private static final int MAX_API_RETRIES = 5;
    private static final long RETRY_INTERVAL_MS = 5000;
    private static final int TIMEOUT_MS = 60000;
    private static final int API_CALL_RETRIES = 3;
    private static final long API_RETRY_INTERVAL_MS = 5000;

    public static List<String> translateLines(List<String> originalLines) {
        if (CollectionUtil.isEmpty(originalLines)) {
            log.error("待翻译内容为空，无法翻译。");
            return new ArrayList<>();
        }

        List<List<String>> chunks = ListUtil.split(originalLines, CHUNK_SIZE);
        List<String> allTranslatedLines = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            List<String> chunk = chunks.get(i);
            boolean chunkSuccess = false;

            for (int retry = 0; retry < MAX_API_RETRIES; retry++) {
                List<String> numberedChunk = new ArrayList<>();
                for (int j = 0; j < chunk.size(); j++) {
                    numberedChunk.add((j + 1) + ". " + chunk.get(j));
                }
                String textToTranslate = String.join("\n", numberedChunk);
                String prompt = buildPrompt(textToTranslate);

                log.info("正在调用 NVIDIA API 翻译字幕... (块 {}/{}, 尝试 {}/{})",
                        i + 1, chunks.size(), retry + 1, MAX_API_RETRIES);
                String translatedText = callNvidiaApi(prompt);

                if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常")) {
                    log.error("NVIDIA API 翻译块失败，返回内容为空或包含错误信息。块索引: {}, 尝试: {}", i, retry + 1);
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
                    }
                    continue;
                }

                List<String> translatedLinesWithNumbers = normalizeTranslatedLines(translatedText, chunk.size());
                if (translatedLinesWithNumbers.size() != chunk.size()) {
                    log.error("翻译块行数不匹配! 块索引: {}, 尝试: {}, 原文: {}行, 译文: {}行.",
                            i, retry + 1, chunk.size(), translatedLinesWithNumbers.size());
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
                    }
                    continue;
                }

                List<String> translatedLines = new ArrayList<>();
                for (String line : translatedLinesWithNumbers) {
                    translatedLines.add(line.replaceAll("^\\s*\\d+[\\.)\\-:]?\\s*", ""));
                }

                List<String> untranslatedLinesInChunk = new ArrayList<>();
                for (String translatedLine : translatedLines) {
                    if (isUrl(translatedLine)) {
                        continue;
                    }
                    if (containsEnglishLetters(translatedLine)
                            && !ChineseCharacterUtil.containsChinese(translatedLine)
                            && !StrUtil.isNumeric(translatedLine.replaceAll("[\\s:.,-]", ""))) {
                        untranslatedLinesInChunk.add(translatedLine);
                    }
                }

                if (untranslatedLinesInChunk.isEmpty()) {
                    log.info("块 {} 翻译和内容校验成功。", i + 1);
                    allTranslatedLines.addAll(translatedLines);
                    chunkSuccess = true;
                    break;
                } else {
                    log.error("翻译块内容校验失败! 块索引: {}, 尝试: {}, 未翻译行: {}",
                            i, retry + 1, untranslatedLinesInChunk);
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
                    }
                }
            }

            if (!chunkSuccess) {
                log.error("块 {} 翻译失败，已达到最大重试次数。", i + 1);
                return new ArrayList<>();
            }
        }

        if (allTranslatedLines.size() != originalLines.size()) {
            log.error("最终合并行数不匹配! 原文总行数: {}, 翻译总行数: {}.",
                    originalLines.size(), allTranslatedLines.size());
            return new ArrayList<>();
        }

        return allTranslatedLines;
    }

    public static String translateText(String text) {
        if (StrUtil.isBlank(text)) {
            log.error("待翻译文本为空。");
            return "";
        }
        List<String> lines = Arrays.asList(text.split("\\R", -1));
        List<String> translatedLines = translateLines(lines);
        if (CollectionUtil.isEmpty(translatedLines)) {
            return "";
        }
        return String.join("\n", translatedLines);
    }

    public static String requestCompletion(String prompt) {
        if (StrUtil.isBlank(prompt)) {
            log.error("请求内容为空，无法调用接口。");
            return "";
        }
        return callNvidiaApi(prompt);
    }

    private static String buildPrompt(String textToTranslate) {
        return "You are a professional translator for subtitles. Your task is to translate the following numbered English lines into Simplified Chinese.\n"
                + "It is CRITICAL that you maintain the exact same number of lines in your output as in the input. Each numbered line in the input must correspond to exactly one numbered line in the output. Do not merge lines.\n\n"
                + "Here is an example:\n"
                + "Input:\n"
                + "1. Hello.\n"
                + "2. And I'm Neil.\n"
                + "3. How was your weekend?\n\n"
                + "Output:\n"
                + "1. 你好。\n"
                + "2. 我是尼尔。\n"
                + "3. 你周末过得怎么样？\n\n"
                + "Now, please translate the following lines:\n"
                + textToTranslate;
    }

    private static String callNvidiaApi(String prompt) {
        String apiKey = resolveApiKey();
        if (StrUtil.isBlank(apiKey)) {
            log.error("NVIDIA API Key 为空，无法调用接口。");
            return "API 调用发生异常: API Key 为空";
        }

        String modelName = activeModel;
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelName);
        requestBody.set("stream", false);
        requestBody.set("temperature", TEMPERATURE);
        requestBody.set("top_p", TOP_P);
        requestBody.set("max_tokens", MAX_TOKENS);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", SYSTEM_PROMPT));
        messages.add(new JSONObject().set("role", "user").set("content", prompt));
        requestBody.set("messages", messages);

        Integer proxyPort = OperatingSystem.getProxyPort();
        for (int attempt = 1; attempt <= API_CALL_RETRIES; attempt++) {
            HttpRequest request = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT_MS)
                    .body(requestBody.toString());

            if (StrUtil.isNotBlank(CdConstants.PROXY_HOST) && proxyPort != null) {
                request = request.setHttpProxy(CdConstants.PROXY_HOST, proxyPort);
            }

            try (HttpResponse response = request.execute()) {
                String responseBody = response.body();
                if (!response.isOk()) {
                    if (isModelNotFound(response.getStatus(), responseBody)
                            && !MODEL_NAME_FALLBACK.equals(modelName)) {
                        log.warn("NVIDIA model {} not available. Falling back to {}.", modelName, MODEL_NAME_FALLBACK);
                        activeModel = MODEL_NAME_FALLBACK;
                        return callNvidiaApi(prompt);
                    }
                    log.error("NVIDIA API 调用失败，状态码: {}, 返回: {}", response.getStatus(), responseBody);
                    if (shouldRetryStatus(response.getStatus()) && attempt < API_CALL_RETRIES) {
                        log.info("{}秒后重试...", API_RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(API_RETRY_INTERVAL_MS);
                        continue;
                    }
                    return "API 调用发生异常: 状态码 " + response.getStatus();
                }
                if (StrUtil.isBlank(responseBody)) {
                    log.error("NVIDIA API 返回为空。");
                    return "API 调用发生异常: 响应为空";
                }

                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                if (jsonResponse.containsKey("error")) {
                    JSONObject errorObj = jsonResponse.getJSONObject("error");
                    String message = errorObj == null ? "未知错误" : errorObj.getStr("message");
                    log.error("NVIDIA API 返回错误: {}", message);
                    if (attempt < API_CALL_RETRIES) {
                        log.info("{}秒后重试...", API_RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(API_RETRY_INTERVAL_MS);
                        continue;
                    }
                    return "API 调用发生异常: " + message;
                }

                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    log.error("NVIDIA API 返回 choices 为空: {}", responseBody);
                    return "API 调用发生异常: choices 为空";
                }

                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject messageObj = firstChoice == null ? null : firstChoice.getJSONObject("message");
                String content = messageObj == null ? null : messageObj.getStr("content");
                if (StrUtil.isBlank(content)) {
                    log.error("NVIDIA API 返回 content 为空: {}", responseBody);
                    return "API 调用发生异常: content 为空";
                }

                return stripThinkContent(content);
            } catch (Exception e) {
                log.error("NVIDIA API 调用异常: {}", e.getMessage(), e);
                if (attempt < API_CALL_RETRIES) {
                    log.info("{}秒后重试...", API_RETRY_INTERVAL_MS / 1000);
                    ThreadUtil.sleep(API_RETRY_INTERVAL_MS);
                    continue;
                }
                return "API 调用发生异常: " + e.getMessage();
            }
        }

        return "API 调用发生异常: 重试后仍失败";
    }

    private static String resolveApiKey() {
        String envKey = System.getenv("NVIDIA_API_KEY");
        if (StrUtil.isNotBlank(envKey)) {
            return envKey;
        }
        String propertyKey = System.getProperty("NVIDIA_API_KEY");
        if (StrUtil.isNotBlank(propertyKey)) {
            return propertyKey;
        }
        return CdConstants.NVIDIA_API_KEY;
    }

    private static boolean shouldRetryStatus(int status) {
        return status == 429 || (status >= 500 && status < 600);
    }

    private static boolean isModelNotFound(int status, String message) {
        if (status == 404) {
            return true;
        }
        if (StrUtil.isBlank(message)) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("model") && lower.contains("not found");
    }

    private static List<String> normalizeTranslatedLines(String translatedText, int expectedSize) {
        List<String> rawLines = Arrays.asList(translatedText.split("\\R"));
        if (rawLines.size() == expectedSize) {
            return rawLines;
        }
        List<String> nonBlankLines = new ArrayList<>();
        for (String line : rawLines) {
            if (StrUtil.isNotBlank(line)) {
                nonBlankLines.add(line);
            }
        }
        if (nonBlankLines.size() == expectedSize) {
            return nonBlankLines;
        }
        return rawLines;
    }

    private static String stripThinkContent(String content) {
        if (content == null) {
            return null;
        }
        if (!content.toLowerCase().contains("<think>")) {
            return content;
        }
        String cleaned = content.replaceAll("(?is)<think>.*?</think>", "");
        cleaned = cleaned.replaceAll("(?i)</?think>", "");
        return cleaned.trim();
    }

    private static boolean containsEnglishLetters(String text) {
        if (text == null) {
            return false;
        }
        return text.matches(".*[a-zA-Z].*");
    }

    private static boolean isUrl(String text) {
        if (text == null) {
            return false;
        }
        String trimmedText = text.trim().toLowerCase();
        return trimmedText.startsWith("www.")
                || trimmedText.startsWith("http://")
                || trimmedText.startsWith("https://");
    }
}
