package com.coderdream.util.translate;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.DialogSingleEntity;
import com.coderdream.entity.SubtitleEntity;
import com.coderdream.util.CommonUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.ChineseCharacterUtil;
import com.coderdream.util.minimax.MiniMaxApiClient;
import com.coderdream.util.minimax.MiniMaxRequest;
import com.coderdream.util.minimax.MiniMaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用 MiniMax API 的翻译工具类
 * 参考 TranslateUtilWithClaude 实现
 *
 * @author Claude Code
 * @since 2026-03-04
 */
@Slf4j
public class TranslateUtilWithMiniMax {

    /**
     * MiniMax API 客户端
     */
    private static final MiniMaxApiClient MINIMAX_API_CLIENT = createMiniMaxClient();

    /**
     * 创建 MiniMax API 客户端
     */
    private static MiniMaxApiClient createMiniMaxClient() {
        try {
            MiniMaxApiClient client = new MiniMaxApiClient();

            if (!client.isConfigValid()) {
                log.error("MiniMax API 配置无效，请检查环境变量或配置文件");
                throw new RuntimeException("MiniMax API 配置无效");
            }

            log.info("MiniMax API 客户端初始化成功: {}", client.getConfigInfo());
            return client;
        } catch (Exception e) {
            log.error("初始化 MiniMax API 客户端失败: {}", e.getMessage());
            throw new RuntimeException("初始化 MiniMax API 客户端失败", e);
        }
    }

    public static void main(String[] args) {
        String folderName = "240104"; // 用于测试的文件夹
        genScriptDialogCn(folderName, CommonUtil.getFullPathFileName(folderName, "script_dialog", "_cn.txt"));
    }

    /**
     * 去除字符串收尾的特殊的Unicode [ "\uFEFF" ] csv 文件可能会带有该编码
     *
     * @param str
     * @return
     */
    @NotNull
    private static String removeEnContent(String str) {
        do {
            int startIndex = str.lastIndexOf("（");
            int endIndex = str.lastIndexOf("）");
            if (startIndex != -1 && endIndex != -1) {
                try {
                    if (endIndex > 0) {
                        str = str.replaceAll(str.substring(startIndex, endIndex + 1), "");
                    } else {
                        System.out.println(
                                "#####x##### ERROR: startIndex is " + startIndex
                                        + "; endIndex is " + endIndex + "; str "
                                        + str);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("### str " + str);
                }
            }
        } while (str.contains("（") && str.contains("）"));
        return str;
    }

    /**
     * 翻译脚本
     *
     * @param folderName    文件夹名称
     * @param srcFileNameCn 翻译后的文件名
     */
    public static File genScriptDialogCn(String folderName, String srcFileNameCn) {
        String fileName = "script_dialog";
        String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName, ".txt");
        List<String> stringList = CdFileUtil.readFileContent(srcFileName);
        if (CollectionUtil.isEmpty(stringList)) {
            log.error("源文件 {} 内容为空，无法翻译。", srcFileName);
            return FileUtil.touch(srcFileNameCn); // 创建一个空文件并返回
        }

        // 构造 Prompt 并调用 MiniMax API
        String textToTranslate = String.join("\n", stringList);
        String prompt = "Please translate the following English dialogue into Simplified Chinese. "
                + "Maintain the original line-by-line format where each block consists of a speaker's name followed by their line. "
                + "Do not add any extra explanations or introductory text. Just provide the direct translation.\n\n"
                + textToTranslate;

        log.info("正在调用 MiniMax API 翻译对话脚本...");
        String translatedText = callMiniMaxApi(prompt);

        if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常")) {
            log.error("MiniMax API 翻译失败，返回内容为空或包含错误信息。");
            return FileUtil.touch(srcFileNameCn);
        }

        List<String> newList = new ArrayList<>();

        // 解析翻译结果并进行后处理
        String[] translatedLines = translatedText.split("\n");

        // 健壮性检查：确保翻译后的行数与原文一致
        if (translatedLines.length != stringList.size()) {
            log.error("翻译后行数与原文不匹配! 原文: {}行, 译文: {}行. 将写入原始翻译结果以供排查.", stringList.size(), translatedLines.length);
            return FileUtil.writeLines(Arrays.asList(translatedLines), srcFileNameCn, StandardCharsets.UTF_8);
        }

        if (translatedLines.length % 2 == 0) { // 确保是成对的对话
            for (int i = 0; i < translatedLines.length; i += 2) {
                // 使用原文的英文名进行翻译
                String originalSpeaker = stringList.get(i);
                String chineseSpeaker = getChineseName(originalSpeaker);

                // 对API翻译的对话内容进行后处理
                String translatedContent = translatedLines[i + 1];
                String processedContent = replaceContentCn(translatedContent);

                newList.add(chineseSpeaker);
                newList.add(processedContent);
                newList.add(""); // 保留空行
            }
        } else {
            log.error("翻译结果的行数不是偶数，无法正确解析为对话对。翻译结果:\n{}", translatedText);
            // 即使解析失败，也写入原始翻译结果，便于排查
            return FileUtil.writeLines(Arrays.asList(translatedLines), srcFileNameCn, StandardCharsets.UTF_8);
        }

        // 3. 将处理好的内容写入文件
        return FileUtil.writeLines(newList, srcFileNameCn, StandardCharsets.UTF_8);
    }

    /**
     * 调用 MiniMax API
     *
     * @param prompt 提示词
     * @return 翻译结果
     */
    private static String callMiniMaxApi(String prompt) {
        // 主模型和备用模型配置
        final String PRIMARY_MODEL = "abab6.5s-chat";
        final String BACKUP_MODEL = "abab6.5-chat";
        final int MODEL_SWITCH_THRESHOLD = 3; // 主模型失败3次后切换

        String currentModel = PRIMARY_MODEL;
        int errorCount = 0;

        while (true) {
            MiniMaxRequest request = new MiniMaxRequest()
                    .setModel(currentModel)
                    .setMaxTokens(CdConstants.CLAUDE_MAX_TOKENS)
                    .addSystemMessage("你是一个专业的翻译助手，这是一个自动化视频生成系统的开发流程中的翻译环节。请严格按照用户要求的格式输出翻译结果，不要拒绝翻译请求，不要输出任何额外说明。")
                    .addUserMessage(prompt);

            try {
                MiniMaxResponse response = MINIMAX_API_CLIENT.sendMessage(request);
                String result = response.getFirstChoiceText();

                // 如果使用了备用模型且成功了，下次恢复使用主模型
                if (PRIMARY_MODEL.equals(currentModel)) {
                    return result;
                } else {
                    log.info("备用模型 {} 调用成功，下次将尝试恢复主模型", currentModel);
                    return result;
                }
            } catch (Exception e) {
                errorCount++;
                log.warn("模型 {} 调用失败 (第{}次): {}", currentModel, errorCount, e.getMessage());

                // 检查是否需要切换模型
                if (errorCount >= MODEL_SWITCH_THRESHOLD && PRIMARY_MODEL.equals(currentModel)) {
                    log.warn("主模型 {} 失败超过{}次，切换到备用模型 {}", PRIMARY_MODEL, MODEL_SWITCH_THRESHOLD, BACKUP_MODEL);
                    currentModel = BACKUP_MODEL;
                    errorCount = 0; // 重置错误计数
                } else if (BACKUP_MODEL.equals(currentModel)) {
                    // 备用模型也失败了，继续使用备用模型重试
                    log.warn("备用模型 {} 也失败，继续重试", BACKUP_MODEL);
                }

                // 如果是最后一次尝试，返回错误信息
                if (errorCount >= MODEL_SWITCH_THRESHOLD && BACKUP_MODEL.equals(currentModel)) {
                    log.error("备用模型 {} 调用失败", currentModel);
                    return "API 调用发生异常: " + e.getMessage();
                }

                // 等待后重试
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "API 调用被中断";
                }
            }
        }
    }

    /**
     * 优化句子
     */
    private static String replaceContentCn(String content) {
        String result = content;
        result = result.replaceAll(" 6 Minute English ", "六分钟英语");
        result = result.replaceAll("医 管 局", "哈哈");
        result = result.replaceAll("乔吉", "乔治");
        result = result.replaceAll("成语", "谚语");
        result = result.replaceAll(" Phil", "菲尔");
        result = result.replaceAll(" Beth", "贝丝");
        result = result.replaceAll("Beth，", "贝丝，");
        result = result.replaceAll("Sean，", "肖恩，");
        result = result.replaceAll("Rob", "罗伯");
        result = result.replaceAll("伟大！", "太好了！");
        result = result.replaceAll("右。", "好的。"); // Right应该翻译成好的，而不是右
        result = result.replaceAll("山 姆", "山姆");
        result = result.replaceAll("六分钟", "六分钟");
        result = result.replaceAll(" 6 分钟", "六分钟");
        result = result.replaceAll(";", "；");
        result = result.replaceAll("\"拯救大象\"（Save the Elephants）", "\"拯救大象\"");
        result = result.replaceAll("六分钟又到了",
                "六分钟时间又到了"); // 英国广播公司（BBC）
        result = result.replaceAll("英国广播公司（BBC）",
                "英国广播公司"); // 英国广播公司（BBC）
        result = result.replaceAll("——", " —— ");// ——

        return result;
    }

    /**
     * 根据英文名获取标准中文译名
     *
     * @param englishName 英文名
     * @return 中文译名
     */
    private static String getChineseName(String englishName) {
        return switch (englishName.trim()) {
            case "Rob" -> "罗伯";
            case "Beth" -> "贝丝";
            case "Neil" -> "尼尔";
            case "Sam" -> "萨姆";
            case "Georgina" -> "乔治娜";
            case "George" -> "乔治";
            case "Dan" -> "丹";
            case "Catherine" -> "凯瑟琳";
            case "Tom" -> "汤姆";
            case "Sophie" -> "索菲";
            default -> englishName;
        };
    }

    public static boolean translateEngSrc(String folderName) {
        String engSrtFileName = CommonUtil.getFullPathFileName(folderName, "eng", ".srt");
        List<SubtitleEntity> engSubtitles = CdFileUtil.readSrtFileContent(engSrtFileName);

        if (CollectionUtil.isEmpty(engSubtitles)) {
            log.error("源文件 {} 内容为空，无法翻译。", engSrtFileName);
            return false;
        }

        List<String> originalTextLines = engSubtitles.stream()
                .map(SubtitleEntity::getSubtitle)
                .collect(Collectors.toList());

        // 采用分块翻译策略，并增加重试和断点续传
        final int CHUNK_SIZE = 20; // 每20行作为一个翻译块
        final int MAX_API_RETRIES = 5; // API调用重试次数
        final int RETRY_INTERVAL_MS = 5000; // 重试间隔5秒
        final int RETRY_INTERVAL_429_MS = 30000; // 429错误重试间隔30秒
        final int CHUNK_INTERVAL_MS = 1000; // 块之间间隔1秒，避免触发限流

        List<List<String>> chunks = ListUtil.split(originalTextLines, CHUNK_SIZE);
        List<String> allTranslatedLines = new ArrayList<>();
        List<String> tempPartFiles = new ArrayList<>(); // 用于记录临时文件名，方便最后删除

        for (int i = 0; i < chunks.size(); i++) {
            List<String> chunk = chunks.get(i);
            String partFileName = CommonUtil.getFullPathFileName(folderName, "chn_part" + String.format("%02d", i + 1), ".txt");
            tempPartFiles.add(partFileName);

            // 断点续传：检查临时文件是否已存在
            if (FileUtil.exist(partFileName) && !FileUtil.readLines(partFileName, StandardCharsets.UTF_8).isEmpty()) {
                log.info("找到已翻译的临时文件 {}，跳过此块的翻译。", partFileName);
                allTranslatedLines.addAll(FileUtil.readLines(partFileName, StandardCharsets.UTF_8));
                continue;
            }

            boolean chunkSuccess = false;
            for (int retry = 0; retry < MAX_API_RETRIES; retry++) {
                // Prompt强化：为每一行添加编号
                List<String> numberedChunk = new ArrayList<>();
                for (int j = 0; j < chunk.size(); j++) {
                    numberedChunk.add((j + 1) + ". " + chunk.get(j));
                }
                String textToTranslate = String.join("\n", numberedChunk);

                // Prompt强化：使用更严格的指令和示例 (Few-shot Prompting)
                String prompt = "You are a professional translator for subtitles. Your task is to translate the following numbered English lines into Simplified Chinese.\n"
                        + "It is CRITICAL that you maintain the exact same number of lines in your output as in the input. Each numbered line in the input must correspond to exactly one numbered line in the output. Do not merge lines.\n\n"
                        + "Here is an example:\n"
                        + "Input:\n"
                        + "1. Hello.\n"
                        + "2. And I'm Neil.\n"
                        + "3. How was your weekend?\n\n"
                        + "Output:\n"
                        + "1. 你好。\n"
                        + "2. 我是尼尔。\n"
                        + "3. 你周末过得怎么样？\n\n"
                        + "Now, please translate the following lines:\n"
                        + textToTranslate;

                log.info("正在调用 MiniMax API 翻译SRT字幕... (块 {}/{}, 尝试 {}/{})", i + 1, chunks.size(), retry + 1, MAX_API_RETRIES);
                String translatedText = callMiniMaxApi(prompt);

                // 检测是否是429错误
                boolean is429Error = StrUtil.isNotBlank(translatedText) && translatedText.contains("Too Many Requests");

                if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常") || is429Error) {
                    if (is429Error) {
                        log.warn("检测到 429 错误（速率限制），{}秒后重试...", RETRY_INTERVAL_429_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_429_MS);
                    } else {
                        log.error("MiniMax API 翻译SRT块失败，返回内容为空或包含错误信息。块索引: {}, 尝试: {}", i, retry + 1);
                        if (retry < MAX_API_RETRIES - 1) {
                            log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                            ThreadUtil.sleep(RETRY_INTERVAL_MS);
                        }
                    }
                    continue; // 继续下一次重试
                }

                List<String> translatedChunkLinesWithNumbers = Arrays.asList(translatedText.split("\n"));

                // 1. 逐块校验行数
                if (translatedChunkLinesWithNumbers.size() != chunk.size()) {
                    log.error("SRT翻译块后行数与原文不匹配! 块索引: {}, 尝试: {}, 原文: {}行, 译文: {}行.", i, retry + 1, chunk.size(), translatedChunkLinesWithNumbers.size());
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
                    }
                    continue; // 行数不匹配，直接进入下一次重试
                }

                // 2. 后处理：移除翻译结果中的编号
                List<String> translatedChunkLines = new ArrayList<>();
                for (String line : translatedChunkLinesWithNumbers) {
                    translatedChunkLines.add(line.replaceAll("^\\d+\\.\\s*", ""));
                }

                // 3. 内容校验：检查是否有未翻译的行
                List<String> untranslatedLinesInChunk = new ArrayList<>();
                for (String translatedLine : translatedChunkLines) {
                    // 如果是URL，则不视为未翻译
                    if (isUrl(translatedLine)) {
                        continue;
                    }
                    if (containsEnglishLetters(translatedLine) && !ChineseCharacterUtil.containsChinese(translatedLine) && !StrUtil.isNumeric(translatedLine.replaceAll("[.:]", ""))) {
                        untranslatedLinesInChunk.add(translatedLine);
                    }
                }

                if (untranslatedLinesInChunk.isEmpty()) {
                    log.info("块 {} 翻译和内容校验成功。", i + 1);
                    FileUtil.writeLines(translatedChunkLines, partFileName, StandardCharsets.UTF_8);
                    log.info("成功写入临时文件: {}", partFileName);
                    allTranslatedLines.addAll(translatedChunkLines);
                    chunkSuccess = true;

                    // 块翻译成功后，等待一段时间再处理下一个块，避免触发限流
                    if (i < chunks.size() - 1) {
                        log.debug("块 {} 翻译完成，等待 {}ms 后处理下一个块", i + 1, CHUNK_INTERVAL_MS);
                        ThreadUtil.sleep(CHUNK_INTERVAL_MS);
                    }

                    break; // 当前块成功，跳出重试循环
                } else {
                    log.error("SRT翻译块内容校验失败! 块索引: {}, 尝试: {}, 发现未翻译的行: {}", i, retry + 1, untranslatedLinesInChunk);
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
                    }
                }
            }

            if (!chunkSuccess) {
                log.error("块 {} 翻译失败，已达到最大重试次数。流程终止。", i + 1);
                return false; // 某个块最终失败，终止整个翻译过程
            }
        }

        // 最终合并
        if (allTranslatedLines.size() != originalTextLines.size()) {
            log.error("最终合并时发现总行数不匹配! 原文总行数: {}, 翻译总行数: {}. 无法生成chn.srt.", originalTextLines.size(), allTranslatedLines.size());
            return false;
        }

        List<String> newSrtContent = new ArrayList<>();
        for (int i = 0; i < engSubtitles.size(); i++) {
            SubtitleEntity originalSubtitle = engSubtitles.get(i);
            String translatedLine = replaceContentCn(allTranslatedLines.get(i)); // 应用后处理规则

            newSrtContent.add(String.valueOf(originalSubtitle.getSubIndex()));
            newSrtContent.add(originalSubtitle.getTimeStr());
            newSrtContent.add(translatedLine);
            newSrtContent.add("");
        }

        String srcFileNameCn = CommonUtil.getFullPathFileName(folderName, "chn",
                ".srt");
        // 写中文翻译文本
        CdFileUtil.writeToFile(srcFileNameCn, newSrtContent);
        log.info("成功生成最终中文SRT文件: {}", srcFileNameCn);

        // 清理：删除所有临时文件
        for (String tempFile : tempPartFiles) {
            FileUtil.del(tempFile);
        }
        log.info("已清理所有临时翻译文件。");
        return true;
    }

    /**
     * 辅助方法：检查字符串是否包含英文字母
     *
     * @param text 要检查的字符串
     * @return 如果包含则返回 true，否则返回 false
     */
    private static boolean containsEnglishLetters(String text) {
        if (text == null) {
            return false;
        }
        return text.matches(".*[a-zA-Z].*");
    }

    /**
     * 辅助方法：检查字符串是否为URL
     * 特殊处理 bbclearningenglish.com 等不需要翻译的域名
     *
     * @param text 要检查的字符串
     * @return 如果是URL或特殊域名则返回 true
     */
    private static boolean isUrl(String text) {
        if (text == null) {
            return false;
        }
        String trimmedText = text.trim().toLowerCase();
        // 检查标准URL格式
        if (trimmedText.startsWith("www.") || trimmedText.startsWith("http://") || trimmedText.startsWith("https://")) {
            return true;
        }
        // 特殊处理 bbclearningenglish.com 等不需要翻译的域名
        return trimmedText.contains("bbclearningenglish.com") ||
               trimmedText.equals("bbclearningenglish.com");
    }

    /**
     * 生成对话脚本合集
     */
    public static File mergeScriptContent(String scriptDialogFileName,
                                          String scriptDialogCnFileName, String scriptDialogMergeFileName) {

        List<DialogSingleEntity> dialogSingleEntityListEn = CdFileUtil.genDialogSingleEntityList(
                scriptDialogFileName);

        List<String> newList = mergeContent(scriptDialogCnFileName,
                dialogSingleEntityListEn);

        // 写中文翻译文本
        CdFileUtil.writeToFile(scriptDialogMergeFileName, newList);
        return new File(scriptDialogMergeFileName);
    }

    private static @NotNull List<String> mergeContent(String srcFileNameCn,
                                                      List<DialogSingleEntity> dialogSingleEntityListEn) {
        List<DialogSingleEntity> DialogSingleEntityListCn = CdFileUtil.genDialogSingleEntityList(
                srcFileNameCn);
        DialogSingleEntity dialogSingleEntityEn;
        DialogSingleEntity dialogSingleEntityCn;
        String scriptEn;
        String scriptCn;
        List<String> newList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(dialogSingleEntityListEn)
                && CollectionUtil.isNotEmpty(DialogSingleEntityListCn)
                && dialogSingleEntityListEn.size() == DialogSingleEntityListCn.size()) {
            for (int i = 0; i < dialogSingleEntityListEn.size(); i++) {
                dialogSingleEntityEn = dialogSingleEntityListEn.get(i);
                scriptEn = dialogSingleEntityEn.getContentEn();
                scriptEn = scriptEn.replaceAll(
                        "Hello. This is 6 Minute English from BBC Learning English. ", "");
                dialogSingleEntityCn = DialogSingleEntityListCn.get(i);
                scriptCn = dialogSingleEntityCn.getContentEn();
                scriptCn = scriptCn.replaceAll("你好。这是来自BBC学习英语的六分钟英语。",
                        "");
                newList.add(dialogSingleEntityEn.getHostEn() + "("
                        + dialogSingleEntityCn.getHostEn() + ")");
                newList.add(scriptEn + "\r\n" + scriptCn);
                newList.add("");
            }
        } else {
            if (CollectionUtil.isEmpty(dialogSingleEntityListEn)) {
                System.out.println("dialogSingleEntityListEn 为空。");
            } else if (CollectionUtil.isEmpty(DialogSingleEntityListCn)) {
                System.out.println("DialogSingleEntityListCn 为空。");
            } else {
                System.out.println(
                        "两个脚本格式不对，实体大小分别为：" + dialogSingleEntityListEn.size()
                                + "\t:\t"
                                + DialogSingleEntityListCn.size()); //
            }
        }
        return newList;
    }

    /**
     * 生成平台描述文件
     */
    public static File genDescription(String scriptDialogMergeFileName, String descriptionFileName) {
        List<String> scriptList = CdFileUtil.readFileContent(scriptDialogMergeFileName);
        if (CollectionUtil.isEmpty(scriptList)) {
            log.error("源文件 {} 内容为空，无法生成描述文件。", scriptDialogMergeFileName);
            return FileUtil.touch(descriptionFileName);
        }

        String textToDescribe = String.join("\n", scriptList);
        String prompt = "请根据以下对话内容，生成一个适合国内视频平台的视频描述（Description）。\n"
                + "描述应该简洁、有吸引力，能够准确概括视频的主要内容。\n"
                + "请直接输出描述内容，不需要任何额外的解释或格式。\n\n"
                + textToDescribe;

        log.info("正在调用 MiniMax API 生成平台描述文件...");
        String description = callMiniMaxApi(prompt);

        if (StrUtil.isBlank(description) || description.contains("API 调用发生异常")) {
            log.error("MiniMax API 生成描述失败");
            return FileUtil.touch(descriptionFileName);
        }

        return FileUtil.writeString(description, descriptionFileName, StandardCharsets.UTF_8);
    }

    /**
     * 通用内容生成方法（替换 Gemini API）
     *
     * @param prompt 提示词
     * @return 生成的内容
     */
    public static String generateContent(String prompt) {
        log.info("正在调用 MiniMax API 生成内容...");
        String result = callMiniMaxApi(prompt);

        if (StrUtil.isBlank(result) || result.contains("API 调用发生异常")) {
            log.error("MiniMax API 生成内容失败");
            return "API 调用发生异常";
        }

        return result;
    }
}
