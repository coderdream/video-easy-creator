# ClaudeCodeUtil 开发任务清单

**创建日期**: 2026-01-25
**项目**: video-easy-creator
**模块**: ClaudeCodeUtil 工具类开发

---

## 第一阶段：基础框架（优先级：高）

### 1.1 项目结构和配置

- [ ] 创建 `src/main/java/com/coderdream/util/claudecode/` 目录结构
- [ ] 创建 `src/main/java/com/coderdream/util/claudecode/tool/` 子目录
- [ ] 创建 `src/main/java/com/coderdream/util/claudecode/prompt/` 子目录
- [ ] 创建 `src/test/java/com/coderdream/util/claudecode/` 测试目录
- [ ] 验证 Hutool 依赖已在 pom.xml 中（版本 5.8.25+）
- [ ] 验证环境变量配置（ANTHROPIC_BASE_URL, ANTHROPIC_AUTH_TOKEN）

### 1.2 数据对象实现

- [ ] 实现 `ClaudeMessage.java`
  - [ ] 支持 user/assistant 角色
  - [ ] 支持文本和复杂内容
  - [ ] 实现 `toJson()` 方法
  - [ ] 提供静态工厂方法

- [ ] 实现 `ClaudeResponse.java`
  - [ ] 实现 `ContentBlock` 内部类
  - [ ] 实现 `Usage` 内部类
  - [ ] 实现 `getTextContent()` 方法
  - [ ] 实现 `getToolUseBlocks()` 方法

- [ ] 实现 `ClaudeRequest.java`
  - [ ] 支持模型选择
  - [ ] 支持参数配置（temperature, effort 等）
  - [ ] 支持思考模式配置
  - [ ] 支持消息列表管理

### 1.3 API 客户端实现

- [ ] 实现 `ClaudeApiClient.java`
  - [ ] 从环境变量读取配置
  - [ ] 实现 `sendMessage()` 方法
  - [ ] 实现 `sendMessageWithTools()` 方法
  - [ ] 实现请求体构建逻辑
  - [ ] 实现响应解析逻辑
  - [ ] 实现代理配置支持
  - [ ] 实现错误处理和日志记录

### 1.4 常量配置更新

- [ ] 更新 `CdConstants.java`
  - [ ] 添加 Claude 模型常量
  - [ ] 添加 API 参数常量
  - [ ] 添加 effort 级别常量
  - [ ] 添加超时和重试配置

### 1.5 单元测试

- [ ] 创建 `ClaudeApiClientTest.java`
  - [ ] 测试简单消息调用
  - [ ] 测试思考模式
  - [ ] 测试不同模型
  - [ ] 测试错误处理
  - [ ] 测试代理配置

- [ ] 创建 `ClaudeMessageTest.java`
  - [ ] 测试消息创建
  - [ ] 测试 JSON 转换
  - [ ] 测试工厂方法

- [ ] 创建 `ClaudeResponseTest.java`
  - [ ] 测试响应解析
  - [ ] 测试内容提取
  - [ ] 测试工具调用块识别

### 1.6 文档编写

- [ ] 编写 API 文档注释
- [ ] 编写使用示例
- [ ] 编写故障排查指南

**预期产出**: 能够发送简单消息并获取响应

---

## 第二阶段：工具调用（优先级：高）

### 2.1 工具定义系统

- [ ] 实现 `ToolDefinition.java`
  - [ ] 支持工具名称和描述
  - [ ] 支持输入 Schema 定义
  - [ ] 实现 `toJson()` 方法

- [ ] 实现 `ToolRegistry.java`
  - [ ] 实现工具注册机制
  - [ ] 实现工具查询
  - [ ] 实现工具执行委托

### 2.2 工具执行器

- [ ] 实现 `ClaudeToolExecutor.java`
  - [ ] 实现工具调用循环
  - [ ] 实现响应处理
  - [ ] 实现工具结果反馈
  - [ ] 实现循环终止条件

### 2.3 内置工具实现

- [ ] 实现 `list_files` 工具
  - [ ] 使用 `FileUtil.ls()`
  - [ ] 支持递归列表
  - [ ] 返回文件信息

- [ ] 实现 `read_file` 工具
  - [ ] 使用 `FileUtil.readUtf8String()`
  - [ ] 支持路径验证
  - [ ] 处理大文件

- [ ] 实现 `write_file` 工具
  - [ ] 使用 `FileUtil.writeUtf8String()`
  - [ ] 支持路径验证
  - [ ] 支持覆盖/追加模式

- [ ] 实现 `execute_command` 工具
  - [ ] 使用 `RuntimeUtil.execForStr()`
  - [ ] 实现命令白名单
  - [ ] 实现超时控制

- [ ] 实现 `grep` 工具
  - [ ] 支持正则表达式搜索
  - [ ] 支持多文件搜索
  - [ ] 返回匹配结果

### 2.4 工具调用测试

- [ ] 创建 `ClaudeToolExecutorTest.java`
  - [ ] 测试工具调用循环
  - [ ] 测试各个工具功能
  - [ ] 测试错误处理
  - [ ] 测试循环终止

- [ ] 创建 `ToolRegistryTest.java`
  - [ ] 测试工具注册
  - [ ] 测试工具查询
  - [ ] 测试工具执行

### 2.5 集成测试

- [ ] 创建 `ClaudeToolIntegrationTest.java`
  - [ ] 测试完整的工具调用流程
  - [ ] 测试多轮工具调用
  - [ ] 测试工具组合使用

**预期产出**: 支持文件读写、命令执行等工具调用

---

## 第三阶段：高级功能（优先级：中）

### 3.1 会话管理

- [ ] 实现 `ClaudeSession.java`
  - [ ] 实现消息历史存储
  - [ ] 实现上下文管理
  - [ ] 实现会话隔离
  - [ ] 实现会话导出/导入

### 3.2 上下文管理

- [ ] 实现上下文压缩机制
- [ ] 实现长对话处理
- [ ] 实现内存优化

### 3.3 高级功能测试

- [ ] 创建 `ClaudeSessionTest.java`
  - [ ] 测试消息历史
  - [ ] 测试上下文管理
  - [ ] 测试会话导出

### 3.4 性能优化

- [ ] 实现请求缓存
- [ ] 实现响应缓存
- [ ] 实现连接池

**预期产出**: 支持多轮对话和深度推理

---

## 第四阶段：应用场景（优先级：中）

### 4.1 翻译应用

- [ ] 实现 `TranslatorPrompt.java`
  - [ ] 定义资深翻译官 System Prompt
  - [ ] 支持多种翻译场景
  - [ ] 支持动态 Prompt 调整

- [ ] 实现 `ClaudeTranslator.java`
  - [ ] 实现 `translate()` 方法
  - [ ] 支持场景选择
  - [ ] 支持质量验证

- [ ] 创建 `ClaudeTranslatorTest.java`
  - [ ] 测试基础翻译
  - [ ] 测试技术文档翻译
  - [ ] 测试文学作品翻译

### 4.2 代码生成应用

- [ ] 实现 `CodeGeneratorPrompt.java`
  - [ ] 定义编程助手 System Prompt
  - [ ] 支持多种编程场景

- [ ] 实现 `ClaudeCodeGenerator.java`
  - [ ] 实现 `generateCode()` 方法
  - [ ] 实现 `reviewCode()` 方法
  - [ ] 实现 `fixBug()` 方法

- [ ] 创建 `ClaudeCodeGeneratorTest.java`
  - [ ] 测试代码生成
  - [ ] 测试代码审查
  - [ ] 测试 Bug 修复

### 4.3 文档编写

- [ ] 编写完整的 API 文档
- [ ] 编写使用示例
- [ ] 编写故障排查指南
- [ ] 编写性能调优建议

### 4.4 示例代码

- [ ] 创建翻译示例
- [ ] 创建代码生成示例
- [ ] 创建工具调用示例
- [ ] 创建会话管理示例

**预期产出**: 可用的翻译和代码生成功能

---

## 跨阶段任务

### 代码质量

- [ ] 代码审查（每个阶段）
- [ ] 代码格式化检查
- [ ] 静态代码分析
- [ ] 单元测试覆盖率检查（目标 > 80%）

### 文档维护

- [ ] 更新 CLAUDE.md
- [ ] 更新项目 README
- [ ] 维护 API 文档
- [ ] 维护变更日志

### 版本管理

- [ ] 创建 Git 分支
- [ ] 定期提交代码
- [ ] 创建 Pull Request
- [ ] 代码审查和合并

---

## 检查清单

### 功能完整性

- [ ] 所有需求功能已实现
- [ ] 所有测试用例已通过
- [ ] 所有文档已完成
- [ ] 所有示例代码已验证

### 代码质量

- [ ] 代码风格一致
- [ ] 注释完整
- [ ] 异常处理完善
- [ ] 日志记录详细

### 性能指标

- [ ] API 调用延迟 < 10 秒（一般场景）
- [ ] 工具调用循环 < 60 秒（复杂推理）
- [ ] 内存占用 < 500MB
- [ ] CPU 占用 < 50%

### 安全性

- [ ] API 密钥不硬编码
- [ ] 工具调用有白名单限制
- [ ] 文件操作有路径验证
- [ ] 命令执行有安全限制

---

## 依赖关系

```
第一阶段（基础框架）
    ↓
第二阶段（工具调用）
    ↓
第三阶段（高级功能）
    ↓
第四阶段（应用场景）
```

---

## 风险项

| 风险 | 影响 | 缓解措施 |
|------|------|--------|
| API 配额限制 | 功能不可用 | 实现请求限流 |
| 网络延迟 | 响应缓慢 | 设置合理超时 |
| 工具执行失败 | 任务中断 | 实现错误恢复 |
| 上下文过长 | Token 超限 | 实现上下文压缩 |

---

## 联系方式

- **技术负责人**: [待填写]
- **项目经理**: [待填写]
- **文档维护**: [待填写]

---

**最后更新**: 2026-01-25
**下一次审查**: 2026-02-01
