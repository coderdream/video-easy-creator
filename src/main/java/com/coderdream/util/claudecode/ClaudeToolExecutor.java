package com.coderdream.util.claudecode;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.claudecode.tool.ToolExecutor;
import com.coderdream.util.claudecode.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Claude 工具调用执行器
 * 负责管理工具调用循环
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeToolExecutor {

    /**
     * API 客户端
     */
    private ClaudeApiClient apiClient;

    /**
     * 工具注册表
     */
    private ToolRegistry toolRegistry;

    /**
     * 构造函数
     *
     * @param apiClient    API 客户端
     * @param toolRegistry 工具注册表
     */
    public ClaudeToolExecutor(ClaudeApiClient apiClient, ToolRegistry toolRegistry) {
        this.apiClient = apiClient;
        this.toolRegistry = toolRegistry;
    }

    /**
     * 执行带工具调用的任务
     *
     * @param prompt       用户提示
     * @param systemPrompt 系统提示
     * @return 最终的文本响应
     */
    public String executeWithTools(String prompt, String systemPrompt) {
        log.info("开始工具执行循环，提示: {}", prompt.substring(0, Math.min(50, prompt.length())));

        // 初始化消息列表
        List<ClaudeMessage> messages = new ArrayList<>();
        messages.add(ClaudeMessage.userMessage(prompt));

        // 工具调用循环
        int iteration = 0;
        while (iteration < CdConstants.CLAUDE_TOOL_LOOP_MAX_ITERATIONS) {
            iteration++;
            log.info("工具循环迭代: {}", iteration);

            // 构建请求
            ClaudeRequest request = new ClaudeRequest()
                    .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                    .setMaxTokens(CdConstants.CLAUDE_MAX_TOKENS)
                    .setSystemPrompt(systemPrompt);

            // 添加所有消息
            for (ClaudeMessage msg : messages) {
                request.addMessage(msg);
            }

            // 发送请求（带工具定义）
            ClaudeResponse response = apiClient.sendMessageWithTools(
                    request,
                    toolRegistry.getAllToolsAsJson()
            );

            // 将响应添加到消息列表
            JSONArray contentArray = new JSONArray();
            for (ClaudeResponse.ContentBlock block : response.getContent()) {
                JSONObject blockJson = new JSONObject();
                blockJson.set("type", block.getType());

                if ("text".equals(block.getType())) {
                    blockJson.set("text", block.getText());
                } else if ("tool_use".equals(block.getType())) {
                    blockJson.set("id", block.getToolUseId());
                    blockJson.set("name", block.getToolName());
                    blockJson.set("input", block.getToolInput());
                }

                contentArray.add(blockJson);
            }

            ClaudeMessage assistantMessage = new ClaudeMessage()
                    .setRole("assistant")
                    .setContentArray(contentArray);
            messages.add(assistantMessage);

            // 检查是否有工具调用
            List<ClaudeResponse.ContentBlock> toolUseBlocks = response.getToolUseBlocks();

            if (toolUseBlocks.isEmpty()) {
                // 没有工具调用，返回最终答案
                log.info("工具循环完成。已收到最终答案。");
                return response.getTextContent();
            }

            // 执行工具调用
            JSONArray toolResults = new JSONArray();
            for (ClaudeResponse.ContentBlock toolBlock : toolUseBlocks) {
                String toolName = toolBlock.getToolName();
                JSONObject toolInput = toolBlock.getToolInput();
                String toolUseId = toolBlock.getToolUseId();

                log.info("执行工具：{}，输入：{}", toolName, toolInput);

                try {
                    String result = toolRegistry.executeTool(toolName, toolInput);

                    JSONObject toolResult = new JSONObject();
                    toolResult.set("type", "tool_result");
                    toolResult.set("tool_use_id", toolUseId);
                    toolResult.set("content", result);
                    toolResults.add(toolResult);

                    log.info("工具执行成功: {}", toolName);
                } catch (Exception e) {
                    log.error("工具执行失败: {}", toolName, e);

                    JSONObject toolResult = new JSONObject();
                    toolResult.set("type", "tool_result");
                    toolResult.set("tool_use_id", toolUseId);
                    toolResult.set("content", "Error: " + e.getMessage());
                    toolResult.set("is_error", true);
                    toolResults.add(toolResult);
                }
            }

            // 添加工具结果消息
            ClaudeMessage toolResultMessage = new ClaudeMessage()
                    .setRole("user")
                    .setContentArray(toolResults);
            messages.add(toolResultMessage);

            log.info("工具结果已添加到消息历史。继续循环...");
        }

        log.warn("工具循环达到最大迭代次数: {}", CdConstants.CLAUDE_TOOL_LOOP_MAX_ITERATIONS);
        return "Error: Tool loop reached maximum iterations";
    }

    /**
     * 执行带工具调用的任务（使用默认系统提示）
     *
     * @param prompt 用户提示
     * @return 最终的文本响应
     */
    public String executeWithTools(String prompt) {
        String systemPrompt = "You are a helpful assistant with access to tools. " +
                "Use the available tools to help the user accomplish their tasks. " +
                "Always explain what you're doing and provide clear results.";
        return executeWithTools(prompt, systemPrompt);
    }

    /**
     * 获取工具注册表
     *
     * @return 工具注册表
     */
    public ToolRegistry getToolRegistry() {
        return this.toolRegistry;
    }

    /**
     * 获取 API 客户端
     *
     * @return API 客户端
     */
    public ClaudeApiClient getApiClient() {
        return this.apiClient;
    }
}
