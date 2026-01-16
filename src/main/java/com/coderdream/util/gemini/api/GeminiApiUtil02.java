package com.coderdream.util.gemini.api;

import com.coderdream.util.gemini.api.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Gemini API 工具类
 * 提供查询可用模型和计算Token数量的功能。
 */
public final class GeminiApiUtil02 {

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String API_KEY = "AIzaSyDgIpLul9IQcjCZA2cSZhiKMV5k0UN7evE";// CdConstants.GEMINI_API_KEY;

    // 创建可重用的客户端和JSON映射器以提高效率
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(20))
        .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 工具类不应被实例化
     */
    private GeminiApiUtil02() {
    }

    /**
     * 获取所有可用的 Gemini 模型列表。
     *
     * @return 模型列表
     * @throws IOException          当网络请求失败时抛出
     * @throws InterruptedException 当线程被中断时抛出
     */
    public static List<Model> listAvailableModels() throws IOException, InterruptedException {
        String url = String.format("%s/models?key=%s", API_BASE_URL, API_KEY);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-Type", "application/json")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to list models. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        ModelListResponse modelListResponse = objectMapper.readValue(response.body(), ModelListResponse.class);
        return modelListResponse.getModels();
    }

    /**
     * 计算给定文本在特定模型下会消耗的Token数量。
     *
     * @param modelName 模型的名称, 例如 "models/gemini-pro"
     * @param text      需要计算Token的文本
     * @return Token总数
     * @throws IOException          当网络请求失败时抛出
     * @throws InterruptedException 当线程被中断时抛出
     */
    public static int countTokens(String modelName, String text) throws IOException, InterruptedException {
        String url = String.format("%s/%s:countTokens?key=%s", API_BASE_URL, modelName, API_KEY);

        // 构建请求体
        Part part = new Part(text);
        Content content = new Content(List.of(part));
        CountTokensRequest countTokensRequest = new CountTokensRequest(List.of(content));
        String requestBody = objectMapper.writeValueAsString(countTokensRequest);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to count tokens. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        CountTokensResponse countTokensResponse = objectMapper.readValue(response.body(), CountTokensResponse.class);
        return countTokensResponse.getTotalTokens();
    }
}
