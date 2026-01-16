package com.coderdream.util;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

@Slf4j
public class RenameUtil {

    /**
     * 根据特定规则批量重命名文件夹中的文件。
     * 规则如下：
     * 1. 将文件名（不含扩展名）的最后四个字符移动到最前面。
     * 2. 将新生成的文件名从繁体中文转换为简体中文。
     *
     * @param folderPath 文件夹的绝对路径。
     * @param extension  要重命名的文件的扩展名（例如："mkv" 或 "webm"）。
     */
    public static void rename(String folderPath, String extension) {
        File folder = new File(folderPath);

        // 检查路径是否存在且为目录
        if (!folder.isDirectory()) {
            log.error("提供的路径不是一个有效的目录: {}", folderPath);
            return;
        }

        // 使用 lambda 表达式根据指定的扩展名过滤文件
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith("." + extension));

        // 检查是否找到了匹配的文件
        if (files == null || files.length == 0) {
            log.info("在目录 '{}' 中没有找到扩展名为 '.{}' 的文件。", folderPath, extension);
            return;
        }

        for (File file : files) {
            String originalName = file.getName();
            // 使用 FilenameUtils 安全地获取基本名称和扩展名
            String baseName = FilenameUtils.getBaseName(originalName);
            String fileExtension = FilenameUtils.getExtension(originalName);

            // 去除文件名首尾可能存在的空格
            baseName = baseName.trim();

            // 1. 将基本名称的最后四个字符移动到前面
            String newBaseName;
            if (baseName.length() >= 4) {
                String lastFourChars = baseName.substring(baseName.length() - 4);
                String beginning = baseName.substring(0, baseName.length() - 4);
                newBaseName = lastFourChars + beginning;
            } else {
                // 如果文件名长度不足4，则不进行移动，只做后续的简繁转换
                log.warn("文件名 '{}' 长度小于4，跳过字符移动规则。", baseName);
                newBaseName = baseName;
            }

            // 2. 将新的基本名称从繁体转换为简体
            String simplifiedBaseName = ZhConverterUtil.toSimple(newBaseName);

            // 3. 构造最终的新文件名并执行重命名
            String newFileName = simplifiedBaseName + "." + fileExtension;
            File newFile = new File(folder.getAbsolutePath(), newFileName);

            // 如果新旧文件名相同，则无需重命名
            if (file.equals(newFile)) {
                log.info("文件名 '{}' 无需改动。", originalName);
                continue;
            }

            // 检查目标文件是否已存在，防止覆盖
            if (newFile.exists()) {
                log.error("重命名失败：文件 '{}' 已存在，无法将 '{}' 重命名。", newFileName, originalName);
                continue;
            }

            // 执行重命名操作
            if (file.renameTo(newFile)) {
                log.info("成功: '{}' -> '{}'", originalName, newFileName);
            } else {
                log.error("失败: 未能将 '{}' 重命名为 '{}'", originalName, newFileName);
            }
        }
    }

    public static void main(String[] args) {
        // 处理 .mkv 文件
        RenameUtil.rename("D:\\羽毛球38集(1)", "mkv");

// 处理 .webm 文件
        RenameUtil.rename("D:\\羽毛球38集(1)", "webm");
    }
}
