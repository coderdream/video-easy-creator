package com.coderdream.util.minimax;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置读取工具类
 * 支持从 config.properties 读取配置，失败则回退到环境变量
 *
 * @author Claude Code
 * @since 2026-01-27
 */
@Slf4j
public class ConfigUtil {

    private static Properties props;

    // 配置文件名
    private static final String CONFIG_FILE = "config.properties";

    // MiniMax API 配置键
    private static final String KEY_MINIMAX_BASE_URL = "minimax.base_url";
    private static final String KEY_MINIMAX_API_KEY = "minimax.api_key";
    private static final String KEY_MINIMAX_API_VERSION = "minimax.api_version";
    private static final String KEY_MINIMAX_TIMEOUT = "minimax.timeout";

    // Claude 模型配置键
    private static final String KEY_CLAUDE_MODEL = "claude.model";
    private static final String KEY_CLAUDE_MAX_TOKENS = "claude.max_tokens";

    // 环境变量键
    private static final String ENV_MINIMAX_BASE_URL = "MINIMAX_BASE_URL";
    private static final String ENV_MINIMAX_API_KEY = "MINIMAX_API_KEY";
    private static final String ENV_ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";
    private static final String ENV_ANTHROPIC_AUTH_TOKEN = "ANTHROPIC_AUTH_TOKEN";

    static {
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        props = new Properties();

        // 尝试从项目根目录加载 config.properties
        Path configPath = Paths.get(System.getProperty("user.dir"), CONFIG_FILE);

        if (configPath.toFile().exists()) {
            try (InputStream is = new FileInputStream(configPath.toFile())) {
                props.load(is);
                log.info("成功加载配置文件: {}", configPath.toAbsolutePath());
                return;
            } catch (Exception e) {
                log.warn("加载配置文件失败: {}", e.getMessage());
            }
        }

        // 尝试从类路径加载
        try (InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                log.info("成功从类路径加载配置文件");
                return;
            }
        } catch (Exception e) {
            log.warn("从类路径加载配置文件失败: {}", e.getMessage());
        }

        log.warn("未找到配置文件 {}，将使用环境变量", CONFIG_FILE);
    }

    /**
     * 获取 MiniMax API 基础 URL
     */
    public static String getMiniMaxBaseUrl() {
        // 优先从配置文件读取（新格式）
        String baseUrl = props.getProperty(KEY_MINIMAX_BASE_URL);
        if (StrUtil.isNotBlank(baseUrl)) {
            return baseUrl;
        }

        // 兼容旧格式: api.minimax.base.url
        baseUrl = props.getProperty("api.minimax.base.url");
        if (StrUtil.isNotBlank(baseUrl)) {
            return baseUrl;
        }

        // 回退到环境变量
        baseUrl = System.getenv(ENV_MINIMAX_BASE_URL);
        if (StrUtil.isNotBlank(baseUrl)) {
            return baseUrl;
        }

        // 尝试从旧的环境变量名读取
        baseUrl = System.getenv(ENV_ANTHROPIC_BASE_URL);
        if (StrUtil.isNotBlank(baseUrl)) {
            return baseUrl;
        }

        log.error("MiniMax Base URL 未配置！请在 config.properties 中设置 {} 或设置环境变量 {}", KEY_MINIMAX_BASE_URL, ENV_MINIMAX_BASE_URL);
        return null;
    }

    /**
     * 获取 MiniMax API Key
     */
    public static String getMiniMaxApiKey() {
        // 优先从配置文件读取（新格式）
        String apiKey = props.getProperty(KEY_MINIMAX_API_KEY);
        if (StrUtil.isNotBlank(apiKey)) {
            return apiKey;
        }

        // 兼容旧格式: api.minimax.key
        apiKey = props.getProperty("api.minimax.key");
        if (StrUtil.isNotBlank(apiKey)) {
            return apiKey;
        }

        // 回退到环境变量
        apiKey = System.getenv(ENV_MINIMAX_API_KEY);
        if (StrUtil.isNotBlank(apiKey)) {
            return apiKey;
        }

        // 尝试从旧的环境变量名读取
        apiKey = System.getenv(ENV_ANTHROPIC_AUTH_TOKEN);
        if (StrUtil.isNotBlank(apiKey)) {
            return apiKey;
        }

        log.error("MiniMax API Key 未配置！请在 config.properties 中设置 {} 或设置环境变量 {}", KEY_MINIMAX_API_KEY, ENV_MINIMAX_API_KEY);
        return null;
    }

    /**
     * 获取 API 版本
     */
    public static String getMiniMaxApiVersion() {
        String version = props.getProperty(KEY_MINIMAX_API_VERSION);
        if (StrUtil.isNotBlank(version)) {
            return version;
        }
        return "2023-06-01"; // 默认版本
    }

    /**
     * 获取超时时间（毫秒）
     */
    public static int getMiniMaxTimeout() {
        String timeout = props.getProperty(KEY_MINIMAX_TIMEOUT);
        if (StrUtil.isNotBlank(timeout)) {
            try {
                return Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                log.warn("无效的超时时间配置: {}，使用默认值", timeout);
            }
        }
        return 60000; // 默认 60 秒
    }

    /**
     * 获取模型名称
     */
    public static String getClaudeModel() {
        // 优先从配置文件读取（新格式）
        String model = props.getProperty(KEY_CLAUDE_MODEL);
        if (StrUtil.isNotBlank(model)) {
            return model;
        }

        // 兼容旧格式: api.minimax.default.model
        model = props.getProperty("api.minimax.default.model");
        if (StrUtil.isNotBlank(model)) {
            return model;
        }

        return "MiniMax-M2.1"; // 默认模型
    }

    /**
     * 获取最大 Token 数
     */
    public static int getClaudeMaxTokens() {
        String maxTokens = props.getProperty(KEY_CLAUDE_MAX_TOKENS);
        if (StrUtil.isNotBlank(maxTokens)) {
            try {
                return Integer.parseInt(maxTokens);
            } catch (NumberFormatException e) {
                log.warn("无效的 max_tokens 配置: {}，使用默认值", maxTokens);
            }
        }
        return 4096; // 默认 4096
    }

    /**
     * 检查配置是否有效
     */
    public static boolean isConfigValid() {
        return StrUtil.isNotBlank(getMiniMaxBaseUrl()) &&
               StrUtil.isNotBlank(getMiniMaxApiKey());
    }

    /**
     * 获取配置信息（不包含敏感信息）
     */
    public static String getConfigInfo() {
        return String.format(
                "ConfigUtil: baseUrl=%s, version=%s, timeout=%d, model=%s",
                getMiniMaxBaseUrl(),
                getMiniMaxApiVersion(),
                getMiniMaxTimeout(),
                getClaudeModel()
        );
    }
}
