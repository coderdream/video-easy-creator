package com.coderdream.util.minimax;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 环境变量验证测试
 *
 * @author Claude Code
 * @since 2026-03-04
 */
@Slf4j
public class EnvironmentVariableVerifyTest {

    @Test
    public void testVerifyEnvironmentVariables() {
        log.info("========================================");
        log.info("环境变量验证");
        log.info("========================================");

        // 读取环境变量
        String baseUrl = System.getenv("MINIMAX_BASE_URL");
        String apiKey = System.getenv("MINIMAX_API_KEY");

        // 验证 BASE_URL
        log.info("1. MINIMAX_BASE_URL 验证:");
        if (baseUrl == null) {
            log.error("   ✗ 未设置");
        } else {
            log.info("   实际值: {}", baseUrl);
            log.info("   期望值: https://api.minimaxi.com");

            if ("https://api.minimaxi.com".equals(baseUrl)) {
                log.info("   ✓ 正确");
            } else {
                log.warn("   ✗ 不匹配！");
            }
        }

        log.info("");

        // 验证 API_KEY
        log.info("2. MINIMAX_API_KEY 验证:");
        if (apiKey == null) {
            log.error("   ✗ 未设置");
        } else {
            log.info("   长度: {} 字符", apiKey.length());
            log.info("   完整值: {}", apiKey);

            // 检查前缀
            boolean startsCorrect = apiKey.startsWith("sk-cp-g");
            log.info("   前缀检查 (sk-cp-g): {}", startsCorrect ? "✓ 正确" : "✗ 错误");

            // 检查后缀
            boolean endsCorrect = apiKey.endsWith("ww5m8");
            log.info("   后缀检查 (ww5m8): {}", endsCorrect ? "✓ 正确" : "✗ 错误");

            // 显示前后部分
            if (apiKey.length() >= 15) {
                String prefix = apiKey.substring(0, 10);
                String suffix = apiKey.substring(apiKey.length() - 10);
                log.info("   前10位: {}", prefix);
                log.info("   后10位: {}", suffix);
            }

            if (startsCorrect && endsCorrect) {
                log.info("   ✓ API Key 格式正确");
            } else {
                log.error("   ✗ API Key 格式不正确！");
            }
        }

        log.info("");
        log.info("========================================");
        log.info("验证结果汇总:");
        log.info("========================================");

        boolean baseUrlOk = baseUrl != null && "https://api.minimaxi.com".equals(baseUrl);
        boolean apiKeyOk = apiKey != null && apiKey.startsWith("sk-cp-g") && apiKey.endsWith("ww5m8");

        if (baseUrlOk && apiKeyOk) {
            log.info("✓ 所有环境变量配置正确！");
            log.info("可以继续运行 API 测试。");
        } else {
            log.error("✗ 环境变量配置有误，请检查：");
            if (!baseUrlOk) {
                log.error("  - MINIMAX_BASE_URL 需要设置为: https://api.minimaxi.com");
            }
            if (!apiKeyOk) {
                log.error("  - MINIMAX_API_KEY 格式不正确");
            }
        }

        log.info("========================================");
    }
}
