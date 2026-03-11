# MiniMax API 集成 TODO

## 当前进度

已完成以下工作：
1. ✅ 创建 `TranslateUtilWithMiniMax.java` - 基于 MiniMax API 的翻译工具类
2. ✅ 创建 `TranslateUtilWithMiniMaxTest.java` - 完整的测试类
3. ✅ 创建 `MiniMaxApiClientSimpleTest.java` - 简化的测试类
4. ✅ 创建 `MiniMaxApiDebugTest.java` - 详细的调试测试类
5. ✅ 创建 `EnvironmentVariableVerifyTest.java` - 环境变量验证测试
6. ✅ 创建 `QuickVerifyTest.java` - 快速验证测试（支持硬编码配置）

## 待完成任务

### 第一步：验证环境变量配置（重启 IDEA 后）

**环境变量要求：**
```bash
MINIMAX_BASE_URL=https://api.minimaxi.com
MINIMAX_API_KEY=sk-cp-g...ww5m8  # 你的完整 API Key
```

**验证命令：**
```bash
mvn test -Dtest=EnvironmentVariableVerifyTest#testVerifyEnvironmentVariables
```

**预期结果：**
- ✓ MINIMAX_BASE_URL 正确
- ✓ MINIMAX_API_KEY 格式正确（以 sk-cp-g 开头，以 ww5m8 结尾）

---

### 第二步：测试 API 连接

**测试命令（按顺序执行）：**

1. **直接 API 调用测试**（最重要）
   ```bash
   mvn test -Dtest=MiniMaxApiDebugTest#test01DirectApiCall
   ```
   这个测试会显示完整的请求和响应信息。

2. **使用封装客户端测试**
   ```bash
   mvn test -Dtest=MiniMaxApiDebugTest#test02UsingMiniMaxApiClient
   ```

3. **简单 API 调用测试**
   ```bash
   mvn test -Dtest=MiniMaxApiClientSimpleTest#test03TestApiConnection
   ```

**预期结果：**
- API 调用成功
- 返回中文响应："你好！我在线..."

---

### 第三步：测试翻译功能

**测试命令：**
```bash
mvn test -Dtest=TranslateUtilWithMiniMaxTest#test04TranslationFunction
```

**预期结果：**
- 成功翻译英文文本到中文
- 无 "API 调用发生异常" 错误

---

### 第四步：集成到项目中

如果以上测试都通过，可以在项目中使用 `TranslateUtilWithMiniMax` 替换现有的翻译工具：

**使用示例：**
```java
// 1. 翻译对话脚本
String folderName = "240104";
String outputFile = CommonUtil.getFullPathFileName(folderName, "script_dialog", "_cn.txt");
TranslateUtilWithMiniMax.genScriptDialogCn(folderName, outputFile);

// 2. 翻译 SRT 字幕
boolean success = TranslateUtilWithMiniMax.translateEngSrc(folderName);

// 3. 生成视频描述
String descFile = CommonUtil.getFullPathFileName(folderName, "description", ".txt");
TranslateUtilWithMiniMax.genDescription(scriptDialogMergeFile, descFile);

// 4. 通用内容生成
String result = TranslateUtilWithMiniMax.generateContent("翻译提示词");
```

---

## 已知问题和解决方案

### 问题1：环境变量在 IDEA 中不生效
**原因：** IDEA 启动时读取环境变量，运行时修改不会生效

**解决方案：**
1. 设置系统环境变量后重启 IDEA（推荐）
2. 或使用 `QuickVerifyTest.java` 硬编码测试（临时方案）

### 问题2：API 调用返回 401 Unauthorized
**可能原因：**
- API Key 不正确
- API Key 格式错误（多余的空格、换行等）

**解决方案：**
- 运行 `EnvironmentVariableVerifyTest` 验证配置
- 检查 API Key 是否完整复制

### 问题3：API 调用返回 429 Too Many Requests
**原因：** 触发速率限制

**解决方案：**
- 代码已实现自动重试机制（30秒延迟）
- 减少并发请求数量

---

## 参考信息

### MiniMax API 文档
- **Endpoint:** `https://api.minimaxi.com/anthropic/v1/messages`
- **认证方式:** `x-api-key` 请求头
- **API 版本:** `2023-06-01`
- **支持模型:** `MiniMax-M2.1`, `claude-sonnet-4-5-20250929`, `claude-haiku-4-5-20251001`

### 成功的 curl 示例
```bash
curl --location --request POST 'https://api.minimaxi.com/anthropic/v1/messages' \
--header 'x-api-key: sk-cp-gxxxxx' \
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

### 项目文件位置
- 翻译工具类: `src/main/java/com/coderdream/util/translate/TranslateUtilWithMiniMax.java`
- 测试类目录: `src/test/java/com/coderdream/util/minimax/`
- 测试类目录: `src/test/java/com/coderdream/util/translate/`

---

## 下一步行动

**重启 IDEA 后立即执行：**

1. 验证环境变量：
   ```bash
   mvn test -Dtest=EnvironmentVariableVerifyTest#testVerifyEnvironmentVariables
   ```

2. 如果验证通过，测试 API 连接：
   ```bash
   mvn test -Dtest=MiniMaxApiDebugTest#test01DirectApiCall
   ```

3. 把测试结果（完整日志）发给 Claude Code 进行分析

---

## 备注

- 所有测试类都包含详细的日志输出，便于排查问题
- 如果遇到问题，优先运行 `MiniMaxApiDebugTest#test01DirectApiCall`，它会显示最详细的信息
- 对话记录保存在: `C:\Users\Administrator\.claude\projects\D--04-GitHub-video-easy-creator\`

---

**创建时间:** 2026-03-04
**创建者:** Claude Code
