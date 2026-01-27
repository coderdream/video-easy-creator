package com.coderdream.util.claudecode.tool;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 工具注册表
 * 用于管理和执行工具
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ToolRegistry {

    /**
     * 工具定义映射
     */
    private Map<String, ToolDefinition> toolDefinitions;

    /**
     * 工具执行器映射
     */
    private Map<String, Function<JSONObject, String>> toolExecutors;

    /**
     * 构造函数
     */
    public ToolRegistry() {
        this.toolDefinitions = new HashMap<>();
        this.toolExecutors = new HashMap<>();
    }

    /**
     * 注册工具
     *
     * @param name       工具名称
     * @param definition 工具定义
     * @param executor   工具执行器
     * @return 当前对象（支持链式调用）
     */
    public ToolRegistry registerTool(String name, ToolDefinition definition,
                                     Function<JSONObject, String> executor) {
        this.toolDefinitions.put(name, definition);
        this.toolExecutors.put(name, executor);
        log.info("工具已注册: {}", name);
        return this;
    }

    /**
     * 获取工具定义
     *
     * @param name 工具名称
     * @return 工具定义，如果不存在则返回 null
     */
    public ToolDefinition getTool(String name) {
        return this.toolDefinitions.get(name);
    }

    /**
     * 获取所有工具定义
     *
     * @return 工具定义列表
     */
    public List<ToolDefinition> getAllTools() {
        return new ArrayList<>(this.toolDefinitions.values());
    }

    /**
     * 获取所有工具定义的 JSON 数组
     *
     * @return JSON 对象列表
     */
    public List<JSONObject> getAllToolsAsJson() {
        List<JSONObject> tools = new ArrayList<>();
        for (ToolDefinition definition : this.toolDefinitions.values()) {
            tools.add(definition.toJson());
        }
        return tools;
    }

    /**
     * 检查工具是否存在
     *
     * @param name 工具名称
     * @return true 如果工具存在
     */
    public boolean hasTool(String name) {
        return this.toolDefinitions.containsKey(name);
    }

    /**
     * 执行工具
     *
     * @param name  工具名称
     * @param input 工具输入参数
     * @return 工具执行结果
     * @throws IllegalArgumentException 如果工具不存在
     */
    public String executeTool(String name, JSONObject input) {
        if (!this.toolExecutors.containsKey(name)) {
            String errorMsg = "Tool not found: " + name;
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            long startTime = System.currentTimeMillis();
            Function<JSONObject, String> executor = this.toolExecutors.get(name);
            String result = executor.apply(input);
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("工具已执行：{} ({}ms)", name, executionTime);
            return result;
        } catch (Exception e) {
            log.error("工具执行失败: {}", name, e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取工具数量
     *
     * @return 工具数量
     */
    public int getToolCount() {
        return this.toolDefinitions.size();
    }

    /**
     * 获取所有工具名称
     *
     * @return 工具名称列表
     */
    public List<String> getToolNames() {
        return new ArrayList<>(this.toolDefinitions.keySet());
    }

    /**
     * 清空所有工具
     */
    public void clear() {
        this.toolDefinitions.clear();
        this.toolExecutors.clear();
        log.info("工具注册表已清空");
    }

    /**
     * 获取工具注册表的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ToolRegistry{" +
                "toolCount=" + this.toolDefinitions.size() +
                ", tools=" + this.toolDefinitions.keySet() +
                '}';
    }
}
