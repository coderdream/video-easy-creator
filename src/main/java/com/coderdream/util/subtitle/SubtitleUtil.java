package com.coderdream.util.subtitle;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.DurationEntity;
import com.coderdream.entity.SubtitleEntity;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.CdTimeUtil;
import com.coderdream.util.chatgpt.TextParserUtilChatgpt;
import com.coderdream.util.cmd.CommandUtil;
import com.coderdream.util.proxy.OperatingSystem;
import com.coderdream.util.sentence.SentenceParser;
import com.coderdream.util.sentence.StanfordSentenceSplitter;
import com.coderdream.util.string.StringChecker;
import com.coderdream.util.video.BatchCreateVideoCommonUtil;
import com.coderdream.vo.SentenceDurationVO;
import com.coderdream.vo.SentenceVO;

import java.io.File;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubtitleUtil {

  /**
   * 生成带有文本的图片。
   *
   * @param fileName 文件名称
   * @return 图片文件列表
   */
  public static List<SentenceDurationVO> genSubtitle(String fileName) {
    String fullPath =
      CdConstants.RESOURCES_BASE_PATH + File.separator + fileName + ".txt";
//    fullPath = fileName + ".txt";

    List<SubtitleEntity> subtitleEntities = new ArrayList<>();
    List<SentenceVO> sentenceVOs = TextParserUtilChatgpt.parseFileToSentenceVOsSingleLine(
      fullPath);

    String durationFileName =
      BatchCreateVideoCommonUtil.getAudioPath(fileName) + fileName
        + "_duration.txt";
    File durationFile = new File(durationFileName);
//    durationFile.readAllLines();
//    Files.readAllLines(Paths.get(durationFile));

    Map<String, DurationEntity> durationEntityMap = new LinkedHashMap<>();

    // 使用 Hutool 的 readLines 方法读取文件所有行
    List<String> lines = FileUtil.readLines(durationFile, "UTF-8");
    for (String line : lines) {

      DurationEntity durationEntity = new DurationEntity();
      // 解析每行数据，并将其存储到 durationEntityMap 中
      int lastIndex = line.lastIndexOf("." + CdConstants.AUDIO_TYPE_WAV);
      int index = line.lastIndexOf(fileName);
      String line2 = line.substring(index + fileName.length() + 1, lastIndex);
      log.info("line2:{}", line2);
      String[] split = line2.split("_");

      if (split.length == 2) {
        durationEntity.setIndexStr(split[0]);
        durationEntity.setLang(split[1]);
      } else {
        log.error("未找到:{}", line2);
      }

      String[] splitDuration = line.split("\t");
      if (splitDuration.length == 2) {
        String duration = splitDuration[1];
        durationEntity.setDuration(Double.parseDouble(duration.trim()));
      } else {
        durationEntity.setDuration(0);
        log.error("未找到:{}", line);
      }

//      durationEntity.setDuration(line);
      durationEntityMap.put(line2, durationEntity);
    }

    SubtitleEntity subtitleEntity = null;

    List<SentenceDurationVO> sentenceDurationVOs = new ArrayList<>();
    SentenceDurationVO sentenceDurationVO = null;

    // 使用DecimalFormat来确保保留3位小数
    DecimalFormat decimalFormat = new DecimalFormat("#.###");
    // 3
    //00:00:04,000 --> 00:00:06,000
    int number = 0;
    for (SentenceVO sentenceVO : sentenceVOs) {
      number++;
      // 创建字幕实体
      sentenceDurationVO = new SentenceDurationVO();
      sentenceDurationVO.setId(number);
      String indexStr = MessageFormat.format(
        "{0,number,000}",
        number);
      subtitleEntity = new SubtitleEntity();
      subtitleEntity.setSubIndex(1);
      subtitleEntity.setTimeStr("00:00");
      subtitleEntity.setSecondSubtitle(sentenceVO.getEnglish());
      subtitleEntity.setSubtitle(sentenceVO.getChinese());
      subtitleEntities.add(subtitleEntity);
//      log.info("subtitleEntity:{}", subtitleEntity);
      String lang = CdConstants.LANG_CN;
      String key = indexStr + "_" + lang;
      String str = sentenceVO.getChinese() + "\t" + durationEntityMap.get(key)
        .getDuration();
      log.info("strCn:{}", str);

      sentenceDurationVO.setChinese(sentenceVO.getChinese());
      double chineseDuration = durationEntityMap.get(key).getDuration();
      sentenceDurationVO.setChineseDuration(chineseDuration);
      sentenceDurationVO.setPhonetics(sentenceVO.getPhonetics());
      // 英文
      lang = CdConstants.LANG_EN;
      key = indexStr + "_" + lang;
      str = sentenceVO.getEnglish() + "\t" + durationEntityMap.get(key)
        .getDuration();
      log.info("strEn:{}", str);
      sentenceDurationVO.setEnglish(sentenceVO.getEnglish());
      double englishDuration = durationEntityMap.get(key).getDuration();

      sentenceDurationVO.setEnglishDuration(englishDuration);

      // 计算时长

      // 计算总时长: 总时长 = 4 * 英文时长 + 中文时长
      double totalDuration = 4 * englishDuration + chineseDuration;

      // 格式化总时长为保留3位小数
      totalDuration = Double.parseDouble(decimalFormat.format(totalDuration));

      sentenceDurationVO.setTotalDuration(totalDuration);
      sentenceDurationVOs.add(sentenceDurationVO);
    }

    // 遍历 sentenceDurationVOs
    for (SentenceDurationVO sentenceDurationVO1 : sentenceDurationVOs) {
      log.info("sentenceDurationVO:{}", sentenceDurationVO1);
    }

    // 设置路径
//    String outputDir = BatchCreateVideoCommonUtil.getPicPath(
//      fileName);//  "src/main/resources/pic"; // 输出目录

    return sentenceDurationVOs;
  }

  /**
   * @param srcFileNameEng 英文字幕文件
   * @param srcFileNameChn 中文字幕文件
   * @param srcFileName    输出文件路径
   */
  public static void mergeSubtitleFile(String srcFileNameEng,
    String srcFileNameChn, String srcFileName) {
    long startTime = System.currentTimeMillis(); // 记录开始时间
    List<SubtitleEntity> engSubtitleEntityList = CdFileUtil.readSrtFileContent(
      srcFileNameEng);
    List<SubtitleEntity> chnSubtitleEntityList = CdFileUtil.readSrtFileContent(
      srcFileNameChn);

    SubtitleEntity enSubtitleEntity;
    List<String> srtStringList = new ArrayList<>();
    if (CollectionUtil.isNotEmpty(engSubtitleEntityList)
      && CollectionUtil.isNotEmpty(chnSubtitleEntityList)
      && chnSubtitleEntityList.size() == engSubtitleEntityList.size()) {
      log.info("中英文字幕相等 size: {}", engSubtitleEntityList.size());
      for (int i = 0; i < engSubtitleEntityList.size(); i++) {
        enSubtitleEntity = engSubtitleEntityList.get(i);
        srtStringList.add(enSubtitleEntity.getSubIndex() + "");
        srtStringList.add(enSubtitleEntity.getTimeStr());
        srtStringList.add(enSubtitleEntity.getSubtitle());
        srtStringList.add(chnSubtitleEntityList.get(i).getSubtitle());
        srtStringList.add("");
      }
    } else {
      log.error("中英文字幕不相等 英文字幕大小: {}, 英文字幕大小: {}",
        engSubtitleEntityList.size(), chnSubtitleEntityList.size());
    }

    if (CollectionUtil.isNotEmpty(srtStringList)) {
      // 写中文翻译文本
      CdFileUtil.writeToFile(srcFileName, srtStringList);
      long elapsedTime = System.currentTimeMillis() - startTime; // 计算耗时
      log.info("写入完成，文件路径: {}，共计耗时：{}", srcFileName,
        CdTimeUtil.formatDuration(elapsedTime));
    } else {
      System.out.println("newList is empty!");
    }
  }

  /**
   * @param srcFileName 英文字幕文件
   */
  public static void modifySubtitleFile(String srcFileName) {
    long startTime = System.currentTimeMillis(); // 记录开始时间
    List<SubtitleEntity> subtitleEntityList = CdFileUtil.readSrtFileContent(
      srcFileName);

    SubtitleEntity subtitleEntity;
    List<String> srtStringList = new ArrayList<>();
    if (CollectionUtil.isNotEmpty(subtitleEntityList)) {
      log.info("中英文字幕相等 size: {}", subtitleEntityList.size());
      for (int i = 0; i < subtitleEntityList.size(); i++) {
        subtitleEntity = subtitleEntityList.get(i);
        srtStringList.add(subtitleEntity.getSubIndex() + "");
        srtStringList.add(subtitleEntity.getTimeStr());
        String subtitle = subtitleEntity.getSubtitle();
        if (!StringChecker.onlyContainsChineseCharactersAndPunctuation(
          subtitle)) {
          log.error("index:{}, : {}", subtitleEntity.getSubIndex(),
            subtitle);
        }
        srtStringList.add(subtitleEntity.getSubtitle());
        srtStringList.add("");
      }
    } else {
      log.error("中英文字幕不相等 英文字幕大小: {}",
        subtitleEntityList.size());
    }

//    if (CollectionUtil.isNotEmpty(srtStringList)) {
//      // 写中文翻译文本
//      CdFileUtil.writeToFile(srcFileName, srtStringList);
//      long elapsedTime = System.currentTimeMillis() - startTime; // 计算耗时
//      log.info("写入完成，文件路径: {}，共计耗时：{}", srcFileName,
//        CdTimeUtil.formatDuration(elapsedTime));
//    } else {
//      System.out.println("newList is empty!");
//    }
  }

  /**
   * @param
   */
  public static void genSubtitleRaw(String fileName) {
//    String translate = CdFileUtil.readString(new File(fileName),
//      StandardCharsets.UTF_8);// GeminiApiClient.generateContent(text);
//    log.info("translate: {}", translate);

    List<String> sentenceList = new ArrayList<>();
    sentenceList.add(
      "Enhance your English listening with 30-minute sessions of English audio, paired with Chinese dubbing.");
    sentenceList.add("英文加中文配音，每次半小時，增强你的英文听力。");

    // D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
    List<SentenceVO> sentenceVOList = SentenceParser.parseSentencesFromFile(
      fileName);

    for (SentenceVO sentenceVO : sentenceVOList) {
      log.info("sentenceVO:{}", sentenceVO);
      sentenceList.add(sentenceVO.getEnglish());
      sentenceList.add(sentenceVO.getEnglish());
      sentenceList.add(sentenceVO.getEnglish());
      sentenceList.add(sentenceVO.getChinese());
      sentenceList.add(sentenceVO.getEnglish());
    }
    String srtRawFileName = CdFileUtil.addPostfixToFileName(
      fileName, "_raw");
//    log.info("srtRawFileName:{}", srtRawFileName);
    CdFileUtil.writeToFile(srtRawFileName, sentenceList);

    // 生成字幕文件，调用 python 命令
    File file = new File(srtRawFileName);
    String path = file.getParent();
    // D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
    // D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics\audio\ch01_mix.wav
    String audioFileName =
      path + File.separator
        + CdFileUtil.getPureFileNameWithoutExtensionWithPath(
        fileName)
        + File.separator + "audio\\" + "ch01_mix.wav";
    File audioFile = new File(audioFileName);
    if (!audioFile.exists()) {
      log.error("音频文件不存在:{}", audioFileName);
      return;
    }

    String srtFileName = CdFileUtil.changeExtension(
      audioFileName, "srt");
    String lang = "eng"; //  String lang = "cmn";
    log.info("audioFileName:{}", audioFileName);
    SubtitleUtil.genSrtByExecuteCommand(audioFileName, srtRawFileName,
      srtFileName, lang);
  }

  /**
   * @param fileName 英文字幕文件路径，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   *                 生成文本文件，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   */
  public static String transferSubtitleToSentenceTextFile(String fileName) {
    String textFileName = CdFileUtil.changeExtension(
      fileName, "txt");
    List<SubtitleEntity> sentenceVOList = CdFileUtil.readSrtFileContent(
      fileName);
    StringBuilder text = new StringBuilder();
    for (SubtitleEntity sentenceVO : sentenceVOList) {
//      log.info("SubtitleEntity:{}", sentenceVO);
      text.append(sentenceVO.getSubtitle().trim()).append(" ");
    }

    List<String> sentenceList = StanfordSentenceSplitter.splitSentences(
      text.toString());
    List<String> pureSentenceList = new ArrayList<>();
    for (String sentence : sentenceList) {
      String pureSentence = filterContent(sentence);
      if (pureSentence.length() > 150) {
        log.info("sentence length:{},  {}", pureSentence.length(),
          pureSentence);
      }
      pureSentenceList.add(pureSentence);
    }
    CdFileUtil.writeToFile(textFileName,
      pureSentenceList);
//    if (CdFileUtil.isFileEmpty(textFileName)) {
//      CdFileUtil.writeToFile(textFileName, pureSentenceList);
//    } else {
//      log.info("文件已存在，不重复生成:{}", textFileName);
//    }
    return textFileName;
  }

  /**
   * @param sentence 英文字幕文件路径，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   *                 生成文本文件，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   */
  public static String filterContent(String sentence) {
    List<String> filteredContent = Arrays.asList("(Applause.)", "(Laughter.)");

    StringBuilder sb = new StringBuilder(sentence);

    for (String filter : filteredContent) {
      int index = sb.indexOf(filter);
      while (index != -1) {
        sb.delete(index, index + filter.length());
        index = sb.indexOf(filter);  // 继续查找下一个匹配项
      }
    }

    return sb.toString().trim();
  }

  /**
   * @param sentence 英文字幕文件路径，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   *                 生成文本文件，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   */
  public static String filterContentWithRegex(String sentence) {
    List<String> filteredContent = Arrays.asList("(Applause.)", "(Laughter.)");

    // 使用正则表达式，将所有要过滤的字符串用 | 连接起来
    String regex = String.join("|", filteredContent);

    // 使用 replaceAll 替换所有匹配的字符串为空字符串
    String result = sentence.replaceAll(Pattern.quote(regex), "");

    return result.trim();
  }

  /**
   * 生成字幕文件，调用 python 命令
   *
   * @param audioFileName    音频文件路径，例如：D:\0000\EnBook001\900\ch01\audio\ch01_mix.wav
   * @param subtitleFileName 字幕文件路径，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   * @param srtFileName      字幕文件路径，例如：D:\0000\EnBook001\900\ch01\dialog_single_with_phonetics.txt
   * @param lang             语言，例如：eng, cmn
   */
  public static void genSrtByExecuteCommand(String audioFileName,
    String subtitleFileName, String srtFileName, String lang) {
    // 使用 Python 3.9 以支持 aeneas 模块
    String python = OperatingSystem.getPython39Env();
    String command = python +
      " -m aeneas.tools.execute_task " + audioFileName + " "
      + subtitleFileName
      + " \"task_language=" + lang
      + "|os_task_file_format=srt|is_text_type=plain\" "
      + srtFileName;
    CommandUtil.executeCommand(command);
    // python -m aeneas.tools.execute_task D:/0000/v1.mp3 D:/0000/v1_srt.txt "task_language=cmn|os_task_file_format=srt|is_text_type=plain" D:/0000/v1.srt
    //python -m aeneas.tools.execute_task D:/0000/v1.mp3 D:/0000/v1_srt.txt "task_language=eng|os_task_file_format=srt|is_text_type=plain" D:/0000/v1.srt
  }

  /**
   * 生成多份文件
   *
   * @param fileName
   */
  public static void genMultiSubtitle(String fileName, String srtFileName) {
    List<SentenceVO> sentenceVOs = SentenceParser.parseSentencesFromFile(
      fileName);
//    for (SentenceVO sentenceVO : sentenceVOs) {
//      log.info(" sentenceVO:{}", sentenceVO);
//    }
    List<SubtitleEntity> subtitleList = SubtitleParser.parseSubtitleFile(
      srtFileName);
    // 如果两个列表大小不一致，则抛出异常
    int size = sentenceVOs.size();
    size = size * 5 + 2; // 每句英文有5行字幕，再加2个开头字幕
    List<SubtitleEntity> subtitleListIndex;
    List<SubtitleEntity> subtitleListEnglish;
    List<SubtitleEntity> subtitleListPhonetics;
    List<SubtitleEntity> subtitleListChinese;
    SubtitleEntity subtitleEntityIndex;
    SubtitleEntity subtitleEntityEnglish;
    SubtitleEntity subtitleEntityPhonetics;
    SubtitleEntity subtitleEntityChinese;

    if (size != subtitleList.size()) {
      throw new RuntimeException("文件大小不一致");
    } else {
      subtitleListIndex = new ArrayList<>();
      subtitleListEnglish = new ArrayList<>();
      subtitleListPhonetics = new ArrayList<>();
      subtitleListChinese = new ArrayList<>();
      int index = 0;
      int sentenceIndex = 0;
      for (int i = 2; i < subtitleList.size(); i++) {
        index += 1;
        sentenceIndex = (i - 2) / 5; // 每句英文有5行字幕，再加2个开头字幕
        SentenceVO sentenceVO = sentenceVOs.get(sentenceIndex);
        SubtitleEntity subtitleEntity = subtitleList.get(i);

        subtitleEntityIndex = new SubtitleEntity();
        subtitleEntityIndex.setSubIndex(index);
        subtitleEntityIndex.setTimeStr(subtitleEntity.getTimeStr());
        subtitleEntityIndex.setSubtitle(index + "");

        subtitleEntityEnglish = new SubtitleEntity();
        subtitleEntityEnglish.setSubIndex(index);
        subtitleEntityEnglish.setTimeStr(subtitleEntity.getTimeStr());
        subtitleEntityEnglish.setSubtitle(sentenceVO.getEnglish());

        subtitleEntityPhonetics = new SubtitleEntity();
        subtitleEntityPhonetics.setSubIndex(index);
        subtitleEntityPhonetics.setTimeStr(subtitleEntity.getTimeStr());
        subtitleEntityPhonetics.setSubtitle(sentenceVO.getPhonetics());

        subtitleEntityChinese = new SubtitleEntity();
        subtitleEntityChinese.setSubIndex(index);
        subtitleEntityChinese.setTimeStr(subtitleEntity.getTimeStr());
        subtitleEntityChinese.setSubtitle(sentenceVO.getChinese());

        subtitleListIndex.add(subtitleEntityIndex);
        subtitleListEnglish.add(subtitleEntityEnglish);
        subtitleListPhonetics.add(subtitleEntityPhonetics);
        subtitleListChinese.add(subtitleEntityChinese);
      }
    }

//    log.info("subtitleListEnglish:{}", subtitleListEnglish);

    String srtFileNameIndex = srtFileName.replace(".srt", "_index.srt");
    String srtFileNameEnglish = srtFileName.replace(".srt", "_english.srt");
    String srtFileNamePhonetics = srtFileName.replace(".srt", "_phonetics.srt");
    String srtFileNameChinese = srtFileName.replace(".srt", "_chinese.srt");
    log.info("srtFileNameIndex:{}", srtFileNameIndex);
    log.info("srtFileNameEnglish:{}", srtFileNameEnglish);
    log.info("srtFileNamePhonetics:{}", srtFileNamePhonetics);
    log.info("srtFileNameChinese:{}", srtFileNameChinese);
    writeToSubtitleFile(srtFileNameIndex, subtitleListIndex);
    writeToSubtitleFile(srtFileNameEnglish, subtitleListEnglish);
    writeToSubtitleFile(srtFileNamePhonetics, subtitleListPhonetics);
    writeToSubtitleFile(srtFileNameChinese, subtitleListChinese);
  }

  public static void writeToSubtitleFile(String srtFileName,
    List<SubtitleEntity> subtitleList) {
    List<String> contents = new ArrayList<>();
    for (SubtitleEntity subtitleEntity : subtitleList) {
      contents.add(subtitleEntity.getSubIndex().toString());
      contents.add(subtitleEntity.getTimeStr());
      contents.add(subtitleEntity.getSubtitle());
      // 如果不为空就添加第二行字幕
      if (StrUtil.isNotEmpty(subtitleEntity.getSecondSubtitle())) {
        contents.add(subtitleEntity.getTimeStr());
      }
      contents.add("");
    }

    FileUtil.writeUtf8Lines(contents, srtFileName);
  }


  public static void main(String[] args) {
//    genSubtitle("CampingInvitation_cht_03");

    String filePath = "D:\\0000\\0003_PressBriefings\\250128\\250128.txt";
    SubtitleUtil.genSubtitleRaw(filePath);

//    String fileName = "D:\\\\0000\\\\EnBook001\\\\900\\\\ch01\\\\dialog_single_with_phonetics.txt";
//    String srtFileName = "D:\\0000\\EnBook001\\900\\ch01\\dialog_single_with_phonetics\\audio\\ch01_mix.srt";
//    genMultiSubtitle(fileName, srtFileName);
  }


}
