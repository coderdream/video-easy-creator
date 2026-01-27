package com.coderdream.util.claudecode;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Claude API 请求对象
 * 用于构建发送给 Claude API 的请求
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Data
@Accessors(chain = true)
public class ClaudeRequest {

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 最大输出 Token 数
     */
    private int maxTokens = 4096;

    /**
     * 温度参数（0-1），控制输出的随机性
     */
    private Float temperature;

    /**
     * 努力程度：high, medium, low
     */
    private String effort;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 消息列表
     */
    private List<ClaudeMessage> messages = new ArrayList<>();

    /**
     * 是否启用思考模式
     */
    private boolean thinkingEnabled = false;

    /**
     * 思考预算（Token 数）
     */
    private int thinkingBudget = 2048;

    /**
     * 添加消息
     *
     * @param message 消息对象
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest addMessage(ClaudeMessage message) {
        this.messages.add(message);
        return this;
    }

    /**
     * 添加用户消息
     *
     * @param content 消息内容
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest addUserMessage(String content) {
        this.messages.add(ClaudeMessage.userMessage(content));
        return this;
    }

    /**
     * 添加助手消息
     *
     * @param content 消息内容
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest addAssistantMessage(String content) {
        this.messages.add(ClaudeMessage.assistantMessage(content));
        return this;
    }

    /**
     * 清空消息列表
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest clearMessages() {
        this.messages.clear();
        return this;
    }

    /**
     * 获取消息数量
     *
     * @return 消息数量
     */
    public int getMessageCount() {
        return this.messages.size();
    }

    /**
     * 检查是否有消息
     *
     * @return true 如果有消息
     */
    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }

    /**
     * 启用思考模式
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest enableThinking() {
        this.thinkingEnabled = true;
        return this;
    }

    /**
     * 启用思考模式并设置预算
     *
     * @param budget 思考预算（Token 数）
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest enableThinking(int budget) {
        this.thinkingEnabled = true;
        this.thinkingBudget = budget;
        return this;
    }

    /**
     * 禁用思考模式
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest disableThinking() {
        this.thinkingEnabled = false;
        return this;
    }

    /**
     * 设置高努力程度
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest setEffortHigh() {
        this.effort = "high";
        return this;
    }

    /**
     * 设置中等努力程度
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest setEffortMedium() {
        this.effort = "medium";
        return this;
    }

    /**
     * 设置低努力程度
     *
     * @return 当前请求对象（支持链式调用）
     */
    public ClaudeRequest setEffortLow() {
        this.effort = "low";
        return this;
    }

    /**
     * 验证请求的有效性
     *
     * @throws IllegalArgumentException 如果请求无效
     */
    public void validate() {
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("Model must be specified");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be greater than 0");
        }
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("At least one message is required");
        }
    }
}
