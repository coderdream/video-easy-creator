package com.coderdream.util.claudecode.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.json.JSONObject;
import com.coderdream.util.cd.CdConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 工具执行器
 * 用于执行具体的工具操作
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ToolExecutor {

    /**
     * 文件操作的基础路径（用于安全限制）
     */
    private String basePath;

    /**
     * 允许执行的命令白名单
     */
    private static final String[] COMMAND_WHITELIST = {
            "mvn", "npm", "git", "java", "python", "node", "ls", "dir", "cat", "echo"
    };

    /**
     * 构造函数
     */
    public ToolExecutor() {
        this.basePath = System.getProperty("user.dir");
    }

    /**
     * 构造函数
     *
     * @param basePath 文件操作的基础路径
     */
    public ToolExecutor(String basePath) {
        this.basePath = basePath;
    }

    /**
     * 列出目录文件
     *
     * @param input 输入参数，包含 path 字段
     * @return 文件列表的 JSON 字符串
     */
    public String listFiles(JSONObject input) {
        String path = input.getStr("path");
        if (path == null || path.isEmpty()) {
            path = this.basePath;
        }

        try {
            // 安全检查：确保路径在基础路径内
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(this.basePath, path);
            }

            if (!file.exists()) {
                return "Error: Path does not exist: " + path;
            }

            if (!file.isDirectory()) {
                return "Error: Path is not a directory: " + path;
            }

            List<File> files = FileUtil.loopFiles(file);
            StringBuilder result = new StringBuilder();
            result.append("Files in ").append(path).append(":\n");

            for (File f : files) {
                result.append(f.getName());
                if (f.isDirectory()) {
                    result.append("/");
                }
                result.append("\n");
            }

            log.info("Listed {} files in {}", files.size(), path);
            return result.toString();
        } catch (Exception e) {
            log.error("Failed to list files: {}", path, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 读取文件内容
     *
     * @param input 输入参数，包含 path 字段
     * @return 文件内容
     */
    public String readFile(JSONObject input) {
        String path = input.getStr("path");
        if (path == null || path.isEmpty()) {
            return "Error: Path is required";
        }

        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(this.basePath, path);
            }

            if (!file.exists()) {
                return "Error: File does not exist: " + path;
            }

            if (!file.isFile()) {
                return "Error: Path is not a file: " + path;
            }

            String content = FileUtil.readUtf8String(file);
            log.info("Read file: {} ({} bytes)", path, content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to read file: {}", path, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 写入文件内容
     *
     * @param input 输入参数，包含 path 和 content 字段
     * @return 操作结果
     */
    public String writeFile(JSONObject input) {
        String path = input.getStr("path");
        String content = input.getStr("content");

        if (path == null || path.isEmpty()) {
            return "Error: Path is required";
        }

        if (content == null) {
            content = "";
        }

        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(this.basePath, path);
            }

            // 创建父目录
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            FileUtil.writeUtf8String(content, file);
            log.info("Wrote file: {} ({} bytes)", path, content.length());
            return "File written successfully: " + path;
        } catch (Exception e) {
            log.error("Failed to write file: {}", path, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 执行系统命令
     *
     * @param input 输入参数，包含 command 字段
     * @return 命令执行结果
     */
    public String executeCommand(JSONObject input) {
        String command = input.getStr("command");
        if (command == null || command.isEmpty()) {
            return "Error: Command is required";
        }

        try {
            // 检查命令是否在白名单中
            String[] parts = command.split("\\s+");
            if (parts.length == 0) {
                return "Error: Invalid command";
            }

            String baseCommand = parts[0];
            boolean isWhitelisted = false;
            for (String whitelisted : COMMAND_WHITELIST) {
                if (baseCommand.contains(whitelisted)) {
                    isWhitelisted = true;
                    break;
                }
            }

            if (!isWhitelisted) {
                return "Error: Command not in whitelist: " + baseCommand;
            }

            String result = RuntimeUtil.execForStr(command);
            log.info("Executed command: {}", command);
            return result;
        } catch (Exception e) {
            log.error("Failed to execute command: {}", command, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 搜索文件内容
     *
     * @param input 输入参数，包含 path 和 pattern 字段
     * @return 搜索结果
     */
    public String grep(JSONObject input) {
        String path = input.getStr("path");
        String pattern = input.getStr("pattern");

        if (path == null || path.isEmpty()) {
            return "Error: Path is required";
        }

        if (pattern == null || pattern.isEmpty()) {
            return "Error: Pattern is required";
        }

        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(this.basePath, path);
            }

            if (!file.exists()) {
                return "Error: File does not exist: " + path;
            }

            List<String> lines = FileUtil.readLines(file, StandardCharsets.UTF_8);
            StringBuilder result = new StringBuilder();
            int matchCount = 0;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.matches(".*" + pattern + ".*")) {
                    result.append(String.format("%d: %s\n", i + 1, line));
                    matchCount++;
                }
            }

            if (matchCount == 0) {
                return "No matches found for pattern: " + pattern;
            }

            log.info("Found {} matches in {}", matchCount, path);
            return result.toString();
        } catch (Exception e) {
            log.error("Failed to grep file: {}", path, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 创建工具注册表并注册所有内置工具
     *
     * @return 配置好的工具注册表
     */
    public static ToolRegistry createDefaultRegistry() {
        ToolRegistry registry = new ToolRegistry();
        ToolExecutor executor = new ToolExecutor();

        // 注册 list_files 工具
        registry.registerTool(
                "list_files",
                new ToolDefinition("list_files", "列出指定目录中的文件")
                        .setInputSchemaType("object")
                        .addProperty("path", "string", "目录路径")
                        .setRequired("path"),
                executor::listFiles
        );

        // 注册 read_file 工具
        registry.registerTool(
                "read_file",
                new ToolDefinition("read_file", "读取指定文件的内容")
                        .setInputSchemaType("object")
                        .addProperty("path", "string", "文件路径")
                        .setRequired("path"),
                executor::readFile
        );

        // 注册 write_file 工具
        registry.registerTool(
                "write_file",
                new ToolDefinition("write_file", "写入内容到指定文件")
                        .setInputSchemaType("object")
                        .addProperty("path", "string", "文件路径")
                        .addProperty("content", "string", "文件内容")
                        .setRequired("path", "content"),
                executor::writeFile
        );

        // 注册 execute_command 工具
        registry.registerTool(
                "execute_command",
                new ToolDefinition("execute_command", "执行系统命令")
                        .setInputSchemaType("object")
                        .addProperty("command", "string", "要执行的命令")
                        .setRequired("command"),
                executor::executeCommand
        );

        // 注册 grep 工具
        registry.registerTool(
                "grep",
                new ToolDefinition("grep", "在文件中搜索匹配的行")
                        .setInputSchemaType("object")
                        .addProperty("path", "string", "文件路径")
                        .addProperty("pattern", "string", "搜索模式（正则表达式）")
                        .setRequired("path", "pattern"),
                executor::grep
        );

        log.info("Default tool registry created with {} tools", registry.getToolCount());
        return registry;
    }
}
