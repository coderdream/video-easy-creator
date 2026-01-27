package com.coderdream.util.claudecode;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude 消息对象测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeMessageTest {

    /**
     * 测试创建用户消息
     */
    @Test
    public void testCreateUserMessage() {
        log.info("========== 测试：创建用户消息 ==========");

        ClaudeMessage message = ClaudeMessage.userMessage("Hello, Claude!");

        assertNotNull(message, "消息不应为空");
        assertEquals("user", message.getRole(), "角色应为 user");
        assertEquals("Hello, Claude!", message.getContent(), "内容应匹配");
        assertNotNull(message.getTimestamp(), "时间戳不应为空");
        assertTrue(message.isUserMessage(), "应为用户消息");
        assertFalse(message.isAssistantMessage(), "不应为助手消息");

        log.info("用户消息已创建：{}", message.getContent());
    }

    /**
     * 测试创建助手消息
     */
    @Test
    public void testCreateAssistantMessage() {
        log.info("========== 测试：创建助手消息 ==========");

        ClaudeMessage message = ClaudeMessage.assistantMessage("Hello, I'm Claude!");

        assertNotNull(message, "消息不应为空");
        assertEquals("assistant", message.getRole(), "角色应为 assistant");
        assertEquals("Hello, I'm Claude!", message.getContent(), "内容应匹配");
        assertFalse(message.isUserMessage(), "不应为用户消息");
        assertTrue(message.isAssistantMessage(), "应为助手消息");

        log.info("助手消息已创建：{}", message.getContent());
    }

    /**
     * 测试创建工具结果消息
     */
    @Test
    public void testCreateToolResultMessage() {
        log.info("========== 测试：创建工具结果消息 ==========");

        String toolUseId = "tool_123";
        String result = "File content here";

        ClaudeMessage message = ClaudeMessage.toolResultMessage(toolUseId, result);

        assertNotNull(message, "消息不应为空");
        assertEquals("user", message.getRole(), "角色应为 user");
        assertNotNull(message.getContentArray(), "内容数组不应为空");
        assertTrue(message.getContentArray().size() > 0, "内容数组不应为空");

        JSONObject toolResult = message.getContentArray().getJSONObject(0);
        assertEquals("tool_result", toolResult.getStr("type"), "类型应为 工具_result");
        assertEquals(toolUseId, toolResult.getStr("tool_use_id"), "工具使用ID应匹配");
        assertEquals(result, toolResult.getStr("content"), "内容应匹配");

        log.info("工具结果消息已创建，ID：{}", toolUseId);
    }

    /**
     * 测试消息转换为 JSON
     */
    @Test
    public void testMessageToJson() {
        log.info("========== 测试：消息转换为 JSON ==========");

        ClaudeMessage message = ClaudeMessage.userMessage("Test message");
        JSONObject json = message.toJson();

        assertNotNull(json, "JSON 不应为空");
        assertEquals("user", json.getStr("role"), "角色应为 user");
        assertEquals("Test message", json.getStr("content"), "内容应匹配");

        log.info("消息已转换为 JSON：{}", json.toStringPretty());
    }

    /**
     * 测试复杂内容的 JSON 转换
     */
    @Test
    public void testComplexContentToJson() {
        log.info("========== 测试：复杂内容转换为 JSON ==========");

        JSONArray contentArray = new JSONArray();
        JSONObject textBlock = new JSONObject();
        textBlock.set("type", "text");
        textBlock.set("text", "Hello");
        contentArray.add(textBlock);

        ClaudeMessage message = new ClaudeMessage()
                .setRole("assistant")
                .setContentArray(contentArray);

        JSONObject json = message.toJson();

        assertNotNull(json, "JSON 不应为空");
        assertEquals("assistant", json.getStr("role"), "角色应为 assistant");
        assertTrue(json.get("content") instanceof JSONArray, "内容应为数组");

        log.info("复杂消息已转换为 JSON：{}", json.toStringPretty());
    }

    /**
     * 测试链式调用
     */
    @Test
    public void testChainedCalls() {
        log.info("========== 测试：链式调用 ==========");

        ClaudeMessage message = new ClaudeMessage()
                .setRole("user")
                .setContent("Test")
                .setTimestamp(System.currentTimeMillis());

        assertNotNull(message, "消息不应为空");
        assertEquals("user", message.getRole(), "角色应为 user");
        assertEquals("Test", message.getContent(), "内容应为 Test");

        log.info("链式调用成功完成");
    }

    /**
     * 测试消息时间戳
     */
    @Test
    public void testMessageTimestamp() {
        log.info("========== 测试：消息时间戳 ==========");

        long beforeTime = System.currentTimeMillis();
        ClaudeMessage message = ClaudeMessage.userMessage("Test");
        long afterTime = System.currentTimeMillis();

        assertNotNull(message.getTimestamp(), "时间戳不应为空");
        assertTrue(message.getTimestamp() >= beforeTime, "时间戳应 >= beforeTime");
        assertTrue(message.getTimestamp() <= afterTime, "时间戳应 <= afterTime");

        log.info("消息时间戳：{}", message.getTimestamp());
    }

    /**
     * 测试空内容消息
     */
    @Test
    public void testEmptyContentMessage() {
        log.info("========== 测试：空内容消息 ==========");

        ClaudeMessage message = new ClaudeMessage()
                .setRole("user")
                .setContent("");

        JSONObject json = message.toJson();
        assertEquals("", json.getStr("content"), "内容应为空字符串");

        log.info("空内容消息处理正确");
    }

    /**
     * 测试多行内容消息
     */
    @Test
    public void testMultilineContentMessage() {
        log.info("========== 测试：多行内容消息 ==========");

        String multilineContent = "Line 1\nLine 2\nLine 3";
        ClaudeMessage message = ClaudeMessage.userMessage(multilineContent);

        assertEquals(multilineContent, message.getContent(), "内容应匹配");
        JSONObject json = message.toJson();
        assertEquals(multilineContent, json.getStr("content"), "JSON 内容应匹配");

        log.info("多行内容消息处理正确");
    }
}
