# MiniMax API 迁移任务清单

## 任务概述

将项目中的 Claude API 调用替换为 MiniMax API，创建一个完整的测试类 `SixMinutesStepByStepWithMiniMaxTest`。

## 任务列表

### 阶段一：基础设施准备

- [ ] 1.1 分析现有 Claude API 调用方式（已完成）
- [ ] 1.2 创建配置读取工具类 `ConfigUtil`
  - 支持从 `config.properties` 读取配置
  - 支持环境变量回退
- [ ] 1.3 创建 MiniMax API 客户端类 `MiniMaxApiClient`
  - 参考 `ClaudeApiClient` 的实现方式
  - 实现消息 API 调用

### 阶段二：工具类开发

- [ ] 2.1 创建 MiniMax 翻译工具类 `TranslateUtilWithMiniMax`
  - 复制 `TranslateUtilWithClaude` 的功能
  - 替换 API 调用方式
- [ ] 2.2 实现以下方法：
  - `genScriptDialogCn()` - 翻译对话脚本
  - `translateEngSrc()` - 翻译 SRT 字幕
  - `genDescription()` - 生成描述文件
  - `generateContent()` - 通用内容生成

### 阶段三：测试类开发

- [ ] 3.1 创建 `SixMinutesStepByStepWithMiniMaxTest` 测试类
  - 复制 `SixMinutesStepByStepWithClaudeCodeTest` 的结构
  - 将所有 `TranslateUtilWithClaude` 替换为 `TranslateUtilWithMiniMax`
- [ ] 3.2 实现 18 个测试步骤
  - Step17: 生成平台描述文件
  - Step18: 生成 YouTube 专用描述

### 阶段四：配置和测试

- [ ] 4.1 创建 `config.properties` 配置文件模板
- [ ] 4.2 更新 `.gitignore` 排除 `config.properties`
- [ ] 4.3 运行测试验证功能

## 当前进度

- 开始时间：2026-01-27
- 当前状态：阶段一进行中

## 备注

- 使用 MiniMax API 的基础 URL：`https://api.minimaxi.com/anthropic/v1`
- API Key 前缀：`sk-cp-`
- API 版本：`2023-06-01`
