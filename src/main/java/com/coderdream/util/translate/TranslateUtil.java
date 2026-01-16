package com.coderdream.util.translate;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.DialogSingleEntity;
import com.coderdream.entity.SubtitleEntity;
import com.coderdream.util.BbcConstants;
import com.coderdream.util.CommonUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.CdTimeUtil;
import com.coderdream.util.cd.ChineseCharacterUtil;
import com.coderdream.util.gemini.GeminiApiClient;
import com.coderdream.util.ollama.OllamaTranslateUtil;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CoderDream
 */
@Slf4j
public class TranslateUtil {

    public static void main(String[] args) {
        String folderName = "240104"; // 用于测试的文件夹
        genScriptDialogCn(folderName, CommonUtil.getFullPathFileName(folderName, "script_dialog", "_cn.txt"));
    }

    /**
     * @param str 输入字符串
     * @return 去除英文括号内内容，例如：大括号外面的内容（Michael Collins）的内容（Michael Collins）
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

    public static List<String> translateTitle(List<String> folderNameList,
                                              String fileName) {
//        if (fileName == null) {
//            fileName = "script_raw";
//        }
        // 220303_script.txt
//        if (fileName.endsWith("_script.txt")) {
//            // TODO
//        }

        // 6 Minute English
        // word-for-word transcript
        List<String> titleList = new ArrayList<>();
        for (String folderName : folderNameList) {
            fileName = folderName + "_script"; // TODO 指定
            String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                    ".txt");
            List<String> stringList = CdFileUtil.readFileContent(srcFileName);

            String title;
            int size = stringList.size();
            if (CollectionUtil.isNotEmpty(stringList)) {
                //    titleList.add(stringList.get(0));
                titleList.add(getTitleString(stringList));
            }
        }

        String textTitleList = titleList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringListTitleCn = TranslatorTextUtil.translatorText(
                textTitleList);
        String[] arr = new String[0];
        for (int i = 0; i < stringListTitleCn.size(); i++) {
            String temp = stringListTitleCn.get(i);
            arr = temp.split("\r\n");
        }

        List<String> titleCnList = Arrays.asList(arr);
        List<String> newList = new ArrayList<>();
        String titleTranslate;
        for (int i = 0; i < arr.length; i++) {
            titleTranslate =
                    folderNameList.get(i).substring(2) + "\t" + titleList.get(i) + "\t"
                            + arr[i];
            System.out.println(titleTranslate);
            newList.add(titleTranslate);
        }

//        String srcFileNameCn = BbcConstants.ROOT_FOLDER_NAME + File.separator + "title.txt";
        // 写中文翻译文本
//         CdFileUtil.writeToFile(srcFileNameCn, newList);
        return titleCnList;
    }

    public static List<String> translateTitleWithScriptFile(
            List<String> folderNameList, String fileName) {
//        if (fileName == null) {
//            fileName = "script_raw";
//        }
        // 220303_script.txt
//        if (fileName.endsWith("_script.txt")) {
//            // TODO
//        }

        // 6 Minute English
        // word-for-word transcript
        List<String> titleList = new ArrayList<>();
        for (String folderName : folderNameList) {
            fileName = folderName + "_script"; // TODO 指定
            String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                    ".txt");
            List<String> stringList = CdFileUtil.readFileContent(srcFileName);

            String title;
            int size = stringList.size();
            if (CollectionUtil.isNotEmpty(stringList)) {
                //    titleList.add(stringList.get(0));
                titleList.add(getTitleString(stringList));
            }
        }

        String textTitleList = titleList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringListTitleCn = TranslatorTextUtil.translatorText(
                textTitleList);
        String[] arr = new String[0];
        for (int i = 0; i < stringListTitleCn.size(); i++) {
            String temp = stringListTitleCn.get(i);
            arr = temp.split("\r\n");
        }

        List<String> titleCnList = Arrays.asList(arr);
        List<String> newList = new ArrayList<>();
        String titleTranslate;
        for (int i = 0; i < arr.length; i++) {
            titleTranslate =
                    folderNameList.get(i).substring(2) + "\t" + titleList.get(i) + "\t"
                            + arr[i];
            System.out.println(titleTranslate);
            newList.add(titleTranslate);
        }

//        String srcFileNameCn = BbcConstants.ROOT_FOLDER_NAME + File.separator + "title.txt";
        // 写中文翻译文本
//         CdFileUtil.writeToFile(srcFileNameCn, newList);
        return titleCnList;
    }

    private static String getTitleString(List<String> stringList) {
        int startIndex = 0;
        int endIndex = 0;

        for (int i = 0; i < stringList.size(); i++) {
            if (stringList.get(i).contains("6 Minute English")) {
                startIndex = i;
            }
            if (stringList.get(i).contains("word-for-word transcript")) {
                endIndex = i;
                break;
            }
        }
        StringBuilder result = new StringBuilder();

        for (int i = startIndex + 1; i < endIndex; i++) {
            result.append(stringList.get(i));
        }

        return result.toString();
    }

    /**
     * 手工粘贴文本
     *
     * @param folderNameList
     * @param fileName
     * @return
     */
    public static List<String> translateTitleManual(List<String> folderNameList,
                                                    String fileName) {
        if (fileName == null) {
            fileName = "script_raw";
        }
        List<String> titleList = new ArrayList<>();
        for (String folderName : folderNameList) {
            String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                    ".txt");
            List<String> stringList = CdFileUtil.readFileContent(srcFileName);

            String title;
            int size = stringList.size();

            for (int i = 0; i < size; i++) {
                String tempStr = stringList.get(i);
//                System.out.println("tempStr: " + tempStr);
                if ("INTERMEDIATE LEVEL".equals(tempStr)) {
                    title = stringList.get(i + 1);
                    titleList.add(title);
                    break;
                }
            }
        }

        String textTitleList = titleList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringListTitleCn = TranslatorTextUtil.translatorText(
                textTitleList);
        String[] arr = new String[0];
        for (int i = 0; i < stringListTitleCn.size(); i++) {
            String temp = stringListTitleCn.get(i);
            arr = temp.split("\r\n");
        }

        List<String> titleCnList = Arrays.asList(arr);
        List<String> newList = new ArrayList<>();
        String titleTranslate;
        for (int i = 0; i < arr.length; i++) {
            titleTranslate =
                    folderNameList.get(i).substring(2) + "\t" + titleList.get(i) + "\t"
                            + arr[i];
            System.out.println(titleTranslate);
            newList.add(titleTranslate);
        }

        String srcFileNameCn =
                BbcConstants.ROOT_FOLDER_NAME + File.separator + "title.txt";
        // 写中文翻译文本
        CdFileUtil.writeToFile(srcFileNameCn, newList);
        return titleCnList;
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

        // 1. 构造 Prompt 并调用 Gemini API
        String textToTranslate = String.join("\n", stringList);
        String prompt = "Please translate the following English dialogue into Simplified Chinese. "
                + "Maintain the original line-by-line format where each block consists of a speaker's name followed by their line. "
                + "Do not add any extra explanations or introductory text. Just provide the direct translation.\n\n"
                + textToTranslate;

        log.info("正在调用 Gemini API 翻译对话脚本...");
        String translatedText = GeminiApiClient.generateContent(prompt);

        if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常")) {
            log.error("Gemini API 翻译失败，返回内容为空或包含错误信息。");
            return FileUtil.touch(srcFileNameCn);
        }

        List<String> newList = new ArrayList<>();

        // 2. 解析翻译结果并进行后处理
        String[] translatedLines = translatedText.split("\n");

        // 【新增】健壮性检查：确保翻译后的行数与原文一致
        if (translatedLines.length != stringList.size()) {
            log.error("翻译后行数与原文不匹配! 原文: {}行, 译文: {}行. 将写入原始翻译结果以供排查.", stringList.size(), translatedLines.length);
            return FileUtil.writeLines(Arrays.asList(translatedLines), srcFileNameCn, StandardCharsets.UTF_8);
        }

        if (translatedLines.length % 2 == 0) { // 确保是成对的对话
            for (int i = 0; i < translatedLines.length; i += 2) {
                // 【核心修改】使用原文的英文名进行翻译，而不是依赖API的翻译结果
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
     * 优化句子
     */
    private static String replaceContentCn(String content) {
        String result = content;
//    result = result.replaceAll("抢", "罗伯");
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
        result = result.replaceAll("6分钟", "六分钟");
        result = result.replaceAll(" 6 分钟", "六分钟");
        result = result.replaceAll(";", "；");
        result = result.replaceAll("“拯救大象”（Save the Elephants）", "“拯救大象”");
        result = result.replaceAll("六分钟又到了",
                "六分钟时间又到了"); // 英国广播公司（BBC）
        result = result.replaceAll("英国广播公司（BBC）",
                "英国广播公司"); // 英国广播公司（BBC）
        result = result.replaceAll("——", " —— ");// ——

        return result;
    }

    /**
     * 根据英文名获取标准中文译名。
     * 这种方式比依赖AI翻译人名更可靠。
     *
     * @param englishName 英文名
     * @return 中文译名
     */
    private static String getChineseName(String englishName) {
        // A simple mapping for common names in 6 Minute English
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
            // 如果没有匹配，返回原始英文名，以便观察和添加新映射
            default -> englishName;
        };
    }

    /**
     * 优化句子
     *
     * @param folderName
     * @param arr
     * @param j
     */
    private static void upgradeTranslate(String folderName, String[] arr, int j,
                                         List<String> subtitleList) {
        if ("抢".equals(arr[j])) {
            arr[j] = "罗伯";
        }
        if ("山 姆".equals(arr[j])) {
            arr[j] = "山姆";
        }
        if ("井".equals(arr[j])) {
            arr[j] = "嗯";
        }
        if ("右".equals(arr[j])) {
            arr[j] = "是的";
        }

        if ("右！".equals(arr[j])) {
            arr[j] = "好的！";
        }

        // 医 管 局！
        // （Michael Collins） 去掉大括号及大括号内的内容
        arr[j] = removeEnContent(arr[j]);
        arr[j] = arr[j].replaceAll(" 6 Minute English ", "六分钟英语");
        arr[j] = arr[j].replaceAll("医 管 局", "哈哈");
        arr[j] = arr[j].replaceAll("乔吉", "乔治");
        arr[j] = arr[j].replaceAll("成语", "谚语");
        arr[j] = arr[j].replaceAll("Rob", "罗伯");
        arr[j] = arr[j].replaceAll("伟大！", "太好了！");
        arr[j] = arr[j].replaceAll("右。", "好的。"); // Right应该翻译成好的，而不是右
        arr[j] = arr[j].replaceAll("山 姆", "山姆");
        arr[j] = arr[j].replaceAll("6分钟", "六分钟");
        arr[j] = arr[j].replaceAll(" 6 分钟", "六分钟");
        arr[j] = arr[j].replaceAll(";", "；");
        arr[j] = arr[j].replaceAll("“拯救大象”（Save the Elephants）", "“拯救大象”");
        arr[j] = arr[j].replaceAll("六分钟又到了",
                "六分钟时间又到了"); // 英国广播公司（BBC）
        arr[j] = arr[j].replaceAll("英国广播公司（BBC）",
                "英国广播公司"); // 英国广播公司（BBC）

        arr[j] = arr[j].replaceAll("——", " —— ");// ——
        if (-1 != arr[j].lastIndexOf("程序") && -1 != subtitleList.get(j)
                .lastIndexOf("programme")) {
            arr[j] = arr[j].replaceAll("程序", "节目");
        }
        if (-1 != arr[j].lastIndexOf("课程") && -1 != subtitleList.get(j)
                .lastIndexOf("programme")) {
            arr[j] = arr[j].replaceAll("课程", "节目");
        }

        // 维克托
        arr[j] = arr[j].replaceAll("Victor;", "维克多");

        arr[j] = arr[j].trim();//去掉前后空格
        if ("231026".equals(folderName)) {
            arr[j] = arr[j].replaceAll("一百个", "一百岁");
        }

        if ("231109".equals(folderName)) {
            if (arr[j].equals("幸运")) {
                arr[j] = arr[j].replaceAll("幸运", "幸运的是");
            }
        }

        // 针对230413的AI翻译优化
        if ("230413".equals(folderName)) {
            arr[j] = arr[j].replaceAll("垃圾场", "情绪低落");
            arr[j] = arr[j].replaceAll("垃圾堆", "情绪低落");
            arr[j] = arr[j].replaceAll("最尖锐的", "极度");
            arr[j] = arr[j].replaceAll(" Covid", "冠状病毒");
            arr[j] = arr[j].replaceAll("Covid", "冠状病毒");
            arr[j] = arr[j].replaceAll("英国广播公司（BBC）", "BBC");//英国广播公司（BBC）
            arr[j] = arr[j].replaceAll("《纪录片》（The Documentary）",
                    "《纪录片》");//《纪录片》（The Documentary）
            arr[j] = arr[j].replaceAll("海伦·罗素（Helen Russell）",
                    "海伦·罗素");// 海伦·罗素（Helen Russell）
            arr[j] = arr[j].replaceAll("托马斯·迪克森（Thomas Dixon）",
                    "托马斯·迪克森");// 托马斯·迪克森（Thomas Dixon）
        }
        // 针对230302的AI翻译优化
        if ("230302".equals(folderName)) {
            arr[j] = arr[j].replaceAll("Rob", "'dunk'");
            arr[j] = arr[j].replaceAll("扣篮", "'dunk'");
            arr[j] = arr[j].replaceAll("英国广播公司（BBC）", "BBC");//英国广播公司（BBC）
            arr[j] = arr[j].replaceAll("迈克尔·罗森（Michael Rosen）", "迈克尔·罗森");
            arr[j] = arr[j].replaceAll("朱莉·塞迪维（Julie Sedivy）", "朱莉·塞迪维");
            arr[j] = arr[j].replaceAll("计划", "节目");
        }

        //
        // 针对230330的AI翻译优化
        if ("230330".equals(folderName)) {
            arr[j] = arr[j].replaceAll("历克斯·米尔克（Alex Mielke）", "历克斯·米尔克");
        }
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

        // 【核心优化】采用分块翻译策略，并增加重试和断点续传
        final int CHUNK_SIZE = 20; // 每20行作为一个翻译块
        final int MAX_API_RETRIES = 5; // API调用重试次数
        final int RETRY_INTERVAL_MS = 5000; // 重试间隔5秒

        List<List<String>> chunks = ListUtil.split(originalTextLines, CHUNK_SIZE);
        List<String> allTranslatedLines = new ArrayList<>();
        List<String> tempPartFiles = new ArrayList<>(); // 用于记录临时文件名，方便最后删除

        for (int i = 0; i < chunks.size(); i++) {
            List<String> chunk = chunks.get(i);
            String partFileName = CommonUtil.getFullPathFileName(folderName, "chn_part" + String.format("%02d", i + 1), ".txt");
            tempPartFiles.add(partFileName);

            // 【断点续传】检查临时文件是否已存在
            if (FileUtil.exist(partFileName) && !FileUtil.readLines(partFileName, StandardCharsets.UTF_8).isEmpty()) {
                log.info("找到已翻译的临时文件 {}，跳过此块的翻译。", partFileName);
                allTranslatedLines.addAll(FileUtil.readLines(partFileName, StandardCharsets.UTF_8));
                continue;
            }

            boolean chunkSuccess = false;
            for (int retry = 0; retry < MAX_API_RETRIES; retry++) {
                // 【Prompt强化】为每一行添加编号
                List<String> numberedChunk = new ArrayList<>();
                for (int j = 0; j < chunk.size(); j++) {
                    numberedChunk.add((j + 1) + ". " + chunk.get(j));
                }
                String textToTranslate = String.join("\n", numberedChunk);

                // 【Prompt强化】使用更严格的指令和示例 (Few-shot Prompting)
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


                log.info("正在调用 Gemini API 翻译SRT字幕... (块 {}/{}, 尝试 {}/{})", i + 1, chunks.size(), retry + 1, MAX_API_RETRIES);
                String translatedText = GeminiApiClient.generateContent(prompt);

                if (StrUtil.isBlank(translatedText) || translatedText.contains("API 调用发生异常")) {
                    log.error("Gemini API 翻译SRT块失败，返回内容为空或包含错误信息。块索引: {}, 尝试: {}", i, retry + 1);
                    if (retry < MAX_API_RETRIES - 1) {
                        log.info("{}秒后重试...", RETRY_INTERVAL_MS / 1000);
                        ThreadUtil.sleep(RETRY_INTERVAL_MS);
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

                // 2. 【后处理】移除翻译结果中的编号
                List<String> translatedChunkLines = new ArrayList<>();
                for (String line : translatedChunkLinesWithNumbers) {
                    translatedChunkLines.add(line.replaceAll("^\\d+\\.\\s*", ""));
                }

                // 3. 【内容校验】检查是否有未翻译的行
                List<String> untranslatedLinesInChunk = new ArrayList<>();
                for (String translatedLine : translatedChunkLines) {
                    // 【新增】如果是URL，则不视为未翻译
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

        // 【清理】删除所有临时文件
        for (String tempFile : tempPartFiles) {
            FileUtil.del(tempFile);
        }
        log.info("已清理所有临时翻译文件。");
        return true;
    }

    /**
     * 辅助方法：检查字符串是否包含英文字母。
     *
     * @param text 要检查的字符串
     * @return 如果包含则返回 true，否则返回 false
     */
    private static boolean containsEnglishLetters(String text) {
        if (text == null) {
            return false;
        }
        // 使用正则表达式匹配任何英文字母（不区分大小写）
        return text.matches(".*[a-zA-Z].*");
    }

    /**
     * 辅助方法：检查字符串是否为URL。
     *
     * @param text 要检查的字符串
     * @return 如果是URL则返回 true
     */
    private static boolean isUrl(String text) {
        if (text == null) {
            return false;
        }
        String trimmedText = text.trim().toLowerCase();
        return trimmedText.startsWith("www.") || trimmedText.startsWith("http://") || trimmedText.startsWith("https://");
    }

    public static void translateEngSrc(String folderName, String fileName) {
        //String fileName = "eng";
//        String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName, ".srt");
        String srcFileName = folderName + fileName + ".eng" + ".srt";
        // readSrcFileContent

        List<SubtitleEntity> SubtitleEntityList = CdFileUtil.readSrtFileContent(
                srcFileName);

        List<String> subtitleList = SubtitleEntityList.stream()
                .map(SubtitleEntity::getSubtitle)
                .collect(Collectors.toList());

        String text = subtitleList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringListCn = TranslatorTextUtil.translatorText(text);

        List<String> newList = new ArrayList<>();
        List<String> newListEnCn = new ArrayList<>();
        List<String> lrcListEnCn = new ArrayList<>();
        SubtitleEntity SubtitleEntity;
        String timeStr;
        String lrc;
        for (int i = 0; i < stringListCn.size(); i++) {
            String temp = stringListCn.get(i);
            String[] arr = temp.split("\r\n");
            // 检查大小
            if (arr.length != SubtitleEntityList.size()) {
                System.out.println("###");
                break;
            }
            for (int j = 0; j < arr.length; j++) {
                // 优化翻译
                upgradeTranslate(folderName, arr, j, subtitleList);

                System.out.println(arr[j]);
                SubtitleEntity = SubtitleEntityList.get(j);
                newList.add(SubtitleEntity.getSubIndex() + "");
                newList.add(SubtitleEntity.getTimeStr());
                newList.add(arr[j]);
                newList.add("");

                newListEnCn.add(SubtitleEntity.getSubIndex() + "");
                newListEnCn.add(SubtitleEntity.getTimeStr());
                newListEnCn.add(subtitleList.get(j) + "\r" + arr[j]);
                newListEnCn.add("");
                timeStr = SubtitleEntity.getTimeStr();
                timeStr = timeStr.substring(3, 11);
                timeStr = timeStr.replaceAll(",", ".");
                lrc = "[" + timeStr + "]" + subtitleList.get(j) + "|" + arr[j];
                lrcListEnCn.add(lrc);
            }
        }

        String srcFileNameCn = folderName + fileName + ".chn" + ".srt";
        // 写中文翻译文本
        CdFileUtil.writeToFile(srcFileNameCn, newList);

    }

//    public static void translateSrc(String srcFileNameEng,
//                                    String srcFileNameChn) {
//        //String fileName = "eng";
////        String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName, ".srt");
////    String srcFileName = folderName + fileName + ".eng" + ".srt";
//        // readSrcFileContent
//
//        List<SubtitleEntity> SubtitleEntityList = CdFileUtil.readSrtFileContent(
//                srcFileNameEng);
//
//        List<String> subtitleList = SubtitleEntityList.stream()
//                .map(SubtitleEntity::getSubtitle)
//                .collect(Collectors.toList());
//
////    String text = subtitleList.stream().map(String::valueOf)
////      .collect(Collectors.joining("\r\n"));
////    List<String> stringListCn = TranslatorTextUtil.translatorText(text);
//        List<String> stringListCn = new ArrayList<>();
//        // 按200行分割字符串数组
//        List<List<String>> stringListEnGroup = ListUtil.split(subtitleList, 200);
//        log.info("stringListEnGroup size: {}", stringListEnGroup.size());
//        for (List<String> stringListEn : stringListEnGroup) {
//            String text = stringListEn.stream().map(String::valueOf)
//                    .collect(Collectors.joining("\r\n"));
//            stringListCn.addAll(TranslatorTextUtil.translatorText(text));
//            ThreadUtil.sleep(2000);
//            log.info("TranslatorTextUtil.translatorText(text) size: {}",
//                    stringListCn.size());
//        }
//
//        List<String> newList = new ArrayList<>();
//        List<String> newListEnCn = new ArrayList<>();
//        List<String> lrcListEnCn = new ArrayList<>();
//        SubtitleEntity SubtitleEntity;
//        String timeStr;
//        String lrc;
//        for (int i = 0; i < stringListCn.size(); i++) {
//            String temp = stringListCn.get(i);
//            String[] arr = temp.split("\r\n");
//            // 检查大小
//            if (arr.length != SubtitleEntityList.size()) {
//                System.out.println("###");
//                break;
//            }
//            for (int j = 0; j < arr.length; j++) {
//                // 优化翻译
//                upgradeTranslate(arr, j, subtitleList);
//
//                System.out.println(arr[j]);
//                SubtitleEntity = SubtitleEntityList.get(j);
//                newList.add(SubtitleEntity.getSubIndex() + "");
//                newList.add(SubtitleEntity.getTimeStr());
//                newList.add(arr[j]);
//                newList.add("");
//
//                newListEnCn.add(SubtitleEntity.getSubIndex() + "");
//                newListEnCn.add(SubtitleEntity.getTimeStr());
//                newListEnCn.add(subtitleList.get(j) + "\r" + arr[j]);
//                newListEnCn.add("");
//                timeStr = SubtitleEntity.getTimeStr();
//                timeStr = timeStr.substring(3, 11);
//                timeStr = timeStr.replaceAll(",", ".");
//                lrc = "[" + timeStr + "]" + subtitleList.get(j) + "|" + arr[j];
//                lrcListEnCn.add(lrc);
//            }
//        }
//
////    String srcFileNameCn = folderName + fileName + ".chn" + ".srt";
//        if (CollectionUtil.isNotEmpty(newList)) {
//            // 写中文翻译文本
//            CdFileUtil.writeToFile(srcFileNameChn, newList);
//        } else {
//            System.out.println("newList is empty!");
//        }
//    }

    public static void translateSrcWithPlatform(String srcFileNameEng,
                                                String srcFileNameZhCn, String srcFileNameZhTw, String platformName) {
        Integer subListSize = 100;
        translateSrcWithPlatform(srcFileNameEng, srcFileNameZhCn,
                srcFileNameZhTw, platformName, subListSize);
    }

    public static void translateSrcWithPlatform(String srcFileNameEng,
                                                String srcFileNameZhCn, String srcFileNameZhTw, String platformName,
                                                Integer subListSize) {
        long startTime = System.currentTimeMillis(); // 记录开始时间
        List<SubtitleEntity> subtitleEntityList;
        List<SubtitleEntity> enSubtitleEntityList = CdFileUtil.readSrtFileContent(
                srcFileNameEng);

        List<String> subtitleList = enSubtitleEntityList.stream()
                .map(SubtitleEntity::getSubtitle)
                .toList();
        subtitleEntityList = writeTempSrtFiles(srcFileNameZhCn, platformName,
                subListSize, subtitleList);

        SubtitleEntity enSubtitleEntity;
        List<String> zhCnSrtStringList = new ArrayList<>();
        List<String> zhTwSrtStringList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(subtitleEntityList)
                && subtitleEntityList.size() == enSubtitleEntityList.size()) {
            log.info("相等 size: {}",
                    subtitleEntityList.size());
            for (int i = 0; i < enSubtitleEntityList.size(); i++) {
                enSubtitleEntity = enSubtitleEntityList.get(i);
                zhCnSrtStringList.add(enSubtitleEntity.getSubIndex() + "");
                zhCnSrtStringList.add(enSubtitleEntity.getTimeStr());
                zhTwSrtStringList.add(enSubtitleEntity.getSubIndex() + "");
                zhTwSrtStringList.add(enSubtitleEntity.getTimeStr());
                switch (platformName) {
                    case CdConstants.TRANSLATE_PLATFORM_GEMINI:
                        zhCnSrtStringList.add(ZhConverterUtil.toSimple(
                                subtitleEntityList.get(i).getSecondSubtitle()));
                        zhTwSrtStringList.add(ZhConverterUtil.toTraditional(
                                subtitleEntityList.get(i).getSecondSubtitle()));
                        break;
                    case CdConstants.TRANSLATE_PLATFORM_MSTTS:
                        zhCnSrtStringList.add(ZhConverterUtil.toSimple(
                                subtitleEntityList.get(i).getSubtitle()));
                        zhTwSrtStringList.add(
                                ZhConverterUtil.toTraditional(
                                        subtitleEntityList.get(i).getSubtitle()));
                        break;
                    default:
                        log.error("平台名称不正确: {}", platformName);
                        break;
                }
                zhCnSrtStringList.add("");
                zhTwSrtStringList.add("");
            }
        } else {
            log.error("返回结果: {}, 期待结果: {}",
                    subtitleEntityList.size(), enSubtitleEntityList.size());
        }

        if (CollectionUtil.isNotEmpty(zhCnSrtStringList)) {
            // 写中文翻译文本
            CdFileUtil.writeToFile(srcFileNameZhCn, zhCnSrtStringList);
            CdFileUtil.writeToFile(srcFileNameZhTw, zhTwSrtStringList);
            long elapsedTime = System.currentTimeMillis() - startTime; // 计算耗时
            log.info("写入完成，文件路径: {}，共计耗时：{}", srcFileNameZhCn,
                    CdTimeUtil.formatDuration(elapsedTime));
        } else {
            log.info("newList is empty!");
        }
    }

//  public static void translateScriptFileWithPlatform(String scriptFileName,
//    String srcFileNameZhCn, String srcFileNameZhTw, String platformName,
//    Integer subListSize) {
//    long startTime = System.currentTimeMillis(); // 记录开始时间
//    List<String> stringList = CdFileUtil.readSrcFileContent(
//      scriptFileName);
//
//    List<String> subtitleList = stringList.stream()
//      .map(SubtitleEntity::getSubtitle)
//      .toList();
//    subtitleEntityList = writeTempSrtFiles(srcFileNameZhCn, platformName,
//      subListSize, subtitleList);
//
//    SubtitleEntity enSubtitleEntity;
//    List<String> zhCnSrtStringList = new ArrayList<>();
//    List<String> zhTwSrtStringList = new ArrayList<>();
//    if (CollectionUtil.isNotEmpty(subtitleEntityList)
//      && subtitleEntityList.size() == stringList.size()) {
//      log.info("相等 size: {}",
//        subtitleEntityList.size());
//      for (int i = 0; i < stringList.size(); i++) {
//        enSubtitleEntity = stringList.get(i);
//        zhCnSrtStringList.add(enSubtitleEntity.getSubIndex() + "");
//        zhCnSrtStringList.add(enSubtitleEntity.getTimeStr());
//        zhTwSrtStringList.add(enSubtitleEntity.getSubIndex() + "");
//        zhTwSrtStringList.add(enSubtitleEntity.getTimeStr());
//        switch (platformName) {
//          case CdConstants.TRANSLATE_PLATFORM_GEMINI:
//            zhCnSrtStringList.add(ZhConverterUtil.toSimple(
//              subtitleEntityList.get(i).getSecondSubtitle()));
//            zhTwSrtStringList.add(ZhConverterUtil.toTraditional(
//              subtitleEntityList.get(i).getSecondSubtitle()));
//            break;
//          case CdConstants.TRANSLATE_PLATFORM_MSTTS:
//            zhCnSrtStringList.add(ZhConverterUtil.toSimple(
//              subtitleEntityList.get(i).getSubtitle()));
//            zhTwSrtStringList.add(
//              ZhConverterUtil.toTraditional(
//                subtitleEntityList.get(i).getSubtitle()));
//            break;
//          default:
//            log.error("平台名称不正确: {}", platformName);
//            break;
//        }
//        zhCnSrtStringList.add("");
//        zhTwSrtStringList.add("");
//      }
//    } else {
//      log.error("返回结果: {}, 期待结果: {}",
//        subtitleEntityList.size(), stringList.size());
//    }
//
//    if (CollectionUtil.isNotEmpty(zhCnSrtStringList)) {
//      // 写中文翻译文本
//      CdFileUtil.writeToFile(srcFileNameZhCn, zhCnSrtStringList);
//      CdFileUtil.writeToFile(srcFileNameZhTw, zhTwSrtStringList);
//      long elapsedTime = System.currentTimeMillis() - startTime; // 计算耗时
//      log.info("写入完成，文件路径: {}，共计耗时：{}", srcFileNameZhCn,
//        CdTimeUtil.formatDuration(elapsedTime));
//    } else {
//      log.info("newList is empty!");
//    }
//  }

    public static List<SubtitleEntity> writeTempSrtFiles(String srcFileNameZhCn,
                                                         String platformName, Integer subListSize, List<String> subtitleList) {
        List<SubtitleEntity> subtitleEntityList = new ArrayList<>();
        List<List<String>> stringListEnGroup = ListUtil.split(subtitleList,
                subListSize);
        int count = 0;
        for (List<String> stringListEn : stringListEnGroup) {
            count++;
            String number = String.format("%03d", count);
            List<SubtitleEntity> responseListTemp;
            String fileName = CdFileUtil.addPostfixToFileName(srcFileNameZhCn,
                    "_" + platformName + "_" + number);
            if (!CdFileUtil.isFileEmpty(fileName)) {
                responseListTemp = CdFileUtil.genSubtitleEntityList(
                        CdFileUtil.readFileContent(fileName), platformName);
            } else {
                responseListTemp = retryGetResponseList(stringListEn,
                        platformName);
            }
            if (CollectionUtil.isEmpty(responseListTemp)
                    || responseListTemp.size() != stringListEn.size()) {
                log.error(
                        "返回数组大小: {} != 返回数组大小: {}",
                        responseListTemp.size(), stringListEn.size());
            } else {
                log.info("请求正常，返回数组大小: {}",
                        responseListTemp.size());
                List<String> tempList = new ArrayList<>();
                for (SubtitleEntity subtitleEntity : responseListTemp) {
                    tempList.add(subtitleEntity.getSubtitle());
                    tempList.add(subtitleEntity.getSecondSubtitle());
                }
                CdFileUtil.writeToFile(fileName, tempList);
                subtitleEntityList.addAll(responseListTemp);
            }
        }

        return subtitleEntityList;
    }

    public static void translateSrcWithPlatform(String srcFileNameEng,
                                                String srcFileNameZhCn, String srcFileNameZhTw) {
        String platformName = CdConstants.TRANSLATE_PLATFORM_GEMINI;
        translateSrcWithPlatform(srcFileNameEng, srcFileNameZhCn, srcFileNameZhTw,
                platformName);
    }

    private static @NotNull List<SubtitleEntity> retryGetResponseList(
            List<String> stringListEn, String platformName) {
        List<SubtitleEntity> subtitleEntityList = null;
        List<String> responseList;
        switch (platformName) {
            case CdConstants.TRANSLATE_PLATFORM_GEMINI:
                String text = CdConstants.SRC_TRANSLATE_PREFIX + stringListEn.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("\r\n"))
                        + " ";  // 添加空格分隔不同的文本块，避免一次性发送过多内容导致请求失败
                log.info("请求内容为：{}", text);
                String response = GeminiApiClient.generateContent(text);
                // 将两个回车换行替换成一个
                response = response.replaceAll("\n\n", "\n");
                // 解析成字符串数组，并以行为单位拆分字符串数组
                responseList = new ArrayList<>(
                        Arrays.asList(response.split("\n")));
                subtitleEntityList = CdFileUtil.genSubtitleEntityList(
                        responseList, platformName);
                if (stringListEn.size() != subtitleEntityList.size()) {
                    log.error("返回结果不一致，期待值：{}，实际值：{}, \r\n，返回内容为：{}",
                            stringListEn.size(),
                            subtitleEntityList.size(), response);
                } else {
                    log.info("返回结果一致，期待值：{}，实际值：{}", stringListEn.size(),
                            subtitleEntityList.size());
                }
                break;
            case CdConstants.TRANSLATE_PLATFORM_DEEP_SEEK:
                text = stringListEn.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("\r\n"))
                        + " ";  // 添加空格分隔不同的文本块，避免一次性发送过多内容导致请求失败
                log.info("请求内容为：{}", text);
                response = OllamaTranslateUtil.translateText(text);
                // 将两个回车换行替换成一个
                response = response.replaceAll("\n\n", "\n");
                // 解析成字符串数组，并以行为单位拆分字符串数组
                responseList = new ArrayList<>(
                        Arrays.asList(response.split("\n")));
                subtitleEntityList = CdFileUtil.genSubtitleEntityList(
                        responseList, platformName);
                if (stringListEn.size() != subtitleEntityList.size()) {
                    log.error("返回结果不一致，期待值：{}，实际值：{}, \r\n，返回内容为：{}",
                            stringListEn.size(),
                            subtitleEntityList.size(), response);
                } else {
                    log.info("返回结果一致，期待值：{}，实际值：{}", stringListEn.size(),
                            subtitleEntityList.size());
                }
                break;
            case CdConstants.TRANSLATE_PLATFORM_MSTTS:
                String textTitleList = stringListEn.stream().map(String::valueOf)
                        .collect(Collectors.joining("\r\n"));
                responseList = TranslatorTextUtil.translatorText(textTitleList);

//        responseList=
                // 将两个回车换行替换成一个
//        response = response.replaceAll("\n\n", "\n");
                // 解析成字符串数组，并以行为单位拆分字符串数组
                if (CollectionUtil.isNotEmpty(responseList)
                        && responseList.size() == 1) {// 将两个回车换行替换成一个r)
                    responseList = new ArrayList<>(
                            Arrays.asList(responseList.get(0).split("\r\n")));
                }
                // 解析字符串为字幕对象列表
                subtitleEntityList = CdFileUtil.genSubtitleEntityList(responseList,
                        platformName);
                if (stringListEn.size() != subtitleEntityList.size()) {
                    log.error("返回结果不一致，期待值：{}，实际值：{}, \r\n，返回内容为：",
                            stringListEn.size(),
                            subtitleEntityList.size());
                    // 打印列表
                    for (String s : responseList) {
                        log.error(s);
                    }
                } else {
                    log.info("返回结果一致，期待值：{}，实际值：{}", stringListEn.size(),
                            subtitleEntityList.size());
                }
                break;
            default:
                break;
        }

        return subtitleEntityList;
    }

    /**
     * 生成对话脚本合集
     *
     * @param folderName
     */
    public static void mergeScriptContent(String folderName) {
        String fileName = "script_dialog";
        String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                ".txt");
        List<DialogSingleEntity> dialogSingleEntityListEn = CdFileUtil.genDialogSingleEntityList(
                srcFileName);

        String srcFileNameCn = CommonUtil.getFullPathFileName(folderName, fileName,
                "_cn.txt");

        List<String> newList = mergeContent(srcFileNameCn,
                dialogSingleEntityListEn);

        String srcFileNameMerge = CommonUtil.getFullPathFileName(folderName,
                folderName, "_中英双语对话脚本.txt");
        // 写中文翻译文本
        CdFileUtil.writeToFile(srcFileNameMerge, newList);
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
     * 生成对话脚本合集
     *
     * @param folderName
     */
    public static void mergeScriptContentWx(String folderName) {
        String fileName = "script_dialog_wx2";
        String srcFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                ".txt");

        List<String> stringList = CdFileUtil.readFileContent(srcFileName);

        String text = stringList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringListCn = TranslatorTextUtil.translatorText(text);

        List<String> newList = new ArrayList<>();
        List<String> newListEnCn = new ArrayList<>();
        List<String> lrcListEnCn = new ArrayList<>();
        SubtitleEntity SubtitleEntity;
        String timeStr;
        String lrc;

        for (int i = 0; i < stringListCn.size(); i++) {
            String temp = stringListCn.get(i);
            String[] arr = temp.split("\r\n");

            for (int j = 0; j < arr.length; j++) {

                upgradeTranslate(folderName, arr, j, stringList);

                System.out.println(arr[j]);
//                SubtitleEntity = SubtitleEntityList.get(j);
//                newList.add(SubtitleEntity.getSubIndex() + "");
//                newList.add(SubtitleEntity.getTimeStr());
//                newList.add(arr[j]);
//                newList.add("");

//                newListEnCn.add(SubtitleEntity.getSubIndex() + "");
//                newListEnCn.add(SubtitleEntity.getTimeStr());
                newListEnCn.add(stringList.get(j) + "\r" + arr[j]);
                newListEnCn.add("");

            }
        }

        String srcFileNameMerge = CommonUtil.getFullPathFileName(folderName,
                fileName, "_merge.txt");
        // 写中文翻译文本
        CdFileUtil.writeToFile(srcFileNameMerge, newListEnCn);
    }

    /**
     * 翻译字符串数组
     *
     * @param textTopicStringList
     * @return
     */
    public static List<String> translateStringList(
            List<String> textTopicStringList) {

        List<String> result = new ArrayList<>();
        String textTopic = textTopicStringList.stream().map(String::valueOf)
                .collect(Collectors.joining("\r\n"));
        List<String> stringList = TranslatorTextUtil.translatorText(textTopic);

        for (int i = 0; i < stringList.size(); i++) {
            String temp = stringList.get(i);
            String[] arr = temp.split("\r\n");
            result = Arrays.asList(arr);
        }

        return result;
    }

}
