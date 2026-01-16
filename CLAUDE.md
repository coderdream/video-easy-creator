# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**video-easy-creator** 是一个高度自动化的视频生成系统,专门用于生成教育类英语学习视频(特别是BBC六分钟英语课程)。系统集成了多个AI服务、音视频处理工具,实现从原始文本到最终视频的全流程自动化。

**技术栈**: Spring Boot 3.4.0 + Java 17 + MyBatis Plus + FFmpeg + Azure TTS + Gemini API + Aspose Slides

## 常用命令

### 构建和测试

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行特定测试类
mvn test -Dtest=SixMinutesStepByStepTest

# 运行特定测试方法
mvn test -Dtest=SixMinutesStepByStepTest#testStep01

# 打包(跳过测试)
mvn clean package -DskipTests

# 运行Spring Boot应用
mvn spring-boot:run
```

### 开发常用测试

```bash
# BBC六分钟英语完整流程测试
mvn test -Dtest=SixMinutesStepByStepTest

# 单步测试(翻译步骤)
mvn test -Dtest=SixMinutesStepByStepTest#testStep03TranslateDialog

# 音频生成测试
mvn test -Dtest=GenDualAudioUtilTest

# 视频生成测试
mvn test -Dtest=SingleCreateVideoUtilTest

# 字幕处理测试
mvn test -Dtest=GenSubtitleUtilTest
```

## 核心架构

### 1. BBC六分钟英语处理流程(18步)

这是项目的核心流程,位于 `SixMinutesStepByStepTest.java`,包含以下阶段:

**阶段一: 文本处理(Step 00-04)**
- Step00: 预处理原始脚本 → `ProcessScriptUtil.processScriptTxt()`
- Step01: 生成对话脚本 → `ProcessScriptUtil.genScriptDialogTxt()`
- Step02: 生成词汇文件 → `ProcessScriptUtil.genVocTxt()`
- Step03: **翻译对话**(使用Gemini API,支持3次重试) → `TranslateUtil.genScriptDialogCn()`
- Step04: **翻译词汇**(使用Gemini API) → `DictUtil.genVocCnWithGemini()`

**阶段二: 格式化(Step 05-08)**
- Step05: 生成优化后的对话脚本 → `GenSrtUtil.genScriptDialogNew()`
- Step06: 生成音标文件 → `GenSrtUtil.genPhonetics()`
- Step07: 生成SRT字幕 → `GenSrtUtil.genSrt()`
- Step08: 生成单行字幕 → `SubtitleUtil.genSubtitle()`

**阶段三: 音频生成(Step 09-13)**
- Step09: 创建音频文件夹结构
- Step10: **批量生成双语音频**(Azure TTS,最多16次重试) → `GenDualAudioUtil.genDialog2Audio900()`
- Step11: 合并中文音频 → `AudioMergerSingleBatch.batchMergeCnAudio()`
- Step12: 合并英文音频 → `AudioMergerSingleBatch.batchMergeEnAudio()`
- Step13: 生成混合音频 → `AudioMergerMixBatch.genDualAudio()`

**阶段四: 图片生成(Step 14-15)**
- Step14: 处理PPT(填充内容) → `GetSixMinutesPpt.process()`
- Step15: PPT转PNG(1920x1080) → `PptToImageConverter` / `PptToPng.savePptToPng()`

**阶段五: 视频生成(Step 16-18)**
- Step16: **并行生成视频**(使用FFmpeg优化) → `SingleCreateVideoUtil.batchCreateSingleVideo()`
- Step17: 生成带字幕视频 → `AssSubtitleGenerator.addSubtitleToVideo()`
- Step18: 发布准备 → `PreparePublishUtil.process()`

### 2. AI服务集成策略(三级降级)

系统使用多个AI服务,并实现了智能降级机制:

**优先级**: Gemini API (主要) → ChatGPT (备选1) → Ollama (备选2/本地)

**关键配置位置**:
- `CdConstants.java`: API密钥和常量配置
- `application.yml`: Spring AI配置
- 各工具类中的静态配置

**Gemini API**(主要翻译引擎):
- 文件: `com.coderdream.util.gemini.GeminiApiClient`
- 支持模型: gemini-2.5-flash, gemini-2.0-flash, gemini-1.5-pro等
- 失败重试: 最多3次,每次延迟2秒
- 用途: 文本翻译、词汇解释、内容总结

**Azure Text-to-Speech**(音频生成):
- 文件: `com.coderdream.util.mstts.GenDualAudioUtil`
- 配置: 需要两个区域的密钥(eastasia中文, eastus英文)
- 重试机制: 最多16次
- 语音配置:
  - 中文: zh-CN-XiaochenNeural
  - 英文: en-US-JennyNeural

**Ollama**(本地LLM,离线场景):
- 基础URL: http://192.168.3.165:11434
- 模型: Qwen2.5-7B-Instruct-1M
- 用途: 备选翻译引擎

### 3. 核心模块间协作

```
原始文本
  ↓
[文本处理层] ProcessScriptUtil → 提取对话和词汇
  ↓
[AI翻译层] TranslateUtil + GeminiApiClient → 中英文翻译
  ↓
[格式化层] GenSrtUtil + SubtitleUtil → 字幕和音标
  ↓
[音频生成层] GenDualAudioUtil (Azure TTS) → 双语音频
  ↓
[音频合并层] AudioMerger* → 混合音频
  ↓
[图片生成层] GetSixMinutesPpt + PptToImageConverter → PNG图片
  ↓
[视频合成层] FFmpegParallelOptimized → 并行生成视频
  ↓
[字幕嵌入层] AssSubtitleGenerator → 带字幕的视频
  ↓
[发布准备层] PreparePublishUtil → 最终输出
```

### 4. 性能优化关键点

**并行视频生成** (`com.coderdream.util.video.demo02.FFmpegParallelOptimized`):
- 使用BlockingQueue实现任务队列
- 线程数 = CPU核心数
- 动态负载均衡和进度监控

**音频批量处理**:
- 单语言批量合并: `AudioMergerSingleBatch`
- 双语言混合: `AudioMergerMixBatch`
- WAV级合并: `WavMerger`

**PPT批量处理**:
- 模板复用机制
- 支持1920x1080、2K、4K多种分辨率
- 使用Aspose Slides高性能处理

## 关键数据结构

### SubtitleEntity (字幕实体)
```java
SubtitleEntity {
    Integer subIndex;          // 字幕序号
    String timeStr;            // 时间范围 "00:00:50,280 --> 00:00:52,800"
    String subtitle;           // 主字幕(英文)
    String secondSubtitle;     // 副字幕(中文)
}
```

### DialogSingleEntity (对话实体)
```java
DialogSingleEntity {
    String hostEn;             // 主持人英文
    String contentEn;          // 内容英文
    String hostCn;             // 主持人中文
    String contentCn;          // 内容中文
}
```

### VocInfo (词汇信息)
```java
VocInfo {
    String word;               // 单词
    String wordExplainEn;      // 英文解释
    String wordExplainCn;      // 中文解释
    String sampleSentenceEn;   // 英文例句
    String sampleSentenceCn;   // 中文例句
}
```

## 环境配置要求

### 必需的外部服务

1. **Gemini API**
   - 需要Google账户和API密钥
   - 配置: `CdConstants.GEMINI_API_KEY`

2. **Azure Cognitive Services**
   - Text-to-Speech API密钥(2个区域)
   - 配置: `CdConstants.SPEECH_KEY_EAST_US`, `CdConstants.SPEECH_KEY_EASTASIA`

3. **FFmpeg**(本地安装)
   - 系统环境变量配置
   - 用于视频编码和音视频合成

4. **MySQL数据库**
   - 端口: 3306
   - 数据库: dictionary_db
   - 配置: `application.yml`

5. **Ollama**(可选,离线场景)
   - 本地LLM服务
   - 默认: http://192.168.3.165:11434

### 环境变量配置示例

```bash
# Azure语音服务
export SPEECH_KEY_EAST_US=your_key_here
export SPEECH_KEY_EASTASIA=your_key_here

# Gemini API
export GEMINI_API_KEY=your_key_here

# OpenAI(可选)
export OPENAI_API_KEY=your_key_here

# 代理配置(可选)
export PROXY_HOST=127.0.0.1
export PROXY_PORT=7890
```

## 重要的文件路径约定

### 资源文件结构
```
src/main/resources/
├── data/bbc/              # BBC课程数据
│   ├── todo.txt           # 待处理文件夹列表(用于参数化测试)
│   ├── title.txt          # 课程标题
│   └── pdf_names.txt      # PDF名称列表
├── ppt/                   # PPT模板
│   ├── 6min_page1.pptx
│   ├── 6min_page2.pptx
│   └── ...
└── pdf/                   # PDF资源文件
```

### 工作目录结构(动态生成)
```
{基础目录}/{课程文件夹}/
├── script_dialog.txt           # 对话脚本
├── script_dialog_cn.txt        # 中文翻译
├── voc.txt                     # 词汇表
├── voc_cn.txt                  # 词汇中文翻译
├── audio/                      # 音频文件夹
│   ├── cn/*.wav               # 中文音频
│   └── en/*.wav               # 英文音频
├── audio_mix/                  # 混合音频
├── pic_cht/                    # 图片(1920x1080)
└── video_cht/                  # 视频输出
```

## 错误处理和重试机制

### Gemini API调用
- 最大重试次数: 3次
- 重试延迟: 2000ms
- 失败后自动降级到备用模型

### Azure TTS调用
- 最大重试次数: 16次
- 详细的错误日志记录
- 失败时提供明确的错误信息

### 视频生成
- 使用BlockingQueue队列避免线程饥饿
- 每个任务独立的错误处理
- 详细的进度日志和执行时间统计

## 测试数据驱动

项目使用参数化测试框架,从 `src/main/resources/data/bbc/todo.txt` 读取待处理的文件夹列表。每个文件夹会执行完整的18步流程。

**添加新课程**:
1. 在 `todo.txt` 中添加新文件夹名称(如: `250101_new_lesson`)
2. 准备原始脚本文件到对应目录
3. 运行测试: `mvn test -Dtest=SixMinutesStepByStepTest`

## 代码风格注意事项

### Lombok使用
项目使用 `@Data` 和 `@Accessors(chain = true)` 注解,支持链式调用:
```java
new DictionaryEntity()
    .setSource("source")
    .setWord("word")
    .setPhonetic("phonetic");
```

### 工具类命名规范
- `*Util`: 通用工具类
- `Gen*`: 生成类工具
- `Process*`: 处理类工具
- `*Parser`: 解析器类
- `*Merger`: 合并类工具

### 常量配置
所有API密钥、区域配置、路径配置集中在 `CdConstants.java` 中管理。

## 依赖版本管理

### 关键依赖版本
- Spring Boot: 3.4.0
- Spring AI: 1.0.0-M4
- Java: 17
- MyBatis Plus: 3.5.5
- Gemini API: 1.0.0-2.7
- Aspose Slides: 24.5
- Stanford NLP: 4.5.8
- FFmpeg (JAVE): 3.3.1

### 本地依赖
Aspose Slides 使用本地JAR: `lib/aspose-slides-24.5-jdk16.jar`

## 数据库配置

项目使用MyBatis Plus + MySQL:
- 数据库名: dictionary_db
- 用户名: root (在application.yml中配置)
- Mapper位置: `src/main/resources/mapper/*.xml`
- 实体包: `com.coderdream.entity`
- 自动驼峰转换: 已启用

## 开发注意事项

1. **FFmpeg依赖**: 确保系统已安装FFmpeg并配置环境变量
2. **API配额管理**: Gemini和Azure TTS都有配额限制,注意监控使用量
3. **并行处理**: 视频生成使用CPU核心数作为线程数,注意系统资源
4. **文件路径**: 所有路径使用绝对路径或基于 `CdConstants` 的配置路径
5. **字符编码**: 项目统一使用UTF-8编码
6. **重试机制**: AI调用和TTS调用都有重试机制,避免手动重试
7. **日志记录**: 关键步骤都有详细日志,便于调试和监控
