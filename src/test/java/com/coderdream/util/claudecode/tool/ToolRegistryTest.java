package com.coderdream.util.claudecode.tool;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具注册表测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ToolRegistryTest {

    private ToolRegistry registry;
    private ToolExecutor executor;

    @BeforeEach
    public void setUp() {
        registry = new ToolRegistry();
        executor = new ToolExecutor();
    }

    /**
     * 测试工具注册
     */
    @Test
    public void testToolRegistration() {
        log.info("========== 测试：工具注册 ==========");

        ToolDefinition definition = new ToolDefinition("test_tool", "A test 工具");
        registry.registerTool("test_tool", definition, input -> "Test result");

        assertTrue(registry.hasTool("test_tool"), "工具应已注册");
        assertEquals(1, registry.getToolCount(), "工具数量应为 1");

        log.info("工具注册测试通过");
    }

    /**
     * 测试获取工具定义
     */
    @Test
    public void testGetToolDefinition() {
        log.info("========== 测试：获取工具定义 ==========");

        ToolDefinition definition = new ToolDefinition("test_tool", "A test 工具");
        registry.registerTool("test_tool", definition, input -> "Test result");

        ToolDefinition retrieved = registry.getTool("test_tool");
        assertNotNull(retrieved, "工具定义不应为空");
        assertEquals("test_tool", retrieved.getName(), "工具名称应匹配");
        assertEquals("A test tool", retrieved.getDescription(), "工具描述应匹配");

        log.info("获取工具定义测试通过");
    }

    /**
     * 测试获取所有工具
     */
    @Test
    public void testGetAllTools() {
        log.info("========== 测试：获取所有工具 ==========");

        registry.registerTool("tool1", new ToolDefinition("tool1", "Tool 1"), input -> "Result 1");
        registry.registerTool("tool2", new ToolDefinition("tool2", "Tool 2"), input -> "Result 2");
        registry.registerTool("tool3", new ToolDefinition("tool3", "Tool 3"), input -> "Result 3");

        List<ToolDefinition> tools = registry.getAllTools();
        assertEquals(3, tools.size(), "应有 3 个工具");

        log.info("获取所有工具测试通过");
    }

    /**
     * 测试工具执行
     */
    @Test
    public void testToolExecution() {
        log.info("========== 测试：工具执行 ==========");

        registry.registerTool("echo", new ToolDefinition("echo", "Echo tool"),
                input -> "Echo: " + input.getStr("message"));

        JSONObject input = new JSONObject();
        input.set("message", "Hello");

        String result = registry.executeTool("echo", input);
        assertEquals("Echo: Hello", result, "结果应匹配");

        log.info("工具执行测试通过");
    }

    /**
     * 测试工具不存在的情况
     */
    @Test
    public void testNonexistentTool() {
        log.info("========== 测试：不存在的工具 ==========");

        assertFalse(registry.hasTool("nonexistent"), "工具不应存在");

        try {
            registry.executeTool("nonexistent", new JSONObject());
            fail("不存在的工具应抛出异常");
        } catch (IllegalArgumentException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }
    }

    /**
     * 测试默认工具注册表
     */
    @Test
    public void testDefaultRegistry() {
        log.info("========== 测试：默认工具注册表 ==========");

        ToolRegistry defaultRegistry = ToolExecutor.createDefaultRegistry();

        assertNotNull(defaultRegistry, "默认注册表不应为空");
        assertEquals(5, defaultRegistry.getToolCount(), "应有 5 个工具");

        assertTrue(defaultRegistry.hasTool("list_files"), "应有 list_files");
        assertTrue(defaultRegistry.hasTool("read_file"), "应有 read_file");
        assertTrue(defaultRegistry.hasTool("write_file"), "应有 write_file");
        assertTrue(defaultRegistry.hasTool("execute_command"), "应有 execute_command");
        assertTrue(defaultRegistry.hasTool("grep"), "应有 grep");

        log.info("默认注册表测试通过");
    }

    /**
     * 测试工具名称列表
     */
    @Test
    public void testGetToolNames() {
        log.info("========== 测试：获取工具名称列表 ==========");

        registry.registerTool("tool1", new ToolDefinition("tool1", "Tool 1"), input -> "Result 1");
        registry.registerTool("tool2", new ToolDefinition("tool2", "Tool 2"), input -> "Result 2");

        List<String> names = registry.getToolNames();
        assertEquals(2, names.size(), "应有 2 个工具名称");
        assertTrue(names.contains("tool1"), "应包含 工具1");
        assertTrue(names.contains("tool2"), "应包含 工具2");

        log.info("获取工具名称列表测试通过");
    }

    /**
     * 测试工具 JSON 转换
     */
    @Test
    public void testToolJsonConversion() {
        log.info("========== 测试：工具 JSON 转换 ==========");

        ToolRegistry defaultRegistry = ToolExecutor.createDefaultRegistry();
        List<JSONObject> toolsJson = defaultRegistry.getAllToolsAsJson();

        assertNotNull(toolsJson, "工具 JSON 不应为空");
        assertEquals(5, toolsJson.size(), "应有 5 个工具");

        for (JSONObject toolJson : toolsJson) {
            assertNotNull(toolJson.getStr("name"), "工具名称不应为空");
            assertNotNull(toolJson.getStr("description"), "工具描述不应为空");
            assertNotNull(toolJson.get("input_schema"), "工具输入模式不应为空");
        }

        log.info("工具 JSON 转换测试通过");
    }

    /**
     * 测试清空工具注册表
     */
    @Test
    public void testClearRegistry() {
        log.info("========== 测试：清空工具注册表 ==========");

        registry.registerTool("tool1", new ToolDefinition("tool1", "Tool 1"), input -> "Result 1");
        registry.registerTool("tool2", new ToolDefinition("tool2", "Tool 2"), input -> "Result 2");

        assertEquals(2, registry.getToolCount(), "应有 2 个工具");

        registry.clear();

        assertEquals(0, registry.getToolCount(), "清空后应有 0 个工具");

        log.info("清空注册表测试通过");
    }

    /**
     * 测试工具执行异常处理
     */
    @Test
    public void testToolExecutionException() {
        log.info("========== 测试：工具执行异常处理 ==========");

        registry.registerTool("error_tool", new ToolDefinition("error_tool", "Error tool"),
                input -> {
                    throw new RuntimeException("Tool error");
                });

        try {
            registry.executeTool("error_tool", new JSONObject());
            fail("应抛出异常");
        } catch (RuntimeException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }
    }

    /**
     * 测试工具定义的链式调用
     */
    @Test
    public void testToolDefinitionChaining() {
        log.info("========== 测试：工具定义的链式调用 ==========");

        ToolDefinition definition = new ToolDefinition()
                .setName("chained_tool")
                .setDescription("A chained tool")
                .setInputSchemaType("object")
                .addProperty("param1", "string", "Parameter 1")
                .addProperty("param2", "number", "Parameter 2")
                .setRequired("param1", "param2");

        assertNotNull(definition, "定义不应为空");
        assertEquals("chained_tool", definition.getName(), "名称应匹配");

        log.info("工具定义链式调用测试通过");
    }
}
