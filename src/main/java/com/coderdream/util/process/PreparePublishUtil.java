package com.coderdream.util.process;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.SubtitleEntity;
import com.coderdream.util.CommonUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cmd.CommandUtil;
import com.coderdream.util.gemini.*;
import com.coderdream.util.proxy.OperatingSystem;
import com.coderdream.util.resource.ResourcesSourcePathUtil;
import com.coderdream.util.subtitle.SubtitleUtil;
import com.coderdream.util.video.demo04.Mp4MergeUtil;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import swiss.ameri.gemini.api.GenAi;

@Slf4j
public class PreparePublishUtil {

    /**
     * 商务英语900句
     */

//  public static final String B_E_900_CHAPTER_NAME = "900_cht_name.txt";

//  public static void process(String folderPath, String subFolder) {
//    // 1. 生成字幕
//
////    String path = "D:/0000/Book02/";
////    String pureName = "Boo02_v2";
////    String mp3FileName = path + pureName + ".mp3";
////    String subtitleFileName = path + pureName + "_srt.txt";
//
//    String lang = "cmn";
//
//    Map<String, String> chapterNameMap = new HashMap<>();
//    List<String> stringList = CdFileUtil.readLines(B_E_900_CHAPTER_NAME,
//      StandardCharsets.UTF_8);
//    for (String line : stringList) {
//      String[] split = line.split(" ");
//      chapterNameMap.put(split[1], split[2]);
//    }
//    String shortSubFolder = subFolder.substring(3);
//    String chapterName = chapterNameMap.get(shortSubFolder);
//
//    String mp3FileName =
//      "D:\\0000\\商務英語-EP-" + shortSubFolder + "-" + chapterName
//        + "\\商務英語-EP-" + shortSubFolder + "-" + chapterName + ".MP3";
//    File mp3File = new File(mp3FileName);
//    if (!mp3File.exists() || mp3File.length() == 0) {
//      log.info("mp3文件不存在, {}", mp3FileName);
//      return;
//    }
//    String srtFileName = CdFileUtil.changeExtension(mp3FileName, "srt");
//    // D:\0000\EnBook001\900\ch003\ch003_total.txt
//    String subtitleFileName =
//      folderPath + File.separator + subFolder + File.separator + subFolder +
//        "_total.txt";
//    File totalFile = new File(subtitleFileName);
//    if (!totalFile.exists() || totalFile.length() == 0) {
//      log.info("subtitleFileName文件不存在, {}", subtitleFileName);
//      return;
//    }
//
//    List<String> textList = new ArrayList<>(List.of(
//      "Enhance your English listening with 30-minute sessions of English audio, paired with Chinese dubbing.",
//      "英文加中文配音，每次半小時，增强你的英文听力。"));
//    List<String> srtList = CdFileUtil.readLines(subtitleFileName,
//      StandardCharsets.UTF_8);
//    for (String srtLine : srtList) {
//      textList.add(ZhConverterUtil.toTraditional(srtLine));
//    }
////    textList.addAll(srtList);
//    String newSubtitleFileName = CdFileUtil.addPostfixToFileName(
//      subtitleFileName,
//      "_cht");// "D:\\0000\\EnBook001\\900\\ch003\\ch003_total_new.txt";
//    CdFileUtil.writeToFile(newSubtitleFileName, textList);
//

    /// /    String srtFileName = CdFileUtil.changeExtension(newSubtitleFileName,
    /// "srt") ;// "D:\\0000\\EnBook001\\900\\ch003\\ch003_total.srt";
//    lang = "eng";
//
//    File srtFile = new File(srtFileName);
//    if (!srtFile.exists() || srtFile.length() == 0) {
//      log.info("srt文件不存在, {}", srtFileName);
//      SubtitleUtil.genSrtByExecuteCommand(mp3FileName, newSubtitleFileName,
//        srtFileName, lang);
//    }
//
//    // 2. 生成描述
//
//    log.info("----- 4.测试 generateContent 方法开始");
//    String prompt = CdFileUtil.readString(
//      CdFileUtil.getResourceRealPath() + "\\youtube\\description_prompt.txt",
//      StandardCharsets.UTF_8);
//    ;
//    prompt += "字幕如下：";
//    prompt += CdFileUtil.readString(
//      srtFileName,
//      StandardCharsets.UTF_8);
//    // 生成文本内容（阻塞式）
//    GeneratedContent generatedContent = null;
//    try {
//      generatedContent = GeminiApiUtil.generateContent(prompt);
//    } catch (InterruptedException | ExecutionException | TimeoutException e) {
//      throw new RuntimeException(e);
//    }
//
//    String scriptFileName = "";
//    try {
//      FileUtils.writeStringToFile(
//        new File(
//          Objects.requireNonNull(
//            CdFileUtil.changeExtension(srtFileName, "md"))),
//        generatedContent.text(), "UTF-8");
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//    log.info("4. Generated content: {}", generatedContent);
//
//    log.info("----- 4.测试 generateContent 方法结束");
//
//  } chapterName
    public static void process(String bookFolderName, String subFolder,
                               String chapterFileName, String headContentFileName) {
        String folderPath =
                OperatingSystem.getBaseFolder() + File.separator + bookFolderName
                        + File.separator;

        // 1. 生成字幕
        String lang = "cmn";

        String mp4FileName =
                folderPath + File.separator + subFolder + File.separator + "video"
                        + File.separator
                        + subFolder
                        + ".mp4";
        String mp3FileName = com.coderdream.util.cd.CdFileUtil.changeExtension(
                mp4FileName, "mp3");

        if (CdFileUtil.isFileEmpty(mp3FileName)) {
            log.info("mp3文件不存在，先生成： {}", mp3FileName);
            CommandUtil.extractAudioFromMp4(mp4FileName, mp3FileName);
        } else {
            log.info("mp3文件存在, {}", mp3FileName);
        }

        String subtitleFolderPath =
                folderPath + File.separator + subFolder + File.separator + "subtitle"
                        + File.separator;
        if (!new File(subtitleFolderPath).exists()) {
            log.info("subtitle文件夹不存在，先创建： {}", subtitleFolderPath);
            try {
                FileUtils.forceMkdir(new File(subtitleFolderPath));
            } catch (IOException e) {
                log.error("创建文件夹失败：{}", subtitleFolderPath);
            }
        }

//        String totalFileNameTotal =
//          folderPath + File.separator + subFolder + File.separator + subFolder + "_total.txt";
        String totalFileNameTotal =
                folderPath + File.separator + subFolder + File.separator + subFolder
                        + ".txt";
        if (CdFileUtil.isFileEmpty(totalFileNameTotal)) {
            log.info("文件不存在或为空，已生成新文件: {}",
                    totalFileNameTotal);
            return;
        }

        Map<String, String> chapterNameMap = new LinkedHashMap<>();
        String resourcesPath = ResourcesSourcePathUtil.getResourcesSourceAbsolutePath();
        List<String> stringList = com.coderdream.util.cd.CdFileUtil.readFileContent(
                resourcesPath + File.separator + chapterFileName);

        assert stringList != null;
        for (String line : stringList) {
            // String[] split = line.split("-");
            String[] split = line.split("-");
            if (split.length == 4) {
                chapterNameMap.put(split[2], split[3]);
                chapterNameMap.put(split[2], split[3]);
                chapterNameMap.put(split[2], split[3]);
            }
        }
        String shortSubFolder = "";
        if (subFolder.length() == 5) {
            shortSubFolder = subFolder.substring(2);
        } else {
            shortSubFolder = subFolder.substring(7);
        }

        String chapterName = chapterNameMap.get(shortSubFolder);
        Map<String, String> contentEnMap = new LinkedHashMap<>();
        Map<String, String> contentCnMap = new LinkedHashMap<>();

        List<String> contentList = com.coderdream.util.cd.CdFileUtil.readFileContent(
                resourcesPath + File.separator + headContentFileName);
        if (CollectionUtil.isEmpty(contentList)) {
            log.error("{} 文件内容为空", headContentFileName);
            return;
        }

        for (String content : contentList) {
            String[] split = content.split("\\|");
            if (split.length == 3) {
                // 随机整数0~4
                String bookName = split[0].trim();
                String enContent = split[1].trim();
                contentEnMap.put(bookName, enContent);
                String cnContent = split[2].trim();
                contentCnMap.put(bookName, cnContent);
            }
        }

        String subtitleRawFileName =
                subtitleFolderPath + subFolder + "_subtitle_raw.txt";
        if (CdFileUtil.isFileEmpty(subtitleRawFileName)) {

            String enContent = contentEnMap.get(bookFolderName);
            String cnContent = contentCnMap.get(bookFolderName);

            List<String> textList = new ArrayList<>(
//        List.of(
//        contentEnMap.get(chapterName),
//        contentCnMap.get(chapterName)
//        // 英文加中文配音，每次半小時，增强你的英文听力。 小時
//        //ZhConverterUtil.toTraditional("增强你的英文听力。"))
//      )
            );
            // trim 去除空格
            textList.addAll(Arrays.stream(enContent.split(","))
                    .map(String::trim)
                    .toList());
            textList.addAll(Arrays.stream(cnContent.split("，"))
                    .map(String::trim)
                    .toList());

            makeSrcRawFile(totalFileNameTotal, subtitleRawFileName, textList);
        } else {
            log.info("文件已存在，不再生成: {}", subtitleRawFileName);
        }

        lang = "eng";
        String srtFileNameInSubtitleFolder =
                folderPath + File.separator + subFolder + File.separator + "subtitle"
                        + File.separator
                        + subFolder
                        + ".srt";

        String srtFileName =
                folderPath + File.separator + subFolder + File.separator + "video"
                        + File.separator
                        + subFolder
                        + ".srt";

        if (CdFileUtil.isFileEmpty(srtFileName)) {
            if (!com.coderdream.util.cd.CdFileUtil.isFileEmpty(
                    srtFileNameInSubtitleFolder)) {
                log.info("subtitle中的srt文件存在，直接拷贝： {}",
                        srtFileNameInSubtitleFolder);
                FileUtil.copy(Paths.get(srtFileNameInSubtitleFolder),
                        Paths.get(srtFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                log.info("srt文件不存在, {}", srtFileName);
                SubtitleUtil.genSrtByExecuteCommand(mp3FileName, subtitleRawFileName,
                        srtFileName, lang);
            }
        }

        // 生成双语字幕文件
        List<String> srtList = FileUtil.readLines(srtFileName,
                StandardCharsets.UTF_8);

        //  繁体中文列表
        List<String> zhTwList = new ArrayList<>();
        for (String srt : srtList) {
            zhTwList.add(ZhConverterUtil.toTraditional(srt));
        }
        String zhCnSrtFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(
                srtFileName,
                ".zh-CN");
        String zhTwSrtFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(
                srtFileName,
                ".zh-TW");

        if (
                com.coderdream.util.cd.CdFileUtil.isFileEmpty(zhCnSrtFileName)
                        || com.coderdream.util.cd.CdFileUtil.isFileEmpty(
                        zhTwSrtFileName)) {
            com.coderdream.util.cd.CdFileUtil.writeToFile(zhCnSrtFileName, srtList);
            com.coderdream.util.cd.CdFileUtil.writeToFile(zhTwSrtFileName, zhTwList);
        }

        String mdFileName = Objects.requireNonNull(
                com.coderdream.util.cd.CdFileUtil.changeExtension(srtFileName, "md"));
        String chnMdFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(
                mdFileName,
                "_zh_CN");
        String chtMdFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(
                mdFileName,
                "_zh_TW");
        // 2. 生成描述
        if (
                com.coderdream.util.cd.CdFileUtil.isFileEmpty(chnMdFileName)
                        || com.coderdream.util.cd.CdFileUtil.isFileEmpty(
                        chtMdFileName)) {
            log.info("文件已存在，不再生成: {}", mdFileName);

            log.info("----- 4.测试 generateContent 方法开始");
            String prompt = FileUtil.readString(
                    com.coderdream.util.cd.CdFileUtil.getResourceRealPath() + File.separator
                            + "youtube"
                            + File.separator + "description_prompt.txt",
                    StandardCharsets.UTF_8);
            prompt += "字幕如下：";
            prompt += FileUtil.readString(
                    srtFileName,
                    StandardCharsets.UTF_8);
            // 生成文本内容（阻塞式）
            GenAi.GeneratedContent generatedContent = GeminiApiUtil.generateContent(prompt);

            // 商務英語 EP 18 餐館英語|🎧30分鐘英文聽力訓練|中英雙語配音，效果加倍|雙語沉浸式學習|英文聽力大提升，附帶中文翻譯|每日英文聽力|讓你的耳朵更靈敏|生活化英文會話|輕鬆掌握實用口語
            String title = "商務英語 EP " + shortSubFolder + " " + chapterName
                    + "商務英語 EP 18 餐館英語|\uD83C\uDFA730分鐘英文聽力訓練" +
                    "|中英雙語配音，效果加倍|雙語沉浸式學習" +
                    "|英文聽力大提升，附帶中文翻譯|每日英文聽力" +
                    "|讓你的耳朵更靈敏|生活化英文會話|輕鬆掌握實用口語";
//      String text = generatedContent.text();
//      text = title + "\n\n" + text;

            try {
//      if (CdFileUtil.isFileEmpty(chnMdFileName) || CdFileUtil.isFileEmpty(
//        chtMdFileName)) {
                String text = generatedContent.text();
                text = title + "\n\n" + text;
                FileUtils.writeStringToFile(new File(chtMdFileName),
                        ZhConverterUtil.toTraditional(text), "UTF-8");
                FileUtils.writeStringToFile(new File(chnMdFileName),
                        ZhConverterUtil.toSimple(text), "UTF-8");
//      } else {
//        log.info("md文件已存在, {}", chnMdFileName);
//      }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//      CdFileUtil.writeToFile(chtMdFileName, Collections.singletonList(
//        ZhConverterUtil.toTraditional(generatedContent.text())));
//
//      CdFileUtil.writeToFile(chnMdFileName, Collections.singletonList(
//        ZhConverterUtil.toSimple(generatedContent.text())));

            log.info("4. Generated content: {}", generatedContent);
        } else {
            log.info("md文件已存在, {}", chnMdFileName);
            String title =
                    "一輩子夠用的英語口語大全集 EP " + shortSubFolder + " " + chapterName
                            + " | \uD83C\uDFA730分鐘英文聽力訓練" +
                            "|中英雙語配音，效果加倍" +
                            "|雙語沉浸式學習" +
                            "|英文聽力大提升，附帶中文翻譯" +
                            "|每日英文聽力|讓你的耳朵更靈敏" +
                            "|生活化英文會話|輕鬆掌握實用口語";

            String text = FileUtil.readString(chnMdFileName, StandardCharsets.UTF_8);
            text = title + "\n\n" + text;
            try {
                FileUtils.writeStringToFile(new File(chtMdFileName),
                        ZhConverterUtil.toTraditional(text), "UTF-8");
                FileUtils.writeStringToFile(new File(chnMdFileName),
                        ZhConverterUtil.toSimple(text), "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("----- 4.测试 generateContent 方法结束");

        // 复制封面文件到视频文件夹
        // 生成封面图
        String coverPath = folderPath + File.separator + "cover" + File.separator;
        File coverPathFile = new File(coverPath);
        if (!coverPathFile.exists()) {
            boolean mkdir = coverPathFile.mkdirs();
            if (mkdir) {
                log.info("封面图创建目录成功，路径：{}", coverPath);
            } else {
                log.error("封面图创建目录失败，路径：{}", coverPath);
                return;
            }
        }
        String imageFormat = "png";
        String coverFileName =
                coverPath + subFolder + "_" + "720p." + imageFormat;
        String destinationCoverFileName =
                folderPath + File.separator + subFolder + File.separator + "video"
                        + File.separator
                        + subFolder + "_" + "720p." + imageFormat;
        if (CdFileUtil.isFileEmpty(coverFileName)) {
            log.error("封面图不存在，先生成： {}", coverFileName);
            return;
        }
        if (CdFileUtil.isFileEmpty(
                destinationCoverFileName)) {
            FileUtil.copy(Paths.get(coverFileName),
                    Paths.get(destinationCoverFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void process(String bookFolderName, String subFolder) {
        String chapterName = "900_cht_name2.txt";
        String headContentFileName = "head_content.txt";
        process(bookFolderName, subFolder, chapterName, headContentFileName);
    }

    public static void makeSrcRawFile(String totalFileName, String srtFileName,
                                      List<String> textList) {
        List<String> responseList = FileUtil.readLines(totalFileName,
                StandardCharsets.UTF_8);
        List<String> srtList = new ArrayList<>(textList);
        // 解析字符串为字幕对象列表
        List<SubtitleEntity> subtitleEntityList = CdFileUtil.genSubtitleEntityList(
                responseList, CdConstants.TRANSLATE_PLATFORM_GEMINI);
        for (SubtitleEntity subtitleEntity : subtitleEntityList) {
            String subtitle = subtitleEntity.getSubtitle();
            String secondSubtitle = subtitleEntity.getSecondSubtitle();
            srtList.add(subtitle);
            srtList.add(subtitle);
            srtList.add(subtitle);
            srtList.add(secondSubtitle);
            srtList.add(subtitle);
        }

        if (CdFileUtil.isFileEmpty(srtFileName)) {
            CdFileUtil.writeToFile(srtFileName, srtList);
        }
    }

    public static void process(String folderPath, String subFolder,
                               String shortSubFolder, String bookFolderName,
                               String bookName, String chapterFileName, String timeStr) {
        // TODO
        //Prox

        // 1. 生成字幕
        String lang = "cmn";

        Map<String, String> chapterNameMap = new HashMap<>();
        List<String> stringList = FileUtil.readLines(chapterFileName,
                StandardCharsets.UTF_8);
        for (String line : stringList) {
            String[] split = line.split(" ");
            chapterNameMap.put(split[1], split[2]);
        }
//    String shortSubFolder = subFolder.substring(8);
        String chapterName = chapterNameMap.get(shortSubFolder);

        String mp3FileName =
                OperatingSystem.getVideoBaseFolder() + File.separator + bookFolderName
                        + File.separator + bookName + "-EP-"
                        + shortSubFolder + "-" + chapterName
                        + File.separator + bookName + "-EP-" + shortSubFolder + "-"
                        + chapterName
                        + ".MP3";

        // "/Users/coderdream/Documents/EnBook002/一輩子夠用的英語口語大全集-EP-10-情緒 "

        File mp3File = new File(mp3FileName);
        if (!mp3File.exists() || mp3File.length() == 0) {
            log.info("mp3文件不存在, {}", mp3FileName);
            return;
        }
        String srtFileName = com.coderdream.util.cd.CdFileUtil.changeExtension(
                mp3FileName, "srt");
        // D:\0000\EnBook001\900\ch003\ch003_total.txt
        String subtitleFileName =
                OperatingSystem.getBaseFolder() + File.separator + bookFolderName
                        + File.separator
                        + subFolder + File.separator + subFolder +
                        "_total.txt";
        File totalFile = new File(subtitleFileName);
        if (!totalFile.exists() || totalFile.length() == 0) {
            log.info("subtitleFileName文件不存在, {}", subtitleFileName);
            return;
        }

        List<String> textList = new ArrayList<>(List.of(
                "Enhance your English listening with 30-minute sessions of English audio, paired with Chinese dubbing.",
                "英文加中文配音，每次半小時，增强你的英文听力。"));
        List<String> srtList = FileUtil.readLines(subtitleFileName,
                StandardCharsets.UTF_8);
        for (String srtLine : srtList) {
            textList.add(ZhConverterUtil.toTraditional(srtLine));
        }
//    textList.addAll(srtList);
        // 生成繁体字幕文件
        String newSubtitleFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(
                subtitleFileName,
                "_cht");// "D:\\0000\\EnBook001\\900\\ch003\\ch003_total_new.txt";
        com.coderdream.util.cd.CdFileUtil.writeToFile(newSubtitleFileName,
                textList);

        lang = "eng";

        if (CdFileUtil.isFileEmpty(srtFileName)) {
            log.info("srtFile 文件不存在, {}", srtFileName);
            SubtitleUtil.genSrtByExecuteCommand(mp3FileName, newSubtitleFileName,
                    srtFileName, lang);
        }

        // 2. 生成描述
        genDescriptionForYT(folderPath, subFolder, shortSubFolder, bookName,
                timeStr, srtFileName,
                chapterName);

        log.info("----- 4.测试 generateContent 方法结束");
    }

    public static void genDescriptionForYT(String folderPath, String subFolder,
                                           String shortSubFolder, String bookName,
                                           String timeStr, String srtFileName, String chapterName) {
        log.info("----- 4.测试 generateContent 方法开始");
        String prompt = FileUtil.readString(
                com.coderdream.util.cd.CdFileUtil.getResourceRealPath() + File.separator
                        + "youtube"
                        + File.separator + "description_prompt.txt",
                StandardCharsets.UTF_8);
        prompt += "字幕如下：";
        prompt += FileUtil.readString(
                srtFileName,
                StandardCharsets.UTF_8);
        // 生成文本内容（阻塞式）
        String generatedContent = GeminiApiClient.generateContent(prompt);
        // 【关键修复】使用 subFolder (文件夹名) 而不是 folderPath (完整路径) 来构建文件名
        String pptxFileName = CommonUtil.getFullPathFileName(subFolder, subFolder, ".pptx");
        String descriptionFileNameYT = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(com.coderdream.util.cd.CdFileUtil.changeExtension(pptxFileName, "md"), "_description_yt");
        String chnMdFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_chn");
        String chtMdFileName = com.coderdream.util.cd.CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_cht");
        String title = "";
        // 商務英語 EP 18 餐館英語|🎧30分鐘英文聽力訓練|中英雙語配音，效果加倍|雙語沉浸式學習|英文聽力大提升，附帶中文翻譯|每日英文聽力|讓你的耳朵更靈敏|生活化英文會話|輕鬆掌握實用口語
        if (StrUtil.isNotBlank(bookName)) {
            title = bookName + " EP " + shortSubFolder + " " + chapterName
                    + "|\uD83C\uDFA7" + timeStr
                    + "分鐘英文聽力訓練|中英雙語配音，效果加倍|雙語沉浸式學習|英文聽力大提升，附帶中文翻譯|每日英文聽力|讓你的耳朵更靈敏|生活化英文會話|輕鬆掌握實用口語";
        } else {
            title = "EP " + subFolder
                    + "六分鐘英文聽力訓練|雙語沉浸式學習|英文聽力大提升，附帶中文翻譯|每日英文聽力|讓你的耳朵更靈敏|生活化英文會話|輕鬆掌握實用口語";

        }
        try {
            if (
                    com.coderdream.util.cd.CdFileUtil.isFileEmpty(chnMdFileName)
                            || com.coderdream.util.cd.CdFileUtil.isFileEmpty(
                            chtMdFileName)
            ) {
                // 【关键修改】增加健壮性检查
                if (StrUtil.isBlank(generatedContent) || generatedContent.contains("API 调用发生异常")) {
                    log.error("通过 Gemini API 生成YouTube描述失败。返回内容: {}", generatedContent);
                    return;
                }

                String text = title + "\n\n" + generatedContent;
                FileUtils.writeStringToFile(new File(chtMdFileName),
                        ZhConverterUtil.toTraditional(text), "UTF-8");
                FileUtils.writeStringToFile(new File(chnMdFileName),
                        text, "UTF-8");
            } else {
                log.info("md文件已存在, {}", chnMdFileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("4. Generated content length: {}", (generatedContent != null) ? generatedContent.length() : 0);
    }

    public static void process(String folderPath, String subFolder,
                               String shortSubFolder, String bookFolderName,
                               String bookName, String chapterFileName) {
        String timeStr = "30";
        process(folderPath, subFolder, shortSubFolder, bookFolderName,
                bookName, chapterFileName, timeStr);
    }

    public static void copyFileToPublishFolder(String bookName,
                                               Integer chapterSize) {

        // , String subFolder,
        //    String shortSubFolder, String bookFolderName,
        //    String bookName, String chapterFileName
        String folderPath = OperatingSystem.getFolderPath(bookName);

        List<String> subFolders = new ArrayList<>();
        int end = chapterSize + 1; // 假定总共100章 101
        for (int i = 1; i < chapterSize + 1; i++) {
            String dayNumberString = String.format("%03d", i); // 格式化天数序号为3位字符串
            subFolders.add("Chapter" + dayNumberString);
        }

//    String folderPath =
//      OperatingSystem.getBaseFolder() + File.separator + typeName + File.separator + folderName;
        String distFolderName = folderPath + File.separator + "publish";
        File directory = new File(distFolderName);

        if (directory.mkdirs()) {
            System.out.println("文件夹创建成功: " + distFolderName);
        } else {
            System.err.println("文件夹创建失败: " + distFolderName);
            // 可以添加更详细的错误处理，例如检查文件夹是否已经存在
            if (directory.exists()) {
                System.err.println("文件夹已经存在: " + distFolderName);
            } else {
                System.err.println("创建文件夹时发生未知错误.");
            }
        }

//    if (!FileUtil.exist(distFolderName)) {
//      File file= new File(distFolderName);
//      boolean mkdirs = file.mkdir();
//      if (mkdirs) {
//        log.error("创建文件夹结果：{} {}", mkdirs, distFolderName);
//      }
//    }

//    File pathFile = new File(path);
//    File file = new File(path + File.separator + filename);
//    if (!pathFile.exists()) {
//
//      logger.info(" =========== 创建文件夹 " + path);
//      pathFile.mkdirs();
//      file.createNewFile();
//    }


        for (String folderName : subFolders) {
//      GenVideoUtil.processV20250317(bookName, folderPath, subFolder);
//      Mp4MergeUtil.processMerge(folderPath, subFolder);

            String destinationCoverFileName =
                    folderPath + File.separator + folderName + File.separator + "video"
                            + File.separator
                            + folderName + "_" + "720p.png";
            if (!CdFileUtil.isFileEmpty(destinationCoverFileName)) {
                FileUtil.copy(destinationCoverFileName, distFolderName, true);
            }

            // 0. 清理文件夹
//      boolean del = FileUtil.del(distFolderName);
//      log.info("删除文件夹结果：{}", del);
            // 2. 拷贝视频
            String mp4FilePath = folderPath + File.separator + folderName + File.separator
                    + CdConstants.VIDEO_FOLDER + File.separator + folderName
                    + "_new.mp4";
            String mp4FileDestinationPath = distFolderName + File.separator + folderName
                    + "_new.mp4";
            if (!CdFileUtil.isFileEmpty(mp4FilePath) && CdFileUtil.isFileEmpty(mp4FileDestinationPath)) {
                FileUtil.copy(mp4FilePath, mp4FileDestinationPath, true);
            }
            // 3. 字幕
            String subtitleFileNameEng =
                    folderPath + File.separator + folderName + File.separator
                            + CdConstants.VIDEO_FOLDER + File.separator + folderName
                            + "_zh_CN.md";
            FileUtil.copy(subtitleFileNameEng, distFolderName, true);
            String subtitleFileNameChn =
                    folderPath + File.separator + folderName + File.separator
                            + CdConstants.VIDEO_FOLDER + File.separator + folderName
                            + "_zh_TW.md";
            FileUtil.copy(subtitleFileNameChn, distFolderName, true);

            // 4. 封面
//      String coverFileName =
//        folderPath + File.separator + "cover" + File.separator + folderName  + "_720p.png";
//      FileUtil.copy(coverFileName, distFolderName, true);
        }
    }

    /**
     * 生成 B站 使用的 publish.txt 文件
     *
     * @param folderName 文件夹名称
     */
    public static void genPublishCommon(String folderName) {
        String folderPath = CommonUtil.getFullPath(folderName);
        String publishFileName = folderPath + File.separator + "publish.txt";

        // 【第一部分】：固定内容 + 动态问题
        StringBuilder part1 = new StringBuilder();
        part1.append("●宝子们，求个三连[保卫萝卜_哭哭]\n");
        part1.append("●本期文本及翻译，音频在视频简介哦[给心心]\n");
        part1.append("●小店有合集电子档下载，包括【对话音频、英文文本、中文翻译、核心词汇和高级词汇表】哦[打call]\n");
        part1.append("●本期问题: ");

        String scriptDialogFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", ".txt");
        List<String> scriptLines = FileUtil.readLines(scriptDialogFileName, StandardCharsets.UTF_8);
        String questionLineRaw = "";
        for (String line : scriptLines) {
            if (line.contains("a)") && line.contains("b)") && line.contains("c)")) {
                questionLineRaw = line;
                break;
            }
        }

        // TODO 1
        String questionFileName = ResourcesSourcePathUtil.getResourcesSourceAbsolutePath()
            + File.separator + "data" + File.separator + "bbc" + File.separator + "question.txt";
        Set<String> questionSet = new HashSet<>(FileUtil.readLines(questionFileName, StandardCharsets.UTF_8));

        String questionPart = "";
        if (StrUtil.isNotBlank(questionLineRaw)) {
            int questionStartIndex = -1;
            // TODO 2
            for (String question : questionSet) {
                if (questionLineRaw.contains(question)) {
                    questionStartIndex = questionLineRaw.lastIndexOf(question);
                    break;
                }
            }

            if (questionStartIndex != -1) {
                String rawQuestionWithOptions = questionLineRaw.substring(questionStartIndex);
                // 分割问题和选项
                int optionAIndex = rawQuestionWithOptions.indexOf("a)");
                String questionText = rawQuestionWithOptions.substring(0, optionAIndex).trim();
                String optionsText = rawQuestionWithOptions.substring(optionAIndex);

                // 格式化输出，将选项分割成多行
                questionPart = questionText + "\n" +
                    optionsText.replace(" b)", "\nb)").replace(" c)", "\nc)");
                part1.append(questionPart);
            } else {
                log.error("没有找到问题所在的行！");
                return;
            }
        }

        // 查找问题时间戳
        String srtFileName = CommonUtil.getFullPathFileName(folderName, "eng", ".srt");
        String questionTimestamp = "00:00"; // 默认值
        if (FileUtil.exist(srtFileName)) {
            List<SubtitleEntity> subtitles = CdFileUtil.readSrtFileContent(srtFileName);
            for (SubtitleEntity subtitle : subtitles) {
                if (subtitle.getSubtitle().contains("a)")) {
                    // 提取开始时间戳，并格式化为 mm:ss
                    String startTime = subtitle.getTimeStr().split(" --> ")[0];
                    questionTimestamp = startTime.substring(3, 8); // 从 "00:01:44,480" 提取 "01:44"
                    break;
                }
            }
        }

        // 【第二部分】：翻译问题
        StringBuilder part2 = new StringBuilder();
        part2.append("●本期问题时间戳：").append(questionTimestamp).append("\n");

        String translatedQuestion = "";
        if (StrUtil.isNotBlank(questionPart)) {
            String prompt = "请将以下英文问题和选项翻译成中文。要求：\n" +
                "1. 将问题意译为一个自然流畅、通俗易懂的简短中文问题，总长度不超过12个汉字（含标点）。\n" +
                "2. 将每个选项翻译成不超过6个汉字的中文（不包含选项字母）。\n" +
                "3. 保持原有的 a) b) c) 格式，但使用中文全角括号，例如 a）b）c）。\n" +
                "4. 每个选项占一行。\n" +
                "5. 【重要】问题和第一个选项之间不要有任何空行。\n" +
                "需要翻译的内容如下：\n" +
                "\n" +
                questionPart + "\n" +
                "";
            // 增加重试机制
            int maxRetries = 5;
            long retryDelayMillis = 3000; // 2秒

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                translatedQuestion = GeminiApiClient.generateContent(prompt);
                if (StrUtil.isNotBlank(translatedQuestion)) {
                    log.info("Gemini API 翻译成功 (尝试次数: {})", attempt);
                    break; // 成功，跳出循环
                }

                log.warn("调用 Gemini API 翻译问题失败 (尝试 {}/{})，返回内容为空。", attempt, maxRetries);

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("重试等待时发生中断异常", e);
                    }
                } else {
                    log.error("调用 Gemini API 翻译问题失败，已达最大重试次数。Prompt: {}", prompt);
                    throw new RuntimeException("调用 Gemini API 翻译问题失败，已达最大重试次数。");
                }
            }
            part2.append(translatedQuestion);
        }

        // 【第三部分】：广告信息
        Map<String, String> adMap = new LinkedHashMap<>();
        adMap.put("2025年52期全套资料（更新中）\nhttps://b23.tv/KTDB9bQ", "2025");
        adMap.put("2024年52期全套资料\nhttps://b23.tv/rMZCkcd", "2024");
        adMap.put("2023年52期全套资料\nhttps://b23.tv/CMl2coN", "2023");
        adMap.put("2022年52期全套资料\nhttps://b23.tv/EEiF0hK", "2022");
        adMap.put("2021年52期全套资料\nhttps://b23.tv/VuIgRyj", "2021");
        adMap.put("2020年52期全套资料\nhttps://b23.tv/y5CM65f", "2020");
        adMap.put("2019年52期全套资料\nhttps://b23.tv/BSRhboW", "2019");
        adMap.put("2018年52期全套资料\nhttps://b23.tv/vWzWzvA", "2018");
        adMap.put("2017年52期全套资料\nhttps://b23.tv/y5CM65f", "2017");

        List<String> adList = new ArrayList<>(adMap.keySet());

        // 如果 folderName 不是以 '2' 开头，则说明是历史的，将列表反转
        if (!folderName.startsWith("2")) {
            Collections.reverse(adList); // 反转，变为升序
        }

        StringBuilder part3 = new StringBuilder();
        part3.append("小店资料每期包含【音频、英文pdf、双语对话、核心词汇、高级词汇表和完整词汇表】6份文档，全年共计300+文档！\n");
        part3.append(String.join("\n", adList));

        // 【第四部分】：打卡信息
        StringBuilder part4 = new StringBuilder();
        part4.append("宝子期期都来打卡\n");
        part4.append("请你给视频打个分\n");
        part4.append("宝子打卡超过99次\n");
        part4.append("打卡199次并不难");

        // 合并所有部分并写入文件
        StringBuilder finalContent = new StringBuilder();
        finalContent.append("【第一部分】\n");
        finalContent.append(part1.toString()).append("\n\n");
        finalContent.append("【第二部分】\n");
        finalContent.append(part2.toString()).append("\n\n");
        finalContent.append("【第三部分】\n");
        finalContent.append(part3.toString()).append("\n\n");
        finalContent.append("【第四部分】\n");
        finalContent.append(part4.toString());

        FileUtil.writeString(finalContent.toString(), publishFileName, StandardCharsets.UTF_8);
        log.info("成功生成 B站 publish.txt 文件: {}", publishFileName);
    }
}
