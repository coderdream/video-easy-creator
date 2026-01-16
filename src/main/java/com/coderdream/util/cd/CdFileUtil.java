package com.coderdream.util.cd;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.coderdream.entity.ArticleTitle;
import com.coderdream.entity.DialogSingleEntity;
import com.coderdream.entity.SubtitleEntity;
//import com.coderdream.entity.ThumbnailInfoEntity;
import com.coderdream.entity.YoutubeInfoEntity;
import com.coderdream.entity.YoutubeVideoSplitEntity;
import com.coderdream.util.proxy.OperatingSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ResourceUtils;

/**
 * Java按一行一行进行文件的读取或写入 https://blog.csdn.net/yuanhaiwn/article/details/83090540
 *
 * @author CoderDream
 */
@Slf4j
public class CdFileUtil {


    /**
     * 获取对话实体列表
     *
     * @param fileName 脚本位置
     * @return 对话实体列表
     */

    public static List<DialogSingleEntity> genDialogSingleEntityList(
            String fileName) {
        List<String> stringList = FileUtil.readLines(fileName,
                StandardCharsets.UTF_8);
        // 如果不为空，且最后一行不是空串，则添加一行空串
        if (CollectionUtil.isNotEmpty(stringList) && !StrUtil.isBlankOrUndefined(
                stringList.get(stringList.size() - 1))) {
            stringList.add("");
        }

        List<DialogSingleEntity> result = new ArrayList<>();

        DialogSingleEntity scriptEntity;
        if (CollectionUtils.isNotEmpty(stringList)) {
            int size = stringList.size();
            if (size % 3 != 0) {
                System.out.println(
                        "文件格式有问题，行数应该是3的倍数，实际为：" + size + "; fileName: "
                                + fileName);
                return null;
            }

            for (int i = 0; i < stringList.size(); i += 3) {
                scriptEntity = new DialogSingleEntity();
                scriptEntity.setHostEn(stringList.get(i));
                scriptEntity.setContentEn(stringList.get(i + 1));
                result.add(scriptEntity);
            }
        }

        return result;
    }

    //

    /**
     * 获取对话实体列表
     *
     * @param fileName 脚本位置
     * @return 对话实体列表
     */

    public static List<SubtitleEntity> genPureSubtitleEntityList(
            String fileName) {
        List<String> stringList = FileUtil.readLines(fileName,
                StandardCharsets.UTF_8);

        List<SubtitleEntity> result = new ArrayList<>();

        SubtitleEntity scriptEntity;
        if (CollectionUtils.isNotEmpty(stringList)) {
            int size = stringList.size();
            if (size % 2 != 0) {
                System.out.println(
                        "文件格式有问题，行数应该是2的倍数，实际为：" + size + "; fileName: "
                                + fileName);
                return null;
            }

            for (int i = 0; i < stringList.size(); i += 2) {
                scriptEntity = new SubtitleEntity();
                scriptEntity.setSubtitle(stringList.get(i));
                scriptEntity.setSecondSubtitle(stringList.get(i + 1));
                result.add(scriptEntity);
            }
        }

        return result;
    }

    /**
     * 读取resources文件夹下13500文件夹中的1-3500.txt文件并返回内容列表
     *
     * @return 文件内容的列表
     */
    public static List<String> readFileContent(String resourcePath) {
        // 获取资源的URL
//        String resourcePath = "classpath:13500/" + filename;
        try {
            // 使用HuTool的ResourceUtil获取资源路径
            // 指定要下载的文件
            File file = ResourceUtils.getFile(resourcePath);
            // 定义UTF-16 Little Endian编码
//      Charset utf16Le = StandardCharsets.UTF_16LE;
            // 读取文件内容到列表
//            return CdFileUtil.readLines(file, "UTF-8");
//            List<String> lines = CdFileUtil.readLines(file, utf16Le);
            List<String> lines = FileUtil.readLines(file, StandardCharsets.UTF_8);
            // 移除每行首尾空格，并过滤掉空行
            return lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("读取文件失败: {}, {}", resourcePath, e.getMessage(), e);
            // 抛出运行时异常或进行其他错误处理
//      throw new RuntimeException("读取文件失败", e);
        }
        return null;
    }

    /**
     * 读取resources文件夹下13500文件夹中的1-3500.txt文件并返回内容列表
     *
     * @return 文件内容的列表
     */
    public static List<String> readFileContentWithCharset(String resourcePath,
                                                          Charset charset) {
        // 获取资源的URL
//        String resourcePath = "classpath:13500/" + filename;
        try {
            // 使用HuTool的ResourceUtil获取资源路径
            // 指定要下载的文件
            File file = ResourceUtils.getFile(resourcePath);
            // 定义UTF-16 Little Endian编码
//            Charset utf16Le = StandardCharsets.UTF_16LE;
            // 读取文件内容到列表
//            return CdFileUtil.readLines(file, "UTF-8");
            List<String> lines = FileUtil.readLines(file, charset);
            // 移除每行首尾空格，并过滤掉空行
            return lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("读取文件失败: {}", e.getMessage());
            // 抛出运行时异常或进行其他错误处理
            throw new RuntimeException("读取文件失败", e);
        }
    }

    // ArticleTitle
    public static List<ArticleTitle> getArticleTitleList(String fileName) {
        List<ArticleTitle> articleTitleList = new ArrayList<>();
        List<String> titleList = FileUtil.readLines(fileName,
                StandardCharsets.UTF_8);
        ArticleTitle articleTitle;
        for (String title : titleList) {
            articleTitle = new ArticleTitle();
            String[] arrs = title.split("\\|");
            if (arrs.length == 2) {
                articleTitle.setDateStr(arrs[0]);
                articleTitle.setTitle(arrs[1]);
                articleTitleList.add(articleTitle);
            }
        }

        // 使用 Comparator.comparing 对 ArticleTitleList 进行排序
        articleTitleList.sort(Comparator.comparing(ArticleTitle::getDateStr));

        return articleTitleList;
    }

    public static String getArticleTitle(String folderName) {
//    String fileName = CdConstants.RESOURCES_BASE_PATH + "\\bbc\\title.txt";
        String fileName =
                OperatingSystem.getProjectResourcesFolder() + File.separator + "data"
                        + File.separator + "bbc" + File.separator + "title.txt";

        if (CdFileUtil.isFileEmpty(fileName)) {
            log.error("文件不存在：{}", fileName);
            return "";
        }
        List<ArticleTitle> articleTitleList = getArticleTitleList(fileName);
        for (ArticleTitle article : articleTitleList) {
            if (article.getDateStr().equals(folderName)) {
                return article.getTitle();
            }
        }
        return "";
    }

    public static void writeToFile(String fileName, String content) {
        try {
//            String[] arrs = {
//                    "zhangsan,23,福建",
//                    "lisi,30,上海",
//                    "wangwu,43,北京",
//                    "laolin,21,重庆",
//                    "ximenqing,67,贵州"
//            };
            String[] contentList = content.split(" ");
            //写入中文字符时解决中文乱码问题
            FileOutputStream fos = null;

//            fos = new FileOutputStream(new File("E:/phsftp/evdokey/evdokey_201103221556.txt"));

            fos = new FileOutputStream(fileName);

            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            //简写如下：
            //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            //        new FileOutputStream(new File("E:/phsftp/evdokey/evdokey_201103221556.txt")), "UTF-8"));

            for (String arr : contentList) {
                bw.write(arr + "\t\n");
            }

            //注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public static boolean writeToFile(String fileName, List<String> contentList) {
//        FileOutputStream fos = null;
//        OutputStreamWriter osw = null;
//        BufferedWriter bw = null;
//        try {
//            //写入中文字符时解决中文乱码问题
//            fos = new FileOutputStream(fileName);
//
//            osw = new OutputStreamWriter(fos, "UTF-8");
//            bw = new BufferedWriter(osw);
//            int size = contentList.size();
//            for (int i = 0; i < size; i++) {
//                String str = contentList.get(i);
//                //  str = "\uFEFF" + str; BOM格式，剪映不认识，Subindex 合并字幕时要打开
//                // 如果不是最后一行，就加上回车换行
//                if (i != size - 1) {
//                    if (str != null) {
//                        str = str.trim().replaceAll("  ", " ") + "\r\n";
//                    }
//                }
//
//                if (str != null) {
//                    bw.write(str);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        } finally {
//            //注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
//            if (bw != null) {
//                try {
//                    bw.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (osw != null) {
//                try {
//                    osw.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            return true;
//        }
//    }

    /**
     * 将内容写入指定的文件，并处理异常。
     *
     * @param fileName    文件名
     * @param contentList 要写入文件的内容列表
     * @return 如果写入成功返回 true，否则返回 false
     */
    public static boolean writeToFile(String fileName, List<String> contentList) {
        // 使用 try-with-resources 自动关闭资源
        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStreamWriter osw = new OutputStreamWriter(fos,
                     StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            int size = contentList.size();  // 获取内容列表的大小
            log.info("开始写入文件: {}", fileName);  // 记录开始写入文件的日志

            // 遍历所有内容并写入文件
            for (int i = 0; i < size; i++) {
                String str = contentList.get(i);

                // 如果内容不是最后一行，添加换行符
                if (i != size - 1 && str != null) {
                    str = str.trim().replaceAll("  ", " ") + "\r\n";
                }

                // 如果内容不为空，则写入
                if (str != null) {
                    bw.write(str);
                }
            }

            log.info("文件写入成功: {}", fileName);  // 记录成功写入文件的日志
            return true;  // 写入成功返回true

        } catch (IOException e) {
            // 捕获IOException并打印堆栈信息，返回false表示写入失败
            log.error("写入文件失败: {}，错误信息: {}", fileName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 返回 D:\04_GitHub\java-architect-util\free-apps\src\main\resources //
     * https://blog.csdn.net/qq_38319289/article/details/115236819 //
     * SpringBoot获取resources文件路径 // File directory = new
     * File("src/main/resources"); // String reportPath =
     * directory.getCanonicalPath(); // String resource =reportPath +
     * "/static/template/resultTemplate.docx";
     *
     * @return 资源文件夹路径
     */
    public static String getResourceRealPath() {
        File directory = new File("src/main/resources");
        String reportPath = "";
        try {
            reportPath = directory.getCanonicalPath();
        } catch (Exception e) {
            log.error("获取资源文件夹路径失败: {}", e.getMessage());
        }

        return reportPath;
    }

    /**
     * 在文件名后添加指定的字符串，保留文件路径和扩展名
     *
     * @param filePath 原始文件路径
     * @param part     要添加的字符串
     * @return 修改后的文件路径
     */
    public static String addPostfixToFileName(String filePath, String part) {
        // 将文件路径字符串转换为Path对象
        Path path = Paths.get(filePath);

        // 获取文件名
        Path fileName = path.getFileName();
        if (fileName == null) {
            return filePath;  // 如果没有文件名，则直接返回原始路径
        }

        String fileNameStr = fileName.toString();
        // 分割文件名和扩展名
        int dotIndex = fileNameStr.lastIndexOf('.');
        String baseName, extension = "";

        if (dotIndex > 0) {
            baseName = fileNameStr.substring(0, dotIndex);
            extension = fileNameStr.substring(dotIndex);
        } else {
            baseName = fileNameStr; // 没有扩展名
        }

        // 构建新的文件名
        String newFileName = baseName + part + extension;

        // 获取文件所在的目录
        Path parent = path.getParent();

        // 构建新的文件路径，如果parent为空，则保持原路径不变
        Path newPath =
                (parent != null) ? parent.resolve(newFileName) : Paths.get(newFileName);

        return newPath.toString();
    }

    /**
     * 在文件名后添加指定的字符串，保留文件路径和扩展名
     *
     * @param filePath 原始文件路径
     * @param part     要添加的字符串
     * @return 修改后的文件路径
     */
    public static String removePostfixToFileName(String filePath, String part) {
        // 将文件路径字符串转换为Path对象
        Path path = Paths.get(filePath);

        // 获取文件名
        Path fileName = path.getFileName();
        if (fileName == null) {
            return filePath;  // 如果没有文件名，则直接返回原始路径
        }

        String fileNameStr = fileName.toString();
        // 分割文件名和扩展名
        int dotIndex = fileNameStr.lastIndexOf('.');
        String baseName, extension = "";

        if (dotIndex > 0) {
            baseName = fileNameStr.substring(0, dotIndex);
            extension = fileNameStr.substring(dotIndex);
        } else {
            baseName = fileNameStr; // 没有扩展名
        }

        // 构建新的文件名
        if (baseName.endsWith(part)) {
            baseName = baseName.substring(0, baseName.length() - part.length());
        }

        String newFileName = baseName + extension;

        // 获取文件所在的目录
        Path parent = path.getParent();

        // 构建新的文件路径，如果parent为空，则保持原路径不变
        Path newPath =
                (parent != null) ? parent.resolve(newFileName) : Paths.get(newFileName);

        return newPath.toString();
    }

    public static String changeExtension(String filePathString,
                                         String newExtension) {
        Path filePath = Paths.get(filePathString);
//    if (!Files.exists(filePath)) {
//      log.warn("文件不存在: {}", filePath.toAbsolutePath());
//      return null;
//    }

        String originalFileName = filePath.getFileName().toString();
        String newFileName =
                getFileNameWithoutExtension(originalFileName) + "." + newExtension;

        Path newFilePath = filePath.resolveSibling(newFileName);

        //log.info("新的文件名已生成，但文件未被修改: {}", newFilePath.toAbsolutePath());
        return newFilePath.toAbsolutePath().toString(); // 返回新的绝对路径，但不做实际重命名

    }

    public static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 获取不带扩展名的文件名
     */
    public static String getPureFileNameWithoutExtensionWithPath(
            String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName; // 如果没有点，直接返回
        }
        return fileName.substring(0, lastDotIndex);
    }

    public static List<SubtitleEntity> readSrtFileContent(String... fileName) {
        if (fileName == null || fileName.length == 0 || StrUtil.isBlank(fileName[0])) {
            return null;
        }

        List<String> stringList;
        try {
            // 使用 Hutool 的 FileUtil.readLines，它能正确处理编码和行，代码更简洁
            // 它会读取所有行，包括空行，这正是 getSubtitleEntityList 所期望的
            stringList = FileUtil.readLines(fileName[0], StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取SRT文件失败: {}", fileName[0], e);
            // 抛出运行时异常，以便上层调用者知道发生了严重错误
            throw new RuntimeException("读取SRT文件失败: " + fileName[0], e);
        }

        // 直接将读取到的行列表传递给解析方法
        return getSubtitleEntityList(stringList);
    }

//  public static List<YoutubeInfoEntity> getYoutubeVideoInfoEntityList() {
//    String fileName =
//      CdFileUtil.getResourceRealPath() + File.separator + "youtube"
//        + File.separator + "my_video_ids.txt";
//
//    return getYoutubeInfoEntityList(fileName);
//  }

    public static List<YoutubeInfoEntity> getTodoYoutubeVideoInfoEntityList() {
        String fileName =
                CdFileUtil.getResourceRealPath() + File.separator + "youtube"
                        + File.separator + "yt_todo.txt";

        return getYoutubeInfoEntityList(fileName);
    }

    public static @NotNull List<YoutubeInfoEntity> getYoutubeInfoEntityList(
            String fileName) {
        List<YoutubeInfoEntity> result = new ArrayList<>();
        List<String> stringList = FileUtil.readLines(fileName,
                StandardCharsets.UTF_8);

        YoutubeInfoEntity subtitleBaseEntity;
        if (CollectionUtils.isNotEmpty(stringList)) {
            for (String s : stringList) {
                if (!StrUtil.isEmpty(s)) {
                    subtitleBaseEntity = new YoutubeInfoEntity();
                    String[] split = s.split("\\|");
                    if (split.length == 8) {
                        subtitleBaseEntity.setCategory(split[0]);
                        subtitleBaseEntity.setDateString(split[1]);
                        subtitleBaseEntity.setRawVideoId(split[2]);
                        subtitleBaseEntity.setMyVideoId(split[3]);
                        subtitleBaseEntity.setTitle(split[4]);
                        subtitleBaseEntity.setHeadTitle(split[5]);
                        subtitleBaseEntity.setSubTitle(split[6]);
                        subtitleBaseEntity.setMainTitle(split[7]);
                        result.add(subtitleBaseEntity);
                    } else {
                        log.error("数据格式不对2, {}", s);
                    }
                }
            }
        }
        return result;
    }

    public static List<YoutubeVideoSplitEntity> getYoutubeVideoSplitEntityList() {
        String fileName =
                CdFileUtil.getResourceRealPath() + File.separator + "youtube"
                        + File.separator + "yt_split.txt";

        return getYoutubeVideoSplitEntityList(fileName);
    }

    public static boolean emptyYoutubeVideoSplitFile() {
        String fileName =
                CdFileUtil.getResourceRealPath() + File.separator + "youtube"
                        + File.separator + "yt_split.txt";

        return CdFileUtil.clearFileContent(fileName);
    }

    public static @NotNull List<YoutubeVideoSplitEntity> getYoutubeVideoSplitEntityList(
            String fileName) {
        List<YoutubeVideoSplitEntity> result = new ArrayList<>();
        List<String> stringList = FileUtil.readLines(fileName,
                StandardCharsets.UTF_8);

        YoutubeVideoSplitEntity youtubeVideoSplitEntity;
        if (CollectionUtils.isNotEmpty(stringList)) {
            for (String s : stringList) {
                if (!StrUtil.isEmpty(s)) {
                    youtubeVideoSplitEntity = new YoutubeVideoSplitEntity();
                    String[] split = s.split("\\|");
                    if (split.length == 3) {
                        youtubeVideoSplitEntity.setCategory(split[0]);
                        youtubeVideoSplitEntity.setDateString(split[1]);
                        youtubeVideoSplitEntity.setTimeStr(split[2]);
                        result.add(youtubeVideoSplitEntity);
                    } else {
                        log.error("数据格式不对1, {}", s);
                    }
                }
            }
        }
        return result;
    }

//  public static @NotNull List<ThumbnailInfoEntity> getThumbnailInfoEntityList() {
//    String fileName =
//      CdFileUtil.getResourceRealPath() + File.separator + "youtube"
//        + File.separator + "thumbnail_info.txt";
//
//    return getThumbnailInfoEntityList(fileName);
//  }


//  public static @NotNull List<ThumbnailInfoEntity> getThumbnailInfoEntityList(
//    String fileName) {
//    List<ThumbnailInfoEntity> result = new ArrayList<>();
//    List<String> stringList = FileUtil.readLines(fileName,
//      StandardCharsets.UTF_8);
//
//    ThumbnailInfoEntity thumbnailInfoEntity;
//    if (CollectionUtils.isNotEmpty(stringList)) {
//      for (String s : stringList) {
//        if (!StrUtil.isEmpty(s)) {
//          thumbnailInfoEntity = new ThumbnailInfoEntity();
//          String[] split = s.split("\\|");
//          if (split.length == 5) {
//            thumbnailInfoEntity.setCategory(split[0]);
//            thumbnailInfoEntity.setDateString(split[1]);
//            thumbnailInfoEntity.setHeadTitle(split[2]);
//            thumbnailInfoEntity.setSubTitle(split[3]);
//            thumbnailInfoEntity.setMainTitle(split[4]);
//            result.add(thumbnailInfoEntity);
//          } else {
//            log.error("数据格式不对, {}", s);
//          }
//        }
//      }
//    }
//    return result;
//  }

    public static @NotNull List<SubtitleEntity> getSubtitleEntityList(
            List<String> stringList) {
        List<SubtitleEntity> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(stringList)) {
            return result;
        }

        // 确保文件末尾有空行，以便处理最后一个字幕块
        if (StrUtil.isNotBlank(stringList.get(stringList.size() - 1))) {
            stringList.add("");
        }

        List<String> currentBlock = new ArrayList<>();
        for (String line : stringList) {
            if (StrUtil.isBlank(line)) {
                // 当遇到空行时，处理之前收集的字幕块
                if (currentBlock.size() >= 3) { // 一个有效的块至少有序号、时间和一行文本
                    try {
                        SubtitleEntity subtitleEntity = new SubtitleEntity();
                        // 1. 序号
                        subtitleEntity.setSubIndex(Integer.parseInt(processStr(currentBlock.get(0))));
                        // 2. 时间
                        subtitleEntity.setTimeStr(processStr(currentBlock.get(1)));
                        // 3. 字幕文本 (可能有多行)
                        String subtitleText = currentBlock.stream()
                                .skip(2) // 跳过序号和时间行
                                .map(CdFileUtil::processStr)
                                .collect(Collectors.joining("\n")); // 多行文本用换行符连接

                        // 只有当字幕文本不为空时才添加
                        if (StrUtil.isNotBlank(subtitleText)) {
                            subtitleEntity.setSubtitle(subtitleText);
                            result.add(subtitleEntity);
                        } else {
                            subtitleEntity.setSubtitle(" "); // 为了双语字幕，补上空格
                            result.add(subtitleEntity);
                        }

                    } catch (NumberFormatException e) {
                        log.error("SRT字幕块解析失败，序号非数字: {}", currentBlock.get(0));
                    } catch (IndexOutOfBoundsException e) {
                        log.error("SRT字幕块解析失败，格式不完整: {}", currentBlock);
                    }
                }
                // 重置块，准备下一个
                currentBlock.clear();
            } else {
                // 非空行，加入当前块
                currentBlock.add(line);
            }
        }

        return result;
    }

    public static @NotNull List<SubtitleEntity> genSubtitleEntityList(
            List<String> stringList, String platformName) {
        List<SubtitleEntity> result = new ArrayList<>();
        int firstSpaceIndex = 0;
//    String subIndexStr = "";
        String subIndexStr = "";
//    String subIndexStr = "";
        SubtitleEntity subtitleBaseEntity;
//    // 如果最后一个字符串不为空，则补一个空字符串到列表中，以便处理最后一个字幕条目
//    if (StrUtil.isNotEmpty(stringList.get(stringList.size() - 1))) {
//      stringList.add("");
//    }
        // 移除stringList 的空行
        stringList = stringList.stream().filter(s -> !StrUtil.isBlankIfStr(s))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(stringList)) {
            int size = stringList.size();
            switch (platformName) {
                case CdConstants.TRANSLATE_PLATFORM_GEMINI:
                    // 如果字符串的个数不是2的倍数，则直接返回空列表
                    if (size % 2 != 0) {
                        log.warn("字符串的个数不是2的倍数，则直接返回空列表，{}", size);
                        return result;
                    }

                    for (int i = 0; i < stringList.size(); i += 2) {
                        subtitleBaseEntity = new SubtitleEntity();
                        subtitleBaseEntity.setSubtitle(
                                processStr(stringList.get(i)));
                        subtitleBaseEntity.setSecondSubtitle(
                                processStr(stringList.get(i + 1)));
                        result.add(subtitleBaseEntity);
                    }
                    break;
                case CdConstants.TRANSLATE_PLATFORM_MSTTS:
                    for (int i = 0; i < stringList.size(); i++) {
                        subtitleBaseEntity = new SubtitleEntity();
                        subtitleBaseEntity.setSubtitle(
                                processStr(stringList.get(i)));
                        result.add(subtitleBaseEntity);
                    }
                    break;
                default:
                    // 如果最后一个字符串不为空，则补一个空字符串到列表中，以便处理最后一个字幕条目
                    if (StrUtil.isNotEmpty(stringList.get(size - 1))) {
                        stringList.add("");
                    }
            }


        }
        return result;
    }

    public static final String UTF8_BOM = "\uFEFF";

    public static String processStr(String string) {
        if (string.startsWith(UTF8_BOM)) {
            return string.substring(1);
        }
        return string;
    }

    public static boolean isFileEmpty(String targetPath) {
        File file = new File(targetPath);

        return !file.exists() || file.length() == 0;
    }


    /**
     * 清空指定路径的文本文件内容。 如果文件不存在，此方法通常会创建一个新文件（取决于 PrintWriter 的行为和权限）。
     * 如果路径指向一个目录或无写入权限，会抛出 IOException。
     *
     * @param filePath 要清空内容的文件路径
     */
    public static boolean clearFileContent(String filePath) {
        // 参数校验（可选但推荐）
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("错误：文件路径不能为空。");
            return true;
        }

        // 使用 try-with-resources 确保 PrintWriter 被正确关闭
        // PrintWriter 的构造函数 PrintWriter(String fileName) 在默认情况下
        // 会打开文件用于写入，如果文件已存在，则会先清空（覆盖）文件内容。
        try (PrintWriter writer = new PrintWriter(filePath)) {
            // 不需要写入任何内容，打开流的动作已经清空了文件
            // writer.print(""); // 可以选择性地写一个空字符串，但不是必需的
            log.info("文件内容已清空: {}", filePath);
            return true;
        } catch (FileNotFoundException e) {
            // PrintWriter 构造函数可能抛出 FileNotFoundException
            // 这通常发生在路径无效或无法创建/打开文件时（如权限问题或路径是目录）
//      System.err.println("错误：无法找到或打开文件（可能是权限问题或路径是目录）: " + filePath);
//      e.printStackTrace();
            log.error("错误：无法找到或打开文件（可能是权限问题或路径是目录）: {}",
                    filePath);
            return false;
        }
    }

    /**
     * 根据文件名生成新的文件名，去掉最后的下划线加temp。
     *
     * @param inputFilePath 输入文件路径
     * @return 生成的新文件名
     */
    public static String generateOutputFilePath(String inputFilePath) {
        File inputFile = new File(inputFilePath);
        String fileName = inputFile.getName();
        String parentPath = inputFile.getParent(); // 获取父目录

        // 移除文件名最后的 "_temp"
        String newFileName = fileName.replace("_temp", "");  // 更简单的方法

        // 构建新的文件路径
        if (parentPath != null) {
            return parentPath + File.separator + newFileName;
        } else {
            return newFileName; // 如果没有父目录，直接返回文件名
        }
    }

    /**
     * 获取指定目录下第一层文件夹列表
     *
     * @param directoryPath 目录路径
     * @return 第一层文件夹列表，如果目录不存在或为空，则返回null
     */
    public static List<File> getFirstLevelDirectories(String directoryPath) {
        File directory = new File(directoryPath);

        // 检查目录是否存在并且是目录
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("指定的路径不存在或不是一个目录。");
            return null;
        }

        // 使用 CdFileUtil.ls(String) 获取所有文件和文件夹，然后过滤出文件夹
        File[] filesArray = FileUtil.ls(directoryPath);  // 使用 String 路径，返回 File[]

        // 修复空目录的校验逻辑
        if (filesArray == null || filesArray.length == 0) {
            System.out.println("该目录为空。");
            return null;
        }

        // 将 File[] 转换为 List<File>
        List<File> files = Arrays.asList(filesArray);

        // 过滤出文件夹
        return files.stream()
                .filter(File::isDirectory)
                .collect(Collectors.toList());
    }

    public static List<File> getDirectSubdirectories(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + directory);
        }

        File[] files = FileUtil.ls(directory.getAbsolutePath());
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
//        String filePath = "D:\\0000\\EnBook001\\900\\900V1_ch0201.txt";
//    String filePath = "900V1_ch0201.txt";
//    String newFilePath = CdFileUtil.addPostfixToFileName(filePath, "_part01");
//    System.out.println("原始文件路径: " + filePath);
//    System.out.println("修改后的文件路径: " + newFilePath);
//
//    String filePath2 = "D:/0000/EnBook001/900/900V1_ch0201.txt";
//    String newFilePath2 = CdFileUtil.addPostfixToFileName(filePath2, "_part01");
//    System.out.println("原始文件路径2: " + filePath2);
//    System.out.println("修改后的文件路径2: " + newFilePath2);

        String inputFilePath = "D:\\0000\\EnBook002\\Chapter007\\Chapter007_temp.txt"; // 替换为你的输入文件路径
        String outputFilePath = com.coderdream.util.cd.CdFileUtil.generateOutputFilePath(
                inputFilePath);
        System.out.println("新的文件路径: " + outputFilePath);

    }
}
