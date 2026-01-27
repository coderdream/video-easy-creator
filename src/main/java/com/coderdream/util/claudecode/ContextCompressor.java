package com.coderdream.util.claudecode;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文压缩器
 * 用于压缩长对话的上下文，减少 Token 消耗
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ContextCompressor {

    /**
     * 默认的最大消息数
     */
    private static final int DEFAULT_MAX_MESSAGES = 20;

    /**
     * 默认的摘要长度
     */
    private static final int DEFAULT_SUMMARY_LENGTH = 500;

    /**
     * 最大消息数
     */
    private int maxMessages;

    /**
     * 摘要长度
     */
    private int summaryLength;

    /**
     * 构造函数
     */
    public ContextCompressor() {
        this.maxMessages = DEFAULT_MAX_MESSAGES;
        this.summaryLength = DEFAULT_SUMMARY_LENGTH;
    }

    /**
     * 构造函数
     *
     * @param maxMessages   最大消息数
     * @param summaryLength 摘要长度
     */
    public ContextCompressor(int maxMessages, int summaryLength) {
        this.maxMessages = maxMessages;
        this.summaryLength = summaryLength;
    }

    /**
     * 压缩消息列表
     *
     * @param messages 原始消息列表
     * @return 压缩后的消息列表
     */
    public List<ClaudeMessage> compress(List<ClaudeMessage> messages) {
        if (messages.size() <= this.maxMessages) {
            log.debug("消息数量 ({}) 在限制内 ({})，无需压缩",
                    messages.size(), this.maxMessages);
            return new ArrayList<>(messages);
        }

        log.info("压缩 {} 条消息到 {} 条消息", messages.size(), this.maxMessages);

        List<ClaudeMessage> compressed = new ArrayList<>();

        // 保留最后 N-1 条消息
        int keepCount = this.maxMessages - 1;
        int startIndex = Math.max(0, messages.size() - keepCount);

        // 添加压缩的早期消息摘要
        if (startIndex > 0) {
            String summary = createSummary(messages.subList(0, startIndex));
            compressed.add(ClaudeMessage.userMessage(
                    "[Earlier conversation summary]\n" + summary
            ));
        }

        // 添加最后的消息
        compressed.addAll(messages.subList(startIndex, messages.size()));

        log.info("压缩完成：{} 条消息 -> {} 条消息",
                messages.size(), compressed.size());

        return compressed;
    }

    /**
     * 创建消息摘要
     *
     * @param messages 消息列表
     * @return 摘要字符串
     */
    private String createSummary(List<ClaudeMessage> messages) {
        StringBuilder summary = new StringBuilder();
        int charCount = 0;

        for (ClaudeMessage msg : messages) {
            String content = msg.getContent();
            if (content == null || content.isEmpty()) {
                continue;
            }

            // 限制摘要长度
            if (charCount + content.length() > this.summaryLength) {
                summary.append("\n... (truncated)");
                break;
            }

            summary.append("[").append(msg.getRole()).append("]: ");
            summary.append(content.substring(0, Math.min(100, content.length())));
            if (content.length() > 100) {
                summary.append("...");
            }
            summary.append("\n");

            charCount += content.length();
        }

        return summary.toString();
    }

    /**
     * 计算消息的 Token 数（粗略估计）
     *
     * @param message 消息对象
     * @return 估计的 Token 数
     */
    public static int estimateTokens(ClaudeMessage message) {
        String content = message.getContent();
        if (content == null || content.isEmpty()) {
            return 0;
        }

        // 粗略估计：1 Token ≈ 4 个字符
        return (content.length() + 3) / 4;
    }

    /**
     * 计算消息列表的总 Token 数（粗略估计）
     *
     * @param messages 消息列表
     * @return 估计的总 Token 数
     */
    public static int estimateTotalTokens(List<ClaudeMessage> messages) {
        int total = 0;
        for (ClaudeMessage msg : messages) {
            total += estimateTokens(msg);
        }
        return total;
    }

    /**
     * 根据 Token 限制压缩消息
     *
     * @param messages   消息列表
     * @param tokenLimit Token 限制
     * @return 压缩后的消息列表
     */
    public List<ClaudeMessage> compressByTokenLimit(List<ClaudeMessage> messages, int tokenLimit) {
        int totalTokens = estimateTotalTokens(messages);

        if (totalTokens <= tokenLimit) {
            log.debug("总 Token 数 ({}) 在限制内 ({})，无需压缩",
                    totalTokens, tokenLimit);
            return new ArrayList<>(messages);
        }

        log.info("压缩消息以适应 Token 限制：{} -> {}", totalTokens, tokenLimit);

        List<ClaudeMessage> compressed = new ArrayList<>();
        int currentTokens = 0;

        // 从后向前添加消息，直到达到 Token 限制
        for (int i = messages.size() - 1; i >= 0; i--) {
            ClaudeMessage msg = messages.get(i);
            int msgTokens = estimateTokens(msg);

            if (currentTokens + msgTokens > tokenLimit && !compressed.isEmpty()) {
                // 添加摘要
                String summary = createSummary(messages.subList(0, i + 1));
                compressed.add(0, ClaudeMessage.userMessage(
                        "[Earlier conversation summary]\n" + summary
                ));
                break;
            }

            compressed.add(0, msg);
            currentTokens += msgTokens;
        }

        log.info("压缩完成：{} Token -> {} Token",
                totalTokens, estimateTotalTokens(compressed));

        return compressed;
    }

    /**
     * 设置最大消息数
     *
     * @param maxMessages 最大消息数
     * @return 当前对象（支持链式调用）
     */
    public ContextCompressor setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
        return this;
    }

    /**
     * 设置摘要长度
     *
     * @param summaryLength 摘要长度
     * @return 当前对象（支持链式调用）
     */
    public ContextCompressor setSummaryLength(int summaryLength) {
        this.summaryLength = summaryLength;
        return this;
    }
}
