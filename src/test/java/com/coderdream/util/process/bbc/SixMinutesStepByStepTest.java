package com.coderdream.util.process.bbc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.SubtitleEntity;
import com.coderdream.util.CommonUtil;
import com.coderdream.util.DictUtil;
import com.coderdream.util.bbc.GenSrtUtil;
import com.coderdream.util.bbc.ProcessScriptUtil;
import com.coderdream.util.bbc.WordCountUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.CdMP3SplitterUtil;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.TextProcessor;
import com.coderdream.util.gemini.TranslationUtil;
import com.coderdream.util.ppt.GetSixMinutesPpt;
import com.coderdream.util.ppt.PptToImageConverter;
import com.coderdream.util.process.PreparePublishUtil;
import com.coderdream.util.subtitle.SubtitleUtil;
import com.coderdream.util.translate.TranslateUtil;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static cn.hutool.core.io.FileUtil.readString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 6 Minute English 全流程端到端集成测试 (参数化数据驱动版)
 * <p>
 * 本测试类从 todo.txt 文件中读取要处理的文件夹列表，
 * 并为每个文件夹自动执行一遍完整的18步测试流程。
 *
 * @author Gemini Code Assist
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class SixMinutesStepByStepTest {

    /**
     * 为参数化测试提供数据源。
     * 此方法会读取todo.txt文件，并将其中的每一行（文件夹名称）作为一个测试用例的参数。
     *
     * @return 包含所有文件夹名称的Stream。
     */
    private static Stream<String> folderNameProvider() {
        log.info("========【参数化测试数据源初始化：读取todo.txt】========");
        String todoFilePath = CdFileUtil.getResourceRealPath() + File.separatorChar + "data" + File.separatorChar + "bbc" + File.separatorChar + "todo.txt";
        List<String> folderNames = FileUtil.readLines(todoFilePath, "UTF-8");
        return folderNames.stream()
            .filter(StrUtil::isNotBlank) // 过滤掉空行
            .map(String::trim);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(1)
    @DisplayName("01. 预处理原始脚本")
    void test01_ProcessRawScript(String folderName) {
        log.info("\n========【01. 预处理原始脚本 | {}】========", folderName);
        String srcScriptFileName = CommonUtil.getFullPathFileName(folderName, folderName + "_script", ".txt");
        ProcessScriptUtil.processScriptTxt(srcScriptFileName);
        assertFalse(StrUtil.isBlank(readString(srcScriptFileName, StandardCharsets.UTF_8)), "预处理后的原始脚本文件内容为空白");
        log.info("成功预处理原始脚本: {}", srcScriptFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(2)
    @DisplayName("02. 生成对话脚本")
    void test02_GenerateDialogScript(String folderName) {
        log.info("\n========【02. 生成对话脚本 | {}】========", folderName);
        String scriptDialogFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", ".txt");
        if (FileUtil.exist(scriptDialogFileName) && !StrUtil.isBlank(readString(scriptDialogFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogFileName);
            return;
        }
        File generatedFile = ProcessScriptUtil.genScriptDialogTxt(folderName, scriptDialogFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的对话脚本 script_dialog.txt 内容为空白");
        log.info("成功生成对话脚本: {}", scriptDialogFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(3)
    @DisplayName("03. 生成词汇脚本")
    void test03_GenerateVocabularyScript(String folderName) {
        log.info("\n========【03. 生成词汇脚本 | {}】========", folderName);
        String vocFileName = CommonUtil.getFullPathFileName(folderName, "voc", ".txt");
        if (FileUtil.exist(vocFileName) && !StrUtil.isBlank(readString(vocFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", vocFileName);
            return;
        }
        File generatedFile = ProcessScriptUtil.genVocTxt(folderName, vocFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的词汇脚本 voc.txt 内容为空白");
        log.info("成功生成词汇脚本: {}", vocFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(4)
    @DisplayName("04. 翻译对话脚本")
    void test04_TranslateDialogScript(String folderName) {
        log.info("\n========【04. 翻译对话脚本 | {}】========", folderName);
        String scriptDialogCnFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", "_cn.txt");
        if (FileUtil.exist(scriptDialogCnFileName) && !StrUtil.isBlank(readString(scriptDialogCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogCnFileName);
            return;
        }

        File generatedFile = null;
        boolean success = false;
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            log.info("正在进行第 {}/{} 次翻译尝试...", i + 1, maxRetries);
            generatedFile = TranslateUtil.genScriptDialogCn(folderName, scriptDialogCnFileName);
            if (generatedFile != null && FileUtil.exist(generatedFile) && !StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8))) {
                success = true;
                break;
            }
            log.warn("第 {} 次翻译失败，生成的文件为空或不存在。将在2秒后重试...", i + 1);
            try {
                Thread.sleep(2000); // 等待2秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertTrue(success, "翻译后的对话脚本 script_dialog_cn.txt 内容为空白，已重试 " + maxRetries + " 次");
        log.info("成功翻译对话脚本: {}", scriptDialogCnFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(5)
    @DisplayName("05. 翻译词汇脚本")
    void test05_TranslateVocabularyScript(String folderName) {
        log.info("\n========【05. 翻译词汇脚本 | {}】========", folderName);
        String vocFileName = CommonUtil.getFullPathFileName(folderName, "voc", ".txt");
        String vocCnFileName = CommonUtil.getFullPathFileName(folderName, "voc_cn", ".txt");
        if (FileUtil.exist(vocCnFileName) && !StrUtil.isBlank(readString(vocCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", vocCnFileName);
            return;
        }
        File generatedFile = DictUtil.genVocCnWithGemini(vocFileName, vocCnFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "翻译后的词汇脚本 voc_cn.txt 内容为空白");
        log.info("成功翻译词汇脚本: {}", vocCnFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(6)
    @DisplayName("06. 生成SRT专用脚本")
    void test06_GenerateSrtReadyScript(String folderName) {
        log.info("\n========【06. 生成SRT专用脚本 | {}】========", folderName);
        String scriptDialogNewFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", "_new.txt");
        if (FileUtil.exist(scriptDialogNewFileName) && !StrUtil.isBlank(readString(scriptDialogNewFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogNewFileName);
            return;
        }
        File generatedFile = GenSrtUtil.genScriptDialogNew(folderName, scriptDialogNewFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "SRT专用脚本 script_dialog_new.txt 内容为空白");
        log.info("成功生成SRT专用脚本: {}", scriptDialogNewFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(7)
    @DisplayName("07. 合并为中英双语脚本")
    void test07_MergeBilingualScript(String folderName) {
        log.info("\n========【07. 合并为中英双语脚本 | {}】========", folderName);
        String scriptDialogFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", ".txt");
        String scriptDialogCnFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", "_cn.txt");
        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(folderName, folderName, "_中英双语对话脚本.txt");

        assertTrue(FileUtil.exist(scriptDialogFileName), "前置条件失败: 对话脚本文件不存在");
        assertTrue(FileUtil.exist(scriptDialogCnFileName), "前置条件失败: 中文对话脚本文件不存在");

        if (FileUtil.exist(scriptDialogMergeFileName) && !StrUtil.isBlank(readString(scriptDialogMergeFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogMergeFileName);
            return;
        }
        File generatedFile = TranslateUtil.mergeScriptContent(scriptDialogFileName, scriptDialogCnFileName, scriptDialogMergeFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "合并后的中英双语对话脚本.txt 内容为空白");
        log.info("成功合并为中英双语脚本: {}", scriptDialogMergeFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(8)
    @DisplayName("08. 生成原始SRT字幕")
    void test08_GenerateRawSrt(String folderName) {
        log.info("\n========【08. 生成原始SRT字幕 | {}】========", folderName);
        String folderPath = CommonUtil.getFullPath(folderName);
        String mp3FileName = FileUtil.listFileNames(folderPath).stream()
            .filter(name -> name.startsWith(folderName) && name.endsWith(CdConstants.MP3_EXTENSION))
            .map(name -> name.substring(0, name.length() - 4))
            .findFirst()
            .orElse(null);

        assertNotNull(mp3FileName, "测试失败：在文件夹 " + folderName + " 中未找到对应的MP3文件");
        String mp3FileNameFull = CommonUtil.getFullPathFileName(folderName, mp3FileName, CdConstants.MP3_EXTENSION);
        log.info("找到原始音频文件: {}", mp3FileNameFull);

        String srtScriptFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog_new", CdConstants.TXT_EXTENSION);
        String srtEngRawFileName = CommonUtil.getFullPathFileName(folderName, "eng_raw", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(srtEngRawFileName) && !StrUtil.isBlank(readString(srtEngRawFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", srtEngRawFileName);
            return;
        }

        SubtitleUtil.genSrtByExecuteCommand(mp3FileNameFull, srtScriptFileName, srtEngRawFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(srtEngRawFileName, StandardCharsets.UTF_8)), "生成的原始SRT字幕 eng_raw.srt 内容为空白");
        log.info("成功生成原始SRT字幕: {}", srtEngRawFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(9)
    @DisplayName("09. 提取时间戳并切割音频")
    void test09_ProcessTimestampsAndSplitAudio(String folderName) {
        log.info("\n========【09. 提取时间戳并切割音频 | {}】========", folderName);
        String srtEngRawFileName = CommonUtil.getFullPathFileName(folderName, "eng_raw", CdConstants.SRT_EXTENSION);
        String mp3FileName = FileUtil.listFileNames(CommonUtil.getFullPath(folderName)).stream()
            .filter(name -> name.startsWith(folderName) && name.endsWith(CdConstants.MP3_EXTENSION))
            .findFirst().orElse(null);
        assertNotNull(mp3FileName, "前置条件失败: 原始MP3文件未找到");
        String mp3FileNameFull = CommonUtil.getFullPathFileName(folderName, mp3FileName.replace(".mp3", ""), ".mp3");

        assertTrue(FileUtil.exist(srtEngRawFileName), "前置条件失败: 原始SRT文件 'eng_raw.srt' 未生成");

        String result = TextProcessor.processFile(srtEngRawFileName);
        assertNotNull(result, "从eng_raw.srt中未能提取到起止时间");
        String[] split = result.split("\\s+");
        assertTrue(split.length >= 3, "提取的起止时间格式不正确，应包含文件名、开始时间、结束时间");
        String startTime = split[1];
        log.info("提取到对话开始时间: {}", startTime);

        String mp3FileNameFullNew = CommonUtil.getFullPathFileName(folderName, "audio5", CdConstants.MP3_EXTENSION);
        if (FileUtil.exist(mp3FileNameFullNew) && FileUtil.size(new File(mp3FileNameFullNew)) > 0) {
            log.info("文件已存在，跳过生成: {}", mp3FileNameFullNew);
            return;
        }

        CdMP3SplitterUtil.splitMP3(mp3FileNameFull, mp3FileNameFullNew, startTime, split[2]);
        assertTrue(FileUtil.exist(mp3FileNameFullNew) && FileUtil.size(new File(mp3FileNameFullNew)) > 0, "切割后的音频 audio5.mp3 生成失败或文件大小为0");
        log.info("成功切割音频: {}", mp3FileNameFullNew);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(10)
    @DisplayName("10. 生成用于最终SRT的脚本")
    void test10_GenerateFinalScriptForSrt(String folderName) {
        log.info("\n========【10. 生成用于最终SRT的脚本 | {}】========", folderName);
        String srtEngRawFileName = CommonUtil.getFullPathFileName(folderName, "eng_raw", CdConstants.SRT_EXTENSION);
        assertTrue(FileUtil.exist(srtEngRawFileName), "前置条件失败: 原始SRT文件 'eng_raw.srt' 未生成");

        String result = TextProcessor.processFile(srtEngRawFileName);
        assertNotNull(result, "前置条件失败: 无法从eng_raw.srt中提取到起止时间");
        String startTime = result.split("\\s+")[1];

        String scriptDialogNew2FileName = CommonUtil.getFullPathFileName(folderName, "script_dialog_new2", CdConstants.TXT_EXTENSION);
        if (FileUtil.exist(scriptDialogNew2FileName) && !StrUtil.isBlank(readString(scriptDialogNew2FileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogNew2FileName);
            return;
        }

        List<String> lines = new ArrayList<>();
        List<SubtitleEntity> subtitleEntityList = CdFileUtil.readSrtFileContent(srtEngRawFileName);
        if (CollectionUtil.isNotEmpty(subtitleEntityList)) {
            boolean isSubtitle = false;
            for (SubtitleEntity subtitleEntity : subtitleEntityList) {
                if (subtitleEntity.getTimeStr().startsWith(startTime)) {
                    isSubtitle = true;
                }
                if (isSubtitle) {
                    lines.add(subtitleEntity.getSubtitle());
                }
            }
        }
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        assertFalse(lines.isEmpty(), "从 eng_raw.srt 提取字幕内容失败");
        File generatedFile = FileUtil.writeLines(lines, scriptDialogNew2FileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "最终SRT脚本 script_dialog_new2.txt 内容为空白");
        log.info("成功生成最终SRT脚本: {}", scriptDialogNew2FileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(11)
    @DisplayName("11. 生成最终英文字幕(eng.srt)")
    void test11_GenerateFinalEnglishSrt(String folderName) {
        log.info("\n========【11. 生成最终英文字幕(eng.srt) | {}】========", folderName);
        String mp3FileNameFullNew = CommonUtil.getFullPathFileName(folderName, "audio5", CdConstants.MP3_EXTENSION);
        String scriptDialogNew2FileName = CommonUtil.getFullPathFileName(folderName, "script_dialog_new2", CdConstants.TXT_EXTENSION);
        assertTrue(FileUtil.exist(mp3FileNameFullNew), "前置条件失败：切割后的音频文件不存在");
        assertTrue(FileUtil.exist(scriptDialogNew2FileName), "前置条件失败：用于生成最终字幕的脚本不存在");

        String srtFileName = CommonUtil.getFullPathFileName(folderName, "eng", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(srtFileName) && !StrUtil.isBlank(readString(srtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", srtFileName);
            return;
        }
        SubtitleUtil.genSrtByExecuteCommand(mp3FileNameFullNew, scriptDialogNew2FileName, srtFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(srtFileName, StandardCharsets.UTF_8)), "最终英文字幕 eng.srt 内容为空白");
        log.info("成功生成最终英文字幕: {}", srtFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(12)
    @DisplayName("12. 翻译为中文字幕(chn.srt)")
    void test12_TranslateToChineseSrt(String folderName) {
        log.info("\n========【12. 翻译为中文字幕(chn.srt) | {}】========", folderName);
        String srtFileName = CommonUtil.getFullPathFileName(folderName, "eng", CdConstants.SRT_EXTENSION);
        assertTrue(FileUtil.exist(srtFileName), "前置条件失败: 英文SRT文件 'eng.srt' 未生成");

        String chnSrtFileName = CommonUtil.getFullPathFileName(folderName, "chn", ".srt");
        if (FileUtil.exist(chnSrtFileName) && !StrUtil.isBlank(readString(chnSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", chnSrtFileName);
            return;
        }

        boolean translationSuccess = TranslateUtil.translateEngSrc(folderName);
        assertTrue(translationSuccess, "翻译流程最终失败，请检查 TranslateUtil 中的错误日志。");
        log.info("成功生成最终中文字幕: {}", chnSrtFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(13)
    @DisplayName("13. 合并为双语字幕(bilingual.srt)")
    void test13_MergeToBilingualSrt(String folderName) {
        log.info("\n========【13. 合并为双语字幕(bilingual.srt) | {}】========", folderName);
        String srtFileName = CommonUtil.getFullPathFileName(folderName, "eng", CdConstants.SRT_EXTENSION);
        String chnSrtFileName = CommonUtil.getFullPathFileName(folderName, "chn", ".srt");
        assertTrue(FileUtil.exist(srtFileName), "前置条件失败: 英文SRT文件 'eng.srt' 未生成");
        assertTrue(FileUtil.exist(chnSrtFileName), "前置条件失败: 中文SRT文件 'chn.srt' 未生成");

        String bilingualSrtFileName = CommonUtil.getFullPathFileName(folderName, "bilingual", ".srt");
        if (FileUtil.exist(bilingualSrtFileName) && !StrUtil.isBlank(readString(bilingualSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", bilingualSrtFileName);
            return;
        }

        List<SubtitleEntity> engSubtitles = CdFileUtil.readSrtFileContent(srtFileName);
        Map<Integer, SubtitleEntity> engSubtitleMap = engSubtitles.stream()
            .collect(Collectors.toMap(SubtitleEntity::getSubIndex, sub -> sub));

        List<SubtitleEntity> chnSubtitles = CdFileUtil.readSrtFileContent(chnSrtFileName);
        Map<Integer, SubtitleEntity> chnSubtitleMap = chnSubtitles.stream()
            .collect(Collectors.toMap(SubtitleEntity::getSubIndex, sub -> sub));

        int maxSubIndex = Math.max(engSubtitles.size(), chnSubtitles.size());
        List<SubtitleEntity> bilingualSubtitles = new ArrayList<>();
        for (int i = 0; i < maxSubIndex; i++) {
            SubtitleEntity engSubtitle = engSubtitleMap.get(i + 1);
            SubtitleEntity chnSubtitle = chnSubtitleMap.get(i + 1);
            if (engSubtitle == null && chnSubtitle == null) {
                continue;
            }
            if (engSubtitle == null) {
                bilingualSubtitles.add(new SubtitleEntity(i + 1, chnSubtitle.getTimeStr(), "", chnSubtitle.getSubtitle()));
            }
            if (chnSubtitle == null) {
                bilingualSubtitles.add(new SubtitleEntity(i + 1, engSubtitle.getTimeStr(), engSubtitle.getSubtitle(), ""));
            }
            if (engSubtitle != null && chnSubtitle != null) {
                bilingualSubtitles.add(new SubtitleEntity(i + 1, engSubtitle.getTimeStr(), engSubtitle.getSubtitle(), chnSubtitle.getSubtitle()));
            }
        }

        // 增加健壮性：过滤掉字幕内容为空的无效条目，防止因解析差异导致数量不匹配
//        engSubtitles = engSubtitles.stream()
//            .filter(sub -> StrUtil.isNotBlank(sub.getSubtitle())).toList();
//        chnSubtitles = chnSubtitles.stream()
//            .filter(sub -> StrUtil.isNotBlank(sub.getSubtitle())).toList();

//        assertEquals(engSubtitles.size(), chnSubtitles.size(), "中英文字幕数量不一致，无法合并");
//        assertFalse(engSubtitles.isEmpty(), "过滤后，读取到的英文字幕列表为空");

        List<String> bilingualLines = new ArrayList<>();
        for (SubtitleEntity bilingualSubtitle : bilingualSubtitles) {
            bilingualLines.add(String.valueOf(bilingualSubtitle.getSubIndex()));
            bilingualLines.add(bilingualSubtitle.getTimeStr());
            bilingualLines.add(bilingualSubtitle.getSubtitle());
            bilingualLines.add(bilingualSubtitle.getSubtitle());
            bilingualLines.add(""); // Blank line
        }

        File generatedFile = FileUtil.writeLines(bilingualLines, bilingualSrtFileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的双语字幕文件 bilingual.srt 内容为空白");
        log.info("成功生成双语字幕文件: {}", bilingualSrtFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(14)
    @DisplayName("14. 生成多维度词汇表")
    void test14_GenerateVocabularyTables(String folderName) {
        log.info("\n========【14. 生成多维度词汇表 | {}】========", folderName);
        String fullVocFileName = CommonUtil.getFullPathFileName(folderName, folderName, "_完整词汇表.xlsx");
        if (FileUtil.exist(fullVocFileName) && FileUtil.size(new File(fullVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", fullVocFileName);
        } else {
            File generatedFullVoc = WordCountUtil.genVocTable(folderName);
            assertTrue(FileUtil.exist(generatedFullVoc) && FileUtil.size(generatedFullVoc) > 0, "完整词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成完整词汇表: {}", fullVocFileName);
        }

        String excelCoreVocFileName = CommonUtil.getFullPathFileName(folderName, folderName, "_核心词汇表.xlsx");
        if (FileUtil.exist(excelCoreVocFileName) && FileUtil.size(new File(excelCoreVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelCoreVocFileName);
        } else {
            File generatedCoreVoc = CoreWordUtil.genCoreWordTable(folderName);
            assertTrue(FileUtil.exist(generatedCoreVoc) && FileUtil.size(generatedCoreVoc) > 0, "核心词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成核心词汇表: {}", excelCoreVocFileName);
        }

        String excelAdvancedFileName = CommonUtil.getFullPathFileName(folderName, folderName, "_高级词汇表.xlsx");
        if (FileUtil.exist(excelAdvancedFileName) && FileUtil.size(new File(excelAdvancedFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelAdvancedFileName);
        } else {
            File generatedAdvancedVoc = AdvancedWordUtil.genAdvancedWordTable(folderName, CdConstants.TEMPLATE_FLAG);
            assertTrue(FileUtil.exist(generatedAdvancedVoc) && FileUtil.size(generatedAdvancedVoc) > 0, "高级词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成高级词汇表: {}", excelAdvancedFileName);
        }
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(15)
    @DisplayName("15. 生成教学PPT")
    void test15_GeneratePpt(String folderName) {
        log.info("\n========【15. 生成教学PPT | {}】========", folderName);

        String chapterName = GetSixMinutesPpt.queryChapterNameForSixMinutes(folderName);
        assertFalse(StrUtil.isBlank(chapterName), "未能查询到章节名称");
        log.info("查询到章节名称: {}", chapterName);

        String pptxFileName = CommonUtil.getFullPathFileName(folderName, folderName, ".pptx");
        if (FileUtil.exist(pptxFileName) && FileUtil.size(new File(pptxFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", pptxFileName);
            return;
        }

        File generatedPpt = GetSixMinutesPpt.process(folderName, chapterName);
        assertTrue(FileUtil.exist(generatedPpt) && FileUtil.size(generatedPpt) > 0, "PPTX 文件生成失败或文件大小为0");
        log.info("成功生成教学PPT: {}", pptxFileName);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(16)
    @DisplayName("16. 转换PPT为图片")
    void test16_ConvertPptToImages(String folderName) {
        log.info("\n========【16. 转换PPT为图片 | {}】========", folderName);
        String pptxFileName = CommonUtil.getFullPathFileName(folderName, folderName, ".pptx");
        assertTrue(FileUtil.exist(pptxFileName), "前置条件失败: PPTX文件未生成");
        String pptPicDir = new File(pptxFileName).getParent() + File.separator + folderName + File.separator;
        if (FileUtil.exist(pptPicDir) && FileUtil.isNotEmpty(new File(pptPicDir))) {
            log.info("文件夹已存在且不为空，跳过生成: {}", pptPicDir);
            return;
        }
        PptToImageConverter.convertPptToImages(pptxFileName, pptPicDir, "snapshot");
        assertTrue(FileUtil.isNotEmpty(new File(pptPicDir)), "PPT 图片文件夹生成失败或为空");
        log.info("成功转换PPT为图片，输出目录: {}", pptPicDir);
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(17)
    @DisplayName("17. 生成平台描述文件")
    void test17_GenerateDescriptionFiles(String folderName) {
        log.info("\n========【17. 生成平台描述文件 | {}】========", folderName);
        String pptxFileName = CommonUtil.getFullPathFileName(folderName, folderName, ".pptx");
        assertTrue(FileUtil.exist(pptxFileName), "前置条件失败: PPTX文件未生成");

        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(folderName, folderName, "_中英双语对话脚本.txt");
        String descriptionFileName = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description");
        if (FileUtil.exist(descriptionFileName) && !StrUtil.isBlank(readString(descriptionFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", descriptionFileName);
        } else {
            File generatedDesc = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileName);
            assertFalse(StrUtil.isBlank(readString(generatedDesc, StandardCharsets.UTF_8)), "国内平台描述文件内容为空白");
            log.info("成功生成国内平台描述文件: {}", descriptionFileName);
        }

        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description_yt");
        if (FileUtil.exist(descriptionFileNameYT) && !StrUtil.isBlank(readString(descriptionFileNameYT, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", descriptionFileNameYT);
        } else {
            File generatedDescYt = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileNameYT);
            assertFalse(StrUtil.isBlank(readString(generatedDescYt, StandardCharsets.UTF_8)), "YouTube平台英文描述文件内容为空白");
            log.info("成功生成YouTube平台英文描述文件: {}", descriptionFileNameYT);
        }
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(18)
    @DisplayName("18. 生成YouTube专用描述")
    void test18_GenerateFinalYoutubeDescriptions(String folderName) {
        log.info("\n========【18. 生成YouTube专用描述 | {}】========", folderName);
        String srtFileName = CommonUtil.getFullPathFileName(folderName, "eng", CdConstants.SRT_EXTENSION);
        String chapterName = GetSixMinutesPpt.queryChapterNameForSixMinutes(folderName);
        String pptxFileName = CommonUtil.getFullPathFileName(folderName, folderName, ".pptx");
        String folderPath = CommonUtil.getFullPath(folderName);

        assertTrue(FileUtil.exist(srtFileName), "前置条件失败：最终英文字幕文件不存在");
        assertFalse(StrUtil.isBlank(chapterName), "前置条件失败：章节名称为空");

        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description_yt");
        String chnMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_chn");
        String chtMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_cht");
        if (FileUtil.exist(chnMdFileName) && !StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)) &&
            FileUtil.exist(chtMdFileName) && !StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8))) {
            log.info("YouTube中繁体描述文件已存在，跳过生成。");
            return;
        }
        PreparePublishUtil.genDescriptionForYT(folderPath, folderName, "", "", "6", srtFileName, chapterName);
        assertFalse(StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)), "YouTube平台简体中文描述文件内容为空白");
        assertFalse(StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8)), "YouTube平台繁体中文描述文件内容为空白");
        log.info("成功生成YouTube平台中/繁体描述文件");
    }

    @ParameterizedTest
    @MethodSource("folderNameProvider")
    @Order(19)
    @DisplayName("19. 生成B站发布文件(publish.txt)")
    void test19_GeneratePublishFile(String folderName) {
        log.info("\n========【19. 生成B站发布文件(publish.txt) | {}】========", folderName);
        String publishFileName = CommonUtil.getFullPathFileName(folderName, "publish", ".txt");
        if (FileUtil.exist(publishFileName) && !StrUtil.isBlank(readString(publishFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", publishFileName);
            return;
        }

        String scriptDialogFileName = CommonUtil.getFullPathFileName(folderName, "script_dialog", ".txt");
        assertTrue(FileUtil.exist(scriptDialogFileName), "前置条件失败: 对话脚本文件 'script_dialog.txt' 未生成");

        PreparePublishUtil.genPublishCommon(folderName);

        // 重新获取文件名用于断言
        assertTrue(FileUtil.exist(publishFileName), "B站发布文件 publish.txt 生成失败");
        assertFalse(StrUtil.isBlank(readString(publishFileName, StandardCharsets.UTF_8)), "生成的B站发布文件 publish.txt 内容为空白");
        log.info("成功生成B站发布文件: {}", publishFileName);
    }
}
