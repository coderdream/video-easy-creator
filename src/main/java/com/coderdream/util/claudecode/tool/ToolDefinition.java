package com.coderdream.util.claudecode.tool;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具定义
 * 用于定义 Claude 可以调用的工具
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Data
@Accessors(chain = true)
public class ToolDefinition {

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 输入参数 Schema
     */
    private Map<String, Object> inputSchema;

    /**
     * 构造函数
     */
    public ToolDefinition() {
        this.inputSchema = new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param name        工具名称
     * @param description 工具描述
     */
    public ToolDefinition(String name, String description) {
        this.name = name;
        this.description = description;
        this.inputSchema = new HashMap<>();
    }

    /**
     * 设置输入 Schema 类型
     *
     * @param type 类型（通常为 "object"）
     * @return 当前对象（支持链式调用）
     */
    public ToolDefinition setInputSchemaType(String type) {
        this.inputSchema.put("type", type);
        return this;
    }

    /**
     * 添加输入参数属性
     *
     * @param propertyName 属性名
     * @param propertyType 属性类型
     * @param description  属性描述
     * @return 当前对象（支持链式调用）
     */
    public ToolDefinition addProperty(String propertyName, String propertyType, String description) {
        if (!this.inputSchema.containsKey("properties")) {
            this.inputSchema.put("properties", new HashMap<String, Object>());
        }

        Map<String, Object> properties = (Map<String, Object>) this.inputSchema.get("properties");
        Map<String, Object> property = new HashMap<>();
        property.put("type", propertyType);
        property.put("description", description);
        properties.put(propertyName, property);

        return this;
    }

    /**
     * 设置必需的参数
     *
     * @param requiredParams 必需参数列表
     * @return 当前对象（支持链式调用）
     */
    public ToolDefinition setRequired(String... requiredParams) {
        this.inputSchema.put("required", requiredParams);
        return this;
    }

    /**
     * 转换为 JSON 对象
     *
     * @return JSON 对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.set("name", this.name);
        json.set("description", this.description);
        json.set("input_schema", this.inputSchema);
        return json;
    }

    /**
     * 获取工具的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ToolDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
