package com.coderdream.util.minimax;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * MiniMax API 请求对象（兼容 Claude API 格式）
 *
 * @author Claude Code
 * @since 2026-02-08
 */
@Data
@Accessors(chain = true)
public class MiniMaxRequest {

    /**
     * 模型名称
     * 例如: MiniMax-M2.1, MiniMax-Text-01
     */
    private String model;

    /**
     * 消息列表
     */
    private List<MiniMaxMessage> messages;

    /**
     * 生成的最大 token 数
     */
    private Integer maxTokens;

    /**
     * 采样温度 (0.0 - 1.0)
     * 较高的值会使输出更随机，较低的值会使输出更确定
     */
    private Double temperature;

    /**
     * 核采样参数 (0.0 - 1.0)
     */
    private Double topP;

    /**
     * 是否流式输出
     */
    private Boolean stream;

    /**
     * 构造函数
     */
    public MiniMaxRequest() {
        this.messages = new ArrayList<>();
        this.maxTokens = 1024;
        this.stream = false;
    }

    /**
     * 添加消息
     *
     * @param message 消息对象
     * @return 当前请求对象
     */
    public MiniMaxRequest addMessage(MiniMaxMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        return this;
    }

    /**
     * 添加用户消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public MiniMaxRequest addUserMessage(String content) {
        return addMessage(MiniMaxMessage.user(content));
    }

    /**
     * 添加系统消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public MiniMaxRequest addSystemMessage(String content) {
        return addMessage(MiniMaxMessage.system(content));
    }

    /**
     * 添加助手消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public MiniMaxRequest addAssistantMessage(String content) {
        return addMessage(MiniMaxMessage.assistant(content));
    }

    /**
     * 验证请求参数
     *
     * @throws IllegalArgumentException 如果参数无效
     */
    public void validate() {
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }

        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        if (temperature != null && (temperature < 0.0 || temperature > 1.0)) {
            throw new IllegalArgumentException("temperature 必须在 0.0 到 1.0 之间");
        }

        if (topP != null && (topP < 0.0 || topP > 1.0)) {
            throw new IllegalArgumentException("topP 必须在 0.0 到 1.0 之间");
        }

        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens 必须大于 0");
        }
    }
}
