# MiniMax API 迁移解决方案

## 问题背景

项目当前使用 `api.ctwork.cn/api` 的 Claude API 中转服务，但该服务无响应。需要迁移到用户验证通过的 MiniMax API 中转服务。

## 解决方案

### 1. 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                   SixMinutesStepByStepWithMiniMaxTest        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   TranslateUtilWithMiniMax                   │
│  (复制 TranslateUtilWithClaude 的功能，替换 API 调用)         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     MiniMaxApiClient                         │
│  - sendMessage(ClaudeRequest)                               │
│  - buildRequestBody()                                       │
│  - executeRequest()                                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       ConfigUtil                             │
│  - 从 config.properties 读取配置                            │
│  - 支持环境变量回退                                          │
└─────────────────────────────────────────────────────────────┘
```

### 2. 核心类设计

#### 2.1 MiniMaxApiClient

参考 `ClaudeApiClient`，主要区别：

| 配置项 | ClaudeApiClient | MiniMaxApiClient |
|--------|-----------------|------------------|
| Base URL | 环境变量读取 | config.properties |
| API Path | `/v1/messages` | `/v1/messages` |
| Model | `claude-sonnet-4-5-20250929` | `MiniMax-M2.1` |
| Timeout | CdConstants 读取 | config.properties |

#### 2.2 ConfigUtil

```java
public class ConfigUtil {
    private static Properties props;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // 从项目根目录读取 config.properties
    }

    public static String getMiniMaxBaseUrl() {
        // 优先从 config.properties，失败则回退环境变量
    }

    public static String getMiniMaxApiKey() {
        // 优先从 config.properties，失败则回退环境变量
    }
}
```

### 3. 配置文件设计

#### config.properties

```properties
# MiniMax API 配置
minimax.base_url=https://api.minimaxi.com/anthropic/v1
minimax.api_key=sk-cp-你的密钥
minimax.api_version=2023-06-01
minimax.timeout=60000

# Claude 模型配置
claude.model=MiniMax-M2.1
claude.max_tokens=4096
```

### 4. 测试类迁移

将 `SixMinutesStepByStepWithClaudeCodeTest` 重命名为 `SixMinutesStepByStepWithMiniMaxTest`，
将所有 `TranslateUtilWithClaude` 替换为 `TranslateUtilWithMiniMax`。

### 5. API 请求示例

MiniMax API 请求格式：

```bash
curl --location --request POST 'https://api.minimaxi.com/anthropic/v1/messages' \
  --header 'x-api-key: sk-cp-xxx' \
  --header 'anthropic-version: 2023-06-01' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "model": "MiniMax-M2.1",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": "你好，请确认你是否在线。"
      }
    ]
  }'
```

### 6. 实施步骤

1. 创建 `MiniMaxApiClient.java`
2. 创建 `ConfigUtil.java`
3. 创建 `TranslateUtilWithMiniMax.java`
4. 创建 `SixMinutesStepByStepWithMiniMaxTest.java`
5. 创建 `config.properties.sample`
6. 更新 `.gitignore`
7. 运行测试验证

### 7. 风险评估

| 风险 | 应对措施 |
|------|----------|
| API 响应格式差异 | 保持与 Claude API 兼容的响应结构 |
| 模型能力差异 | 调整 prompt 以适应 MiniMax 模型 |
| 速率限制 | 实现重试机制和延迟处理 |

## 总结

本方案通过创建适配层，将 MiniMax API 封装为与 Claude API 相同的接口，
使得现有代码无需大量修改即可切换到新的 API 服务商。
