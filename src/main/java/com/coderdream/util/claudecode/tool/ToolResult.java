package com.coderdream.util.claudecode.tool;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 工具执行结果
 * 用于表示工具执行的结果
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Data
@Accessors(chain = true)
public class ToolResult {

    /**
     * 工具调用 ID
     */
    private String toolUseId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 执行结果内容
     */
    private String content;

    /**
     * 错误信息（如果执行失败）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;

    /**
     * 创建成功的工具结果
     *
     * @param toolUseId 工具调用 ID
     * @param toolName  工具名称
     * @param content   结果内容
     * @return ToolResult 实例
     */
    public static ToolResult success(String toolUseId, String toolName, String content) {
        return new ToolResult()
                .setToolUseId(toolUseId)
                .setToolName(toolName)
                .setSuccess(true)
                .setContent(content);
    }

    /**
     * 创建失败的工具结果
     *
     * @param toolUseId    工具调用 ID
     * @param toolName     工具名称
     * @param errorMessage 错误信息
     * @return ToolResult 实例
     */
    public static ToolResult failure(String toolUseId, String toolName, String errorMessage) {
        return new ToolResult()
                .setToolUseId(toolUseId)
                .setToolName(toolName)
                .setSuccess(false)
                .setErrorMessage(errorMessage);
    }

    /**
     * 获取结果字符串（成功时返回内容，失败时返回错误信息）
     *
     * @return 结果字符串
     */
    public String getResultString() {
        return success ? content : "Error: " + errorMessage;
    }

    /**
     * 获取字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ToolResult{" +
                "toolName='" + toolName + '\'' +
                ", success=" + success +
                ", executionTime=" + executionTime + "ms" +
                '}';
    }
}
