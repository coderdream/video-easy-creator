package com.coderdream.util.txt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*; // 使用 nio 包，更现代和推荐
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * 文件操作工具类
 */
public class FileUtils {

    // 私有构造函数，防止外部实例化工具类
    private FileUtils() {
    }

    /**
     * 在指定基础路径下的所有直接子文件夹中创建或覆盖一个名为 temp.txt 的文件，
     * 并写入指定内容。
     *
     * @param basePathString 目标基础目录的路径字符串 (例如 "D:\\14_LearnEnglish\\6MinuteEnglish\\2025")
     * @param fileName 要创建的文件名 (例如 "temp.txt")
     * @param content 要写入文件的文本内容 (例如 "hello world!")
     */
    public static void addTextFileToSubfolders(String basePathString, String fileName, String content) {
        // 1. 将字符串路径转换为 Path 对象
        Path basePath = Paths.get(basePathString);

        // 2. 检查基础路径是否存在且是一个目录
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            System.err.println("错误：指定的基础路径不存在或不是一个有效的目录: " + basePathString);
            return; // 路径无效，直接返回
        }

        System.out.println("开始处理目录: " + basePath);

        // 3. 使用 try-with-resources 确保 DirectoryStream 被正确关闭
        //    Files.list() 返回一个 Stream<Path>，代表目录中的条目
        try (Stream<Path> stream = Files.list(basePath)) {
            stream
                // 4. 筛选出 basePath 下的直接子目录
                .filter(Files::isDirectory) // 方法引用，等同于 path -> Files.isDirectory(path)
                // 5. 对每个子目录执行操作
                .forEach(subDirectory -> {
                    // 构造目标文件的完整路径
                    Path targetFilePath = subDirectory.resolve(fileName); // 例如 D:\...\2025\Subfolder1\temp.txt

                    try {
                        // 6. 写入文件内容
                        // Files.writeString 提供了一种简洁的方式写入文本文件
                        // StandardOpenOption.CREATE: 如果文件不存在则创建
                        // StandardOpenOption.TRUNCATE_EXISTING: 如果文件已存在，则清空内容再写入（覆盖）
                        // StandardOpenOption.WRITE: 以写入模式打开文件
                        Files.writeString(targetFilePath, content + System.lineSeparator(), StandardCharsets.UTF_8,
                                          StandardOpenOption.CREATE,
                                          StandardOpenOption.TRUNCATE_EXISTING,
                                          StandardOpenOption.WRITE);

                        System.out.println("  成功在子目录 '" + subDirectory.getFileName() + "' 中创建/更新文件: " + targetFilePath.getFileName());

                    } catch (IOException e) {
                        // 处理在特定子目录创建/写入文件时可能发生的 IO 异常
                        System.err.println("  错误：无法在子目录 '" + subDirectory.getFileName() + "' 中创建/写入文件 " + targetFilePath.getFileName() + " - 原因: " + e.getMessage());
                    }
                });

        } catch (IOException e) {
            // 处理列出基础目录内容时可能发生的 IO 异常
            System.err.println("错误：无法列出基础目录 '" + basePathString + "' 的内容 - 原因: " + e.getMessage());
        }

        System.out.println("处理完成。");
    }

    /**
     * 在指定年份文件夹下创建所有周四的文件夹
     *
     * @param yearFolderPath 年份文件夹路径，如 "D:\\14_LearnEnglish\\6MinuteEnglish\\2026"
     * @param firstThursday 年份的第一个周四，格式yyMMdd，如 "260101" 代表2026-01-01
     */
    public static void createThursdayFolders(String yearFolderPath, String firstThursday) {
        // 1. 验证和解析参数
        Path basePath = Paths.get(yearFolderPath);

        // 2. 如果基础目录不存在，则创建
        if (!Files.exists(basePath)) {
            try {
                Files.createDirectories(basePath);
                System.out.println("创建年份文件夹: " + basePath);
            } catch (IOException e) {
                System.err.println("错误：无法创建年份文件夹 " + yearFolderPath + " - 原因: " + e.getMessage());
                return;
            }
        }

        // 3. 解析第一个周四日期
        // 格式：yyMMdd，如 260101
        if (firstThursday.length() != 6) {
            System.err.println("错误：第一个周四日期格式不正确，应为yyMMdd格式，如 260101");
            return;
        }

        try {
            int year = 2000 + Integer.parseInt(firstThursday.substring(0, 2)); // 26 -> 2026
            int month = Integer.parseInt(firstThursday.substring(2, 4));       // 01
            int day = Integer.parseInt(firstThursday.substring(4, 6));         // 01

            LocalDate currentThursday = LocalDate.of(year, month, day);

            // 4. 验证是否是周四
            if (currentThursday.getDayOfWeek() != DayOfWeek.THURSDAY) {
                System.err.println("警告：指定的日期 " + currentThursday + " 不是周四，实际是 " +
                                   currentThursday.getDayOfWeek());
            }

            // 5. 获取年份的最后一天
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            System.out.println("开始创建 " + year + " 年的周四文件夹...");
            System.out.println("起始日期: " + currentThursday);
            System.out.println("结束日期: " + endOfYear);

            int folderCount = 0;

            // 6. 循环创建每个周四的文件夹
            while (currentThursday.getYear() == year && !currentThursday.isAfter(endOfYear)) {
                // 生成文件夹名称，格式：yyMMdd
                String folderName = String.format("%02d%02d%02d",
                    currentThursday.getYear() % 100,  // 2026 -> 26
                    currentThursday.getMonthValue(),
                    currentThursday.getDayOfMonth());

                Path thursdayFolder = basePath.resolve(folderName);

                // 创建文件夹
                try {
                    if (!Files.exists(thursdayFolder)) {
                        Files.createDirectories(thursdayFolder);
                        System.out.println("  创建文件夹: " + folderName + " (" + currentThursday + ")");
                        folderCount++;
                    } else {
                        System.out.println("  文件夹已存在: " + folderName + " (" + currentThursday + ")");
                    }
                } catch (IOException e) {
                    System.err.println("  错误：无法创建文件夹 " + folderName + " - 原因: " + e.getMessage());
                }

                // 移动到下一个周四（+7天）
                currentThursday = currentThursday.plusWeeks(1);
            }

            System.out.println("处理完成，共创建 " + folderCount + " 个周四文件夹。");

        } catch (NumberFormatException e) {
            System.err.println("错误：日期解析失败 - " + e.getMessage());
        } catch (DateTimeException e) {
            System.err.println("错误：无效的日期 - " + e.getMessage());
        }
    }

    /**
     * 主方法，用于演示和执行工具类方法
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // --- 示例1：在子文件夹中创建文本文件 ---
         String targetBaseDirectory = "D:\\14_LearnEnglish\\6MinuteEnglish\\2026";
         String targetFileName = "temp.txt";
         String fileContent = "hello world!";
         addTextFileToSubfolders(targetBaseDirectory, targetFileName, fileContent);

//        // --- 示例2：创建2026年所有周四的文件夹 ---
//        String yearFolder = "D:\\14_LearnEnglish\\6MinuteEnglish\\2026";
//        String firstThursday = "260101";  // 2026-01-01
//        createThursdayFolders(yearFolder, firstThursday);
    }
}
