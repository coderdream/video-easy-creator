# ClaudeCodeUtil 开发方案

**文档版本**: 1.0
**创建日期**: 2026-01-25
**开发阶段**: 第一阶段 - 基础框架

---

## 1. 方案概述

本方案详细描述了 `ClaudeCodeUtil` 的实现策略，分为四个阶段逐步推进，确保代码质量和功能完整性。

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                   应用层 (Application)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Translator   │  │ CodeGenerator│  │ CustomApp    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   业务层 (Business)                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │         ClaudeCodeUtil (主工具类)                │  │
│  │  - 消息调用                                      │  │
│  │  - 工具调用循环                                  │  │
│  │  - 会话管理                                      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   核心层 (Core)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ ClaudeApiClient  │  │ToolExecutor  │  │PromptMgr     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   基础层 (Foundation)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ ClaudeMessage│  │ClaudeResponse│  │ ToolRegistry │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   外部服务 (External)                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Anthropic Messages API                   │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心类设计

#### 2.2.1 ClaudeApiClient（API 客户端）

**职责**: 管理 API 连接和请求发送

```java
public class ClaudeApiClient {
    // 配置管理
    private String baseUrl;
    private String authToken;
    private String apiVersion;
    private int timeout;

    // 核心方法
    public ClaudeResponse sendMessage(ClaudeRequest request);
    public ClaudeResponse sendMessageWithTools(ClaudeRequest request, List<ToolDefinition> tools);

    // 工具方法
    private void configureProxy();
    private void validateResponse(String response);
}
```

#### 2.2.2 ClaudeToolExecutor（工具执行器）

**职责**: 执行工具调用和管理工具循环

```java
public class ClaudeToolExecutor {
    private ToolRegistry toolRegistry;
    private ClaudeApiClient apiClient;

    // 核心方法
    public String executeWithTools(String prompt, String systemPrompt);
    private String executeTool(String toolName, JSONObject input);
    private void handleToolUseResponse(JSONArray content, List<JSONObject> messages);
}
```

#### 2.2.3 ClaudeSession（会话管理）

**职责**: 维护对话上下文和会话状态

```java
public class ClaudeSession {
    private String sessionId;
    private List<ClaudeMessage> messageHistory;
    private Map<String, Object> context;

    // 核心方法
    public void addMessage(ClaudeMessage message);
    public List<ClaudeMessage> getHistory();
    public void clearHistory();
    public void exportContext(String filePath);
}
```

#### 2.2.4 ToolRegistry（工具注册表）

**职责**: 管理可用工具的定义和执行

```java
public class ToolRegistry {
    private Map<String, ToolDefinition> tools;
    private Map<String, ToolExecutor> executors;

    // 核心方法
    public void registerTool(String name, ToolDefinition def, ToolExecutor executor);
    public ToolDefinition getTool(String name);
    public List<ToolDefinition> getAllTools();
    public String executeTool(String name, JSONObject input);
}
```

---

## 3. 第一阶段实现细节

### 3.1 项目结构创建

```bash
# 创建目录结构
mkdir -p src/main/java/com/coderdream/util/claudecode/tool
mkdir -p src/main/java/com/coderdream/util/claudecode/prompt
mkdir -p src/test/java/com/coderdream/util/claudecode
```

### 3.2 核心类实现

#### 3.2.1 ClaudeMessage.java

```java
@Data
@Accessors(chain = true)
public class ClaudeMessage {
    private String role;           // "user" 或 "assistant"
    private String content;        // 消息内容
    private List<JSONObject> contentArray;  // 复杂内容（包含工具调用）
    private Long timestamp;

    public static ClaudeMessage userMessage(String content) {
        return new ClaudeMessage()
            .setRole("user")
            .setContent(content)
            .setTimestamp(System.currentTimeMillis());
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.set("role", this.role);
        if (this.contentArray != null) {
            obj.set("content", this.contentArray);
        } else {
            obj.set("content", this.content);
        }
        return obj;
    }
}
```

#### 3.2.2 ClaudeResponse.java

```java
@Data
@Accessors(chain = true)
public class ClaudeResponse {
    private String id;
    private String model;
    private String stopReason;     // "end_turn", "tool_use", "max_tokens"
    private List<ContentBlock> content;
    private Usage usage;
    private Long timestamp;

    @Data
    @Accessors(chain = true)
    public static class ContentBlock {
        private String type;       // "text", "tool_use", "thinking"
        private String text;
        private String toolUseId;
        private String toolName;
        private JSONObject toolInput;
    }

    @Data
    @Accessors(chain = true)
    public static class Usage {
        private int inputTokens;
        private int outputTokens;
        private int cacheCreationInputTokens;
        private int cacheReadInputTokens;
    }

    public String getTextContent() {
        return content.stream()
            .filter(c -> "text".equals(c.type))
            .map(c -> c.text)
            .collect(Collectors.joining("\n"));
    }

    public List<ContentBlock> getToolUseBlocks() {
        return content.stream()
            .filter(c -> "tool_use".equals(c.type))
            .collect(Collectors.toList());
    }
}
```

#### 3.2.3 ClaudeApiClient.java

```java
@Slf4j
public class ClaudeApiClient {
    private static final String DEFAULT_VERSION = "2023-06-01";
    private static final int DEFAULT_TIMEOUT = 120000;

    private String baseUrl;
    private String authToken;
    private String apiVersion;
    private int timeout;
    private String proxyHost;
    private int proxyPort;

    public ClaudeApiClient() {
        this.baseUrl = System.getenv("ANTHROPIC_BASE_URL");
        this.authToken = System.getenv("ANTHROPIC_AUTH_TOKEN");
        this.apiVersion = DEFAULT_VERSION;
        this.timeout = DEFAULT_TIMEOUT;
        this.proxyHost = System.getenv("PROXY_HOST");
        this.proxyPort = Integer.parseInt(System.getenv("PROXY_PORT") != null ?
            System.getenv("PROXY_PORT") : "0");
    }

    public ClaudeResponse sendMessage(ClaudeRequest request) {
        JSONObject body = buildRequestBody(request);
        return executeRequest(body);
    }

    public ClaudeResponse sendMessageWithTools(ClaudeRequest request,
                                               List<ToolDefinition> tools) {
        JSONObject body = buildRequestBody(request);

        JSONArray toolsArray = new JSONArray();
        for (ToolDefinition tool : tools) {
            toolsArray.add(tool.toJson());
        }
        body.set("tools", toolsArray);

        return executeRequest(body);
    }

    private JSONObject buildRequestBody(ClaudeRequest request) {
        JSONObject body = new JSONObject();
        body.set("model", request.getModel());
        body.set("max_tokens", request.getMaxTokens());

        if (request.getTemperature() != null) {
            body.set("temperature", request.getTemperature());
        }

        if (request.getEffort() != null) {
            body.set("effort", request.getEffort());
        }

        if (request.isThinkingEnabled()) {
            JSONObject thinking = new JSONObject();
            thinking.set("type", "enabled");
            thinking.set("budget_tokens", request.getThinkingBudget());
            body.set("thinking", thinking);
        }

        if (request.getSystemPrompt() != null) {
            body.set("system", request.getSystemPrompt());
        }

        JSONArray messages = new JSONArray();
        for (ClaudeMessage msg : request.getMessages()) {
            messages.add(msg.toJson());
        }
        body.set("messages", messages);

        return body;
    }

    private ClaudeResponse executeRequest(JSONObject body) {
        try {
            HttpRequest httpRequest = HttpRequest.post(baseUrl + "/v1/messages")
                .header("x-api-key", authToken)
                .header("anthropic-version", apiVersion)
                .header("content-type", "application/json")
                .body(JSONUtil.toJsonStr(body))
                .timeout(timeout);

            if (proxyHost != null && proxyPort > 0) {
                httpRequest.setHttpProxy(proxyHost, proxyPort);
            }

            String responseStr = httpRequest.execute().body();
            log.debug("API Response: {}", responseStr);

            return parseResponse(responseStr);
        } catch (Exception e) {
            log.error("API request failed", e);
            throw new RuntimeException("Failed to call Anthropic API", e);
        }
    }

    private ClaudeResponse parseResponse(String responseStr) {
        JSONObject json = JSONUtil.parseObj(responseStr);

        if (json.containsKey("error")) {
            String errorMsg = json.getByPath("error.message").toString();
            throw new RuntimeException("API Error: " + errorMsg);
        }

        ClaudeResponse response = new ClaudeResponse();
        response.setId(json.getStr("id"));
        response.setModel(json.getStr("model"));
        response.setStopReason(json.getStr("stop_reason"));

        // 解析 content
        JSONArray contentArray = json.getJSONArray("content");
        List<ClaudeResponse.ContentBlock> contentBlocks = new ArrayList<>();

        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject block = contentArray.getJSONObject(i);
            ClaudeResponse.ContentBlock cb = new ClaudeResponse.ContentBlock();
            cb.setType(block.getStr("type"));

            if ("text".equals(cb.getType())) {
                cb.setText(block.getStr("text"));
            } else if ("tool_use".equals(cb.getType())) {
                cb.setToolUseId(block.getStr("id"));
                cb.setToolName(block.getStr("name"));
                cb.setToolInput(block.getJSONObject("input"));
            }

            contentBlocks.add(cb);
        }
        response.setContent(contentBlocks);

        // 解析 usage
        JSONObject usage = json.getJSONObject("usage");
        ClaudeResponse.Usage usageObj = new ClaudeResponse.Usage();
        usageObj.setInputTokens(usage.getInt("input_tokens"));
        usageObj.setOutputTokens(usage.getInt("output_tokens"));
        response.setUsage(usageObj);

        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
```

#### 3.2.4 CdConstants.java 更新

```java
public class CdConstants {
    // ... 现有常量 ...

    // Claude API 配置
    public static final String CLAUDE_MODEL_OPUS_45 = "claude-opus-4-5-20251101";
    public static final String CLAUDE_MODEL_SONNET_45 = "claude-sonnet-4-5-20250929";
    public static final String CLAUDE_MODEL_HAIKU_45 = "claude-haiku-4-5-20251001";

    public static final String CLAUDE_DEFAULT_MODEL = CLAUDE_MODEL_SONNET_45;
    public static final int CLAUDE_MAX_TOKENS = 4096;
    public static final int CLAUDE_THINKING_BUDGET = 2048;
    public static final String CLAUDE_EFFORT_HIGH = "high";
    public static final String CLAUDE_EFFORT_MEDIUM = "medium";
    public static final String CLAUDE_EFFORT_LOW = "low";
}
```

### 3.3 单元测试

#### 3.3.1 ClaudeApiClientTest.java

```java
@Slf4j
public class ClaudeApiClientTest {

    private ClaudeApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = new ClaudeApiClient();
    }

    @Test
    public void testSimpleMessage() {
        ClaudeRequest request = new ClaudeRequest()
            .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
            .setMaxTokens(1024)
            .setSystemPrompt("You are a helpful assistant.")
            .addMessage(ClaudeMessage.userMessage("Hello, Claude!"));

        ClaudeResponse response = apiClient.sendMessage(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertFalse(response.getContent().isEmpty());
        assertNotNull(response.getTextContent());
        log.info("Response: {}", response.getTextContent());
    }

    @Test
    public void testWithThinking() {
        ClaudeRequest request = new ClaudeRequest()
            .setModel(CdConstants.CLAUDE_MODEL_OPUS_45)
            .setMaxTokens(4096)
            .setThinkingEnabled(true)
            .setThinkingBudget(2048)
            .setSystemPrompt("You are a code expert.")
            .addMessage(ClaudeMessage.userMessage("Explain the quicksort algorithm"));

        ClaudeResponse response = apiClient.sendMessage(request);

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        log.info("Response: {}", response.getTextContent());
    }
}
```

---

## 4. 第二阶段计划

### 4.1 工具系统实现

1. **ToolDefinition** - 工具定义类
2. **ToolRegistry** - 工具注册表
3. **ClaudeToolExecutor** - 工具执行器
4. **内置工具实现**:
   - `list_files` - 列出目录
   - `read_file` - 读取文件
   - `write_file` - 写入文件
   - `execute_command` - 执行命令
   - `grep` - 文件搜索

### 4.2 工具调用循环

```
用户输入
   ↓
发送请求 + 工具定义
   ↓
Claude 返回响应
   ↓
检查是否有 tool_use
   ├─ 是 → 执行工具 → 构建工具结果消息 → 继续循环
   └─ 否 → 返回最终答案
```

---

## 5. 第三阶段计划

### 5.1 会话管理

- 实现 `ClaudeSession` 类
- 支持对话历史存储
- 支持上下文压缩
- 支持会话导出/导入

### 5.2 高级功能

- 支持流式响应
- 支持并发调用
- 实现请求缓存
- 实现错误重试

---

## 6. 第四阶段计划

### 6.1 应用场景实现

1. **翻译应用** (`TranslatorUtil`)
   - 基于资深翻译官 Prompt
   - 支持多种翻译场景
   - 质量验证

2. **代码生成应用** (`CodeGeneratorUtil`)
   - 代码生成
   - 代码审查
   - Bug 修复

### 6.2 文档和示例

- API 文档
- 使用示例
- 故障排查指南
- 性能调优建议

---

## 7. 开发规范

### 7.1 代码风格

- 使用 Lombok `@Data` 和 `@Accessors(chain = true)`
- 支持链式调用
- 详细的日志记录
- 完整的异常处理

### 7.2 命名规范

- 工具类: `*Util`
- 数据对象: `*Entity` 或 `*DTO`
- 执行器: `*Executor`
- 注册表: `*Registry`

### 7.3 测试规范

- 单元测试覆盖率 > 80%
- 使用 JUnit 4
- 使用 @Before/@After 进行初始化
- 详细的测试日志

---

## 8. 关键技术点

### 8.1 JSON 处理

使用 Hutool 的 `JSONUtil` 处理 JSON：

```java
// 解析
JSONObject json = JSONUtil.parseObj(jsonString);

// 构建
JSONObject obj = new JSONObject();
obj.set("key", "value");

// 转换
String jsonStr = JSONUtil.toJsonStr(obj);
```

### 8.2 HTTP 请求

使用 Hutool 的 `HttpRequest`：

```java
String response = HttpRequest.post(url)
    .header("key", "value")
    .body(bodyString)
    .timeout(60000)
    .execute()
    .body();
```

### 8.3 文件操作

使用 Hutool 的 `FileUtil`：

```java
// 读取
String content = FileUtil.readUtf8String(path);

// 写入
FileUtil.writeUtf8String(content, path);

// 列表
List<File> files = FileUtil.ls(path);
```

---

## 9. 风险管理

| 风险 | 概率 | 影响 | 缓解 |
|------|------|------|------|
| API 调用失败 | 中 | 高 | 实现重试机制 |
| Token 超限 | 中 | 高 | 实现上下文压缩 |
| 工具执行失败 | 中 | 中 | 实现错误恢复 |
| 性能不达标 | 低 | 中 | 实现缓存和优化 |

---

## 10. 时间表

| 阶段 | 任务 | 预计工作量 | 优先级 |
|------|------|----------|--------|
| 第一阶段 | 基础框架 | 3-4 天 | 高 |
| 第二阶段 | 工具调用 | 3-4 天 | 高 |
| 第三阶段 | 高级功能 | 2-3 天 | 中 |
| 第四阶段 | 应用场景 | 2-3 天 | 中 |

---

**方案审批**

| 角色 | 姓名 | 日期 | 签名 |
|------|------|------|------|
| 技术负责人 | - | - | - |
| 项目经理 | - | - | - |
