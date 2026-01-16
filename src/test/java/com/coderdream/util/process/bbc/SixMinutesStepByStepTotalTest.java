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
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cn.hutool.core.io.FileUtil.readString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * 6 Minute English 全流程端到端集成测试 (动态测试版)
 * <p>
 * 本测试类从 todo.txt 文件中读取要处理的文件夹列表，
 * 并为每个文件夹动态生成一个包含完整处理步骤的测试流程。
 *
 * @author Gemini Code Assist
 */
@Slf4j
class SixMinutesStepByStepTotalTest {

    /**
     * 内部类，用于为每个文件夹的测试流程维护一个独立的上下文状态。
     * 替代了原先脆弱的静态变量共享方式。
     */
    private static class TestContext {
        String folderName;
        String folderPath;
        String mp3FileName;
        String mp3FileNameFull;
        String mp3FileNameFullNew;
        String srtEngRawFileName;
        String scriptDialogFileName;
        String scriptDialogCnFileName;
        String scriptDialogNew2FileName;
        String chnSrtFileName;
        String bilingualSrtFileName;
        String srtFileName;
        String pptxFileName;
        String chapterName;
        String startTime;
    }

    /**
     * 使用JUnit 5的@TestFactory动态生成测试。
     * 此方法会读取todo.txt，并为其中的每一个文件夹名称生成一个独立的测试容器(DynamicContainer)。
     *
     * @return 一个包含多个动态测试容器的Stream。
     */
    @TestFactory
    Stream<DynamicNode> runFullPipelineForAllFolders() {
        log.info("========【动态测试套件初始化】========");
        String todoFilePath = CdFileUtil.getResourceRealPath() + File.separatorChar + "data" + File.separatorChar + "bbc" + File.separatorChar + "todo.txt";
        List<String> folderNames = FileUtil.readLines(todoFilePath, "UTF-8");

        return folderNames.stream()
            .filter(StrUtil::isNotBlank) // 过滤掉空行
            .map(String::trim)
            .map(folderName -> {
                // 为每个文件夹创建一个独立的上下文对象
                TestContext context = new TestContext();
                context.folderName = folderName;
                context.folderPath = CommonUtil.getFullPath(folderName);

                // 为每个文件夹创建一个测试容器，容器名为文件夹名
                return dynamicContainer(
                    "Processing Folder: " + folderName,
                    createTestSteps(context) // 在容器内生成有序的测试步骤
                );
            });
    }

    /**
     * 为单个文件夹的上下文(TestContext)生成所有有序的测试步骤。
     *
     * @param context 当前文件夹的测试上下文。
     * @return 一个包含所有测试步骤的Stream。
     */
    private Stream<DynamicTest> createTestSteps(TestContext context) {
        // 将18个步骤封装为动态测试列表
        return Stream.of(
            dynamicTest("01. 预处理原始脚本", () -> test01_ProcessRawScript(context)),
            dynamicTest("02. 生成对话脚本", () -> test02_GenerateDialogScript(context)),
            dynamicTest("03. 生成词汇脚本", () -> test03_GenerateVocabularyScript(context)),
            dynamicTest("04. 翻译对话脚本", () -> test04_TranslateDialogScript(context)),
            dynamicTest("05. 翻译词汇脚本", () -> test05_TranslateVocabularyScript(context)),
            dynamicTest("06. 生成SRT专用脚本", () -> test06_GenerateSrtReadyScript(context)),
            dynamicTest("07. 合并为中英双语脚本", () -> test07_MergeBilingualScript(context)),
            dynamicTest("08. 生成原始SRT字幕", () -> test08_GenerateRawSrt(context)),
            dynamicTest("09. 提取时间戳并切割音频", () -> test09_ProcessTimestampsAndSplitAudio(context)),
            dynamicTest("10. 生成用于最终SRT的脚本", () -> test10_GenerateFinalScriptForSrt(context)),
            dynamicTest("11. 生成最终英文字幕(eng.srt)", () -> test11_GenerateFinalEnglishSrt(context)),
            dynamicTest("12. 翻译为中文字幕(chn.srt)", () -> test12_TranslateToChineseSrt(context)),
            dynamicTest("13. 合并为双语字幕(bilingual.srt)", () -> test13_MergeToBilingualSrt(context)),
            dynamicTest("14. 生成多维度词汇表", () -> test14_GenerateVocabularyTables(context)),
            dynamicTest("15. 生成教学PPT", () -> test15_GeneratePpt(context)),
            dynamicTest("16. 转换PPT为图片", () -> test16_ConvertPptToImages(context)),
            dynamicTest("17. 生成平台描述文件", () -> test17_GenerateDescriptionFiles(context)),
            dynamicTest("18. 生成YouTube专用描述", () -> test18_GenerateFinalYoutubeDescriptions(context))
        );
    }

    // =====================================================================================
    // 以下是将原有的 @Test 方法改造为接受 TestContext 参数的普通方法
    // =====================================================================================

    void test01_ProcessRawScript(TestContext context) {
        log.info("\n========【01. 预处理原始脚本 | {}】========", context.folderName);
        String srcScriptFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName + "_script", ".txt");
        ProcessScriptUtil.processScriptTxt(srcScriptFileName);
        assertFalse(StrUtil.isBlank(readString(srcScriptFileName, StandardCharsets.UTF_8)), "预处理后的原始脚本文件内容为空白");
        log.info("成功预处理原始脚本: {}", srcScriptFileName);
    }

    void test02_GenerateDialogScript(TestContext context) {
        log.info("\n========【02. 生成对话脚本 | {}】========", context.folderName);
        context.scriptDialogFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog", ".txt");
        if (FileUtil.exist(context.scriptDialogFileName) && !StrUtil.isBlank(readString(context.scriptDialogFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.scriptDialogFileName);
            context.scriptDialogCnFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog", "_cn.txt"); // 为后续步骤预设变量
            return;
        }
        File generatedFile = ProcessScriptUtil.genScriptDialogTxt(context.folderName, context.scriptDialogFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的对话脚本 script_dialog.txt 内容为空白");
        log.info("成功生成对话脚本: {}", context.scriptDialogFileName);
    }

    void test03_GenerateVocabularyScript(TestContext context) {
        log.info("\n========【03. 生成词汇脚本 | {}】========", context.folderName);
        String vocFileName = CommonUtil.getFullPathFileName(context.folderName, "voc", ".txt");
        if (FileUtil.exist(vocFileName) && !StrUtil.isBlank(readString(vocFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", vocFileName);
            return;
        }
        File generatedFile = ProcessScriptUtil.genVocTxt(context.folderName, vocFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的词汇脚本 voc.txt 内容为空白");
        log.info("成功生成词汇脚本: {}", vocFileName);
    }

    void test04_TranslateDialogScript(TestContext context) {
        log.info("\n========【04. 翻译对话脚本 | {}】========", context.folderName);
        context.scriptDialogCnFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog", "_cn.txt");
        if (FileUtil.exist(context.scriptDialogCnFileName) && !StrUtil.isBlank(readString(context.scriptDialogCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.scriptDialogCnFileName);
            if (context.scriptDialogFileName == null) { // 确保前一步跳过时，此变量仍被初始化
                context.scriptDialogFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog", ".txt");
            }
            return;
        }
        File generatedFile = TranslateUtil.genScriptDialogCn(context.folderName, context.scriptDialogCnFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "翻译后的对话脚本 script_dialog_cn.txt 内容为空白");
        log.info("成功翻译对话脚本: {}", context.scriptDialogCnFileName);
    }

    void test05_TranslateVocabularyScript(TestContext context) {
        log.info("\n========【05. 翻译词汇脚本 | {}】========", context.folderName);
        String vocFileName = CommonUtil.getFullPathFileName(context.folderName, "voc", ".txt");
        String vocCnFileName = CommonUtil.getFullPathFileName(context.folderName, "voc_cn", ".txt");
        if (FileUtil.exist(vocCnFileName) && !StrUtil.isBlank(readString(vocCnFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", vocCnFileName);
            return;
        }
        File generatedFile = DictUtil.genVocCnWithGemini(vocFileName, vocCnFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "翻译后的词汇脚本 voc_cn.txt 内容为空白");
        log.info("成功翻译词汇脚本: {}", vocCnFileName);
    }

    void test06_GenerateSrtReadyScript(TestContext context) {
        log.info("\n========【06. 生成SRT专用脚本 | {}】========", context.folderName);
        String scriptDialogNewFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog", "_new.txt");
        if (FileUtil.exist(scriptDialogNewFileName) && !StrUtil.isBlank(readString(scriptDialogNewFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogNewFileName);
            return;
        }
        File generatedFile = GenSrtUtil.genScriptDialogNew(context.folderName, scriptDialogNewFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "SRT专用脚本 script_dialog_new.txt 内容为空白");
        log.info("成功生成SRT专用脚本: {}", scriptDialogNewFileName);
    }

    void test07_MergeBilingualScript(TestContext context) {
        log.info("\n========【07. 合并为中英双语脚本 | {}】========", context.folderName);
        assertNotNull(context.scriptDialogFileName, "前置条件失败: 对话脚本文件名为空");
        assertNotNull(context.scriptDialogCnFileName, "前置条件失败: 中文对话脚本文件名为空");
        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, "_中英双语对话脚本.txt");
        if (FileUtil.exist(scriptDialogMergeFileName) && !StrUtil.isBlank(readString(scriptDialogMergeFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", scriptDialogMergeFileName);
            return;
        }
        File generatedFile = TranslateUtil.mergeScriptContent(context.scriptDialogFileName, context.scriptDialogCnFileName, scriptDialogMergeFileName);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "合并后的中英双语对话脚本.txt 内容为空白");
        log.info("成功合并为中英双语脚本: {}", scriptDialogMergeFileName);
    }

    void test08_GenerateRawSrt(TestContext context) {
        log.info("\n========【08. 生成原始SRT字幕 | {}】========", context.folderName);
        List<String> fileNames = FileUtil.listFileNames(context.folderPath);
        context.mp3FileName = fileNames.stream()
            .filter(name -> name.startsWith(context.folderName) && name.endsWith(CdConstants.MP3_EXTENSION))
            .map(name -> name.substring(0, name.length() - 4))
            .findFirst()
            .orElse(null);

        assertNotNull(context.mp3FileName, "测试失败：在文件夹 " + context.folderName + " 中未找到对应的MP3文件");
        context.mp3FileNameFull = CommonUtil.getFullPathFileName(context.folderName, context.mp3FileName, CdConstants.MP3_EXTENSION);
        log.info("找到原始音频文件: {}", context.mp3FileNameFull);

        String srtScriptFileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog_new", CdConstants.TXT_EXTENSION);
        context.srtEngRawFileName = CommonUtil.getFullPathFileName(context.folderName, "eng_raw", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(context.srtEngRawFileName) && !StrUtil.isBlank(readString(context.srtEngRawFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.srtEngRawFileName);
            return;
        }

        SubtitleUtil.genSrtByExecuteCommand(context.mp3FileNameFull, srtScriptFileName, context.srtEngRawFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(context.srtEngRawFileName, StandardCharsets.UTF_8)), "生成的原始SRT字幕 eng_raw.srt 内容为空白");
        log.info("成功生成原始SRT字幕: {}", context.srtEngRawFileName);
    }

    void test09_ProcessTimestampsAndSplitAudio(TestContext context) {
        log.info("\n========【09. 提取时间戳并切割音频 | {}】========", context.folderName);
        assertNotNull(context.srtEngRawFileName, "前置条件失败: 原始SRT文件 'eng_raw.srt' 未生成");
        assertNotNull(context.mp3FileNameFull, "前置条件失败: 原始MP3文件路径未找到");

        String result = TextProcessor.processFile(context.srtEngRawFileName);
        assertNotNull(result, "从eng_raw.srt中未能提取到起止时间");
        String[] split = result.split("\\s+");
        assertTrue(split.length >= 3, "提取的起止时间格式不正确，应包含文件名、开始时间、结束时间");
        context.startTime = split[1];
        log.info("提取到对话开始时间: {}", context.startTime);

        context.mp3FileNameFullNew = CommonUtil.getFullPathFileName(context.folderName, "audio5", CdConstants.MP3_EXTENSION);
        if (FileUtil.exist(context.mp3FileNameFullNew) && FileUtil.size(new File(context.mp3FileNameFullNew)) > 0) {
            log.info("文件已存在，跳过生成: {}", context.mp3FileNameFullNew);
            return;
        }

        CdMP3SplitterUtil.splitMP3(context.mp3FileNameFull, context.mp3FileNameFullNew, context.startTime, split[2]);
        assertTrue(FileUtil.exist(context.mp3FileNameFullNew) && FileUtil.size(new File(context.mp3FileNameFullNew)) > 0, "切割后的音频 audio5.mp3 生成失败或文件大小为0");
        log.info("成功切割音频: {}", context.mp3FileNameFullNew);
    }

    void test10_GenerateFinalScriptForSrt(TestContext context) {
        log.info("\n========【10. 生成用于最终SRT的脚本 | {}】========", context.folderName);
        assertNotNull(context.srtEngRawFileName, "前置条件失败: 原始SRT文件 'eng_raw.srt' 未生成");
        assertNotNull(context.startTime, "前置条件失败: 对话开始时间 startTime 未提取");

        context.scriptDialogNew2FileName = CommonUtil.getFullPathFileName(context.folderName, "script_dialog_new2", CdConstants.TXT_EXTENSION);
        if (FileUtil.exist(context.scriptDialogNew2FileName) && !StrUtil.isBlank(readString(context.scriptDialogNew2FileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.scriptDialogNew2FileName);
            return;
        }

        List<String> lines = new ArrayList<>();
        List<SubtitleEntity> subtitleEntityList = CdFileUtil.readSrtFileContent(context.srtEngRawFileName);
        if (CollectionUtil.isNotEmpty(subtitleEntityList)) {
            boolean isSubtitle = false;
            for (SubtitleEntity subtitleEntity : subtitleEntityList) {
                if (subtitleEntity.getTimeStr().startsWith(context.startTime)) {
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
        File generatedFile = FileUtil.writeLines(lines, context.scriptDialogNew2FileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "最终SRT脚本 script_dialog_new2.txt 内容为空白");
        log.info("成功生成最终SRT脚本: {}", context.scriptDialogNew2FileName);
    }

    void test11_GenerateFinalEnglishSrt(TestContext context) {
        log.info("\n========【11. 生成最终英文字幕(eng.srt) | {}】========", context.folderName);
        assertNotNull(context.mp3FileNameFullNew, "前置条件失败：切割后的音频路径为空");
        assertNotNull(context.scriptDialogNew2FileName, "前置条件失败：用于生成最终字幕的脚本为空");

        context.srtFileName = CommonUtil.getFullPathFileName(context.folderName, "eng", CdConstants.SRT_EXTENSION);
        if (FileUtil.exist(context.srtFileName) && !StrUtil.isBlank(readString(context.srtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.srtFileName);
            context.chnSrtFileName = CommonUtil.getFullPathFileName(context.folderName, "chn", ".srt"); // 为下一步准备
            return;
        }
        SubtitleUtil.genSrtByExecuteCommand(context.mp3FileNameFullNew, context.scriptDialogNew2FileName, context.srtFileName, "eng");
        assertFalse(StrUtil.isBlank(readString(context.srtFileName, StandardCharsets.UTF_8)), "最终英文字幕 eng.srt 内容为空白");
        log.info("成功生成最终英文字幕: {}", context.srtFileName);
    }

    void test12_TranslateToChineseSrt(TestContext context) {
        log.info("\n========【12. 翻译为中文字幕(chn.srt) | {}】========", context.folderName);
        assertNotNull(context.srtFileName, "前置条件失败: 英文SRT文件 'eng.srt' 未生成");

        context.chnSrtFileName = CommonUtil.getFullPathFileName(context.folderName, "chn", ".srt");
        if (FileUtil.exist(context.chnSrtFileName) && !StrUtil.isBlank(readString(context.chnSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.chnSrtFileName);
            context.bilingualSrtFileName = CommonUtil.getFullPathFileName(context.folderName, "bilingual", ".srt"); // 为下一步准备
            return;
        }

        boolean translationSuccess = TranslateUtil.translateEngSrc(context.folderName);
        assertTrue(translationSuccess, "翻译流程最终失败，请检查 TranslateUtil 中的错误日志。");
        log.info("成功生成最终中文字幕: {}", context.chnSrtFileName);
    }

    void test13_MergeToBilingualSrt(TestContext context) {
        log.info("\n========【13. 合并为双语字幕(bilingual.srt) | {}】========", context.folderName);
        assertNotNull(context.srtFileName, "前置条件失败: 英文SRT文件 'eng.srt' 未生成");
        assertNotNull(context.chnSrtFileName, "前置条件失败: 中文SRT文件 'chn.srt' 未生成");

        context.bilingualSrtFileName = CommonUtil.getFullPathFileName(context.folderName, "bilingual", ".srt");
        if (FileUtil.exist(context.bilingualSrtFileName) && !StrUtil.isBlank(readString(context.bilingualSrtFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", context.bilingualSrtFileName);
            return;
        }

        List<SubtitleEntity> engSubtitles = CdFileUtil.readSrtFileContent(context.srtFileName);
        List<SubtitleEntity> chnSubtitles = CdFileUtil.readSrtFileContent(context.chnSrtFileName);

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

        File generatedFile = FileUtil.writeLines(bilingualLines, context.bilingualSrtFileName, StandardCharsets.UTF_8);
        assertFalse(StrUtil.isBlank(readString(generatedFile, StandardCharsets.UTF_8)), "生成的双语字幕文件 bilingual.srt 内容为空白");
        log.info("成功生成双语字幕文件: {}", context.bilingualSrtFileName);
    }

    void test14_GenerateVocabularyTables(TestContext context) {
        log.info("\n========【14. 生成多维度词汇表 | {}】========", context.folderName);
        String fullVocFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, "_完整词汇表.xlsx");
        if (FileUtil.exist(fullVocFileName) && FileUtil.size(new File(fullVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", fullVocFileName);
        } else {
            File generatedFullVoc = WordCountUtil.genVocTable(context.folderName);
            assertTrue(FileUtil.exist(generatedFullVoc) && FileUtil.size(generatedFullVoc) > 0, "完整词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成完整词汇表: {}", fullVocFileName);
        }

        String excelCoreVocFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, "_核心词汇表.xlsx");
        if (FileUtil.exist(excelCoreVocFileName) && FileUtil.size(new File(excelCoreVocFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelCoreVocFileName);
        } else {
            File generatedCoreVoc = CoreWordUtil.genCoreWordTable(context.folderName);
            assertTrue(FileUtil.exist(generatedCoreVoc) && FileUtil.size(generatedCoreVoc) > 0, "核心词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成核心词汇表: {}", excelCoreVocFileName);
        }

        String excelAdvancedFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, "_高级词汇表.xlsx");
        if (FileUtil.exist(excelAdvancedFileName) && FileUtil.size(new File(excelAdvancedFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", excelAdvancedFileName);
        } else {
            File generatedAdvancedVoc = AdvancedWordUtil.genAdvancedWordTable(context.folderName, CdConstants.TEMPLATE_FLAG);
            assertTrue(FileUtil.exist(generatedAdvancedVoc) && FileUtil.size(generatedAdvancedVoc) > 0, "高级词汇表.xlsx 生成失败或文件大小为0");
            log.info("成功生成高级词汇表: {}", excelAdvancedFileName);
        }
    }

    void test15_GeneratePpt(TestContext context) {
        log.info("\n========【15. 生成教学PPT | {}】========", context.folderName);

        context.chapterName = GetSixMinutesPpt.queryChapterNameForSixMinutes(context.folderName);
        assertFalse(StrUtil.isBlank(context.chapterName), "未能查询到章节名称");
        log.info("查询到章节名称: {}", context.chapterName);

        context.pptxFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, ".pptx");
        if (FileUtil.exist(context.pptxFileName) && FileUtil.size(new File(context.pptxFileName)) > 0) {
            log.info("文件已存在，跳过生成: {}", context.pptxFileName);
            return;
        }

        File generatedPpt = GetSixMinutesPpt.process(context.folderName, context.chapterName);
        assertTrue(FileUtil.exist(generatedPpt) && FileUtil.size(generatedPpt) > 0, "PPTX 文件生成失败或文件大小为0");
        log.info("成功生成教学PPT: {}", context.pptxFileName);
    }

    void test16_ConvertPptToImages(TestContext context) {
        log.info("\n========【16. 转换PPT为图片 | {}】========", context.folderName);
        assertNotNull(context.pptxFileName, "前置条件失败: PPTX文件未生成");
        String pptPicDir = new File(context.pptxFileName).getParent() + File.separator + context.folderName + File.separator;
        if (FileUtil.exist(pptPicDir) && FileUtil.isNotEmpty(new File(pptPicDir))) {
            log.info("文件夹已存在且不为空，跳过生成: {}", pptPicDir);
            return;
        }
        PptToImageConverter.convertPptToImages(context.pptxFileName, pptPicDir, "snapshot");
        assertTrue(FileUtil.isNotEmpty(new File(pptPicDir)), "PPT 图片文件夹生成失败或为空");
        log.info("成功转换PPT为图片，输出目录: {}", pptPicDir);
    }

    void test17_GenerateDescriptionFiles(TestContext context) {
        log.info("\n========【17. 生成平台描述文件 | {}】========", context.folderName);
        assertNotNull(context.pptxFileName, "前置条件失败: PPTX文件未生成");

        String scriptDialogMergeFileName = CommonUtil.getFullPathFileName(context.folderName, context.folderName, "_中英双语对话脚本.txt");
        String descriptionFileName = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(context.pptxFileName, "md"), "_description");
        if (FileUtil.exist(descriptionFileName) && !StrUtil.isBlank(readString(descriptionFileName, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", descriptionFileName);
        } else {
            File generatedDesc = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileName);
            assertFalse(StrUtil.isBlank(readString(generatedDesc, StandardCharsets.UTF_8)), "国内平台描述文件内容为空白");
            log.info("成功生成国内平台描述文件: {}", descriptionFileName);
        }

        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(context.pptxFileName, "md"), "_description_yt");
        if (FileUtil.exist(descriptionFileNameYT) && !StrUtil.isBlank(readString(descriptionFileNameYT, StandardCharsets.UTF_8))) {
            log.info("文件已存在，跳过生成: {}", descriptionFileNameYT);
        } else {
            File generatedDescYt = TranslationUtil.genDescription(scriptDialogMergeFileName, descriptionFileNameYT);
            assertFalse(StrUtil.isBlank(readString(generatedDescYt, StandardCharsets.UTF_8)), "YouTube平台英文描述文件内容为空白");
            log.info("成功生成YouTube平台英文描述文件: {}", descriptionFileNameYT);
        }
    }

    void test18_GenerateFinalYoutubeDescriptions(TestContext context) {
        log.info("\n========【18. 生成YouTube专用描述 | {}】========", context.folderName);
        assertNotNull(context.srtFileName, "前置条件失败：最终英文字幕文件名为空");
        assertNotNull(context.chapterName, "前置条件失败：章节名称为空");

        String descriptionFileNameYT = CdFileUtil.addPostfixToFileName(CdFileUtil.changeExtension(context.pptxFileName, "md"), "_description_yt");
        String chnMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_chn");
        String chtMdFileName = CdFileUtil.addPostfixToFileName(descriptionFileNameYT, "_cht");
        if (FileUtil.exist(chnMdFileName) && !StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)) &&
            FileUtil.exist(chtMdFileName) && !StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8))) {
            log.info("YouTube中繁体描述文件已存在，跳过生成。");
            return;
        }
        PreparePublishUtil.genDescriptionForYT(context.folderPath, context.folderName, "", "", "6", context.srtFileName, context.chapterName);
        assertFalse(StrUtil.isBlank(readString(chnMdFileName, StandardCharsets.UTF_8)), "YouTube平台简体中文描述文件内容为空白");
        assertFalse(StrUtil.isBlank(readString(chtMdFileName, StandardCharsets.UTF_8)), "YouTube平台繁体中文描述文件内容为空白");
        log.info("成功生成YouTube平台中/繁体描述文件");
    }
}
