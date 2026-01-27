package com.coderdream.util.claudecode;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude 响应对象测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeResponseTest {

    private ClaudeResponse response;

    @BeforeEach
    public void setUp() {
        response = new ClaudeResponse();
        response.setId("msg_123");
        response.setModel("claude-sonnet-4-5-20250929");
        response.setStopReason("end_turn");
        response.setTimestamp(System.currentTimeMillis());

        // 创建 Usage 对象
        ClaudeResponse.Usage usage = new ClaudeResponse.Usage();
        usage.setInputTokens(100);
        usage.setOutputTokens(50);
        response.setUsage(usage);

        // 创建内容块（使用可修改的 ArrayList）
        JSONArray content = new JSONArray();
        ClaudeResponse.ContentBlock textBlock = new ClaudeResponse.ContentBlock();
        textBlock.setType("text");
        textBlock.setText("Hello, this is a test response");
        java.util.List<ClaudeResponse.ContentBlock> contentList = new java.util.ArrayList<>();
        contentList.add(textBlock);
        response.setContent(contentList);
    }

    /**
     * 测试获取文本内容
     */
    @Test
    public void testGetTextContent() {
        log.info("========== 测试：获取文本内容 ==========");

        String textContent = response.getTextContent();
        assertNotNull(textContent, "文本内容不应为空");
        assertEquals("Hello, this is a test response", textContent, "文本内容应匹配");

        log.info("文本内容：{}", textContent);
    }

    /**
     * 测试获取工具调用块
     */
    @Test
    public void testGetToolUseBlocks() {
        log.info("========== 测试：获取工具调用块 ==========");

        // 添加工具调用块
        ClaudeResponse.ContentBlock toolBlock = new ClaudeResponse.ContentBlock();
        toolBlock.setType("tool_use");
        toolBlock.setToolUseId("tool_123");
        toolBlock.setToolName("read_file");
        toolBlock.setToolInput(new JSONObject().set("path", "/tmp/test.txt"));

        response.getContent().add(toolBlock);

        List<ClaudeResponse.ContentBlock> toolBlocks = response.getToolUseBlocks();
        assertNotNull(toolBlocks, "工具块不应为空");
        assertEquals(1, toolBlocks.size(), "应有 1 个工具块");
        assertEquals("read_file", toolBlocks.get(0).getToolName(), "工具名称应匹配");

        log.info("找到 {} 个工具块", toolBlocks.size());
    }

    /**
     * 测试检查是否有工具调用
     */
    @Test
    public void testHasToolUse() {
        log.info("========== 测试：检查是否有工具调用 ==========");

        assertFalse(response.hasToolUse(), "初始时不应有工具调用");

        // 添加工具调用块
        ClaudeResponse.ContentBlock toolBlock = new ClaudeResponse.ContentBlock();
        toolBlock.setType("tool_use");
        toolBlock.setToolUseId("tool_123");
        toolBlock.setToolName("read_file");
        response.getContent().add(toolBlock);

        assertTrue(response.hasToolUse(), "添加后应有工具调用");

        log.info("工具调用检查完成");
    }

    /**
     * 测试获取思考块
     */
    @Test
    public void testGetThinkingBlocks() {
        log.info("========== 测试：获取思考块 ==========");

        // 添加思考块
        ClaudeResponse.ContentBlock thinkingBlock = new ClaudeResponse.ContentBlock();
        thinkingBlock.setType("thinking");
        thinkingBlock.setThinking("Let me think about this problem...");
        response.getContent().add(thinkingBlock);

        List<ClaudeResponse.ContentBlock> thinkingBlocks = response.getThinkingBlocks();
        assertNotNull(thinkingBlocks, "思考块不应为空");
        assertEquals(1, thinkingBlocks.size(), "应有 1 个思考块");

        log.info("找到 {} 个思考块", thinkingBlocks.size());
    }

    /**
     * 测试获取思考内容
     */
    @Test
    public void testGetThinkingContent() {
        log.info("========== 测试：获取思考内容 ==========");

        ClaudeResponse.ContentBlock thinkingBlock = new ClaudeResponse.ContentBlock();
        thinkingBlock.setType("thinking");
        thinkingBlock.setThinking("Analyzing the problem...");
        response.getContent().add(thinkingBlock);

        String thinkingContent = response.getThinkingContent();
        assertNotNull(thinkingContent, "思考内容不应为空");
        assertEquals("Analyzing the problem...", thinkingContent, "思考内容应匹配");

        log.info("思考内容：{}", thinkingContent);
    }

    /**
     * 测试检查是否有思考内容
     */
    @Test
    public void testHasThinking() {
        log.info("========== 测试：检查是否有思考内容 ==========");

        assertFalse(response.hasThinking(), "初始时不应有思考内容");

        ClaudeResponse.ContentBlock thinkingBlock = new ClaudeResponse.ContentBlock();
        thinkingBlock.setType("thinking");
        thinkingBlock.setThinking("Thinking...");
        response.getContent().add(thinkingBlock);

        assertTrue(response.hasThinking(), "添加后应有思考内容");

        log.info("思考内容检查完成");
    }

    /**
     * 测试检查是否完成
     */
    @Test
    public void testIsComplete() {
        log.info("========== 测试：检查是否完成 ==========");

        response.setStopReason("end_turn");
        assertTrue(response.isComplete(), "end_turn 时应为完成状态");

        response.setStopReason("tool_use");
        assertFalse(response.isComplete(), "工具_use 时不应为完成状态");

        log.info("完成状态检查通过");
    }

    /**
     * 测试检查是否因为 Token 超限
     */
    @Test
    public void testIsMaxTokensReached() {
        log.info("========== 测试：检查是否达到 Token 上限 ==========");

        response.setStopReason("max_tokens");
        assertTrue(response.isMaxTokensReached(), "应为达到 Token 上限");

        response.setStopReason("end_turn");
        assertFalse(response.isMaxTokensReached(), "不应为达到 Token 上限");

        log.info("Token 上限检查通过");
    }

    /**
     * 测试 Token 使用统计
     */
    @Test
    public void testTokenUsage() {
        log.info("========== 测试：Token 使用统计 ==========");

        ClaudeResponse.Usage usage = response.getUsage();
        assertNotNull(usage, "使用统计不应为空");
        assertEquals(100, usage.getInputTokens(), "输入 Token 应为 100");
        assertEquals(50, usage.getOutputTokens(), "输出 Token 应为 50");
        assertEquals(150, usage.getTotalTokens(), "总 Token 应为 150");

        log.info("输入 Token：{}，输出 Token：{}，总计：{}",
                usage.getInputTokens(), usage.getOutputTokens(), usage.getTotalTokens());
    }

    /**
     * 测试内容块类型检查
     */
    @Test
    public void testContentBlockTypeChecks() {
        log.info("========== 测试：内容块类型检查 ==========");

        ClaudeResponse.ContentBlock textBlock = new ClaudeResponse.ContentBlock();
        textBlock.setType("text");
        assertTrue(textBlock.isText(), "应为文本类型");
        assertFalse(textBlock.isToolUse(), "不应为工具调用类型");
        assertFalse(textBlock.isThinking(), "不应为思考类型");

        ClaudeResponse.ContentBlock toolBlock = new ClaudeResponse.ContentBlock();
        toolBlock.setType("tool_use");
        assertFalse(toolBlock.isText(), "不应为文本类型");
        assertTrue(toolBlock.isToolUse(), "应为工具调用类型");
        assertFalse(toolBlock.isThinking(), "不应为思考类型");

        ClaudeResponse.ContentBlock thinkingBlock = new ClaudeResponse.ContentBlock();
        thinkingBlock.setType("thinking");
        assertFalse(thinkingBlock.isText(), "不应为文本类型");
        assertFalse(thinkingBlock.isToolUse(), "不应为工具调用类型");
        assertTrue(thinkingBlock.isThinking(), "应为思考类型");

        log.info("内容块类型检查通过");
    }

    /**
     * 测试多个内容块
     */
    @Test
    public void testMultipleContentBlocks() {
        log.info("========== 测试：多个内容块 ==========");

        response.getContent().clear();

        // 添加思考块
        ClaudeResponse.ContentBlock thinkingBlock = new ClaudeResponse.ContentBlock();
        thinkingBlock.setType("thinking");
        thinkingBlock.setThinking("Thinking about the problem...");
        response.getContent().add(thinkingBlock);

        // 添加文本块
        ClaudeResponse.ContentBlock textBlock = new ClaudeResponse.ContentBlock();
        textBlock.setType("text");
        textBlock.setText("Here's my answer...");
        response.getContent().add(textBlock);

        // 添加工具调用块
        ClaudeResponse.ContentBlock toolBlock = new ClaudeResponse.ContentBlock();
        toolBlock.setType("tool_use");
        toolBlock.setToolUseId("tool_456");
        toolBlock.setToolName("write_file");
        response.getContent().add(toolBlock);

        assertEquals(3, response.getContent().size(), "应有 3 个内容块");
        assertEquals(1, response.getThinkingBlocks().size(), "应有 1 个思考块");
        assertEquals(1, response.getContent().stream()
                .filter(ClaudeResponse.ContentBlock::isText).count(), "应有 1 个文本块");
        assertEquals(1, response.getToolUseBlocks().size(), "应有 1 个工具块");

        log.info("多个内容块测试通过");
    }

    /**
     * 测试空内容
     */
    @Test
    public void testEmptyContent() {
        log.info("========== 测试：空内容 ==========");

        response.setContent(java.util.Collections.emptyList());

        String textContent = response.getTextContent();
        assertEquals("", textContent, "文本内容应为空");

        List<ClaudeResponse.ContentBlock> toolBlocks = response.getToolUseBlocks();
        assertTrue(toolBlocks.isEmpty(), "工具块应为空");

        assertFalse(response.hasToolUse(), "不应有工具调用");
        assertFalse(response.hasThinking(), "不应有思考内容");

        log.info("空内容测试通过");
    }

    /**
     * 测试缓存 Token 统计
     */
    @Test
    public void testCacheTokenUsage() {
        log.info("========== 测试：缓存 Token 统计 ==========");

        ClaudeResponse.Usage usage = response.getUsage();
        usage.setCacheCreationInputTokens(10);
        usage.setCacheReadInputTokens(20);

        assertEquals(10, usage.getCacheCreationInputTokens(), "缓存创建 Token 应为 10");
        assertEquals(20, usage.getCacheReadInputTokens(), "缓存读取 Token 应为 20");

        log.info("缓存 Token - 创建：{}，读取：{}",
                usage.getCacheCreationInputTokens(), usage.getCacheReadInputTokens());
    }
}
