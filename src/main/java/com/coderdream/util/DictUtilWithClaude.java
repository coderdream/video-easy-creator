package com.coderdream.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.VocInfo;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.claudecode.ClaudeApiClient;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 使用 Claude API 的词汇翻译工具类
 * 替换 DictUtil 中的 Gemini 相关方法
 *
 * @author Claude Code
 * @since 2026-01-26
 */
@Slf4j
public class DictUtilWithClaude {

    /**
     * 从环境变量读取 Claude API 配置
     */
    private static final String ANTHROPIC_BASE_URL = System.getenv("ANTHROPIC_BASE_URL");
    private static final String ANTHROPIC_AUTH_TOKEN = System.getenv("ANTHROPIC_AUTH_TOKEN");

    /**
     * Claude API 客户端 - 直接使用环境变量配置
     */
    private static final ClaudeApiClient CLAUDE_API_CLIENT = createClaudeClient();

    /**
     * 创建 Claude API 客户端
     */
    private static ClaudeApiClient createClaudeClient() {
        String baseUrl = ANTHROPIC_BASE_URL;
        String authToken = ANTHROPIC_AUTH_TOKEN;

        if (baseUrl == null || baseUrl.isEmpty()) {
            log.error("错误: ANTHROPIC_BASE_URL 环境变量未设置！");
            throw new RuntimeException("ANTHROPIC_BASE_URL 环境变量未设置，请配置第三方中转平台的 API 地址");
        }

        if (authToken == null || authToken.isEmpty()) {
            log.error("错误: ANTHROPIC_AUTH_TOKEN 环境变量未设置！");
            throw new RuntimeException("ANTHROPIC_AUTH_TOKEN 环境变量未设置，请配置 API Key");
        }

        log.info("初始化 Claude API 客户端: baseUrl={}, authToken=***", baseUrl);

        // 创建客户端
        ClaudeApiClient client = new ClaudeApiClient();

        return client;
    }

    /**
     * 对单个词汇进行翻译，包含重试和校验逻辑。
     *
     * @param vocInfo 待翻译的词汇信息对象
     * @return 如果成功翻译并填充了对象，返回 true；否则返回 false。
     */
    public static boolean translateSingleVocWithRetry(VocInfo vocInfo) {
        final int MAX_RETRIES = 5;
        final String promptTemplate =
                "你是一位专业的英汉词典助手。请根据给出的英文单词及其定义，提供翻译和例句。\n"
                        + "请严格按照输出格式提供内容。你必须提供恰好4行文本。不要在回复中包含任何其他内容。\n\n"
                        + "**输入：**\n"
                        + "单词: %s\n"
                        + "定义: %s\n\n"
                        + "**要求的输出格式（4行）：**\n"
                        + "1. 单词的中文翻译。\n"
                        + "2. 释义的中文翻译。\n"
                        + "3. 使用该单词的英文例句。\n"
                        + "4. 例句的中文翻译。";

        String prompt = String.format(promptTemplate, vocInfo.getWord(), vocInfo.getWordExplainEn());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.info("正在翻译词汇 '{}'... (尝试 {}/{})", vocInfo.getWord(), attempt, MAX_RETRIES);
            String translatedText = callClaudeApi(prompt);

            if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常")) {
                log.error("Claude API 调用失败或返回错误。词汇: '{}', 尝试: {}", vocInfo.getWord(), attempt);
                if (attempt < MAX_RETRIES) {
                    continue;
                } else {
                    return false;
                }
            }

            // 增强解析逻辑：从返回结果中智能提取最后4行有效内容
            List<String> allLines = Arrays.stream(translatedText.split("\\r?\\n"))
                    .filter(StrUtil::isNotBlank)
                    .toList();

            // 如果有效行数大于等于4，就从列表末尾取最后4行
            List<String> lines;
            if (allLines.size() >= 4) {
                lines = allLines.subList(allLines.size() - 4, allLines.size());
                log.info("成功从响应末尾提取到4行翻译。");
            } else {
                lines = new ArrayList<>(allLines);
                log.warn("翻译词汇 '{}' 后返回的有效行数不足4行，实际为 {} 行。", vocInfo.getWord(), allLines.size());
            }

            // 校验返回的是否是4行
            if (lines.size() == 4) {
                // 【后处理】移除行首序号、可能存在的拼音和括号
                String prefixRegex = "^\\d+\\.\\s*";
                String pinyinRegex = "\\s*\\(.*?\\)\\s*";

                String wordCn = lines.get(0).replaceAll(prefixRegex, "").replaceAll(pinyinRegex, "").trim();
                String wordExplainCn = lines.get(1).replaceAll(prefixRegex, "").replaceAll(pinyinRegex, "").trim();
                String sampleSentenceEn = lines.get(2).replaceAll(prefixRegex, "").trim();
                String sampleSentenceCn = lines.get(3).replaceAll(prefixRegex, "").replaceAll(pinyinRegex, "").trim();

                vocInfo.setWordCn(wordCn);
                vocInfo.setWordExplainCn(wordExplainCn);
                vocInfo.setSampleSentenceEn(sampleSentenceEn);
                vocInfo.setSampleSentenceCn(sampleSentenceCn);
                log.info("词汇 '{}' 翻译成功。", vocInfo.getWord());
                return true; // 成功，退出循环
            } else {
                log.warn("翻译词汇 '{}' 后返回行数不为4，实际为 {} 行。将在5秒后重试... (尝试 {}/{})",
                        vocInfo.getWord(), lines.size(), attempt, MAX_RETRIES);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return false; // 所有重试都失败了
    }

    /**
     * 调用 Claude API
     *
     * @param prompt 提示词
     * @return 翻译结果
     */
    private static String callClaudeApi(String prompt) {
        com.coderdream.util.claudecode.ClaudeRequest request = new com.coderdream.util.claudecode.ClaudeRequest()
                .setModel(CdConstants.CLAUDE_DEFAULT_MODEL)
                .setMaxTokens(CdConstants.CLAUDE_MAX_TOKENS)
                .addUserMessage(prompt);

        try {
            return CLAUDE_API_CLIENT.sendMessage(request).getTextContent();
        } catch (Exception e) {
            log.error("Claude API 调用失败", e);
            return "API 调用发生异常: " + e.getMessage();
        }
    }

    /**
     * 生成词汇中文翻译文件
     *
     * @param vocFileName   原始词汇文件路径
     * @param vocCnFileName 输出翻译文件路径
     * @return 生成的翻译文件
     */
    public static File genVocCnWithClaude(String vocFileName, String vocCnFileName) {
        List<VocInfo> vocInfoList = genRawList(vocFileName);
        if (CollectionUtil.isEmpty(vocInfoList)) {
            log.error("从 {} 读取到的原始词汇列表为空，无法进行翻译。", vocFileName);
            return new File(vocCnFileName); // 返回一个空文件句柄
        }

        // 【核心优化】逐词翻译并校验
        for (VocInfo vocInfo : vocInfoList) {
            boolean success = translateSingleVocWithRetry(vocInfo);
            if (!success) {
                log.error("词汇 '{}' 在多次尝试后翻译失败，流程终止。", vocInfo.getWord());
                return new File(vocCnFileName);
            }
        }

        // 所有词汇都成功翻译后，写入最终文件
        List<String> finalContent = new ArrayList<>();
        for (VocInfo vocInfo : vocInfoList) {
            finalContent.add(vocInfo.getWord());
            finalContent.add(vocInfo.getWordExplainEn());
            finalContent.add(vocInfo.getWordCn());
            finalContent.add(vocInfo.getWordExplainCn());
            finalContent.add(vocInfo.getSampleSentenceEn());
            finalContent.add(vocInfo.getSampleSentenceCn());
        }

        FileUtil.writeLines(finalContent, vocCnFileName, StandardCharsets.UTF_8);
        log.info("成功生成词汇翻译文件: {}", vocCnFileName);
        return new File(vocCnFileName);
    }

    /**
     * 解析原始词汇文件
     *
     * @param filePath 文件路径
     * @return 词汇信息列表
     */
    private static List<VocInfo> genRawList(String filePath) {
        List<VocInfo> vocInfoList = new ArrayList<>();

        List<String> scriptList = FileUtil.readLines(filePath, StandardCharsets.UTF_8);

        // 判断是否为6个词汇（每2行一个词汇）
        int size = 0;
        if (scriptList.size() != 12 && scriptList.size() != 18 && scriptList.size() != 17) {
            log.warn("词汇文件行数不是预期的6的倍数，当前为：{} 行", scriptList.size());
            return null;
        } else {
            size = scriptList.size();
        }

        VocInfo vocInfo;
        for (int i = 0; i < size; i++) {
            vocInfo = new VocInfo();
            vocInfo.setWord(scriptList.get(i));
            vocInfo.setWordExplainEn(scriptList.get(i + 1));
            vocInfoList.add(vocInfo);
            i += 2;
        }
        return vocInfoList;
    }

    /**
     * 处理词汇文件（主入口方法）
     *
     * @param folderName 文件夹名称
     * @return 生成的翻译文件
     */
    public static File processVoc(String folderName) {
        String fileName = "voc";
        String filePath = CommonUtil.getFullPathFileName(folderName, fileName, ".txt");
        String vocCnFileName = CommonUtil.getFullPathFileName(folderName, fileName, "_cn.txt");

        return genVocCnWithClaude(filePath, vocCnFileName);
    }

    /**
     * 处理词汇文件（带文件名参数）
     *
     * @param folderName 文件夹路径
     * @param fileName   文件名称（不含扩展名）
     * @return 生成的翻译文件
     */
    public static File processVoc(String folderName, String fileName) {
        String filePath = folderName + fileName + ".txt";
        String vocCnFileName = folderName + fileName + "_cn.txt";

        return genVocCnWithClaude(filePath, vocCnFileName);
    }
}
