package com.coderdream.util.claudecode;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Claude API 响应对象
 * 用于表示 Claude API 返回的响应
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Data
@Accessors(chain = true)
public class ClaudeResponse {

    /**
     * 响应 ID
     */
    private String id;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 停止原因：end_turn, tool_use, max_tokens
     */
    private String stopReason;

    /**
     * 响应内容块列表
     */
    private List<ContentBlock> content;

    /**
     * Token 使用统计
     */
    private Usage usage;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 内容块
     */
    @Data
    @Accessors(chain = true)
    public static class ContentBlock {
        /**
         * 内容类型：text, tool_use, thinking
         */
        private String type;

        /**
         * 文本内容（type=text 时）
         */
        private String text;

        /**
         * 工具调用 ID（type=tool_use 时）
         */
        private String toolUseId;

        /**
         * 工具名称（type=tool_use 时）
         */
        private String toolName;

        /**
         * 工具输入参数（type=tool_use 时）
         */
        private JSONObject toolInput;

        /**
         * 思考内容（type=thinking 时）
         */
        private String thinking;

        /**
         * 检查是否为文本块
         */
        public boolean isText() {
            return "text".equals(this.type);
        }

        /**
         * 检查是否为工具调用块
         */
        public boolean isToolUse() {
            return "tool_use".equals(this.type);
        }

        /**
         * 检查是否为思考块
         */
        public boolean isThinking() {
            return "thinking".equals(this.type);
        }
    }

    /**
     * Token 使用统计
     */
    @Data
    @Accessors(chain = true)
    public static class Usage {
        /**
         * 输入 Token 数
         */
        private int inputTokens;

        /**
         * 输出 Token 数
         */
        private int outputTokens;

        /**
         * 缓存创建的输入 Token 数
         */
        private int cacheCreationInputTokens;

        /**
         * 缓存读取的输入 Token 数
         */
        private int cacheReadInputTokens;

        /**
         * 获取总 Token 数
         */
        public int getTotalTokens() {
            return inputTokens + outputTokens;
        }
    }

    /**
     * 获取所有文本内容
     *
     * @return 拼接后的文本内容
     */
    public String getTextContent() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::getText)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 获取所有工具调用块
     *
     * @return 工具调用块列表
     */
    public List<ContentBlock> getToolUseBlocks() {
        if (content == null) {
            return new ArrayList<>();
        }
        return content.stream()
                .filter(ContentBlock::isToolUse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有思考块
     *
     * @return 思考块列表
     */
    public List<ContentBlock> getThinkingBlocks() {
        if (content == null) {
            return new ArrayList<>();
        }
        return content.stream()
                .filter(ContentBlock::isThinking)
                .collect(Collectors.toList());
    }

    /**
     * 获取思考内容
     *
     * @return 拼接后的思考内容
     */
    public String getThinkingContent() {
        return getThinkingBlocks().stream()
                .map(ContentBlock::getThinking)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 检查是否有工具调用
     *
     * @return true 如果有工具调用
     */
    public boolean hasToolUse() {
        return !getToolUseBlocks().isEmpty();
    }

    /**
     * 检查是否有思考内容
     *
     * @return true 如果有思考内容
     */
    public boolean hasThinking() {
        return !getThinkingBlocks().isEmpty();
    }

    /**
     * 检查是否已完成（不需要继续调用）
     *
     * @return true 如果已完成
     */
    public boolean isComplete() {
        return "end_turn".equals(stopReason);
    }

    /**
     * 检查是否因为 Token 超限而停止
     *
     * @return true 如果因为 Token 超限
     */
    public boolean isMaxTokensReached() {
        return "max_tokens".equals(stopReason);
    }
}
