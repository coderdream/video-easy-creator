package com.coderdream.util.claudecode;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude 会话管理测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeSessionTest {

    private ClaudeSession session;

    @BeforeEach
    public void setUp() {
        session = new ClaudeSession();
        log.info("测试设置完成");
    }

    @Test
    public void testSessionInitialization() {
        log.info("========== 测试：会话初始化 ==========");
        assertNotNull(session, "会话不应为空");
        assertEquals(0, session.getMessageCount(), "初始消息数应为 0");
        log.info("会话初始化测试通过");
    }

    @Test
    public void testAddMessage() {
        log.info("========== 测试：添加消息 ==========");
        ClaudeMessage userMsg = ClaudeMessage.userMessage("Hello, Claude!");
        session.addMessage(userMsg);
        assertEquals(1, session.getMessageCount(), "消息数应为 1");
        var lastMessages = session.getLastMessages(1);
        assertNotNull(lastMessages, "最近消息不应为空");
        assertEquals(1, lastMessages.size(), "应有一条消息");
        log.info("添加消息测试通过");
    }

    @Test
    public void testAddMultipleMessages() {
        log.info("========== 测试：添加多条消息 ==========");
        session.addMessage(ClaudeMessage.userMessage("First message"));
        session.addMessage(ClaudeMessage.assistantMessage("Response 1"));
        session.addMessage(ClaudeMessage.userMessage("Second message"));
        session.addMessage(ClaudeMessage.assistantMessage("Response 2"));
        assertEquals(4, session.getMessageCount(), "消息 count 应为 4");
        log.info("添加多条消息测试通过");
    }

    @Test
    public void testGetLastMessages() {
        log.info("========== 测试：获取最近消息 ==========");
        for (int i = 0; i < 5; i++) {
            session.addMessage(ClaudeMessage.userMessage("Message " + i));
        }
        var lastThree = session.getLastMessages(3);
        assertEquals(3, lastThree.size(), "Should return 3 消息s");
        var lastTen = session.getLastMessages(10);
        assertEquals(5, lastTen.size(), "Should return 5 消息s");
        log.info("获取最近消息测试通过");
    }

    @Test
    public void testClearMessageHistory() {
        log.info("========== 测试： Clear 消息 History ==========");
        session.addMessage(ClaudeMessage.userMessage("Message 1"));
        session.addMessage(ClaudeMessage.assistantMessage("Response 1"));
        assertEquals(2, session.getMessageCount(), "消息 count 应为 2");
        session.clearMessageHistory();
        assertEquals(0, session.getMessageCount(), "清空后消息数应为 0");
        log.info("Clear 消息 history 测试通过");
    }

    @Test
    public void testMessageRoleValidation() {
        log.info("========== 测试： 消息 Role Validation ==========");
        session.addMessage(ClaudeMessage.userMessage("User message"));
        session.addMessage(ClaudeMessage.assistantMessage("Assistant message"));
        var messages = session.getLastMessages(2);
        assertEquals("user", messages.get(0).getRole(), "第一 消息 role 应为 user");
        assertEquals("assistant", messages.get(1).getRole(), "第二 消息 role 应为 assistant");
        log.info("消息 role validation 测试通过");
    }

    @Test
    public void testMessageContent() {
        log.info("========== 测试： 消息 Content ==========");
        String userContent = "What is the weather?";
        String assistantContent = "I don't have access to real-time weather data.";
        session.addMessage(ClaudeMessage.userMessage(userContent));
        session.addMessage(ClaudeMessage.assistantMessage(assistantContent));
        var messages = session.getLastMessages(2);
        assertEquals(userContent, messages.get(0).getContent(), "User 消息 content 应匹配");
        assertEquals(assistantContent, messages.get(1).getContent(), "Assistant 消息 content 应匹配");
        log.info("消息 content 测试通过");
    }

    @Test
    public void testMessageLimitHandling() {
        log.info("========== 测试： 消息 Limit Handling ==========");
        for (int i = 0; i < 100; i++) {
            session.addMessage(ClaudeMessage.userMessage("Message " + i));
        }
        assertEquals(100, session.getMessageCount(), "消息 count 应为 100");
        var lastTen = session.getLastMessages(10);
        assertEquals(10, lastTen.size(), "Should return 10 消息s");
        log.info("消息 limit handling 测试通过");
    }
}
