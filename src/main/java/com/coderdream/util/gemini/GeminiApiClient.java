package com.coderdream.util.gemini;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdDateUtil;
import com.coderdream.util.callapi.HttpUtil;
import com.coderdream.util.proxy.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gemini API 通用工具类，提供静态方法调用 Gemini API。
 */
@Slf4j
public class GeminiApiClient {

    private static final String BASE_API_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String API_KEY = CdConstants.GEMINI_API_KEY;

    // 更新默认 URL 为 gemini-2.5-flash，保持一致性
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public static String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + CdConstants.GEMINI_API_KEY;

    // 最大重试次数 (针对网络错误或503)
    private static final int MAX_RETRIES = 3;
    // 重试间隔（毫秒）
    private static final long RETRY_DELAY_MS = 2000;

    // 备用模型列表，按优先级排序
    // 根据测试结果，优先使用已验证可用的模型
    private static final List<String> FALLBACK_MODELS = Arrays.asList(
            "gemini-2.5-flash",             // 首选，性能最好
            "gemini-2.5-flash-lite",        // 备选1，已验证可用
            "gemini-flash-lite-latest",     // 备选2，已验证可用
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-1.0-pro"
    );

    /**
     * 【新功能】获取所有可用的 Gemini 模型列表
     *
     * @return 模型名称列表，如果失败则返回空列表
     */
    public static List<String> listModels() {
        String url = BASE_API_URL + "/models?key=" + API_KEY;
        log.info("开始获取 Gemini 模型列表，请求 URL: {}", url.split("\\?")[0]);

        try {
            // 【关键修复】调用支持代理的GET方法
            String result = HttpUtil.httpHutoolGet(url, null, CdConstants.PROXY_HOST, OperatingSystem.getProxyPort());
            if (StrUtil.isBlank(result)) {
                log.error("获取模型列表失败，API 返回为空。");
                return new ArrayList<>();
            }

            log.info("获取模型列表成功，原始响应: {}", result);
            JSONObject resultObject = JSONUtil.parseObj(result);
            JSONArray modelsArray = resultObject.getJSONArray("models");

            if (modelsArray == null) {
                log.error("响应中未找到 'models' 字段。");
                return new ArrayList<>();
            }

            // 提取并返回所有模型的名称
            return modelsArray.stream()
                    .map(obj -> ((JSONObject) obj).getStr("name"))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取 Gemini 模型列表时发生异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 【新功能】测试所有支持内容生成的模型
     *
     * @param prompt 用于测试的提示语
     */
    public static void testAllModels(String prompt) {
        log.info("========== 开始测试所有支持的模型 ==========");
        List<String> modelNames = listModels();
        List<String> availableModels = new ArrayList<>(); // 用于记录可用的模型

        if (modelNames.isEmpty()) {
            log.error("未能获取到任何模型，测试中止。");
            return;
        }

        for (String modelName : modelNames) {
            // 从 "models/gemini-1.5-flash-latest" 中提取 "gemini-1.5-flash-latest"
            String simpleModelName = modelName.replace("models/", "");

            // 通常只有包含 "generateContent" 支持的模型才能用于内容生成
            // 这里我们简单筛选一下，避免调用不支持的模型
            if (simpleModelName.contains("gemini")) {
                log.info("--- 正在测试模型: {} ---", simpleModelName);
                // 调用重载后的方法进行测试
                String result = generateContent(prompt, simpleModelName);
                if (StrUtil.isNotBlank(result) && !result.contains("API 调用发生异常")) {
                    log.info("模型 {} 测试成功，返回: {}", simpleModelName, result);
                    availableModels.add(simpleModelName); // 记录可用模型
                } else {
                    log.error("模型 {} 测试失败或返回空。", simpleModelName);
                }
                log.info("--- 模型 {} 测试结束 ---\n", simpleModelName);
            } else {
                log.debug("跳过不支持内容生成的模型: {}", simpleModelName);
            }
        }
        log.info("========== 所有模型测试完毕 ==========");

        // 汇总输出可用模型
        log.info("********** 可用模型汇总 **********");
        if (availableModels.isEmpty()) {
            log.warn("没有发现任何可用的模型。");
        } else {
            for (String model : availableModels) {
                log.info("可用模型: {}", model);
            }
        }
        log.info("**********************************");
    }

    /**
     * 【恢复】调用 Gemini API 生成内容 (使用默认模型 "gemini-2.5-flash")
     * 此方法保持原始签名，以确保向后兼容。
     *
     * @param prompt 用户输入的提示文本
     * @return Gemini API 返回的结果文本，或者错误信息
     */
    public static String generateContent(String prompt) {
        // 内部调用重载方法，并指定默认模型
        return generateContent(prompt, "gemini-2.5-flash");
    }

    /**
     * 【重载】调用 Gemini API 生成内容（可指定模型）
     * 增加了重试机制，以应对 503 Overloaded 等临时错误。
     *
     * @param prompt    用户输入的提示文本
     * @param modelName 要使用的模型名称 (例如 "gemini-2.5-flash")
     * @return Gemini API 返回的结果文本，或者错误信息
     */
    public static String generateContent(String prompt, String modelName) {
        Instant startTime = Instant.now();
        JSONObject requestBody = buildRequestBody(prompt);

        String currentModelName = modelName;
        // 确定当前模型在备用列表中的索引，以便后续降级
        int currentModelIndex = -1;
        for (int i = 0; i < FALLBACK_MODELS.size(); i++) {
            if (FALLBACK_MODELS.get(i).equals(modelName)) {
                currentModelIndex = i;
                break;
            }
        }
        // 如果指定的模型不在备用列表中，则将其视为自定义模型，不参与自动降级（除非手动添加到列表）
        // 或者我们可以简单地从列表头开始降级，但这可能不符合用户预期。
        // 这里我们采取的策略是：如果指定模型失败且是429/404，尝试列表中的下一个模型。

        String lastErrorMessage = "";

        // 总尝试次数 = 初始尝试 + 重试次数 + 降级尝试
        // 这里我们使用一个循环来控制流程，包括重试和降级
        int maxTotalAttempts = MAX_RETRIES + FALLBACK_MODELS.size(); 
        int attemptCount = 0;

        while (attemptCount < maxTotalAttempts) {
            attemptCount++;
            
            // 每次循环都重新构建 URL，因为模型名称可能已经改变
            String apiUrl = String.format("%s/models/%s:generateContent?key=%s", BASE_API_URL, currentModelName, API_KEY);

            try {
                if (attemptCount > 1) {
                    log.info("正在进行第 {} 次尝试 (当前模型: {})...", attemptCount, currentModelName);
                } else {
                    log.info("Gemini API 请求 URL: {}", apiUrl.split("\\?")[0]); // URL不记录Key
                    log.info("Gemini API Key (部分隐藏): {}", maskApiKey(API_KEY));
                    log.debug("Gemini API 请求体: {}", requestBody);
                }

                String result = HttpUtil.httpHutoolPost(apiUrl, requestBody.toString(),
                        CdConstants.PROXY_HOST,
                        OperatingSystem.getProxyPort());

                log.info("Gemini API 原始响应 (模型: {}, 尝试: {}): {}", currentModelName, attemptCount, result);

                if (StrUtil.isBlank(result)) {
                    lastErrorMessage = "API 响应为空";
                    log.warn("模型 {} 的 API 响应为空", currentModelName);
                    Thread.sleep(RETRY_DELAY_MS); // 等待后重试
                    continue;
                }

                // 检查是否包含错误信息
                if (result.contains("\"error\"")) {
                    JSONObject resultObject = JSONUtil.parseObj(result);
                    if (resultObject.containsKey("error")) {
                        JSONObject errorObj = resultObject.getJSONObject("error");
                        String message = errorObj.getStr("message");
                        Integer code = errorObj.getInt("code");
                        
                        lastErrorMessage = "API 错误: " + message;
                        log.warn("Gemini API 返回错误 (代码: {}, 信息: {})", code, message);

                        // 503 Overloaded: 稍后重试当前模型
                        if (code != null && code == 503) {
                             log.warn("检测到模型过载 (503)，准备重试...");
                             Thread.sleep(RETRY_DELAY_MS);
                             continue;
                        }

                        // 429 Resource Exhausted 或 404 Not Found: 尝试降级
                        if (code != null && (code == 429 || code == 404)) {
                            String nextModel = getNextFallbackModel(currentModelName);
                            if (nextModel != null) {
                                log.warn("检测到错误 ({})，正在降级到备用模型: {}", code, nextModel);
                                currentModelName = nextModel;
                                continue; // 立即使用新模型重试
                            } else {
                                log.error("模型 {} 发生错误 ({})，且无更多备用模型可降级。", currentModelName, code);
                                return "API 调用发生异常: " + (code == 429 ? "配额耗尽" : "模型未找到");
                            }
                        }

                        // 其他错误
                        return "API 调用发生异常: " + message;
                    }
                }

                JSONObject resultObject = JSONUtil.parseObj(result);
                GeminiApiResponse response = resultObject.toBean(GeminiApiResponse.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    String extractedText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    log.info("模型 {} 内容提取成功。", currentModelName);
                    
                    Instant endTime = Instant.now();
                    long duration = Duration.between(startTime, endTime).toMillis();
                    log.info("Gemini API (模型: {}) 调用成功，耗时: {}", currentModelName, CdDateUtil.formatDurationHMSS(duration));
                    
                    return extractedText;
                } else {
                    lastErrorMessage = "未返回有效内容";
                    log.error("模型 {} 未返回有效的候选内容。解析后的响应: {}", currentModelName, response);
                    Thread.sleep(RETRY_DELAY_MS);
                }

            } catch (Exception e) {
                lastErrorMessage = e.getMessage();
                log.error("Gemini API (模型: {}) 调用发生异常: {}", currentModelName, e.getMessage());
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
                "Gemini API 调用失败，已尝试 %d 次，耗时: %s, 最后错误: %s",
                attemptCount, CdDateUtil.formatDurationHMSS(duration), lastErrorMessage);
        log.error(finalErrorMessage);
        return "API 调用发生异常: " + lastErrorMessage;
    }

    /**
     * 获取下一个备用模型
     * @param currentModel 当前模型名称
     * @return 下一个模型名称，如果没有更多备用模型则返回 null
     */
    private static String getNextFallbackModel(String currentModel) {
        int index = FALLBACK_MODELS.indexOf(currentModel);
        if (index != -1 && index < FALLBACK_MODELS.size() - 1) {
            return FALLBACK_MODELS.get(index + 1);
        }
        // 如果当前模型不在列表中，或者已经是最后一个，尝试从头开始找一个不是当前模型的（简单起见，这里只处理在列表中的情况）
        // 如果当前模型不在列表中（比如用户指定了一个新模型），我们默认降级到列表的第一个
        if (index == -1 && !FALLBACK_MODELS.isEmpty()) {
             return FALLBACK_MODELS.get(0);
        }
        return null;
    }

    /**
     * 调用 Gemini API 生成内容，并返回原始JSON响应。
     * 此方法保持不变，以兼容 TxtPhoneticFiller 的调用。
     *
     * @param prompt 用户输入的提示文本
     * @return Gemini API 返回的原始JSON字符串
     */
    public static String generateRawContent(String prompt) {
        Instant startTime = Instant.now();
        String modelName = "gemini-2.5-flash"; // 默认模型修改为 gemini-2.5-flash
        String apiUrl = String.format("%s/models/%s:generateContent?key=%s", BASE_API_URL, modelName, API_KEY);
        JSONObject requestBody = buildRequestBody(prompt);
        try {
            String result = HttpUtil.httpHutoolPost(apiUrl, requestBody.toString(),
                    CdConstants.PROXY_HOST,
                    OperatingSystem.getProxyPort());
            Instant endTime = Instant.now();
            long duration = Duration.between(startTime, endTime).toMillis();
            log.info("Gemini API (generateRawContent) 调用成功，耗时: {}",
                    CdDateUtil.formatDurationHMSS(duration));
            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            long duration = Duration.between(startTime, endTime).toMillis();
            String errorMessage = String.format(
                    "Gemini API (generateRawContent) 调用发生异常，耗时: %s, 异常信息: %s",
                    CdDateUtil.formatDurationHMSS(duration), e.getMessage());
            log.error(errorMessage, e);
            return "{\"error\": \"" + e.getMessage() + "\"}"; // 返回一个包含错误的JSON字符串
        }
    }

    /**
     * 构造 Gemini API 的请求体
     *
     * @param prompt 用户提示文本
     * @return 请求体 JSON 对象
     */
    private static JSONObject buildRequestBody(String prompt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("contents", new JSONObject[]{new JSONObject().set("parts",
                new JSONObject[]{new JSONObject().set("text", prompt)})});
        return jsonObject;
    }

    /**
     * 隐藏API密钥的中间部分，用于安全地记录日志。
     *
     * @param apiKey 原始API密钥
     * @return 部分隐藏的API密钥
     */
    private static String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey) || apiKey.length() < 12) {
            return "API Key is too short to be masked or is blank.";
        }
        // 显示前6位和后4位
        return apiKey.substring(0, 6) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    public static void main(String[] args) {
        // 示例：测试所有模型
        String prompt = "翻译成简体中文："
                + "Why are countryside walks no longer so popular?";// "Explain how AI works.";
        // testAllModels(prompt);

        // 示例：只调用默认模型 (gemini-2.5-flash)
        String result = generateContent(prompt);
        System.out.println(result);
    }
}
