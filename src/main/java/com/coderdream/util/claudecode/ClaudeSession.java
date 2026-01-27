package com.coderdream.util.claudecode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Claude 会话管理
 * 用于管理多轮对话的上下文和状态
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
@Data
public class ClaudeSession {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 消息历史
     */
    private List<ClaudeMessage> messageHistory;

    /**
     * 会话上下文（自定义数据）
     */
    private Map<String, Object> context;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 创建时间
     */
    private long createdTime;

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    /**
     * 消息计数
     */
    private int messageCount;

    /**
     * Token 使用统计
     */
    private long totalInputTokens;
    private long totalOutputTokens;

    /**
     * 构造函数
     */
    public ClaudeSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.sessionName = "Session-" + this.sessionId.substring(0, 8);
        this.messageHistory = new ArrayList<>();
        this.context = new HashMap<>();
        this.createdTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();
        this.messageCount = 0;
        this.totalInputTokens = 0;
        this.totalOutputTokens = 0;
        log.info("Session created: {}", this.sessionId);
    }

    /**
     * 构造函数
     *
     * @param sessionName 会话名称
     */
    public ClaudeSession(String sessionName) {
        this();
        this.sessionName = sessionName;
    }

    /**
     * 添加消息
     *
     * @param message 消息对象
     */
    public void addMessage(ClaudeMessage message) {
        this.messageHistory.add(message);
        this.messageCount++;
        this.lastUpdateTime = System.currentTimeMillis();
        log.debug("消息已添加到会话: {}", this.sessionId);
    }

    /**
     * 添加用户消息
     *
     * @param content 消息内容
     */
    public void addUserMessage(String content) {
        this.addMessage(ClaudeMessage.userMessage(content));
    }

    /**
     * 添加助手消息
     *
     * @param content 消息内容
     */
    public void addAssistantMessage(String content) {
        this.addMessage(ClaudeMessage.assistantMessage(content));
    }

    /**
     * 获取消息历史
     *
     * @return 消息列表
     */
    public List<ClaudeMessage> getMessageHistory() {
        return new ArrayList<>(this.messageHistory);
    }

    /**
     * 获取最后 N 条消息
     *
     * @param n 消息数量
     * @return 最后 N 条消息
     */
    public List<ClaudeMessage> getLastMessages(int n) {
        int startIndex = Math.max(0, this.messageHistory.size() - n);
        return new ArrayList<>(this.messageHistory.subList(startIndex, this.messageHistory.size()));
    }

    /**
     * 清空消息历史
     */
    public void clearMessageHistory() {
        this.messageHistory.clear();
        this.messageCount = 0;
        this.lastUpdateTime = System.currentTimeMillis();
        log.info("Message history cleared for session: {}", this.sessionId);
    }

    /**
     * 获取消息数量
     *
     * @return 消息数量
     */
    public int getMessageCount() {
        return this.messageHistory.size();
    }

    /**
     * 设置上下文值
     *
     * @param key   键
     * @param value 值
     */
    public void setContextValue(String key, Object value) {
        this.context.put(key, value);
    }

    /**
     * 获取上下文值
     *
     * @param key 键
     * @return 值
     */
    public Object getContextValue(String key) {
        return this.context.get(key);
    }

    /**
     * 获取上下文值（带默认值）
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public Object getContextValue(String key, Object defaultValue) {
        return this.context.getOrDefault(key, defaultValue);
    }

    /**
     * 更新 Token 统计
     *
     * @param inputTokens  输入 Token 数
     * @param outputTokens 输出 Token 数
     */
    public void updateTokenUsage(long inputTokens, long outputTokens) {
        this.totalInputTokens += inputTokens;
        this.totalOutputTokens += outputTokens;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 获取总 Token 数
     *
     * @return 总 Token 数
     */
    public long getTotalTokens() {
        return this.totalInputTokens + this.totalOutputTokens;
    }

    /**
     * 获取输入 Token 数
     *
     * @return 输入 Token 数
     */
    public long getInputTokens() {
        return this.totalInputTokens;
    }

    /**
     * 获取输出 Token 数
     *
     * @return 输出 Token 数
     */
    public long getOutputTokens() {
        return this.totalOutputTokens;
    }

    /**
     * 获取会话统计信息
     *
     * @return 统计信息字符串
     */
    public String getStatistics() {
        long duration = System.currentTimeMillis() - this.createdTime;
        return String.format(
                "Session: %s\n" +
                "Messages: %d\n" +
                "Input Tokens: %d\n" +
                "Output Tokens: %d\n" +
                "Total Tokens: %d\n" +
                "Duration: %dms",
                this.sessionName,
                this.messageCount,
                this.totalInputTokens,
                this.totalOutputTokens,
                this.getTotalTokens(),
                duration
        );
    }

    /**
     * 导出会话为 JSON 格式
     *
     * @return JSON 字符串
     */
    public String exportAsJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"sessionId\": \"").append(this.sessionId).append("\",\n");
        json.append("  \"sessionName\": \"").append(this.sessionName).append("\",\n");
        json.append("  \"messageCount\": ").append(this.messageCount).append(",\n");
        json.append("  \"totalInputTokens\": ").append(this.totalInputTokens).append(",\n");
        json.append("  \"totalOutputTokens\": ").append(this.totalOutputTokens).append(",\n");
        json.append("  \"createdTime\": ").append(this.createdTime).append(",\n");
        json.append("  \"lastUpdateTime\": ").append(this.lastUpdateTime).append("\n");
        json.append("}");
        return json.toString();
    }

    /**
     * 获取字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ClaudeSession{" +
                "sessionId='" + sessionId + '\'' +
                ", sessionName='" + sessionName + '\'' +
                ", messageCount=" + messageCount +
                ", totalTokens=" + getTotalTokens() +
                '}';
    }
}
