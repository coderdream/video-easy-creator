package com.coderdream.util.codex;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.proxy.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodexApiClient {

  private static final String API_BASE_URL = CdConstants.CODEX_API_BASE_URL;
  private static final String API_KEY = resolveApiKey();
  private static final String DEFAULT_MODEL = CdConstants.CODEX_MODEL;
  private static final int TIMEOUT_MS = 60000;
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_INTERVAL_MS = 5000;

  public static String generateContent(String prompt) {
    return generateContent(prompt, DEFAULT_MODEL);
  }

  public static String generateContent(String prompt, String modelName) {
    if (StrUtil.isBlank(prompt)) {
      log.warn("Prompt is blank.");
      return "";
    }
    if (StrUtil.isBlank(API_BASE_URL)) {
      log.error("Codex API base URL is blank.");
      return "API call failed: base URL is blank";
    }
    if (StrUtil.isBlank(API_KEY)) {
      log.error("Codex API key is blank.");
      return "API call failed: API key is blank";
    }

    JSONObject responsesBody = buildResponsesRequestBody(prompt, modelName);
    for (String apiUrl : buildResponsesCandidateUrls(API_BASE_URL)) {
      String result = executeRequestWithRetry(apiUrl, responsesBody);
      if (isApiFailure(result)) {
        continue;
      }
      return result;
    }

    JSONObject requestBody = buildRequestBody(prompt, modelName);
    for (String apiUrl : buildChatCandidateUrls(API_BASE_URL)) {
      String result = executeRequestWithRetry(apiUrl, requestBody);
      if (isApiFailure(result)) {
        continue;
      }
      return result;
    }
    JSONObject completionsBody = buildCompletionsRequestBody(prompt, modelName);
    for (String apiUrl : buildCompletionsCandidateUrls(API_BASE_URL)) {
      String result = executeRequestWithRetry(apiUrl, completionsBody);
      if (isApiFailure(result)) {
        continue;
      }
      return result;
    }
    return "API call failed: no valid endpoint";
  }

  private static JSONObject buildRequestBody(String prompt, String modelName) {
    JSONObject requestBody = new JSONObject();
    requestBody.set("model", modelName);
    requestBody.set("stream", false);
    requestBody.set("temperature", 0);

    JSONArray messages = new JSONArray();
    messages.add(new JSONObject().set("role", "user").set("content", prompt));
    requestBody.set("messages", messages);
    return requestBody;
  }

  private static JSONObject buildResponsesRequestBody(String prompt,
    String modelName) {
    JSONObject requestBody = new JSONObject();
    requestBody.set("model", modelName);
    requestBody.set("input", prompt);
    requestBody.set("temperature", 0);
    return requestBody;
  }

  private static String resolveApiKey() {
    String envKey = System.getenv("CRS_OAI_KEY");
    return StrUtil.isBlank(envKey) ? CdConstants.CODEX_API_KEY : envKey;
  }

  private static JSONObject buildCompletionsRequestBody(String prompt,
    String modelName) {
    JSONObject requestBody = new JSONObject();
    requestBody.set("model", modelName);
    requestBody.set("prompt", prompt);
    requestBody.set("temperature", 0);
    return requestBody;
  }

  private static String executeRequestWithRetry(String apiUrl,
    JSONObject requestBody) {
    Integer proxyPort = OperatingSystem.getProxyPort();

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      HttpRequest request = HttpRequest.post(apiUrl)
        .header("Authorization", "Bearer " + API_KEY)
        .header("Content-Type", "application/json")
        .timeout(TIMEOUT_MS)
        .body(requestBody.toString());

      if (StrUtil.isNotBlank(CdConstants.PROXY_HOST) && proxyPort != null) {
        request = request.setHttpProxy(CdConstants.PROXY_HOST, proxyPort);
      }

      try (HttpResponse response = request.execute()) {
        String responseBody = response.body();
        if (!response.isOk()) {
          log.error("Codex API request failed: status={}, body={}",
            response.getStatus(), responseBody);
          if (response.getStatus() == 429 && attempt < MAX_RETRIES) {
            ThreadUtil.sleep(RETRY_INTERVAL_MS);
            continue;
          }
          return "API call failed: status " + response.getStatus();
        }

        if (StrUtil.isBlank(responseBody)) {
          log.error("Codex API response is empty.");
          return "API call failed: empty response";
        }

        String content = extractContent(responseBody);
        if (StrUtil.isBlank(content)) {
          return "API call failed: empty content";
        }
        return stripThinkContent(content);
      } catch (Exception e) {
        log.error("Codex API request error: {}", e.getMessage(), e);
        if (attempt < MAX_RETRIES) {
          ThreadUtil.sleep(RETRY_INTERVAL_MS);
          continue;
        }
        return "API call failed: " + e.getMessage();
      }
    }

    return "API call failed: retries exhausted";
  }

  private static String[] buildChatCandidateUrls(String baseUrl) {
    String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0,
      baseUrl.length() - 1) : baseUrl;
    java.util.LinkedHashSet<String> urls = new java.util.LinkedHashSet<>();
    if (normalized.endsWith("/v1")) {
      urls.add(normalized + "/chat/completions");
      urls.add(normalized);
    } else {
      urls.add(normalized + "/v1/chat/completions");
      urls.add(normalized + "/chat/completions");
      urls.add(normalized);
    }
    if (normalized.endsWith("/openai")) {
      String root = normalized.substring(0,
        normalized.length() - "/openai".length());
      urls.add(root + "/v1/chat/completions");
      urls.add(root + "/chat/completions");
      urls.add(root);
    }
    return urls.toArray(new String[0]);
  }

  private static String[] buildResponsesCandidateUrls(String baseUrl) {
    String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0,
      baseUrl.length() - 1) : baseUrl;
    java.util.LinkedHashSet<String> urls = new java.util.LinkedHashSet<>();
    if (normalized.endsWith("/v1")) {
      urls.add(normalized + "/responses");
      urls.add(normalized);
    } else {
      urls.add(normalized + "/v1/responses");
      urls.add(normalized + "/responses");
      urls.add(normalized);
    }
    if (normalized.endsWith("/openai")) {
      String root = normalized.substring(0,
        normalized.length() - "/openai".length());
      urls.add(root + "/v1/responses");
      urls.add(root + "/responses");
      urls.add(root);
    }
    return urls.toArray(new String[0]);
  }

  private static String[] buildCompletionsCandidateUrls(String baseUrl) {
    String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0,
      baseUrl.length() - 1) : baseUrl;
    java.util.LinkedHashSet<String> urls = new java.util.LinkedHashSet<>();
    if (normalized.endsWith("/v1")) {
      urls.add(normalized + "/completions");
      urls.add(normalized);
    } else {
      urls.add(normalized + "/v1/completions");
      urls.add(normalized + "/completions");
      urls.add(normalized);
    }
    if (normalized.endsWith("/openai")) {
      String root = normalized.substring(0,
        normalized.length() - "/openai".length());
      urls.add(root + "/v1/completions");
      urls.add(root + "/completions");
      urls.add(root);
    }
    return urls.toArray(new String[0]);
  }

  private static boolean isApiFailure(String result) {
    return result == null || result.startsWith("API call failed:");
  }

  private static String extractContent(String responseBody) {
    JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
    if (jsonResponse.containsKey("error")) {
      JSONObject errorObj = jsonResponse.getJSONObject("error");
      String message = errorObj == null ? "unknown error" : errorObj.getStr(
        "message");
      log.error("Codex API error: {}", message);
      return "";
    }

    JSONArray choices = jsonResponse.getJSONArray("choices");
    if (choices == null || choices.isEmpty()) {
      String outputText = jsonResponse.getStr("output_text");
      if (StrUtil.isNotBlank(outputText)) {
        return outputText;
      }
      JSONArray output = jsonResponse.getJSONArray("output");
      if (output != null && !output.isEmpty()) {
        JSONObject firstOutput = output.getJSONObject(0);
        JSONArray content = firstOutput == null ? null : firstOutput.getJSONArray(
          "content");
        if (content != null && !content.isEmpty()) {
          JSONObject firstContent = content.getJSONObject(0);
          String text = firstContent == null ? null : firstContent.getStr(
            "text");
          if (StrUtil.isNotBlank(text)) {
            return text;
          }
        }
      }
      log.error("Codex API content missing.");
      return "";
    }

    JSONObject firstChoice = choices.getJSONObject(0);
    JSONObject messageObj = firstChoice == null ? null : firstChoice.getJSONObject(
      "message");
    if (messageObj != null) {
      return messageObj.getStr("content");
    }
    String text = firstChoice == null ? null : firstChoice.getStr("text");
    return text == null ? "" : text;
  }

  private static String stripThinkContent(String content) {
    if (content == null) {
      return null;
    }
    if (!content.contains("<think>")) {
      return content;
    }
    String cleaned = content.replaceAll("(?s)<think>.*?</think>", "");
    return cleaned.trim();
  }
}
