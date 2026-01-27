package com.coderdream.util.claudecode;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Claude 消息对象
 * 用于表示与 Claude API 的对话消息
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Data
@Accessors(chain = true)
public class ClaudeMessage {

    /**
     * 消息角色：user 或 assistant
     */
    private String role;

    /**
     * 简单文本内容
     */
    private String content;

    /**
     * 复杂内容数组（包含工具调用等）
     */
    private JSONArray contentArray;

    /**
     * 消息时间戳
     */
    private Long timestamp;

    /**
     * 创建用户消息
     *
     * @param content 消息内容
     * @return ClaudeMessage 实例
     */
    public static ClaudeMessage userMessage(String content) {
        return new ClaudeMessage()
                .setRole("user")
                .setContent(content)
                .setTimestamp(System.currentTimeMillis());
    }

    /**
     * 创建助手消息
     *
     * @param content 消息内容
     * @return ClaudeMessage 实例
     */
    public static ClaudeMessage assistantMessage(String content) {
        return new ClaudeMessage()
                .setRole("assistant")
                .setContent(content)
                .setTimestamp(System.currentTimeMillis());
    }

    /**
     * 创建工具结果消息
     *
     * @param toolUseId 工具调用 ID
     * @param result    工具执行结果
     * @return ClaudeMessage 实例
     */
    public static ClaudeMessage toolResultMessage(String toolUseId, String result) {
        JSONArray contentArray = new JSONArray();
        JSONObject toolResult = new JSONObject();
        toolResult.set("type", "tool_result");
        toolResult.set("tool_use_id", toolUseId);
        toolResult.set("content", result);
        contentArray.add(toolResult);

        return new ClaudeMessage()
                .setRole("user")
                .setContentArray(contentArray)
                .setTimestamp(System.currentTimeMillis());
    }

    /**
     * 将消息转换为 JSON 对象
     *
     * @return JSON 对象
     */
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.set("role", this.role);

        if (this.contentArray != null && this.contentArray.size() > 0) {
            obj.set("content", this.contentArray);
        } else {
            obj.set("content", this.content);
        }

        return obj;
    }

    /**
     * 检查是否为用户消息
     *
     * @return true 如果是用户消息
     */
    public boolean isUserMessage() {
        return "user".equals(this.role);
    }

    /**
     * 检查是否为助手消息
     *
     * @return true 如果是助手消息
     */
    public boolean isAssistantMessage() {
        return "assistant".equals(this.role);
    }
}
