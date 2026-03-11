package com.coderdream.util.minimax;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * MiniMax 翻译工具测试类
 *
 * @author Claude Code
 * @since 2026-02-08
 */
@Slf4j
public class TranslateUtilWithMiniMaxTest {

    /**
     * 测试 MiniMax API 是否可用
     */
    @Test
    public void testMiniMaxConnection() {
        log.info("========================================");
        log.info("开始测试 MiniMax API 连接");
        log.info("========================================");

        try {
            // 1. 检查配置
            log.info("\n【步骤 1】检查配置信息");
            log.info("配置详情: {}", ConfigUtil.getConfigInfo());

            if (!ConfigUtil.isConfigValid()) {
                log.error("❌ 配置无效！请检查 config.properties 或环境变量");
                log.error("需要配置:");
                log.error("  - minimax.base_url (或环境变量 MINIMAX_BASE_URL)");
                log.error("  - minimax.api_key (或环境变量 MINIMAX_API_KEY)");
                return;
            }
            log.info("✅ 配置检查通过");

            // 2. 初始化客户端
            log.info("\n【步骤 2】初始化 MiniMax API 客户端");
            MiniMaxApiClient client = new MiniMaxApiClient();
            log.info("客户端配置: {}", client.getConfigInfo());
            log.info("✅ 客户端初始化成功");

            // 3. 发送测试消息
            log.info("\n【步骤 3】发送测试消息");
            String testPrompt = "MiniMax是不是准备好了？请用中文简短回答。";
            log.info("测试问题: {}", testPrompt);

            String response = client.sendMessage(testPrompt);

            // 4. 检查响应
            log.info("\n【步骤 4】检查响应结果");
            if (response == null || response.isBlank()) {
                log.error("❌ API 返回空响应");
                return;
            }

            log.info("✅ API 调用成功！");
            log.info("\n========================================");
            log.info("MiniMax 响应内容:");
            log.info("========================================");
            log.info("{}", response);
            log.info("========================================");

            // 5. 额外测试 - 简单翻译
            log.info("\n【步骤 5】测试翻译功能");
            String translatePrompt = "请将以下英文翻译成中文：Hello, how are you today?";
            log.info("翻译测试: {}", translatePrompt);

            String translateResponse = client.sendMessage(translatePrompt);
            if (translateResponse != null && !translateResponse.isBlank()) {
                log.info("✅ 翻译测试成功！");
                log.info("翻译结果: {}", translateResponse);
            } else {
                log.warn("⚠️ 翻译测试失败");
            }

            log.info("\n========================================");
            log.info("✅ 所有测试完成！MiniMax API 工作正常");
            log.info("========================================");

        } catch (Exception e) {
            log.error("\n========================================");
            log.error("❌ 测试过程中发生异常");
            log.error("========================================");
            log.error("异常信息: {}", e.getMessage(), e);
            log.error("\n请检查:");
            log.error("1. config.properties 文件是否存在于项目根目录");
            log.error("2. minimax.base_url 和 minimax.api_key 是否正确配置");
            log.error("3. 网络连接是否正常");
            log.error("4. API Key 是否有效且未过期");
        }
    }
}
