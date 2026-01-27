package com.coderdream.util.claudecode;

import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.claudecode.tool.ToolExecutor;
import com.coderdream.util.claudecode.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude 工具调用执行器测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeToolExecutorTest {

    private ClaudeApiClient apiClient;
    private ToolRegistry toolRegistry;
    private ClaudeToolExecutor toolExecutor;

    @BeforeEach
    public void setUp() {
        apiClient = new ClaudeApiClient();
        toolRegistry = ToolExecutor.createDefaultRegistry();
        toolExecutor = new ClaudeToolExecutor(apiClient, toolRegistry);

        log.info("测试设置完成");
        log.info("可用工具: {}", toolRegistry.getToolNames());
    }

    /**
     * 测试工具注册表
     */
    @Test
    public void testToolRegistry() {
        log.info("========== 测试：工具注册表 ==========");

        assertNotNull(toolRegistry, "工具注册表不应为空");
        assertEquals(5, toolRegistry.getToolCount(), "应有 5 个工具");

        assertTrue(toolRegistry.hasTool("list_files"), "应有 list_files 工具");
        assertTrue(toolRegistry.hasTool("read_file"), "应有 read_file 工具");
        assertTrue(toolRegistry.hasTool("write_file"), "应有write_file 工具");
        assertTrue(toolRegistry.hasTool("execute_command"), "应有 execute_command 工具");
        assertTrue(toolRegistry.hasTool("grep"), "应有 grep 工具");

        log.info("工具注册表测试通过");
    }

    /**
     * 测试工具定义
     */
    @Test
    public void testToolDefinitions() {
        log.info("========== 测试： 工具 定义s ==========");

        assertNotNull(toolRegistry.getTool("list_files"), "list_files 定义 不应为空");
        assertNotNull(toolRegistry.getTool("read_file"), "read_file 定义 不应为空");
        assertNotNull(toolRegistry.getTool("write_file"), "write_file 定义 不应为空");
        assertNotNull(toolRegistry.getTool("execute_command"), "execute_command 定义 不应为空");
        assertNotNull(toolRegistry.getTool("grep"), "grep 定义 不应为空");

        log.info("工具 定义s 测试通过");
    }

    /**
     * 测试工具执行器初始化
     */
    @Test
    public void testToolExecutorInitialization() {
        log.info("========== 测试： 工具 执行器 初始ization ==========");

        assertNotNull(toolExecutor, "工具 执行器 不应为空");
        assertNotNull(toolExecutor.getApiClient(), "API client 不应为空");
        assertNotNull(toolExecutor.getToolRegistry(), "工具注册表不应为空");

        log.info("工具 执行器 初始ization 测试通过");
    }

    /**
     * 测试工具调用循环（简单任务）
     */
    @Test
    public void testSimpleToolLoop() {
        log.info("========== 测试： Simple 工具 Loop ==========");

        String prompt = "请告诉我当前工作目录中有哪些文件。";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（文件操作）
     */
    @Test
    public void testFileOperationToolLoop() {
        log.info("========== 测试： File Operation 工具 Loop ==========");

        String prompt = "请创建一个名为 test_claude.txt 的文件，内容为 'Hello from Claude Tool'，然后读取这个文件的内容。";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（命令执行）
     */
    @Test
    public void testCommandExecutionToolLoop() {
        log.info("========== 测试： Command Execution 工具 Loop ==========");

        String prompt = "请执行 'mvn --version' 命令并告诉我结果。";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（搜索操作）
     */
    @Test
    public void testGrepToolLoop() {
        log.info("========== 测试： Grep 工具 Loop ==========");

        String prompt = "请在当前目录的 pom.xml 文件中搜索包含 'hutool' 的行。";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（复杂任务）
     */
    @Test
    public void testComplexToolLoop() {
        log.info("========== 测试： Complex 工具 Loop ==========");

        String prompt = "请完成以下任务：\n" +
                "1. 列出当前目录中的所有文件\n" +
                "2. 创建一个名为 claude_test.txt 的文件\n" +
                "3. 在文件中写入一些内容\n" +
                "4. 读取文件内容并告诉我";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（自定义系统提示）
     */
    @Test
    public void testToolLoopWithCustomSystemPrompt() {
        log.info("========== 测试： 工具 Loop with Custom System Prompt ==========");

        String prompt = "请列出当前目录中的所有文件。";
        String systemPrompt = "你是一个专业的文件管理助手。使用提供的工具来帮助用户完成任务。" +
                "始终用中文解释你在做什么，并提供清晰的结果。";

        try {
            String result = toolExecutor.executeWithTools(prompt, systemPrompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（错误处理）
     */
    @Test
    public void testToolLoopErrorHandling() {
        log.info("========== 测试： 工具 Loop Error Handling ==========");

        String prompt = "请读取一个不存在的文件 /nonexistent/file.txt。";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull("Result should not be null", result);
            // 结果应该包含错误信息
            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试工具调用循环（多轮交互）
     */
    @Test
    public void testMultiRoundToolLoop() {
        log.info("========== 测试： Multi-round 工具 Loop ==========");

        String prompt = "请执行以下步骤：\n" +
                "1. 首先，列出当前目录中的文件\n" +
                "2. 然后，创建一个新文件 multi_round_test.txt\n" +
                "3. 接着，在文件中写入内容\n" +
                "4. 最后，读取文件内容并总结";

        try {
            String result = toolExecutor.executeWithTools(prompt);

            assertNotNull(result, "Result 不应为空");
            assertFalse(result.isEmpty(), "Result 不应为空");

            log.info("工具 loop result:\n{}", result);
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
}
