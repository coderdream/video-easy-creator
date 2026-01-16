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
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.CdMP3SplitterUtil;
import com.coderdream.util.cd.TextProcessor;
import com.coderdream.util.gemini.TranslationUtil;
import com.coderdream.util.ppt.GetSixMinutesPpt;
import com.coderdream.util.ppt.PptToImageConverter;
import com.coderdream.util.process.PreparePublishUtil;
import com.coderdream.util.subtitle.SubtitleUtil;
import com.coderdream.util.translate.TranslateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.io.FileUtil.readString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 6 Minute English 全流程端到端集成测试 (数据驱动版)
 * <p>
 * 本测试类从 todo.txt 文件中读取要处理的文件夹列表，
 * 并为每个文件夹动态生成一个包含完整处理步骤的测试容器。
 *
 * @author Gemini Code Assist
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class SixMinutesStepByStepOneTest {

    // 将从todo.txt读取的第一个文件夹作为测试目标
    private static String FOLDER_NAME;

    // 用于在测试方法间传递状态的静态变量
    private static String folderPath;
    private static String mp3FileName;
    private static String mp3FileNameFull;
    private static String mp3FileNameFullNew;
    private static String srtEngRawFileName;
    private static String scriptDialogFileName;
    private static String scriptDialogCnFileName;
    private static String scriptDialogNew2FileName;
    private static String chnSrtFileName;
    private static String bilingualSrtFileName;
    private static String srtFileName;
    private static String pptxFileName;
    private static String chapterName;
    private static String startTime;

    @BeforeAll
    static void setup() {
        log.info("========【测试套件初始化】========");
        String todoFilePath = CdFileUtil.getResourceRealPath() + File.separatorChar + "data" + File.separatorChar + "bbc" + File.separatorChar + "todo.txt";
        List<String> folderNames = FileUtil.readLines(todoFilePath, "UTF-8");

        // 寻找第一个非空行作为测试目标
        FOLDER_NAME = folderNames.stream()
            .filter(StrUtil::isNotBlank)
            .findFirst()
            .orElse(null);

        assertNotNull(FOLDER_NAME, "初始化失败: todo.txt 文件中没有找到任何有效的文件夹名称。");
        FOLDER_NAME = FOLDER_NAME.trim();
        folderPath = CommonUtil.getFullPath(FOLDER_NAME);

        log.info("本次测试目标文件夹: {}", FOLDER_NAME);
        log.info("文件夹完整路径: {}", folderPath);
    }

    @Test
    @Order(1)
    @DisplayName("01. 预处理原始脚本")
    void test01_ProcessRawScript() {
        log.info("\n========【01. 预处理原始脚本 | {}】========", FOLDER_NAME);
        String srcScriptFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME + "_script", ".txt");
        // 此步骤是原地修改文件，为保证流程完整性，每次都执行。
        // 如果需要跳过，可以考虑引入一个更复杂的完成状态标记。
        ProcessScriptUtil.processScriptTxt(srcScriptFileName);
        assertFalse(StrUtil.isBlank(readString(srcScriptFileName, StandardCharsets.UTF_8)), "预处理后的原始脚本文件内容为空白");
        log.info("成功预处理原始脚本: {}", srcScriptFileName);
    }

    @Test
    @Order(2)
    @DisplayName("02. 生成对话脚本")
    void test02_GenerateDialogScript() {
        log.info("\n========【02. 生成对话脚本 | {}】========", FOLDER_NAME);
        scriptDialogFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog", ".txt");
        if (FileUtil.exist(scriptDialogFileName) && !StrUtil.isBlank(readString(scriptDialogFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", scriptDialogFileName);
            // 【跳过逻辑优化】即使跳过，也要为后续步骤设置好变量
            if (scriptDialogCnFileName == null) {
                scriptDialogCnFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog", "_cn.txt");
            }
            return;
        }
        File generatedFile = ProcessScriptUtil.genScriptDialogTxt(FOLDER_NAME, scriptDialogFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的对话脚本 script_dialog.txt 内容为空白");
        log.info("成功生成对话脚本: {}", scriptDialogFileName);
    }

    @Test
    @Order(3)
    @DisplayName("03. 生成词汇脚本")
    void test03_GenerateVocabularyScript() {
        log.info("\n========【03. 生成词汇脚本 | {}】========", FOLDER_NAME);
        String vocFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "voc", ".txt");
        if (FileUtil.exist(vocFileName) && !StrUtil.isBlank(readString(vocFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", vocFileName);
            return;
        }
        File generatedFile = ProcessScriptUtil.genVocTxt(FOLDER_NAME, vocFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的词汇脚本 voc.txt 内容为空白");
        log.info("成功生成词汇脚本: {}", vocFileName);
    }

    @Test
    @Order(4)
    @DisplayName("04. 翻译对话脚本")
    void test04_TranslateDialogScript() {
        log.info("\n========【04. 翻译对话脚本 | {}】========", FOLDER_NAME);
        scriptDialogCnFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog", "_cn.txt");
        if (FileUtil.exist(scriptDialogCnFileName) && !StrUtil.isBlank(readString(scriptDialogCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", scriptDialogCnFileName);
            // 【跳过逻辑优化】
            if (scriptDialogFileName == null) {
                scriptDialogFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog", ".txt");
            }
            return;
        }
        File generatedFile = TranslateUtil.genScriptDialogCn(FOLDER_NAME, scriptDialogCnFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "翻译后的对话脚本 script_dialog_cn.txt 内容为空白");
        log.info("成功翻译对话脚本: {}", scriptDialogCnFileName);
    }

    @Test
    @Order(5)
    @DisplayName("05. 翻译词汇脚本")
    void test05_TranslateVocabularyScript() {
        log.info("\n========【05. 翻译词汇脚本 | {}】========", FOLDER_NAME);
        String vocFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "voc", ".txt");
        String vocCnFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "voc_cn", ".txt");
        if (FileUtil.exist(vocCnFileName) && !StrUtil.isBlank(readString(vocCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", vocCnFileName);
            return;
        }
        File generatedFile = DictUtil.genVocCnWithGemini(vocFileName, vocCnFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "翻译后的词汇脚本 voc_cn.txt 内容为空白");
        log.info("成功翻译词汇脚本: {}", vocCnFileName);
    }
    @Test
    @Order(6)
    @DisplayName("06. 生成SRT专用脚本")
    void test06_GenerateSrtReadyScript() {
        log.info("\n========【06. 生成SRT专用脚本 | {}】========", FOLDER_NAME);
        String scriptDialogNewFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog", "_new.txt");
        if (FileUtil.exist(scriptDialogNewFileName) && !StrUtil.isBlank(readString(scriptDialogNewFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", scriptDialogNewFileName);
            return;
        }
        File generatedFile = GenSrtUtil.genScriptDialogNew(FOLDER_NAME, scriptDialogNewFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "SRT专用脚本 script_dialog_new.txt 内容为空白");
        log.info("成功生成SRT专用脚本: {}", scriptDialogNewFileName);
    }

    @Test
    @Order(7)
    @DisplayName("07. 合并为中英双语脚本")
    void test07_MergeBilingualScript() {
        log.info("\n========【07. 合并为中英双语脚本 | {}】========", FOLDER_NAME);
        assertNotNull(scriptDialogFileName, "前置条件失败: 对话脚本文件名为空");
        assertNotNull(scriptDialogCnFileName, "前置条件失败: 中文对话脚本文件名为空");
        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, "_中英双语对话脚本.txt");
        if (FileUtil.exist(scriptDialogMergeFileName) && !StrUtil.isBlank(readString(scriptDialogMergeFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", scriptDialogMergeFileName);
            return;
        }
        File generatedFile = TranslateUtil.mergeScriptContent(scriptDialogFileName, scriptDialogCnFileName, scriptDialogMergeFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "合并后的中英双语对话脚本.txt 内容为空白");
        log.info("成功合并为中英双语脚本: {}", scriptDialogMergeFileName);
    }

    @Test
    @Order(8)
    @DisplayName("08. 生成原始SRT字幕")
    void test08_GenerateRawSrt() {
        log.info("\n========【08. 生成原始SRT字幕 | {}】========", FOLDER_NAME);
        List<String> fileNames = FileUtil.listFileNames(folderPath);
        for (String subFileName : fileNames) {
            if (subFileName.startsWith(FOLDER_NAME) && subFileName.endsWith(CdConstants.MP3_EXTENSION)) {
                mp3FileName = subFileName.substring(0, subFileName.length() - 4);
                break;
            }
        }
        assertNotNull(mp3FileName, "测试失败：在文件夹 " + FOLDER_NAME + " 中未找到对应的MP3文件");
        mp3FileNameFull = CommonUtil.getFullPathFileName(FOLDER_NAME, mp3FileName, CdConstants.MP3_EXTENSION);
        log.info("找到原始音频文件: {}", mp3FileNameFull);

        String srtScriptFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog_new", CdConstants.TXT_EXTENSION);
        srtEngRawFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng_raw", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(srtEngRawFileName) && !StrUtil.isBlank(readString(srtEngRawFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", srtEngRawFileName);
            return;
        }

        SubtitleUtil.genSrtByExecuteCommand(mp3FileNameFull, srtScriptFileName, srtEngRawFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(srtEngRawFileName, StandardCharsets.UTF_8)), "生成的原始SRT字幕 eng_raw.srt 内容为空白");
        log.info("成功生成原始SRT字幕: {}", srtEngRawFileName);
    }

    @Test
    @Order(9)
    @DisplayName("09. 提取时间戳并切割音频")
    void test09_ProcessTimestampsAndSplitAudio() {
        log.info("\n========【09. 提取时间戳并切割音频 | {}】========", FOLDER_NAME);
        assertNotNull(srtEngRawFileName, "前置条件失败: 原始SRT文件 'eng_raw.srt' 未生成");
        assertNotNull(mp3FileNameFull, "前置条件失败: 原始MP3文件路径未找到");

        String result = TextProcessor.processFile(srtEngRawFileName);
        assertNotNull(result, "从eng_raw.srt中未能提取到起止时间");
        String[] split = result.split("\\s+");
        assertTrue(split.length >= 3, "提取的起止时间格式不正确，应包含文件名、开始时间、结束时间");
        startTime = split[1];
        log.info("提取到对话开始时间: {}", startTime);

        mp3FileNameFullNew = CommonUtil.getFullPathFileName(FOLDER_NAME, "audio5", CdConstants.MP3_EXTENSION);
        if (FileUtil.exist(mp3FileNameFullNew) && FileUtil.size(new File(mp3FileNameFullNew)) > 0) {
            log.info("文件已存在，跳过测试: {}", mp3FileNameFullNew);
            // 【跳过逻辑优化】即使跳过，也要确保 startTime 已被赋值
            if (startTime == null) {
                result = TextProcessor.processFile(srtEngRawFileName);
                assertNotNull(result, "从eng_raw.srt中未能提取到起止时间");
                split = result.split("\\s+");
                assertTrue(split.length >= 3, "重新提取时间戳失败");
                startTime = split[1];
            }
            return;
        }

        CdMP3SplitterUtil.splitMP3(mp3FileNameFull, mp3FileNameFullNew, startTime, split[2]);
        assertTrue(FileUtil.exist(mp3FileNameFullNew) && FileUtil.size(new File(mp3FileNameFullNew)) > 0, "切割后的音频 audio5.mp3 生成失败或文件大小为0");
        log.info("成功切割音频: {}", mp3FileNameFullNew);
    }

    @Test
    @Order(10)
    @DisplayName("10. 生成用于最终SRT的脚本")
    void test10_GenerateFinalScriptForSrt() {
        log.info("\n========【10. 生成用于最终SRT的脚本 | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】确保前置步骤即使被跳过，这里的变量也能被正确初始化
        if (srtEngRawFileName == null) {
            srtEngRawFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng_raw", CdConstants.SRT_EXTENSION);
        }
        if (startTime == null) {
            String result = TextProcessor.processFile(srtEngRawFileName);
            assertNotNull(result, "从eng_raw.srt中未能提取到起止时间");
            String[] split = result.split("\\s+");
            assertTrue(split.length >= 2, "重新提取时间戳失败"); // 至少要有开始和结束时间
            startTime = split[1];
            log.info("已重新提取对话开始时间: {}", startTime);
        }

        scriptDialogNew2FileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog_new2", CdConstants.TXT_EXTENSION);
        if (FileUtil.exist(scriptDialogNew2FileName) && !StrUtil.isBlank(readString(scriptDialogNew2FileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", scriptDialogNew2FileName);
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
        assertFalse(lines.isEmpty(), "从 eng_raw.srt 提取字幕内容失败"); // 使用 assertFalse
        File generatedFile = FileUtil.writeLines(lines, scriptDialogNew2FileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "最终SRT脚本 script_dialog_new2.txt 内容为空白");
        log.info("成功生成最终SRT脚本: {}", scriptDialogNew2FileName);
    }

    @Test
    @Order(11)
    @DisplayName("11. 生成最终英文字幕(eng.srt)")
    void test11_GenerateFinalEnglishSrt() {
        log.info("\n========【11. 生成最终英文字幕(eng.srt) | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (mp3FileNameFullNew == null) {
            mp3FileNameFullNew = CommonUtil.getFullPathFileName(FOLDER_NAME, "audio5", CdConstants.MP3_EXTENSION);
        }
        if (scriptDialogNew2FileName == null) {
            scriptDialogNew2FileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "script_dialog_new2", CdConstants.TXT_EXTENSION);
        }
        assertNotNull(mp3FileNameFullNew, "前置条件失败：切割后的音频路径为空");
        assertNotNull(scriptDialogNew2FileName, "前置条件失败：用于生成最终字幕的脚本为空");

        srtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(srtFileName) && !StrUtil.isBlank(readString(srtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", srtFileName);
            chnSrtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "chn", ".srt"); // 为下一步准备
            return;
        }
        SubtitleUtil.genSrtByExecuteCommand(mp3FileNameFullNew, scriptDialogNew2FileName, srtFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(srtFileName, StandardCharsets.UTF_8)), "最终英文字幕 eng.srt 内容为空白");
        log.info("成功生成最终英文字幕: {}", srtFileName);
    }

    @Test
    @Order(12)
    @DisplayName("12. 翻译为中文字幕(chn.srt)")
    void test12_TranslateToChineseSrt() {
        log.info("\n========【12. 翻译为中文字幕(chn.srt) | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (srtFileName == null) {
            srtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng", CdConstants.SRT_EXTENSION);
        }
        assertNotNull(srtFileName, "前置条件失败: 英文SRT文件 'eng.srt' 未生成");

        chnSrtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "chn", ".srt");
        if (FileUtil.exist(chnSrtFileName) && !StrUtil.isBlank(readString(chnSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", chnSrtFileName);
            bilingualSrtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "bilingual", ".srt"); // 为下一步准备
            return;
        }

        boolean translationSuccess = TranslateUtil.translateEngSrc(FOLDER_NAME);

        assertTrue(translationSuccess, "翻译流程最终失败，请检查 TranslateUtil 中的错误日志。");
        log.info("成功生成最终中文字幕: {}", chnSrtFileName);
    }

    @Test
    @Order(13)
    @DisplayName("13. 合并为双语字幕(bilingual.srt)")
    void test13_MergeToBilingualSrt() {
        log.info("\n========【13. 合并为双语字幕(bilingual.srt) | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (srtFileName == null) {
            srtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng", CdConstants.SRT_EXTENSION);
        }
        if (chnSrtFileName == null) {
            chnSrtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "chn", ".srt");
        }
        assertNotNull(srtFileName, "前置条件失败: 英文SRT文件 'eng.srt' 未生成");
        assertNotNull(chnSrtFileName, "前置条件失败: 中文SRT文件 'chn.srt' 未生成");

        bilingualSrtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "bilingual", ".srt");
        if (FileUtil.exist(bilingualSrtFileName) && !StrUtil.isBlank(readString(bilingualSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", bilingualSrtFileName);
            return;
        }

        List<SubtitleEntity> engSubtitles = CdFileUtil.readSrtFileContent(srtFileName);
        List<SubtitleEntity> chnSubtitles = CdFileUtil.readSrtFileContent(chnSrtFileName);

        assertEquals(engSubtitles.size(), chnSubtitles.size(), "中英文字幕数量不一致，无法合并");
        assertTrue(CollectionUtil.isNotEmpty(engSubtitles), "读取到的英文字幕列表为空");

        List<String> bilingualLines = new ArrayList<>();
        for (int i = 0; i < engSubtitles.size(); i++) {
            bilingualLines.add(String.valueOf(engSubtitles.get(i).getSubIndex()));
            bilingualLines.add(engSubtitles.get(i).getTimeStr());
            bilingualLines.add(engSubtitles.get(i).getSubtitle());
            bilingualLines.add(chnSubtitles.get(i).getSubtitle());
            bilingualLines.add(""); // Blank line
        }

        File generatedFile = FileUtil.writeLines(bilingualLines, bilingualSrtFileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的双语字幕文件 bilingual.srt 内容为空白");
        log.info("成功生成双语字幕文件: {}", bilingualSrtFileName);
    }

    @Test
    @Order(14)
    @DisplayName("14. 生成多维度词汇表")
    void test14_GenerateVocabularyTables() {
        log.info("\n========【14. 生成多维度词汇表 | {}】========", FOLDER_NAME);
        String fullVocFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, "_完整词汇表.xlsx");
        if (FileUtil.exist(fullVocFileName) && FileUtil.size(new File(fullVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", fullVocFileName);
        } else {
            File generatedFullVoc = WordCountUtil.genVocTable(FOLDER_NAME);
            assertTrue(FileUtil.exist(generatedFullVoc) && FileUtil.size(generatedFullVoc) > 0, "完整词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成完整词汇表: {}", fullVocFileName);
        }

        String excelCoreVocFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, "_核心词汇表.xlsx");
        if (FileUtil.exist(excelCoreVocFileName) && FileUtil.size(new File(excelCoreVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelCoreVocFileName);
        } else {
            File generatedCoreVoc = CoreWordUtil.genCoreWordTable(FOLDER_NAME);
            assertTrue(FileUtil.exist(generatedCoreVoc) && FileUtil.size(generatedCoreVoc) > 0, "核心词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成核心词汇表: {}", excelCoreVocFileName);
        }

        String excelAdvancedFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, "_高级词汇表.xlsx");
        if (FileUtil.exist(excelAdvancedFileName) && FileUtil.size(new File(excelAdvancedFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelAdvancedFileName);
        } else {
            File generatedAdvancedVoc = AdvancedWordUtil.genAdvancedWordTable(FOLDER_NAME, CdConstants.TEMPLATE_FLAG);
            assertTrue(FileUtil.exist(generatedAdvancedVoc) && FileUtil.size(generatedAdvancedVoc) > 0, "高级词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成高级词汇表: {}", excelAdvancedFileName);
        }
    }

    @Test
    @Order(15)
    @DisplayName("15. 生成教学PPT")
    void test15_GeneratePpt() {
        log.info("\n========【15. 生成教学PPT | {}】========", FOLDER_NAME);

        chapterName = GetSixMinutesPpt.queryChapterNameForSixMinutes(FOLDER_NAME);
        assertFalse(StrUtil.isBlank(chapterName), "未能查询到章节名称");
        log.info("查询到章节名称: {}", chapterName);

        pptxFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, ".pptx");
        // 【关键修复】合并判断逻辑，并在文件存在时立即返回
        if (FileUtil.exist(pptxFileName) && FileUtil.size(new File(pptxFileName)) > 0) {
            log.info("文件已存在，跳过测试: {}", pptxFileName);
            return; // 确保跳过后续的生成步骤
        }

        File generatedPpt = GetSixMinutesPpt.process(FOLDER_NAME, chapterName);
        assertTrue(FileUtil.exist(generatedPpt) && FileUtil.size(generatedPpt) > 0, "PPTX 文件生成失败或文件大小为0");
        log.info("成功生成教学PPT: {}", pptxFileName);
    }

    @Test
    @Order(16)
    @DisplayName("16. 转换PPT为图片")
    void test16_ConvertPptToImages() {
        log.info("\n========【16. 转换PPT为图片 | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (pptxFileName == null) {
            pptxFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, ".pptx");
        }
        assertNotNull(pptxFileName, "前置条件失败: PPTX文件未生成");
        String pptPicDir = new File(pptxFileName).getParent() + File.separator + FOLDER_NAME + File.separator;
        if (FileUtil.exist(pptPicDir) && FileUtil.isNotEmpty(new File(pptPicDir))) {
            log.info("文件夹已存在且不为空，跳过测试: {}", pptPicDir);
            return;
        }
        PptToImageConverter.convertPptToImages(pptxFileName, pptPicDir, "snapshot"); // 确保此方法会创建目录
        assertTrue(FileUtil.isNotEmpty(new File(pptPicDir)), "PPT 图片文件夹生成失败或为空");
        log.info("成功转换PPT为图片，输出目录: {}", pptPicDir);
    }

    @Test
    @Order(17)
    @DisplayName("17. 生成平台描述文件")
    void test17_GenerateDescriptionFiles() {
        log.info("\n========【17. 生成平台描述文件 | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (pptxFileName == null) {
            pptxFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, ".pptx");
        }
        assertNotNull(pptxFileName, "前置条件失败: PPTX文件未生成");

        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, "_中英双语对话脚本.txt");
        String descriptionFileName = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description");
        if (FileUtil.exist(descriptionFileName) && !StrUtil.isBlank(readString(descriptionFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", descriptionFileName);
        } else {
            File generatedDesc = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileName);
            assertFalse(StrUtil.isBlank(readString(generatedDesc, StandardCharsets.UTF_8)), "国内平台描述文件内容为空白");
            log.info("成功生成国内平台描述文件: {}", descriptionFileName);
        }

        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description_yt");
        if (FileUtil.exist(descriptionFileNameYT) && !StrUtil.isBlank(readString(descriptionFileNameYT, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过测试: {}", descriptionFileNameYT);
        } else {
            File generatedDescYt = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileNameYT);
            assertFalse(StrUtil.isBlank(readString(generatedDescYt, StandardCharsets.UTF_8)), "YouTube平台英文描述文件内容为空白");
            log.info("成功生成YouTube平台英文描述文件: {}", descriptionFileNameYT);
        }
    }

    @Test
    @Order(18)
    @DisplayName("18. 生成YouTube专用描述")
    void test18_GenerateFinalYoutubeDescriptions() {
        log.info("\n========【18. 生成YouTube专用描述 | {}】========", FOLDER_NAME);
        // 【跳过逻辑优化】
        if (srtFileName == null) {
            srtFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, "eng", CdConstants.SRT_EXTENSION);
        }
        if (chapterName == null) {
            chapterName = GetSixMinutesPpt.queryChapterNameForSixMinutes(FOLDER_NAME);
        }
        if (pptxFileName == null) {
            pptxFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, ".pptx");
        }
        assertNotNull(srtFileName, "前置条件失败：最终英文字幕文件名为空");
        assertNotNull(chapterName, "前置条件失败：章节名称为空");

        String pptxFileName = CommonUtil.getFullPathFileName(FOLDER_NAME, FOLDER_NAME, ".pptx");
        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(pptxFileName, "md"), "_description_yt");
        String chnMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_chn");
        String chtMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_cht");
        if (FileUtil.exist(chnMdFileName) && !StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)) &&
            FileUtil.exist(chtMdFileName) && !StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8))) {
            log.info("YouTube中繁体描述文件已存在，跳过测试。");
            return;
        }
        PreparePublishUtil.genDescriptionForYT(folderPath, FOLDER_NAME, "", "", "6", srtFileName, chapterName);
        assertFalse(StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)), "YouTube平台简体中文描述文件内容为空白");
        assertFalse(StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8)), "YouTube平台繁体中文描述文件内容为空白");
        log.info("成功生成YouTube平台中/繁体描述文件");
    }
}
