package com.coderdream.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
//import com.coderdream.freeapps.util.callapi.HttpUtil;
//import com.coderdream.freeapps.util.other.CdFileUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.coderdream.entity.DictionaryEntity;
import com.coderdream.entity.MultiLanguageContent;
import com.coderdream.entity.SentencePair;
import com.coderdream.entity.VocInfo;
import com.coderdream.entity.WordDetail;
import com.coderdream.util.callapi.HttpUtil;
import com.coderdream.util.cd.CdChatgptUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdDictionaryUtil;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.gemini.GeminiApiClient;
import com.coderdream.util.cd.CdVocInfoUtil;
import com.coderdream.util.gemini.TranslationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * @author CoderDream
 */
@Slf4j
public class DictUtil {

    public static void main(String[] args) {
//        String word = "sustainable";
//        List<String> wordList = Arrays.asList("genre", "upbeat", "jaunty", "gasp", "gobsmacked", "high five");
//        List<String> wordList = Arrays.asList("upbeat", "jaunty", "gasp", "gobsmacked", "high five");
//    List<String> wordList = Arrays.asList("spoil");
//    List<VocInfo> vocInfoList = DictUtil.queryWords(wordList);
//    for (VocInfo vocInfo : vocInfoList) {
//      System.out.println(vocInfo);
//    }

//        String folderName = "230428";
//        DictUtil.processVoc(folderName);

//        List<VocInfo> vocInfoList = DictUtil.getVocInfoList(folderName);
//        for (VocInfo vocInfo : vocInfoList) {
//            System.out.println(vocInfo);
//        }

//        String wordCn = "气候变化否定者：一种观点，认为全球气候变化是不存在的或者人类活动对气候变化的影响被夸大了。";
//        String wordExplainCn = "气候变化否定者：一种观点，认为全球气候变化是不存在的或者人类活动对气候变化的影响被夸大了。； · Climate deniers often argue that the scientific consensus on climate change is based on flawed data.； 气候变化否定者经常辩称，关于气候变化的科学共识是基于错误的数据的。";
//        //
//        int colonIndex = wordCn.indexOf("：");
//
//        int firstIndexOfIndex = wordExplainCn.indexOf("； · ");
//        int lastIndexOfIndex = wordExplainCn.lastIndexOf("； ");
//        wordCn = wordExplainCn.substring(0, colonIndex);
////        System.out.println(wordCn);
//
//        String sampleSentenceEn = wordExplainCn.substring(firstIndexOfIndex + 3, lastIndexOfIndex);
//        String sampleSentenceCn = wordExplainCn.substring(lastIndexOfIndex + 3);
//        wordExplainCn = wordExplainCn.substring(colonIndex + 1, firstIndexOfIndex);
//
//        System.out.println(wordCn);
//        System.out.println(wordExplainCn);
//        System.out.println(sampleSentenceEn.trim());
//        System.out.println(sampleSentenceCn);

//        String b = "网络自夸（在网络环境中对自己的情况有所隐瞒，通常精心编造一个优质的网络身份，目的是为了给他人留下深刻印象，尤其是为了吸引某人与其发展恋爱关系）";
//        b = "绒毛般的，覆有绒毛的；（食物等）松软的，透气的；轻软状的；<非正式>空洞的，不严肃的";
//        System.out.println(shortStr(b));

        String folderName = "D:\\14_LearnEnglish\\u11_frankenstein\\u11_frankenstein_episode1\\";
        String fileName = "u11_frankenstein_episode1_voc";
        DictUtil.processVoc(folderName, fileName);
    }

    /**
     * 根据英文词汇生成中英文词汇
     *
     * @param folderName 文件夹名称
     * @return List<VocInfo>
     */
    public static List<VocInfo> getVocInfoList(String folderName) {
        String fileName = "voc_cn";
        String filePath = CommonUtil.getFullPathFileName(folderName, fileName,
                ".txt");
        List<String> scriptList = new ArrayList<>();

        List<VocInfo> vocInfoList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                scriptList.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            log.error("生成中英文词汇出错", e);
            //e.printStackTrace();
        }

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList == null || scriptList.size() != 36) {
            System.out.println("判断是否为6个词汇，size：" + scriptList.size());
        } else {
            size = scriptList.size();
        }

        VocInfo vocInfo;
        for (int i = 0; i < size; i++) {
            if ((i + 1) % 6 == 0) {
                vocInfo = new VocInfo();
                vocInfo.setWord(scriptList.get(i - 5));
                vocInfo.setWordExplainEn(scriptList.get(i - 4));
                vocInfo.setWordCn(scriptList.get(i - 3));
                vocInfo.setWordExplainCn(scriptList.get(i - 2));
                vocInfo.setSampleSentenceEn(scriptList.get(i - 1));
                vocInfo.setSampleSentenceCn(scriptList.get(i));
                vocInfoList.add(vocInfo);
            }
        }
        return vocInfoList;
    }

    /**
     * 根据英文词汇生成中英文词汇
     *
     * @param folderName 文件夹名称
     * @return List<VocInfo>
     */
    public static List<VocInfo> getVocInfoList(String folderName,
                                               String fileName) {
        //  String fileName = "voc_cn"; //CommonUtil.getFullPathFileName(folderName, fileName,      ".txt");
        String filePath = folderName + fileName + "_voc.txt";
        List<String> scriptList = new ArrayList<>();

        List<VocInfo> vocInfoList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                scriptList.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList == null || scriptList.size() != 36) {
            System.out.println("判断是否为6个词汇，size：" + scriptList.size());
        } else {
            size = scriptList.size();
        }

        VocInfo vocInfo;
        for (int i = 0; i < size; i++) {
            if ((i + 1) % 6 == 0) {
                vocInfo = new VocInfo();
                vocInfo.setWord(scriptList.get(i - 5));
                vocInfo.setWordExplainEn(scriptList.get(i - 4));
                vocInfo.setWordCn(scriptList.get(i - 3));
                vocInfo.setWordExplainCn(scriptList.get(i - 2));
                vocInfo.setSampleSentenceEn(scriptList.get(i - 1));
                vocInfo.setSampleSentenceCn(scriptList.get(i));
                vocInfoList.add(vocInfo);
            }
        }
        return vocInfoList;
    }

    /**
     * 根据英文词汇生成中英文词汇
     */
    public static List<VocInfo> writeVocCnExcel(String folderName) {
        String fileName = "voc_cn";
        String filePath = CommonUtil.getFullPathFileName(folderName, fileName,
                ".txt");
        List<String> scriptList = new ArrayList<>();

        List<VocInfo> vocInfoList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                scriptList.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 写文件
        String newFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                "_cn_excel.txt");

        List<String> scriptListNew = new ArrayList<>();
        // 添加第一个空格
        scriptListNew.add("");

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList == null || scriptList.size() != 36) {
            System.out.println("判断是否为6个词汇，size：" + scriptList.size());
        } else {
            size = scriptList.size();
            String string;
            for (int i = 0; i < size; i++) {
                string = scriptList.get(i);
                scriptListNew.add(string);
                if ((i + 1) % 12 == 0) {
                    scriptListNew.add("");
                }
            }
        }

        if (CollectionUtil.isNotEmpty(scriptListNew)) {
            CdFileUtil.writeToFile(newFileName, scriptListNew);
        } else {
            System.out.println("###### 空");
        }

        return vocInfoList;
    }

    /**
     * 根据英文词汇生成中英文词汇
     */
    public static List<VocInfo> writeVocCnExcel(String folderName,
                                                String fileName) {
//        String fileName = "voc_cn";
        String filePath = folderName + fileName + "_voc_cn.txt";
        List<String> scriptList = new ArrayList<>();

        List<VocInfo> vocInfoList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                scriptList.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 写文件
        String newFileName = folderName + fileName + "_cn_excel.txt";

        List<String> scriptListNew = new ArrayList<>();
        // 添加第一个空格
        scriptListNew.add("");

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList == null || scriptList.size() != 36) {
            System.out.println("判断是否为6个词汇，size：" + scriptList.size());
        } else {
            size = scriptList.size();
            String string;
            for (int i = 0; i < size; i++) {
                string = scriptList.get(i);
                scriptListNew.add(string);
                if ((i + 1) % 12 == 0) {
                    scriptListNew.add("");
                }
            }
        }

        if (CollectionUtil.isNotEmpty(scriptListNew)) {
            CdFileUtil.writeToFile(newFileName, scriptListNew);
        } else {
            System.out.println("###### 空");
        }

        return vocInfoList;
    }

    /**
     * @param folderName
     */
    public static void processVoc(String folderName) {
        String fileName = "voc";
        String filePath = CommonUtil.getFullPathFileName(folderName, fileName,
                ".txt");
        List<String> scriptList = new ArrayList<>();

        List<VocInfo> vocInfoList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = specialUnicode(line);
                if (StrUtil.isNotEmpty(line)) {
                    scriptList.add(line);
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList.size() != 12) {
            System.out.println("判断是否为6个词汇，当前为：" + scriptList.size());
            return;
        } else {
            size = scriptList.size();
        }

        VocInfo vocInfo;
        String word;
        int startIndex;
        int endIndex;
        String temp;
        for (int i = 0; i < size; i++) {
            if ((i + 1) % 2 == 0) {
                vocInfo = new VocInfo();
                word = scriptList.get(i - 1);
                startIndex = word.lastIndexOf("(");
                endIndex = word.lastIndexOf(")");
                if (startIndex != -1 && endIndex != -1) {
                    // 存在括号，则删除括号
                    temp = word.substring(startIndex, endIndex + 1);
                    System.out.println(word + "\t|\t" + temp);
                    word = word.replaceAll(temp, "");
                    word = word.replaceAll("\\(", "");
                    word = word.replaceAll("\\)", "");
                }
                vocInfo.setWord(word.trim());
                vocInfo.setWordExplainEn(scriptList.get(i));
                vocInfoList.add(vocInfo);
            }
        }

        DictUtil.queryVocInfoList(vocInfoList);

        // 写文件
        String newFileName = CommonUtil.getFullPathFileName(folderName, fileName,
                "_cn.txt");

        List<String> scriptListNew = new ArrayList<>();
        String wordCnBrief = "";
        for (VocInfo vocInfo1 : vocInfoList) {
            scriptListNew.add(vocInfo1.getWord());
            scriptListNew.add(vocInfo1.getWordExplainEn());
            wordCnBrief = vocInfo1.getWordCn() != null ? vocInfo1.getWordCn() : "";

            wordCnBrief = shortStr(wordCnBrief);

            scriptListNew.add(wordCnBrief);
            scriptListNew.add(
                    vocInfo1.getWordExplainCn() != null ? vocInfo1.getWordExplainCn() : "");
            scriptListNew.add(
                    vocInfo1.getSampleSentenceEn() != null ? vocInfo1.getSampleSentenceEn()
                            : "");
            scriptListNew.add(
                    vocInfo1.getSampleSentenceCn() != null ? vocInfo1.getSampleSentenceCn()
                            : "");
//            scriptListNew.add("");
        }

        if (CollectionUtil.isNotEmpty(scriptListNew)) {
            CdFileUtil.writeToFile(newFileName, scriptListNew);
        } else {
            System.out.println("###### 空");
        }
    }

    /**
     * @param folderName 文件夹路径
     * @param fileName   文件名称
     */
    public static void processVoc(String folderName, String fileName) {
        String filePath = folderName + fileName + ".txt";

        List<VocInfo> vocInfoList = genRawList(filePath);

        // 查询词汇解释
        for (VocInfo vocInfo : vocInfoList) {
            // 如果有空值，测试10次，直到不为空为止
            for (int i = 0; i < 10; i++) {
                if (CdVocInfoUtil.isEmpty(vocInfo)) {
                    log.info("词汇详情有空值，开始第{}查询：", i + 1);
                    CdVocInfoUtil.fillVocInfo(vocInfo);
                } else {
                    log.info("词汇详情查询完毕，在第{}查询成功：", i);
                    break;
                }
            }
        }

        // 写文件
        String newFileName = folderName + fileName
                + "_cn.txt";// CommonUtil.getFullPathFileName(folderName, fileName,      "_cn.txt");

        List<String> scriptListNew = new ArrayList<>();
        String wordCnBrief = "";
        for (VocInfo vocInfo1 : vocInfoList) {
            scriptListNew.add(vocInfo1.getWord());
            scriptListNew.add(vocInfo1.getWordExplainEn());
            wordCnBrief = vocInfo1.getWordCn() != null ? vocInfo1.getWordCn() : "";
            wordCnBrief = shortStr(wordCnBrief);
            scriptListNew.add(wordCnBrief);
            scriptListNew.add(
                    vocInfo1.getWordExplainCn() != null ? vocInfo1.getWordExplainCn() : "");
            scriptListNew.add(
                    vocInfo1.getSampleSentenceEn() != null ? vocInfo1.getSampleSentenceEn()
                            : "");
            scriptListNew.add(
                    vocInfo1.getSampleSentenceCn() != null ? vocInfo1.getSampleSentenceCn()
                            : "");
        }

        if (CollectionUtil.isNotEmpty(scriptListNew)) {
            CdFileUtil.writeToFile(newFileName, scriptListNew);
        } else {
            System.out.println("###### 空");
        }
    }

    /**
     * @param folderName 文件夹路径
     * @param fileName   文件名称
     */
    public static void processVocWithGemini(String folderName, String fileName) {
        String filePath = folderName + fileName + ".txt";

        List<VocInfo> vocInfoList = null;// genRawList(filePath);

        // 写文件
        String newFileName = folderName + fileName
                + "_cn.txt";// CommonUtil.getFullPathFileName(folderName, fileName,      "_cn.txt");

        TranslationUtil.processVoc(vocInfoList, newFileName);
    }


    /**
     * @param vocFileName   文件夹路径
     * @param vocCnFileName 文件名称
     */
    public static File genVocCnWithGemini(String vocFileName,
        String vocCnFileName) {
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
                // 可以选择抛出异常或返回null/空文件来中断整个流程
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
     * 对单个词汇进行翻译，包含重试和校验逻辑。
     *
     * @param vocInfo 待翻译的词汇信息对象
     * @return 如果成功翻译并填充了对象，返回 true；否则返回 false。
     */
    private static boolean translateSingleVocWithRetry(VocInfo vocInfo) {
        final int MAX_RETRIES = 5;
        final String promptTemplate =
            "You are a professional English-Chinese dictionary assistant. Your task is to provide a translation and an example sentence for the given English word and its definition.\n"
                + "Please follow the output format strictly. You must provide exactly 4 lines of text. Do NOT include any Hanyu Pinyin in your response.\n\n"
                + "**Input:**\n"
                + "Word: %s\n"
                + "Definition: %s\n\n"
                + "**Required Output Format (4 lines):**\n"
                + "1. Chinese translation of the word.\n"
                + "2. Chinese translation of the definition.\n"
                + "3. An English example sentence using the word.\n"
                + "4. Chinese translation of the example sentence.";

        String prompt = String.format(promptTemplate, vocInfo.getWord(),
            vocInfo.getWordExplainEn());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.info("正在翻译词汇 '{}'... (尝试 {}/{})", vocInfo.getWord(), attempt,
                MAX_RETRIES);
            String translatedText = GeminiApiClient.generateContent(prompt);

            if (StrUtil.isBlank(translatedText) || translatedText.contains(
                "API 调用发生异常")) {
                log.error("Gemini API 调用失败或返回错误。词汇: '{}', 尝试: {}",
                    vocInfo.getWord(), attempt);
                if (attempt < MAX_RETRIES) {
                    continue;
                } else {
                    return false;
                }
            }

            List<String> lines = Arrays.asList(translatedText.split("\n"));


            // 增强解析逻辑：从返回结果中智能提取最后4行有效内容
            List<String> allLines = Arrays.stream(translatedText.split("\\r?\\n"))
                    .filter(StrUtil::isNotBlank)
                    .toList();

            // 如果有效行数大于等于4，就从列表末尾取最后4行
            if (allLines.size() >= 4) {
                lines = allLines.subList(allLines.size() - 4, allLines.size());
                log.info("成功从响应末尾提取到4行翻译。");
            } else {
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
                log.warn(
                    "翻译词汇 '{}' 后返回行数不为4，实际为 {} 行。将在5秒后重试... (尝试 {}/{})",
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

    private static List<VocInfo> genRawList(String filePath) {
        List<VocInfo> vocInfoList = new ArrayList<>();

        List<String> scriptList = FileUtil.readLines(filePath,
                StandardCharsets.UTF_8);

        // 判断是否为6个词汇
        int size = 0;
        if (scriptList.size() != 12 && scriptList.size() != 18
                && scriptList.size() != 17) {
            System.out.println("判断是否为6个词汇，当前为：" + scriptList.size());
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
     * 去除 字符串收尾的 特殊的Unicode [ "\uFEFF" ] csv 文件可能会带有该编码
     *
     * @param str
     * @return
     */
    public static String specialUnicode(String str) {
        if (str.startsWith("\uFEFF")) {
            str = str.replace("\uFEFF", "");
        } else if (str.endsWith("\uFEFF")) {
            str = str.replace("\uFEFF", "");
        }
        return str;
    }

    public static String shortStr(String wordCnBrief) {
        /// 【正则】如何去掉花括号{}大括号，大阔号和里面的值；去掉尖括号里的值
        // https://blog.csdn.net/river_continent/article/details/78214766

        int beginIndex = 0;
        int endIndex = 0;

//        StrUtil.isEmpty();

//        wordCnBrief.

//        String str="{A}123{aaa}456{bbb}";
//        大阔号和里面的值=str.replaceAll("\\{[^}]*\\}","");
        String reg = "（[^}]*）";
        wordCnBrief = wordCnBrief.replaceAll(reg, "");
        System.out.println(wordCnBrief);

        reg = "<[^}]*>";
        wordCnBrief = wordCnBrief.replaceAll(reg, "");
        System.out.println(wordCnBrief);

//        beginIndex = wordCnBrief.indexOf("（");
//        endIndex = wordCnBrief.indexOf("）");
//
//        String temp = "";
//
//        if (beginIndex != -1 && endIndex != -1) {
//            temp = wordCnBrief.substring(beginIndex, endIndex);
//            wordCnBrief = wordCnBrief.replaceAll(temp, "");
//        }
//
//        wordCnBrief = wordCnBrief.substring(beginIndex, endIndex);

        if (wordCnBrief.length() > 13 && wordCnBrief.indexOf("；") != -1
                && wordCnBrief.indexOf("；") < 12) {
            wordCnBrief = wordCnBrief.substring(0, wordCnBrief.indexOf("；"));
        }

        return wordCnBrief;
    }

    /**
     * @param vocInfoList
     */
    public static void queryVocInfoList(List<VocInfo> vocInfoList) {
//    List<VocInfo> result = new ArrayList<>();
        for (VocInfo vocInfo : vocInfoList) {
            // 填充中文解释字段及句子 TODO
            VocInfo vocInfo1 = queryWord(vocInfo.getWord()); // TODO
            BeanUtils.copyProperties(vocInfo1, vocInfo, "wordExplainEn");
//      result.add(vocInfo);
        }
    }

    public static List<VocInfo> queryWords(List<String> wordList) {
        List<VocInfo> vocInfoList = new ArrayList<>();
        for (String word : wordList) {
            vocInfoList.add(queryWord(word));
        }

        return vocInfoList;
    }

    /**
     * 通过网络查找
     *
     * @param word 待查询的单词
     * @return VocInfo
     */
    public static VocInfo queryWord(String word) {
        VocInfo vocInfo = new VocInfo();
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("q", word);
        param.put("le", "en");
        param.put("t", "2");
        param.put("client", "web");
        param.put("sign", "cb7b9683228573db5f84d8fb13e748ae");
        param.put("keyfrom", "webdict");
//        param.put("client", "");
        Map<String, String> head = new LinkedHashMap<>();
        String url = "https://dict.youdao.com/jsonapi_s?doctype=json&jsonversion=4";
//        Class<T> t = Object.class;
        Integer retryTimes = 3;
        JSONObject jsonObject = HttpUtil.postWithForm(param, head, url,
                JSONObject.class, retryTimes);

        vocInfo.setWord(word);

//    System.out.println(jsonObject.toStringPretty());

        // 填充中文意思字段和句子字段
        fillExplain(vocInfo, jsonObject);

        // 如果中文包含一个冒号，则需要分割
        boolean contains = vocInfo.getWordExplainCn().contains("：");
        if (contains) {
            String[] split = vocInfo.getWordExplainCn().split("：");
            vocInfo.setWordCn(split[0]);
            vocInfo.setWordExplainCn(split[1]);
        }

        // 使用字符流和条件过滤
        long countWordCn = vocInfo.getWordCn().chars()
                .filter(ch -> ch == '：') // 过滤中文冒号
                .count();
        long countWordExplainCn = vocInfo.getWordExplainCn().chars()
                .filter(ch -> ch == '：') // 过滤中文冒号
                .count();
        if (countWordCn == 1 && countWordExplainCn == 1) {
            // 分割
            String[] split = vocInfo.getWordExplainCn().split("：");
            vocInfo.setWordCn(split[0]);
            vocInfo.setWordExplainCn(split[1]);
        }

//    if (word.equals("chemistry")) {
//      System.out.println(vocInfo.toString());
//    }

        // 如果中文解释为空，则需要查字典填充
        if (vocInfo.getWordCn().contains("null") || vocInfo.getWordExplainCn()
                .contains("null") || StrUtil.isBlank(vocInfo.getWordCn())
                || StrUtil.isBlank(vocInfo.getWordExplainCn())) {
            queryDictionaryAndFillDefinition(word, vocInfo);
        }

        // 如果中文解释还是为空，则需要Chatgpt TODO
        if (vocInfo.getWordCn().contains("null") || vocInfo.getWordExplainCn()
                .contains("null") || StrUtil.isBlank(vocInfo.getWordCn())
                || StrUtil.isBlank(vocInfo.getWordExplainCn())) {
            //    String translate = CdChatgptUtil.getTranslate(msg);
            vocInfo.setWordCn(CdChatgptUtil.getTranslate(vocInfo.getWord()));
            vocInfo.setWordExplainCn(
                    CdChatgptUtil.getTranslate(vocInfo.getWordExplainEn()));
        }

        boolean wordCnEmpty = vocInfo.getWordCn() == null;
        boolean wordExplainCnEmpty = vocInfo.getWordExplainCn() == null;
        boolean sampleSentenceEnEmpty = vocInfo.getSampleSentenceEn() == null;
        boolean sampleSentenceCnEmpty = vocInfo.getSampleSentenceCn() == null;

        if (wordCnEmpty && wordExplainCnEmpty && sampleSentenceEnEmpty
                && sampleSentenceCnEmpty) {
            System.out.println(vocInfo.getWord() + "翻译和例句都为空");
        } else if (wordCnEmpty && wordExplainCnEmpty) {
            System.out.println(vocInfo.getWord() + "翻译为空");
        } else if (sampleSentenceEnEmpty && sampleSentenceCnEmpty) {
            queryDictionaryAndFillSentence(word, vocInfo);

            // wordDetail(vocInfo, jsonObject);
            if (vocInfo.getSampleSentenceEn() == null
                    && vocInfo.getSampleSentenceCn() == null) {
                // 通过Chatgpt查找 TODO
                List<SentencePair> list = CdChatgptUtil.getSentenceFromChatgpt(
                        vocInfo.getWord());
                if (CollectionUtils.isNotEmpty(list)) {
                    list.stream().findFirst().ifPresent(sentence -> {
                        vocInfo.setSampleSentenceEn(sentence.getEnglishSentence());
                        vocInfo.setSampleSentenceCn(sentence.getChineseSentence());
                    });
                }

                if (vocInfo.getSampleSentenceEn() == null
                        && vocInfo.getSampleSentenceCn() == null) {
                    System.out.println(vocInfo.getWord() + "例句为空");
                } else {
                    System.out.println(vocInfo.getWord() + "词汇信息正常");
                    return vocInfo;
                }

//        System.out.println(vocInfo.getWord() + "例句为空");
            } else {
                log.info("词汇信息正常， {}", vocInfo.getWord());
                return vocInfo;
            }
        } else {
            System.out.println(vocInfo.getWord() + "词汇信息正常");
        }

        return vocInfo;
    }

    /**
     * 通过网络查找
     *
     * @param word 待查询的单词
     * @return VocInfo
     */
    public static VocInfo queryWordFromYoudao(String word) {
        VocInfo vocInfo = new VocInfo();
        vocInfo.setWord(word);

        Map<String, Object> param = new LinkedHashMap<>();
        param.put("q", word);
        param.put("le", "en");
        param.put("t", "2");
        param.put("client", "web");
        param.put("sign", "cb7b9683228573db5f84d8fb13e748ae");
        param.put("keyfrom", "webdict");
//        param.put("client", "");
        Map<String, String> head = new LinkedHashMap<>();
        String url = "https://dict.youdao.com/jsonapi_s?doctype=json&jsonversion=4";
//        Class<T> t = Object.class;
        Integer retryTimes = 3;
        JSONObject jsonObject = HttpUtil.postWithForm(param, head, url,
                JSONObject.class, retryTimes);

        // 填充中文意思字段和句子字段
        fillExplain(vocInfo, jsonObject);

        // 如果中文包含一个冒号，则需要分割
        boolean contains = vocInfo.getWordExplainCn().contains("：");
        if (contains) {
            String[] split = vocInfo.getWordExplainCn().split("：");
            vocInfo.setWordCn(split[0]);
            vocInfo.setWordExplainCn(split[1]);
        }

        return vocInfo;
    }

    /**
     * 通过网络查找
     *
     * @param vocInfo 待查询的单词
     * @return VocInfo
     */
    public static VocInfo queryVocInfo(VocInfo vocInfo) {

        String word = vocInfo.getWord();
        String wordExplainEn = vocInfo.getWordExplainEn();
        VocInfo vocInfoResult = queryVocInfoFromDictionary(word);
        if (vocInfoResult != null) {
            vocInfoResult.setWordExplainEn(wordExplainEn);
            vocInfoResult.setWordExplainCn(
                    CdChatgptUtil.getTranslate(vocInfo.getWordExplainEn()));
            return vocInfoResult;
        } else {

            vocInfoResult = new VocInfo();
            BeanUtil.copyProperties(vocInfo, vocInfoResult);
            vocInfoResult.setWordExplainEn(wordExplainEn);
            vocInfoResult.setWordExplainCn(
                    CdChatgptUtil.getTranslate(vocInfo.getWordExplainEn()));

            vocInfoResult.setWordCn(
                    CdChatgptUtil.getTranslate(vocInfo.getWord()));
            // 通过Chatgpt查找 TODO
            List<SentencePair> list = CdChatgptUtil.getSentenceFromChatgpt(
                    vocInfo.getWord());
            if (CollectionUtils.isNotEmpty(list)) {
                VocInfo finalVocInfoResult = vocInfoResult;
                list.stream().findFirst().ifPresent(sentence -> {
                    finalVocInfoResult.setSampleSentenceEn(sentence.getEnglishSentence());
                    finalVocInfoResult.setSampleSentenceCn(sentence.getChineseSentence());
                });
            }
        }

        return vocInfoResult;
    }

    /**
     * 查询字典并填充内容
     *
     * @param word 单词 return VocInfo
     * @return
     */
    public static VocInfo queryVocInfoFromDictionary(String word) {
        VocInfo vocInfo;
        DictionaryEntity dictionary = CdDictionaryUtil.getDictionaryEntity(
                word, CdConstants.OALDPE);
        // 填充英文例句
        String htmlStr = dictionary.getReserved05();
//    log.info("{} 详情， {}", word, htmlStr);
        WordDetail wordDetail = CdDictionaryUtil.getWordDetailFromHtmlStr(
                htmlStr);
        if (wordDetail != null) {
            vocInfo = new VocInfo();
            vocInfo.setWord(word);
            Optional<MultiLanguageContent> first = wordDetail.getDefinitionList()
                    .stream().findFirst();
            // 方法1：有默认值
            MultiLanguageContent content = first.orElse(new MultiLanguageContent());
            vocInfo.setWordCn(content.getContentSimple());
            // 将列表中的ContentSimple作为连接字符串赋值给wordExplainCn
            vocInfo.setWordExplainCn(
                    wordDetail.getDefinitionList().stream()
                            .map(MultiLanguageContent::getContentSimple)
                            .collect(Collectors.joining("；")));

            // 取第一条数据填充
            wordDetail.getSentenceList().stream().findFirst().ifPresent(
                    sentence -> {
                        // 修复：使用final变量引用
                        vocInfo.setSampleSentenceEn(sentence.getContentEnglish());
                        vocInfo.setSampleSentenceCn(sentence.getContentSimple());
                    });

//      log.info("填充英文， {}", wordDetail);
        } else {
            vocInfo = null;
            log.error("{} 在字典未查到！", word);
        }

        return vocInfo;
    }

    /**
     * 查询字典并填充内容
     *
     * @param word 单词 return VocInfo
     * @return
     */
    public static VocInfo queryVocInfoFromDictionary(String word,
                                                     String dictName) {
        VocInfo vocInfo;

        DictionaryEntity dictionary = null;
        if (StrUtil.isBlank(dictName)) {
            dictionary = CdDictionaryUtil.getDictionaryEntity(
                    word, dictName);
        } else {
            dictionary = CdDictionaryUtil.getDictionaryEntity(
                    word, CdConstants.OALDPE);
        }

        // 填充英文例句
        if (dictionary != null) {

            // 填充英文例句
            String htmlStr = dictionary.getReserved05();
//    log.info("{} 详情， {}", word, htmlStr);
            WordDetail wordDetail = CdDictionaryUtil.getWordDetailFromHtmlStr(
                    htmlStr);
            if (wordDetail != null) {
                vocInfo = new VocInfo();
                vocInfo.setWord(word);
                Optional<MultiLanguageContent> first = wordDetail.getDefinitionList()
                        .stream().findFirst();
                // 方法1：有默认值
                MultiLanguageContent content = first.orElse(new MultiLanguageContent());
                vocInfo.setWordCn(content.getContentSimple());
                // 将列表中的ContentSimple作为连接字符串赋值给wordExplainCn
                vocInfo.setWordExplainCn(
                        wordDetail.getDefinitionList().stream()
                                .map(MultiLanguageContent::getContentSimple)
                                .collect(Collectors.joining("；")));

                // 取第一条数据填充
                wordDetail.getSentenceList().stream().findFirst().ifPresent(
                        sentence -> {
                            // 修复：使用final变量引用
                            vocInfo.setSampleSentenceEn(sentence.getContentEnglish());
                            vocInfo.setSampleSentenceCn(sentence.getContentSimple());
                        });

//      log.info("填充英文， {}", wordDetail);
            } else {
                vocInfo = null;
                log.error("{} 在字典未查到！", word);
            }

        } else {
            vocInfo = null;
            log.error("{} 在字典未查到！", word);
        }

        return vocInfo;
    }

    /**
     * 查询字典并填充内容
     *
     * @param word    单词
     * @param vocInfo vocInfo
     */
    private static void queryDictionaryAndFillDefinition(String word,
                                                         VocInfo vocInfo) {
        DictionaryEntity dictionary = CdDictionaryUtil.getDictionaryEntity(
                word, CdConstants.OALDPE);
        // 填充英文例句 TODO
        String htmlStr = dictionary.getReserved05();
        log.info("{} 详情， {}", word, htmlStr);
        WordDetail wordDetail = CdDictionaryUtil.getWordDetailFromHtmlStr(
                htmlStr);
        if (wordDetail != null) {
            Optional<MultiLanguageContent> first = wordDetail.getDefinitionList()
                    .stream().findFirst();
            // 方法1：有默认值
            MultiLanguageContent content = first.orElse(new MultiLanguageContent());
            vocInfo.setWordCn(content.getContentSimple());
            // 将列表中的ContentSimple作为连接字符串赋值给wordExplainCn
            vocInfo.setWordExplainCn(
                    wordDetail.getDefinitionList().stream()
                            .map(MultiLanguageContent::getContentSimple)
                            .collect(Collectors.joining("；")));
            log.info("填充英文， {}", wordDetail);
        } else {
            log.error("填充英文例句， {}", wordDetail);
        }
    }

    /**
     * 查询字典并填充内容
     *
     * @param word    单词
     * @param vocInfo vocInfo
     */
    private static void queryDictionaryAndFillSentence(String word,
                                                       VocInfo vocInfo) {
        DictionaryEntity dictionary = CdDictionaryUtil.getDictionaryEntity(
                word, CdConstants.OALDPE);
        // 填充英文例句 TODO
        String htmlStr = dictionary.getReserved05();
        WordDetail wordDetail = CdDictionaryUtil.getWordDetailFromHtmlStr(
                htmlStr);

        log.info("填充英文例句， {}", wordDetail);
        if (wordDetail == null) {
            log.error("{} 字典查询返回内容为空！", word);
            return;
        }
        // 取第一条数据填充
        wordDetail.getSentenceList().stream().findFirst().ifPresent(
                sentence -> {
                    vocInfo.setSampleSentenceEn(sentence.getContentEnglish());
                    vocInfo.setSampleSentenceCn(sentence.getContentSimple());
                });
    }

    /**
     * 填充
     *
     * @param vocInfo
     * @param jsonObject
     */
    public static void fillExplain(VocInfo vocInfo, JSONObject jsonObject) {
        // TODO
//    String expression1 = "[ec].[exam_type]";
//    Object byPath1 = jsonObject.getByPath(expression1);
        String usphone = "[ec].[word].[usphone]";
        Object usphoneObject = jsonObject.getByPath(usphone);
        String ukphone = "[ec].[word].[ukphone]";
        Object ukphoneObject = jsonObject.getByPath(ukphone);
        String expression3 = "[ec].[word].[trs].[pos]";
        Object byPath3 = jsonObject.getByPath(expression3);
        String expression4 = "[ec].[word].[trs].[tran]";
        Object byPath4 = jsonObject.getByPath(expression4);

        String wordCn = "";
        String wordExplainCn = "";
        if (ukphoneObject != null) {
            wordExplainCn += "英/" + ukphoneObject + "/";
        }
        if (usphoneObject != null) {
            wordExplainCn += "美/" + usphoneObject + "/";
        }

        String pos;
        String tran;

        if (byPath3 != null && byPath3 instanceof ArrayList && byPath4 != null
                && byPath4 instanceof ArrayList) {
            List posList = (ArrayList) byPath3;
            List tranList = (ArrayList) byPath4;
            int size = posList.size();
            if (posList.size() == tranList.size()) {
                for (int i = 0; i < size; i++) {
                    pos = posList.get(i) != null ? posList.get(i).toString() : "";
                    tran = tranList.get(i) != null ? tranList.get(i).toString() : "";
                    if (i == 0) {
                        wordCn = tran; // 设置中文
                    }
                    // 设置解释
                    if (i == size - 1) {
                        wordExplainCn += pos + "" + tran;
                    } else {
                        wordExplainCn += pos + "" + tran + "； ";
                    }
                }
            }
        }
        vocInfo.setWordCn(wordCn);
        vocInfo.setWordExplainCn(wordExplainCn);

        String expression5 = "[expand_ec].[word].[transList].[content].[sents].[sentSpeech].[0].[0].[0]";
        Object byPath5 = jsonObject.getByPath(expression5);
        String expression6 = "[expand_ec].[word].[transList].[content].[sents].[sentTrans].[0].[0].[0]";
        Object byPath6 = jsonObject.getByPath(expression6);

        String sampleSentenceEn = "";
        if (byPath5 != null) {
            sampleSentenceEn = byPath5.toString();
            vocInfo.setSampleSentenceEn(sampleSentenceEn);
        }
        if (byPath6 != null) {
            String sampleSentenceCn = byPath6.toString();
            vocInfo.setSampleSentenceCn(sampleSentenceCn);
        }

        // 没有找到例句，找柯林斯例句
        if (StrUtil.isEmpty(sampleSentenceEn)) {
            String expressionCollinsEn = "[collins].[collins_entries].[entries].[entry].[tran_entry].[exam_sents].[sent].[eng_sent].[0].[0].[0].[0]";
            Object objectCollinsEn = jsonObject.getByPath(expressionCollinsEn);
            String expressionCollinsCn = "[collins].[collins_entries].[entries].[entry].[tran_entry].[exam_sents].[sent].[chn_sent].[0].[0].[0].[0]";
            Object objectCollinsCn = jsonObject.getByPath(expressionCollinsCn);

            if (objectCollinsEn != null) {
                sampleSentenceEn = objectCollinsEn.toString();
                vocInfo.setSampleSentenceEn(sampleSentenceEn);
            }
            if (objectCollinsCn != null) {
                String sampleSentenceCn = objectCollinsCn.toString();
                vocInfo.setSampleSentenceCn(sampleSentenceCn);
            }
        }

        // 如果既有冒号，又有点号，则拆分
        if (wordExplainCn.contains(".") && wordExplainCn.contains("：")) {
            //
            int colonIndex = wordCn.indexOf("：");

            int firstIndexOfIndex = wordExplainCn.indexOf("； · ");
            int lastIndexOfIndex = wordExplainCn.lastIndexOf("； ");
            wordCn = wordExplainCn.substring(0, colonIndex);
//        System.out.println(wordCn);

            sampleSentenceEn = wordExplainCn.substring(firstIndexOfIndex + 3,
                    lastIndexOfIndex);
            String sampleSentenceCn = wordExplainCn.substring(lastIndexOfIndex + 3);
            wordExplainCn = wordExplainCn.substring(colonIndex + 1,
                    firstIndexOfIndex);

            System.out.println(wordCn);
            System.out.println(wordExplainCn);
            System.out.println(sampleSentenceEn.trim());
            System.out.println(sampleSentenceCn);

            vocInfo.setWordCn(wordCn);
            vocInfo.setWordExplainCn(wordExplainCn);
            vocInfo.setSampleSentenceEn(sampleSentenceEn);
            vocInfo.setSampleSentenceCn(sampleSentenceCn);
        }

        // 没有找到例句，找柯林斯例句和牛津
        if (StrUtil.isEmpty(sampleSentenceEn)) {
            //
//            String expression7 = "[blng_sents_part][sentence-pair]";
//            Object byPath7 = jsonObject.getByPath(expression7);
//            System.out.println(byPath7.getClass().toString());
//
//            //  cn.hutool.json.JSONArray
//            if(byPath7 instanceof JSONArray) {
//                JSONArray jsonArray = (JSONArray) byPath7;
//
//

//            }

            System.out.println("####");

//            if (objectCollinsEn != null) {
//                sampleSentenceEn = objectCollinsEn.toString();
//                vocInfo.setSampleSentenceEn(sampleSentenceEn);
//            }
//            if (objectCollinsCn != null) {
//                String sampleSentenceCn = objectCollinsCn.toString();
//                vocInfo.setSampleSentenceCn(sampleSentenceCn);
//            }
        }

//        JSONObject.get 2 sentSpeech sentTrans
    }

}
