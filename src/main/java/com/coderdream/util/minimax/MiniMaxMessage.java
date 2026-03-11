package com.coderdream.util.minimax;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MiniMax API 消息对象（兼容 Claude API 格式）
 *
 * @author Claude Code
 * @since 2026-02-08
 */
@Data
@Accessors(chain = true)
public class MiniMaxMessage {

    /**
     * 消息角色
     * system: 系统消息
     * user: 用户消息
     * assistant: 助手消息
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 构造函数
     */
    public MiniMaxMessage() {
    }

    /**
     * 构造函数
     *
     * @param role    角色
     * @param content 消息内容
     */
    public MiniMaxMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 创建系统消息
     *
     * @param content 消息内容
     * @return 消息对象
     */
    public static MiniMaxMessage system(String content) {
        return new MiniMaxMessage("system", content);
    }

    /**
     * 创建用户消息
     *
     * @param content 消息内容
     * @return 消息对象
     */
    public static MiniMaxMessage user(String content) {
        return new MiniMaxMessage("user", content);
    }

    /**
     * 创建助手消息
     *
     * @param content 消息内容
     * @return 消息对象
     */
    public static MiniMaxMessage assistant(String content) {
        return new MiniMaxMessage("assistant", content);
    }

    /**
     * 转换为 JSON 对象
     *
     * @return JSON 对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.set("role", role);
        json.set("content", content);
        return json;
    }
}
