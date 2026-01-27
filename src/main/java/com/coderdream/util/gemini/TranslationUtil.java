package com.coderdream.util.gemini;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.VocInfo;
import com.coderdream.util.CommonUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.CdTextUtil;
import com.coderdream.util.codex.CodexApiClient;
import com.coderdream.util.nvidia.NvidiaTranslateUtil;
import com.coderdream.util.process.ListSplitterStream;
import com.coderdream.util.sentence.demo1.SentenceParser;
import com.coderdream.vo.SentenceVO;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TranslationUtil {

  public static String translate(String text) {
    return GeminiApiClient.generateContent(text);
  }

  /**
   * 处理词汇信息，将其翻译后写入文件。
   *
   * @param vocInfoList 包含词汇信息的列表
   * @param fileName    文件名，用于存储翻译结果
   */
  public static void processVoc(List<VocInfo> vocInfoList, String fileName) {
    processVoc(vocInfoList, fileName, CdConstants.AI_PROVIDER_CODEX);
  }

  public static void processVoc(List<VocInfo> vocInfoList, String fileName,
    String provider) {
    if (CollectionUtil.isEmpty(vocInfoList)) {
      log.error("词汇列表为空，无法翻译。");
      return;
    }
    StringBuilder text = new StringBuilder(
      CdConstants.VOC_CN_PREFIX);  // 使用 StringBuilder 拼接字符串，避免多次创建字符串对象

    // 遍历 vocInfoList，拼接文本
    for (VocInfo vocInfo : vocInfoList) {
      text.append(vocInfo.getWord()).append("\n ");
      text.append(vocInfo.getWordExplainEn()).append("\n");
    }

    // 调用翻译方法并记录日志
    String providerName = StrUtil.blankToDefault(provider,
      CdConstants.AI_PROVIDER_GEMINI);
    log.info("开始翻译文本内容，包含 {} 个词汇，provider: {}", vocInfoList.size(),
      providerName);

    String result;
    if (CdConstants.AI_PROVIDER_NVIDIA.equalsIgnoreCase(providerName)) {
      result = NvidiaTranslateUtil.requestCompletion(text.toString());
    } else if (CdConstants.AI_PROVIDER_CODEX.equalsIgnoreCase(providerName)) {
      result = CodexApiClient.generateContent(text.toString());
    } else {
      result = GeminiApiClient.generateContent(text.toString());
    }

    // 记录翻译后的结果日志
    log.info("翻译完成，开始写入文件: {}", fileName);

    // 将翻译结果写入文件
    boolean writeSuccess = writeToFile(fileName, result);

    if (writeSuccess) {
      log.info("文件写入成功: {}", fileName);
    } else {
      log.error("文件写入失败: {}", fileName);
    }
  }

  /**
   * 将翻译结果写入文件
   */
  public static boolean writeToFile(String newFileName, String result) {
    // 使用正则表达式直接处理换行符
    String temp = result.replaceAll("\n{2,}", "\n");  // 将连续的两个或更多的换行符替换为一个换行符
    temp = temp.replace("\n", "__");  // 替换单个换行符为 "__"

    // 使用 Arrays.asList 来避免不必要的 List.of
    List<String> list = Arrays.asList(temp.split("__"));

    // 写入文件
    boolean writeToFileResult = CdFileUtil.writeToFile(newFileName, list);
    log.info("写入文件结果: {}", writeToFileResult);
    return writeToFileResult;
  }

  // 增加英文音标 phonetics
  public static File genPhonetics(String fileName, String jsonFileName) {
    return writePhoneticsToFile(fileName, jsonFileName);
  }

  public static File genAiFile(String fileName) {
    List<String> sentences = CdTextUtil.getAllEnglishSentencesFromFile(
      fileName);
    String aiFileName = CdFileUtil.addPostfixToFileName(fileName, "_ai");
    if (CollectionUtil.isEmpty(sentences)) {
      log.error("sentences is empty");
      return null;
    }
    int groupSize = 100;
    // 拆分句子列表，每组包含 groupSize 个元素
    List<List<String>> sentencesLists = ListSplitterStream.splitList(
      sentences, groupSize);
    int i = 0;
    String englishFileNamePart;
    String jsonFileNamePart;

    List<String> totalTranslateList = new ArrayList<>();
    for (List<String> sentencesList : sentencesLists) {
      i++;
      englishFileNamePart = CdFileUtil.addPostfixToFileName(
        fileName, "_en" + "_" + i);
      jsonFileNamePart = CdFileUtil.addPostfixToFileName(
        fileName, "_ai" + "_" + i);
      assert jsonFileNamePart != null;
      File jsonFilePart = new File(jsonFileNamePart);
      // 如果已经存在的ai文件为空或者行数和句子的大小不一致，则需要重新调用API获取音标
      if (!jsonFilePart.exists() || jsonFilePart.length() == 0
        || FileUtil.readLines(jsonFilePart, StandardCharsets.UTF_8).size()
        != sentencesList.size()) {
        String text = CdConstants.GEN_PHONETICS_TEXT_V2;
        String content = String.join("\n", sentencesList);
        text += content;
        List<String> translateList = getStringsFromGemini(text,
          sentencesList);

        if (CollectionUtil.isNotEmpty(translateList)
          && translateList.size() == sentencesList.size()) {
          FileUtil.writeLines(translateList, jsonFilePart,
            StandardCharsets.UTF_8);
          FileUtil.writeLines(sentencesList, englishFileNamePart,
            StandardCharsets.UTF_8);
          totalTranslateList.addAll(translateList);
//          translateTotal.append(translate);
        } else {
//          CdFileUtil.writeLines(translateList, jsonFilePart,
//            StandardCharsets.UTF_8);
//          CdFileUtil.writeLines(sentencesList, englishFileNamePart,
//            StandardCharsets.UTF_8);

//          totalTranslateList.addAll(translateList);

          if (CollectionUtil.isNotEmpty(translateList)
            && translateList.size() == sentencesList.size()) {
            FileUtil.writeLines(translateList, jsonFilePart,
              StandardCharsets.UTF_8);
            FileUtil.writeLines(sentencesList, englishFileNamePart,
              StandardCharsets.UTF_8);
            totalTranslateList.addAll(translateList);
//          translateTotal.append(translate);
          } else {
//          CdFileUtil.writeLines(translateList, jsonFilePart,
//            StandardCharsets.UTF_8);
//          CdFileUtil.writeLines(sentencesList, englishFileNamePart,
//            StandardCharsets.UTF_8);

//          totalTranslateList.addAll(translateList);
            log.error(
              "translateList size is not equal to sentencesList size,"
                + " translateList.size {},"
                + " sentencesList.size {}, "
                + " jsonFileNamePart: {}",
              translateList.size(), sentencesList.size(), jsonFileNamePart);
            break;
          }
        }
      } else {
        List<String> translateList = FileUtil.readLines(jsonFilePart,
          StandardCharsets.UTF_8);
        totalTranslateList.addAll(translateList);
      }
//      log.info("genPhonetics Total: {}", translate);
    }

    // 把字符串中的空格+斜线替换为回车换行加斜线
//    String translateTotalString = RemoveEmptyLines.removeEmptyLines(
//      translateTotal.toString());
//
//    CdFileUtil.writeUtf8String(translateTotalString, aiFileName);
    if (totalTranslateList.size() != sentences.size()) {
      log.error("totalTranslateList size is not equal to sentences size,"
          + " totalTranslateList.size {},"
          + " sentences.size {}",
        totalTranslateList.size(), sentences.size());
    } else {
      FileUtil.writeLines(totalTranslateList, aiFileName,
        StandardCharsets.UTF_8);
    }

//    log.info("genPhonetics: {}", translate);
    return new File(aiFileName);
  }

  // 最大重试次数
  private static final int MAX_RETRY_ATTEMPTS = 10;

  /**
   * 从 Gemini 获取字符串列表。
   *
   * @param text          输入文本
   * @param sentencesList 句子列表，用于确定期望的翻译大小
   * @return 翻译后的字符串列表
   */
  private static @NotNull List<String> getStringsFromGemini(String text,
    List<String> sentencesList) {
    return getStringsFromGeminiWithRetry(text, sentencesList,
      MAX_RETRY_ATTEMPTS);
  }

  /**
   * 从 Gemini 获取字符串列表（带重试机制）。
   *
   * @param text              输入文本
   * @param sentencesList     句子列表，用于确定期望的翻译大小
   * @param remainingAttempts 剩余重试次数
   * @return 翻译后的字符串列表。如果多次重试后仍无法获得期望大小的列表，则返回空列表。
   */
  private static @NotNull List<String> getStringsFromGeminiWithRetry(
    String text, List<String> sentencesList, int remainingAttempts) {
    // 如果重试次数用尽，返回空列表
    if (remainingAttempts <= 0) {
      // 可以选择抛出异常，或者返回空列表，具体取决于需求
      log.error(
        "在多次尝试后，未能从 Gemini 获取到期望大小的翻译列表。"); // 使用 log.error
      return Collections.emptyList(); // 或者其他合适的默认值
    }

    // 调用 Gemini API 生成内容
    String translate = GeminiApiClient.generateContent(text);
    // 解析句子
    List<SentenceVO> sentenceVOList = SentenceParser.parseSentences(translate);

    // 构造英文句子Map
//    Map<Integer, String> englishSentenceMap = new LinkedHashMap<>();
//    for (int i = 0; i < sentencesList.size(); i++) {
//      englishSentenceMap.put(i, sentencesList.get(i));
//    }
//
//    // 提取音标
//    List<SentenceVO> translateList = new ArrayList<>();
//    SentenceVO newSentenceVO;
//    for (int i = 0; i < sentenceVOList.size(); i++) {
//      newSentenceVO = new SentenceVO();
//      SentenceVO sentenceVO = sentenceVOList.get(i);
//      String english = sentenceVO.getEnglish();
//      String phonetics = sentenceVO.getPhonetics();
//      String oldEnglish = englishSentenceMap.get(i);
//      // 如果英文句子不相同，但是以英文句子开头，则合并两个
//      if (!oldEnglish.equals(english) ) {
//        if(oldEnglish.startsWith(english)) {
//          if (i < sentenceVOList.size() - 1) {
//            String nextEnglish = sentenceVOList.get(i + 1).getEnglish();
//            String nextPhonetics = sentenceVOList.get(i + 1).getPhonetics();
//            String newEnglish = english + " " + nextEnglish;
//            String newPhonetics = phonetics + " " + nextPhonetics;
//            if (!newEnglish.equals(oldEnglish)) {
//              newSentenceVO.setEnglish(newEnglish);
//              newSentenceVO.setPhonetics(newPhonetics);
//            }
//            translateList.add(newSentenceVO);
//          }
//        }
//      } else {
//        newSentenceVO.setEnglish(english);
//        newSentenceVO.setPhonetics(phonetics);
//        translateList.add(newSentenceVO);
//      }
//    }

    List<String> phoneticsList = SentenceParser.getPhoneticsList(
      sentenceVOList);
    if (CollectionUtil.isEmpty(phoneticsList)) {
      log.error("phoneticsList is empty");
      return getStringsFromGeminiWithRetry(text, sentencesList,
        remainingAttempts - 1);
    }

    // 如果列表大小不符合预期，则进行重试
    if (phoneticsList.size() != sentencesList.size()) {
      // 使用 log.error 记录错误信息，而不是 System.err.println
      log.error(
        "翻译结果大小不匹配。期望: {}, 实际: {}, 正在重试... (剩余 {} 次尝试)",
        sentencesList.size(), phoneticsList.size(), remainingAttempts - 1);
      // 递归调用，减少剩余重试次数
      return getStringsFromGeminiWithRetry(text, sentencesList,
        remainingAttempts - 1);
    }

    // 返回翻译结果
    return phoneticsList;
  }

  private static File writePhoneticsToFile(String totalFileName,
    String jsonFileName) {
    String addPostfixToFileName = CdFileUtil.addPostfixToFileName(totalFileName,
      "_phonetics");
    List<String> lines = FileUtil.readLines(new File(jsonFileName),
      StandardCharsets.UTF_8);
    if (CollectionUtil.isEmpty(lines)) {
      log.error("lines is empty");
      return null;
    }

//    List<SentenceVO> sentenceVOPhList = CdTextUtil.parseSentencesFromFileWithEnglishAndPhonetics(
//      jsonFileName);
    List<SentenceVO> sentenceVOPhList = CdTextUtil.parseSentencesFromFileWithPhonetics(
      jsonFileName);
    List<SentenceVO> sentenceVOCnList = CdTextUtil.parseSentencesFromFile(
      totalFileName);
    if (CollectionUtil.isEmpty(sentenceVOPhList) || CollectionUtil.isEmpty(
      sentenceVOPhList) || (sentenceVOPhList.size() != sentenceVOCnList.size()
      && sentenceVOPhList.size() * 2 != sentenceVOCnList.size())) {
      log.error("音标列表和中文列表不一致, 音标列表大小： {}，中文列表大小： {},",
        sentenceVOPhList.size(),
        sentenceVOCnList.size());
      return null;
    }
    File file = new File(addPostfixToFileName);
    String filePath = file.getParent();
    log.info("对话信息将写入到文件: {}", file.getName());
    Path outputFilePath = Paths.get(filePath,
      file.getName());
    try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath,
      StandardCharsets.UTF_8)) {
      SentenceVO sentenceVO = null;
      SentenceVO sentenceVOCn = null;
      for (int i = 0; i < sentenceVOPhList.size(); i++) {
        sentenceVO = sentenceVOPhList.get(i);
        sentenceVOCn = sentenceVOCnList.get(i);
        writer.write(sentenceVOCn.getEnglish());
        writer.newLine();
        writer.write(sentenceVO.getPhonetics());
        writer.newLine();
        writer.write(ZhConverterUtil.toTraditional(sentenceVOCn.getChinese()));
        writer.newLine();
      }
      log.info("对话信息已成功写入到文件: {}", outputFilePath);
    } catch (IOException e) {
      log.error("写入文件 {} 发生异常：{}", outputFilePath, e.getMessage(), e);
    }
    return file;
  }


  /**
   * 生成文章描述
   *
   * @param folderName 文件夹名称
   */
  public static void genDescription(String folderName) {
    String txtFileName = CommonUtil.getFullPathFileName(folderName, folderName,
      ".txt");
    String scriptDialogMergeFileName = CdFileUtil.addPostfixToFileName(
      txtFileName,
      "_中英双语对话脚本");
    String descriptionFileName = CdFileUtil.changeExtension(txtFileName, "md");
    descriptionFileName = CdFileUtil.addPostfixToFileName(descriptionFileName,
      "_description");
    genDescription(scriptDialogMergeFileName, descriptionFileName);
  }

  /**
   * 生成文章描述
   *
   * @param scriptDialogMergeFileName 对话脚本文件名
   * @param descriptionFileName       描述文件名
   * @return
   */
  public static File genDescription(String scriptDialogMergeFileName,
                                    String descriptionFileName) {
//    String folderPath = CommonUtil.getFullPath(folderName);
//    String fileName = folderPath + File.separator + folderName + "_中英双语对话脚本.txt";
    String text = "解析下面的文本，帮我写文章，用来发哔哩哔哩（B站）、快手、小红书和公众号，要根据不同的平台特性生成不同风格的文章，B站的文章字数在1500~2000之间，包含词汇和例句，快手的文章字数在500~600之间，小红书不超过800字，公众号不超过200字；另外，帮我每个平台取3个疑问句的标题，标题中间不要有任何标点符号、表情符号且不超过20个字，快手加入一些表情符号，生成的内容要直接可用，不要让我填空。文本如下：";
    List<String> vocInfoList = CdFileUtil.readFileContent(
      scriptDialogMergeFileName);
    assert vocInfoList != null;
    String content = String.join("\n", vocInfoList);
    text += content;
    String translate = GeminiApiClient.generateContent(text);
    log.info("translate: {}", translate);

    try (BufferedWriter writer = Files.newBufferedWriter(
      Path.of(descriptionFileName),
      StandardCharsets.UTF_8)) {
      for (String line : translate.split("\n")) {
        if (StrUtil.isNotEmpty(line)) {
          // 以斜线\ 分割字符串，然后逐个写入文件 斜线 \\\\ 反斜线  /
//          String[] split = line.split("/");
//          if (CollectionUtil.isNotEmpty(Arrays.asList(split))
//            && split.length == 3) {
//            writer.write(split[0]);
//            writer.newLine();
//            writer.write("/" + split[1] + "/");
//            writer.newLine();
          writer.write(line);
          writer.newLine();
//          }
        }
      }
      log.info("对话信息已成功写入到文件: {}", descriptionFileName);
    } catch (IOException e) {
      log.error("写入文件 {} 发生异常：{}", descriptionFileName, e.getMessage(),
        e);
    }
    return new File(descriptionFileName);
  }


  /**
   * 生成文章描述
   *
   * @param bookFileName           对话脚本文件名
   * @param readBookScriptFileName 描述文件名
   */
  public static void genReadBookScript(String bookFileName,
    String bookFileNameWithPath,
    String readBookScriptFileName) {
//    String folderPath = CommonUtil.getFullPath(folderName);
//    String fileName = folderPath + File.separator + folderName + "_中英双语对话脚本.txt";
    String prompt = genReadBookScriptPromptPrefix(bookFileName);
    prompt += FileUtil.readString(
      bookFileNameWithPath,
      StandardCharsets.UTF_8);
    log.info("prompt: {}", prompt);
    String translate = GeminiApiClient.generateContent(prompt);
    log.info("translate: {}", translate);

    try (BufferedWriter writer = Files.newBufferedWriter(
      Path.of(readBookScriptFileName),
      StandardCharsets.UTF_8)) {
      for (String line : translate.split("\n")) {
        if (StrUtil.isNotEmpty(line)) {
          // 以斜线\ 分割字符串，然后逐个写入文件 斜线 \\\\ 反斜线  /
          writer.write(line);
          writer.newLine();
        }
      }
      log.info("对话信息已成功写入到文件: {}", readBookScriptFileName);
    } catch (IOException e) {
      log.error("写入文件 {} 发生异常：{}", readBookScriptFileName,
        e.getMessage(),
        e);
    }
  }

  private static String genReadBookScriptPromptPrefix(String bookFileName) {
    StringBuilder sb = new StringBuilder();
    sb.append("你是一位精品书的书评的内容创作者，");
    sb.append("现在需要为《").append(bookFileName)
      .append("》制作一个“十分钟精读”文本，");
    sb.append("目标是简洁全面地总结书籍内容，");
    sb.append("帮助读者快速理解书中内容的科学意义和实用建议。以下是具体要求：");
    sb.append("1. 长度与结构：");
    sb.append("文本长度控制在8000-9500字，适合阅读或朗读。");
    sb.append("开头介绍书名、作者背景和核心问题，");
    sb.append("接着分多个部分解读，最后以总结和推荐收尾。");
    sb.append("2. 语气与风格：");
    sb.append("使用轻松、自然的叙述语气，像与朋友聊天，");
    sb.append("偶尔加入幽默或生活化的比喻，");
    sb.append("但保持科学性和可信度。");
    sb.append("避免晦涩术语，");
    sb.append("用通俗语言解释专业概念，");
    sb.append("保留科普书的启发性。");
    sb.append("3. 内容要求：");
    sb.append("各部分内容及解析");
    sb.append("融入书中趣闻或金句，结尾呼吁重视书中提到的核心内容并推荐原书。");
    sb.append("4. 输入素材：");
    sb.append("以下是《").append(bookFileName)
      .append("》电子书的全文（或摘录）：[电子书文本在后面]。");
    sb.append("请基于这些内容生成文本，");
    sb.append("若素材不足，");
    sb.append("可凭对书的理解补充，");
    sb.append("但需符合原著精神。");
    sb.append("5. 输出格式：");
    sb.append("以文本格式输出，");
    sb.append("标题为《").append(bookFileName).append("》，");
    sb.append("包含小标题分段。");
    sb.append("不要包含任何markdown标记。");
    sb.append("序号不要用阿拉伯数字，用中文一、二、三、四、等等。");
    sb.append("内容不要出现括号。");
    sb.append("每句话都要有标点符号，即使是标题也加上句号。");
    sb.append("字数一定要符合，如果字数不达标就重新生成。");
    sb.append("请根据以上要求生成文本。电子书原版内容如下：");

    return sb.toString();
  }
}
