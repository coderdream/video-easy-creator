# MiniMax API 客户端使用说明

## 概述

MiniMaxApiClient 是一个用于与 MiniMax Chat API 通信的 Java 客户端，参考 ClaudeApiClient 的设计模式实现。

## 环境变量配置

在使用 MiniMaxApiClient 之前，需要配置以下环境变量：

### Windows 系统

```cmd
# 设置 MiniMax API 密钥
setx MINIMAX_API_KEY "your_api_key_here"

# 设置 MiniMax Group ID
setx MINIMAX_GROUP_ID "your_group_id_here"

# 可选：设置自定义 API 基础 URL（默认为 https://api.minimax.chat）
setx MINIMAX_BASE_URL "https://api.minimax.chat"
```

### Linux/Mac 系统

```bash
# 在 ~/.bashrc 或 ~/.zshrc 中添加
export MINIMAX_API_KEY="your_api_key_here"
export MINIMAX_GROUP_ID="your_group_id_here"
export MINIMAX_BASE_URL="https://api.minimax.chat"  # 可选

# 使配置生效
source ~/.bashrc  # 或 source ~/.zshrc
```

### IDEA 配置

在 IDEA 中运行测试时，可以在 Run Configuration 中配置环境变量：

1. 打开 Run -> Edit Configurations
2. 选择对应的测试配置
3. 在 Environment variables 中添加：
   ```
   MINIMAX_API_KEY=your_api_key_here;MINIMAX_GROUP_ID=your_group_id_here
   ```

## 核心类说明

### 1. MiniMaxMessage

消息对象，支持三种角色：
- `system`: 系统消息（设置 AI 行为）
- `user`: 用户消息
- `assistant`: 助手消息（用于多轮对话）

```java
// 创建消息的三种方式
MiniMaxMessage msg1 = MiniMaxMessage.system("你是一个专业的翻译助手");
MiniMaxMessage msg2 = MiniMaxMessage.user("请翻译这段文字");
MiniMaxMessage msg3 = MiniMaxMessage.assistant("好的，我来帮你翻译");
```

### 2. MiniMaxRequest

请求对象，支持链式调用：

```java
MiniMaxRequest request = new MiniMaxRequest()
    .setModel("abab6.5s-chat")           // 设置模型
    .setMaxTokens(2048)                   // 最大 token 数
    .setTemperature(0.7)                  // 温度参数 (0.0-1.0)
    .setTopP(0.95)                        // 核采样参数
    .addUserMessage("你好");              // 添加用户消息
```

支持的模型：
- `abab6.5s-chat`: 速度快，适合简单任务
- `abab6.5-chat`: 平衡性能和质量
- `abab5.5-chat`: 高质量输出

### 3. MiniMaxResponse

响应对象，包含：
- `id`: 请求 ID
- `model`: 使用的模型
- `choices`: 生成的选择列表
- `usage`: Token 使用统计
- `finishReason`: 完成原因（stop/length）

```java
// 获取响应内容
String text = response.getFirstChoiceText();
String finishReason = response.getFinishReason();
int totalTokens = response.getUsage().getTotalTokensCount();
```

### 4. MiniMaxApiClient

API 客户端，负责发送请求和解析响应：

```java
// 方式1：从环境变量读取配置
MiniMaxApiClient client = new MiniMaxApiClient();

// 方式2：自定义配置
MiniMaxApiClient client = new MiniMaxApiClient(
    "https://api.minimax.chat",
    "your_api_key",
    "your_group_id"
);

// 发送请求
MiniMaxResponse response = client.sendMessage(request);
```

## 使用示例

### 示例 1：简单对话

```java
MiniMaxApiClient client = new MiniMaxApiClient();

MiniMaxRequest request = new MiniMaxRequest()
    .setModel(CdConstants.MINIMAX_DEFAULT_MODEL)
    .setMaxTokens(1024)
    .addUserMessage("你好，请介绍一下你自己。");

MiniMaxResponse response = client.sendMessage(request);
System.out.println(response.getFirstChoiceText());
```

### 示例 2：翻译任务

```java
MiniMaxApiClient client = new MiniMaxApiClient();

MiniMaxRequest request = new MiniMaxRequest()
    .setModel("abab6.5s-chat")
    .setTemperature(0.3)  // 较低温度，输出更确定
    .addSystemMessage("你是一个专业的英语翻译助手。")
    .addUserMessage("请将以下英文翻译成中文：The quick brown fox jumps over the lazy dog.");

MiniMaxResponse response = client.sendMessage(request);
System.out.println("翻译结果: " + response.getFirstChoiceText());
```

### 示例 3：多轮对话

```java
MiniMaxApiClient client = new MiniMaxApiClient();

MiniMaxRequest request = new MiniMaxRequest()
    .setModel("abab6.5-chat")
    .addUserMessage("什么是人工智能？")
    .addAssistantMessage("人工智能是计算机科学的一个分支...")
    .addUserMessage("那机器学习和深度学习有什么区别？");

MiniMaxResponse response = client.sendMessage(request);
System.out.println(response.getFirstChoiceText());
```

### 示例 4：词汇解释（BBC 项目场景）

```java
MiniMaxApiClient client = new MiniMaxApiClient();

String vocabulary = "cephalopod\nthe group of animals to which the octopus belongs";

MiniMaxRequest request = new MiniMaxRequest()
    .setModel("abab6.5-chat")
    .setMaxTokens(2048)
    .addSystemMessage("你是一个英语词汇学习助手，请提供详细的中文解释和例句。")
    .addUserMessage("请解释以下词汇：\n" + vocabulary);

MiniMaxResponse response = client.sendMessage(request);
System.out.println(response.getFirstChoiceText());
```

## 常量配置

在 `CdConstants.java` 中定义了以下常量：

```java
// MiniMax API 基础 URL
public static final String MINIMAX_BASE_URL = "https://api.minimax.chat";

// MiniMax API 调用超时时间（毫秒）
public static final int MINIMAX_API_TIMEOUT = 120000;

// MiniMax API 最大重试次数
public static final int MINIMAX_MAX_RETRIES = 3;

// MiniMax API 重试延迟（毫秒）
public static final int MINIMAX_RETRY_DELAY = 2000;

// MiniMax 默认模型
public static final String MINIMAX_DEFAULT_MODEL = "abab6.5s-chat";
```

## 错误处理

客户端会自动处理以下错误：
- 空响应
- JSON 解析错误
- API 错误响应
- 网络超时

所有错误都会抛出 `RuntimeException`，包含详细的错误信息。

```java
try {
    MiniMaxResponse response = client.sendMessage(request);
    // 处理响应
} catch (RuntimeException e) {
    log.error("API 调用失败: {}", e.getMessage());
    // 错误处理逻辑
}
```

## 日志输出

客户端使用 Slf4j 记录详细的日志信息：
- 请求详情（URL、Headers、Body）
- 响应详情（模型、长度、内容）
- Token 使用统计
- 错误信息

## 测试

运行测试类：

```bash
# 运行所有测试
mvn test -Dtest=MiniMaxApiClientTest

# 运行单个测试方法
mvn test -Dtest=MiniMaxApiClientTest#testSendMessage
```

## 注意事项

1. **API 密钥安全**：不要将 API 密钥硬编码在代码中，始终使用环境变量
2. **Group ID**：MiniMax API 需要 Group ID 参数，请确保已配置
3. **超时设置**：默认超时为 120 秒，可根据需要调整
4. **Token 限制**：注意模型的 token 限制，避免超出最大值
5. **温度参数**：
   - 0.0-0.3：适合翻译、总结等需要确定性的任务
   - 0.7-0.9：适合创意写作、对话等需要多样性的任务

## 与 ClaudeApiClient 的对比

| 特性 | ClaudeApiClient | MiniMaxApiClient |
|------|----------------|------------------|
| 认证方式 | x-api-key header | Authorization Bearer |
| 特殊参数 | anthropic-version | GroupId (URL 参数) |
| 消息格式 | role + content | sender_type + text |
| 响应格式 | content blocks | choices + message |
| 工具调用 | 支持 | 不支持 |
| 思考模式 | 支持 | 不支持 |

## 获取 API 密钥

1. 访问 MiniMax 官网：https://www.minimaxi.com/
2. 注册账号并登录
3. 进入控制台获取 API Key 和 Group ID
4. 配置到环境变量中

## 相关文档

- MiniMax API 官方文档：https://www.minimaxi.com/document/guides/chat-model/V2
- 项目 CLAUDE.md：项目整体架构说明
- ClaudeApiClient.java：参考实现
