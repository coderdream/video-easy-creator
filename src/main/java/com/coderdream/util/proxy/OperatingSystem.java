package com.coderdream.util.proxy;

import java.io.File;

import static com.coderdream.util.cd.CdConstants.KEYWORD_LINUX1;
import static com.coderdream.util.cd.CdConstants.KEYWORD_LINUX2;
import static com.coderdream.util.cd.CdConstants.KEYWORD_MAC1;
import static com.coderdream.util.cd.CdConstants.KEYWORD_MAC2;
import static com.coderdream.util.cd.CdConstants.KEYWORD_WINDOWS;
import static com.coderdream.util.cd.CdConstants.OS_LINUX;
import static com.coderdream.util.cd.CdConstants.OS_MAC;
import static com.coderdream.util.cd.CdConstants.OS_UNKNOWN;
import static com.coderdream.util.cd.CdConstants.OS_WINDOWS;

public class OperatingSystem {


  public static String getOS() {
    String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains(KEYWORD_WINDOWS)) {
      return OS_WINDOWS;
    } else if (osName.contains(KEYWORD_MAC1) || osName.contains(KEYWORD_MAC2)) {
      return OS_MAC;
    } else if (osName.contains(KEYWORD_LINUX1) || osName.contains(
      KEYWORD_LINUX2)) {
      return OS_LINUX;
    } else {
      return OS_UNKNOWN;
    }
  }

  public static String getSixMinutesBaseFolder() {
//        Integer proxyPort = null;
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:\\14_LearnEnglish\\6MinuteEnglish"
        + File.separator; // "D:\\14_LearnEnglish\\6MinuteEnglish\\";
      case OS_MAC -> folderPath = "/Volumes/System/0001" + File.separator;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getHistoryBBCFolder() {
    String osType = OperatingSystem.getOS();
    String folderPath = "";
    switch (osType) {
      case OS_WINDOWS -> folderPath = "C:\\Users\\CoderDream\\Videos\\History_BBC"
       ; // "D:\\14_LearnEnglish\\6MinuteEnglish\\";
      case OS_MAC -> folderPath = "/Volumes/System/Temp/History_BBC" ;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getBaiduSyncDiskFolder() {
    String osType = OperatingSystem.getOS();
    String folderPath = "";
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:\\14_LearnEnglish\\000_BBC\\BaiduSyncdisk\\000_BBC"      ;
      case OS_MAC -> folderPath = "/Volumes/System/Temp/Baidu_BBC" ;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  /**
   * @return
   */
  public static Integer getProxyPort() {
    Integer proxyPort = null;
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS ->
        proxyPort = 1080;// 7890;//7890; TODO singbox 为1080，看bat文件可以获取端口 clash 为7890
      case OS_MAC -> proxyPort = 1087;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return proxyPort;
  }

  /**
   * @return
   */
  public static String getFolderPath(String bookName) {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath =
        "D:" + File.separator + "0000" + File.separator + bookName;
      case OS_MAC -> folderPath =
        File.separator + "Volumes" + File.separator + "System" + File.separator
          + "0000" + File.separator + bookName;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getBaseGitHubFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:" + File.separator;
      case OS_MAC -> folderPath = "/Volumes/System" + File.separator;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  /**
   * 获取Hexo项目地址
   *
   * @return
   */
  public static String getGitHubCoderDreamHexoFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = ""; // "04_GitHub/hexo-project/Hexo-BlueLake-Blog/
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath =
        "D:\\04_GitHub\\hexo-project\\Hexo-BlueLake-Blog" + File.separator;
      case OS_MAC -> folderPath =
        "/Volumes/System/04_GitHub/Hexo-BlueLake-Blog" + File.separator;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  /**
   * 获取Hexo项目地址 D:\04_GitHub\hexo\half-hour-english
   *
   * @return 项目Hexo文件夹
   */
  public static String getHalfHourEnglishHexoFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = ""; // D:\04_GitHub\hexo\half-hour-english
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath =
        "D:\\04_GitHub\\hexo\\half-hour-english";
      case OS_MAC -> folderPath =
        "/Volumes/System/04_GitHub/half-hour-english";
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  /**
   * 获取Hexo项目地址
   *
   * @return
   */
  public static String getProjectResourcesFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = ""; // "04_GitHub/hexo-project/Hexo-BlueLake-Blog/
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath =
        "D:\\04_GitHub\\video-easy-creator\\src\\main\\resources";
      case OS_MAC -> folderPath =
        "/Volumes/System/04_GitHub/video-easy-creator/src/main/resources";
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getBaseFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:\\0000";
      case OS_MAC -> folderPath = "/Volumes/System/0000";
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getDiskFolder() {
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:";
      case OS_MAC -> folderPath = "/Volumes/System";
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  public static String getVideoBaseFolder() {
//        Integer proxyPort = null;
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String folderPath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> folderPath = "D:\\0000\\video" + File.separator;
      case OS_MAC ->
        folderPath = "/Users/coderdream/Documents" + File.separator;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return folderPath;
  }

  // "D:\\0000\\bgmusic\\page.wav";

  public static String getBaseFolderWav(String waveName) {
//        Integer proxyPort = null;
    String osType = OperatingSystem.getOS();
//        System.out.println("操作系统类型: " + osType);

    String wavePath = "";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS ->
        wavePath = "D:\\0000\\bgmusic" + File.separator + waveName;
      case OS_MAC ->
        wavePath = "/Volumes/System/0000/bgmusic" + File.separator + waveName;
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return wavePath;
  }

  public static String getPythonEnv() {
//        Integer proxyPort = null;
    String osType = OperatingSystem.getOS();
    System.out.println("操作系统类型: " + osType);

    String python = "python";
    // 可以根据操作系统类型执行不同的逻辑 export http_proxy=http://127.0.0.1:1087;export https_proxy=http://127.0.0.1:1087;export ALL_PROXY=socks5://127.0.0.1:1080
    switch (osType) {
      case OS_WINDOWS -> python = "python";
      case OS_MAC -> python = "python3";
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
    return python;
  }

  /**
   * 获取 Python 3.9 路径（用于 aeneas 模块）
   * 因为 aeneas 模块需要 Python 3.9 版本
   * @return Python 3.9 完整路径（Windows 下自动添加引号处理空格）
   */
  public static String getPython39Env() {
    String osType = OperatingSystem.getOS();
    System.out.println("操作系统类型: " + osType);

    String python39;
    // 根据操作系统返回对应的 Python 3.9 路径
    switch (osType) {
      case OS_WINDOWS -> {
        python39 = com.coderdream.util.cd.CdConstants.PYTHON39_PATH_WINDOWS;
        // 如果路径包含空格，则用引号包裹（Windows 命令行需要）
        if (python39.contains(" ")) {
          python39 = "\"" + python39 + "\"";
        }
      }
      case OS_MAC -> python39 = com.coderdream.util.cd.CdConstants.PYTHON39_PATH_MAC;
      case OS_LINUX -> python39 = com.coderdream.util.cd.CdConstants.PYTHON39_PATH_LINUX;
      default -> {
        System.out.println("无法识别的操作系统，使用默认 python3.9");
        python39 = "python3.9";
      }
    }
    return python39;
  }


  public static void main(String[] args) {
    String osType = OperatingSystem.getOS();
    System.out.println("操作系统类型: " + osType);

    // 可以根据操作系统类型执行不同的逻辑
    switch (osType) {
      case OS_WINDOWS -> System.out.println("执行 Windows 相关的操作...");
      case OS_MAC -> System.out.println("执行 Mac 相关的操作...");
      case OS_LINUX -> System.out.println("执行 Linux 相关的操作...");
      default -> System.out.println("无法识别的操作系统。");
    }
  }
}
