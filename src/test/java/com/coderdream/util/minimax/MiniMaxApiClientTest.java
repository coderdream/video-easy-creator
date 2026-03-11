package com.coderdream.util.minimax;

import com.coderdream.util.cd.CdConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * MiniMax API 客户端测试
 *
 * @author Claude Code
 * @since 2026-02-08
 */
@Slf4j
public class MiniMaxApiClientTest {

    /**
     * 测试基本的消息发送
     */
    @Test
    public void testSendMessage() {
        // 创建客户端（从环境变量读取配置）
        MiniMaxApiClient client = new MiniMaxApiClient();

        // 创建请求
        MiniMaxRequest request = new MiniMaxRequest()
                .setModel(CdConstants.MINIMAX_DEFAULT_MODEL)
                .setMaxTokens(1024)
                .setTemperature(0.7)
                .addUserMessage("你好，请用一句话介绍一下你自己。");

        try {
            // 发送请求
            MiniMaxResponse response = client.sendMessage(request);

            // 输出结果
            log.info("响应 ID: {}", response.getId());
            log.info("使用模型: {}", response.getModel());
            log.info("完成原因: {}", response.getFinishReason());
            log.info("回复内容: {}", response.getFirstChoiceText());

            if (response.getUsage() != null) {
                log.info("Token 使用: {}", response.getUsage().getTotalTokensCount());
            }
        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }

    /**
     * 测试翻译功能
     */
    @Test
    public void testTranslation() {
        MiniMaxApiClient client = new MiniMaxApiClient();

        MiniMaxRequest request = new MiniMaxRequest()
                .setModel(CdConstants.MINIMAX_DEFAULT_MODEL)
                .setMaxTokens(2048)
                .setTemperature(0.3)
                .addSystemMessage("你是一个专业的英语翻译助手，请将用户提供的英文翻译成中文。")
                .addUserMessage("Translate the following text to Chinese: The quick brown fox jumps over the lazy dog.");

        try {
            MiniMaxResponse response = client.sendMessage(request);
            log.info("翻译结果: {}", response.getFirstChoiceText());
        } catch (Exception e) {
            log.error("翻译测试失败", e);
        }
    }

    /**
     * 测试多轮对话
     */
    @Test
    public void testMultiTurnConversation() {
        MiniMaxApiClient client = new MiniMaxApiClient();

        MiniMaxRequest request = new MiniMaxRequest()
                .setModel(CdConstants.MINIMAX_DEFAULT_MODEL)
                .setMaxTokens(1024)
                .addUserMessage("请告诉我什么是人工智能？")
                .addAssistantMessage("人工智能（AI）是计算机科学的一个分支，致力于创建能够执行通常需要人类智能的任务的系统。")
                .addUserMessage("那机器学习和深度学习有什么区别？");

        try {
            MiniMaxResponse response = client.sendMessage(request);
            log.info("多轮对话回复: {}", response.getFirstChoiceText());
        } catch (Exception e) {
            log.error("多轮对话测试失败", e);
        }
    }

    /**
     * 测试配置信息
     */
    @Test
    public void testConfigInfo() {
        MiniMaxApiClient client = new MiniMaxApiClient();
        log.info("客户端配置: {}", client.getConfigInfo());
    }
}
