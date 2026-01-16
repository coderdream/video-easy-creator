package com.coderdream.util.cd;

import com.coderdream.util.cmd.CommandUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CdPythonUtil {

    public static void main(String[] args) {
        String pythonCommand = "python -c \"print('Hello, World!')\""; // Python命令
        // python -m aeneas.tools.execute_task audio.mp3 script_dialog_new.txt "task_language=eng|os_task_file_format=srt|is_text_type=plain" eng_raw.srt

        String folderPath = "D:\\14_LearnEnglish\\u11_frankenstein\\u11_frankenstein_episode1\\";
        String mp3FileName = folderPath + File.separator + "u11_frankenstein_episode1.mp3";
        String subtitleFileName = folderPath + File.separator + "u11_frankenstein_episode1_subtitle.txt";
        String srtFileName = folderPath + File.separator + "u11_frankenstein_episode1.srt";
        CdPythonUtil.executePythonCommand(mp3FileName, subtitleFileName, srtFileName);
    }

    public static File executePythonCommand(String mp3FileName, String subtitleFileName,
        String srtFileName) {
        try {
            // 使用 Python 3.9 以支持 aeneas 模块
            String python39 = com.coderdream.util.proxy.OperatingSystem.getPython39Env();
            String pythonCommand = python39 + " -m aeneas.tools.execute_task " + mp3FileName + " " + subtitleFileName
                + " \"task_language=eng|os_task_file_format=srt|is_text_type=plain\" "
                + srtFileName;
            log.info("Execute python command: {}", pythonCommand);
//            Process process = Runtime.getRuntime().exec(pythonCommand);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//            int exitCode = process.waitFor();
//            System.out.println("Exited with error code : " + exitCode);
            CommandUtil.executeCommand(pythonCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new File(srtFileName);
    }

    // ExecutePythonCommand
}
