package com.coderdream.util.cd;

import com.coderdream.util.proxy.OperatingSystem;

import java.io.File;

/**
 * @author CoderDream
 */
public class CdConstants {

  public static String MIDDLE_POINT = "•";

  public static String URL_US_BASE = "https://apps.apple.com/us/app/";

  public static String URL_CN_BASE = "https://apps.apple.com/cn/app/";

  public static String URL_PLATFORM_IPHONE = "?platform=iphone";

  public static String SNAPSHOT_JPG_SUFFIX = "jpg 600w";
  public static String SNAPSHOT_JPG_2_SUFFIX = "jpg 643w";

  public static String SNAPSHOT_PNG_SUFFIX = "png 600w";

  public static String SNAPSHOT_PNG_2_SUFFIX = "png 643w";

  public static String APP_ICON_JPG_SUFFIX = "jpg 492w";
  public static String APP_ICON_JPG_2_SUFFIX = "jpg 460w";

  public static String APP_ICON_PNG_SUFFIX = "png 492w";
  public static String APP_ICON_2_PNG_SUFFIX = "png 460w";

  public static int BATCH_INSERT_UPDATE_ROWS = 25;
  public static int BATCH_UPDATE_ROWS = 1000;

  public static int BATCH_SNAPSHOT_ROWS = 100;

  public static String PPT_TEMPLATE_FILE_NAME = "D:\\04_GitHub\\video-easy-creator\\src\\main\\resources\\ppt\\6min_202501065.pptx";
  public static String PPT_TEMPLATE_FILE_PATH = "D:\\04_GitHub\\video-easy-creator\\src\\main\\resources\\ppt\\";

  public static String MIDDLE_DOT = "·";
  public static String MIDDLE_MINUS = "-";
  public static Integer SALARY_MONTH = 12;

//            switch (dictType) {
//        case "cambridge":
//            mdxFile = "E:\\BaiduPan\\0002_词典共享\\剑桥在线英汉双解词典完美版\\cdepe.mdx"; // 剑桥在线英汉双解词典完美版 400MB
//            break;
//        case "oaldpe":
//            mdxFile = "E:\\BaiduPan\\0002_词典共享\\牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx"; // 74MB
//            break;
//        case "maldpe":
//            mdxFile = "E:\\BaiduPan\\0002_词典共享\\韦氏高阶英汉双解词典2019完美版\\maldpe.mdx"; // 28MB
//            break;
//
//        case "c8":
//            mdxFile = "E:\\BaiduPan\\好用的词典~\\牛津高阶8简体spx\\牛津高阶8简体.mdx"; // 28MB
//            break;   //
//        case "collins":
//            mdxFile = "D:\\Download\\柯林斯COBUILD高阶英汉双解学习词典.mdx";
//            break;   //
//        default:
//            mdxFile = "D:\\Download\\柯林斯COBUILD高阶英汉双解学习词典.mdx";

  /**
   * 剑桥在线英汉双解词典完美版\\cdepe.mdx
   */
  public static String CAMBRIDGE = "cambridge";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String OALDPE = "oaldpe";


  /**
   * 柯林斯COBUILD高阶英汉双解学习词典
   */
  public static String DICT_COLLINS = "collins";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_OALD = "oald";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_OALDPE = "oaldpe";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_CDEPE = "cdepe";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_CDEPE2 = "cdepe";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_CDEPE4 = "cdepe";

  /**
   * 牛津高阶英汉双解词典第10版完美版\\oaldpe.mdx
   */
  public static String DICT_OALDPE3 = "oaldpe";

//  public static String SPEECH_KEY = System.getenv("SPEECH_KEY");

  /**
   * 美国西部
   */
  public static String SPEECH_REGION_EASTUS = "eastus"; // eastus2

  // Azure 配置信息
  /**
   * 美国西部
   */
  public static String SPEECH_KEY_EAST_US = "5OpUZB7whKyJ49kW1MIz4y58TE5KQtSdrZVS9FH24CMGlZElQCHbJQQJ99BCACYeBjFXJ3w3AAAYACOG9v36";// "50vWNO4RVL41CEjbkm4aT5c8VPBjO1XNQOMWEYgX3IrrKQn37XTTJQQJ99BCACYeBjFXJ3w3AAAYACOGjmML";// System.getenv("SPEECH_KEY_EAST_US");

  public static String SPEECH_KEY_EASTASIA = "5DMz134hNCG84o9RED7eRzNnKrxJQjeITI3qqg90FGf1BucMpvQPJQQJ99BCAC3pKaRXJ3w3AAAYACOGBL1z";//System.getenv(    "SPEECH_KEY_EASTASIA");

  /**
   * 东亚 eastasia
   */
  public static String SPEECH_REGION_EASTASIA = "eastasia";

  public static String SPEECH_VOICE_ZH_CN_XIAOCHEN = "zh-CN-XiaochenNeural";

  public static final String IMAGE_PATH = "src/main/resources/pic";
  public static final String AUDIO_CN_PATH = "src/main/resources/wav/cn";
  public static final String AUDIO_EN_PATH = "src/main/resources/wav/en";
//  public static final String OUTPUT_PATH = "src/main/resources/output_video/";
//  public static final String VIDEO_PATH = "src/main/resources/video/";
//  public static final String VIDEO_CN_PATH = "src/main/resources/video/cn/";
//  public static final String VIDEO_EN_PATH = "src/main/resources/video/en/";

  public static final String SAPI =  "sapi";

  // 百词斩标准起始时间字符串
  public static final String BAI_CI_ZAN_START_TIME = "2025-01-01 00:00:00";

  /**
   * 音频文件夹
   */
  public static final String AUDIO_FOLDER = "audio";
  /**
   * SSML文件夹
   */
  public static final String SSML_FOLDER = "ssml";

  /**
   * mix音频文件夹
   */
  public static final String AUDIO_MIX_FOLDER = "audio_mix";
  /**
   * 视频文件夹
   */
  public static final String VIDEO_FOLDER = "video";
  /**
   * cover文件夹
   */
  public static final String COVER_FOLDER = "cover";


  public static final String LANG_CN = "cn";
  public static final String LANG_EN = "en";
  public static final String LANG_EN_FAST = "en_fast";
  public static final String LANG_EN_SLOW = "en_slow";

  // CdConstants.LANG_EN      case CdConstants.LANG_EN -> {
  //        voiceName = "en-US-JennyNeural";
  public static final String DEFAULT_VOICE_NAME_EN = "en-US-JennyNeural";
  public static final String DEFAULT_VOICE_NAME_CN = "zh-CN-XiaochenNeural";

  public static final String PIC_TYPE_NO_SUBTITLE = "no_subtitle"; // 无字幕
  public static final String PIC_TYPE_EN = "en";
  public static final String PIC_TYPE_MIX = "mix";

  public static final String AUDIO_TYPE_WAV = "wav";
  public static final String AUDIO_TYPE_MP3 = "mp3";

  public static final String RESOURCES_BASE_PATH = "src/main/resources/";
  // 设置路径
  public static final String BACKGROUND_IMAGE_FILENAME = "background.png"; // 背景图片
  public static final String PIC_FOLDER = "pic"; // 输出目录

  public static final String YOUTUBE_API_KEY = System.getenv("YOUTUBE_API_KEY");

//  public static final String YOUTUBE_API_KEY = System.getenv("GOOGLE_API_KEY");

  public static final String GEMINI_API_KEY = "AIzaSyAIrJl7ngroAysbkhFPdsZNi3b3YBReIBI";// System.getenv("GEMINI_API_KEY");
  public static final String GROK_API_KEY = "xai-5KRGYNaegWzP3x1C4nxlHvV21WCmCufOYXnaz8FIZAi2k599bkwAQFc5Idq7IF0LKQjQ8c5mEyh9SXki";//System.getenv("GROK_API_KEY");
  public static final String NVIDIA_API_KEY = "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";
  public static final String CODEX_API_KEY = "cr_5f730e8a9831c932153764a2bd1b1f5bb1b77fc07f960e358a08688a7ba355fd";
  public static final String CODEX_API_BASE_URL = "https://gmn.chuangzuoli.cn/openai";
  public static final String CODEX_MODEL = "gpt-5-codex";

  // proxy-host 127.0.0.1
  public static final String PROXY_HOST = "127.0.0.1";

  // proxy-port 7890
//  public static final int PROXY_PORT = 7890;

  /**
   * "帮我实现如下功能：输入是是6组英文词汇，6行为1组；第1行英文单词或词组，第2行是英文释义，第1行和第2行不要处理，按原始文本返回，第3行是对第1行的中文简明翻译，翻译结果尽量少于20个字符串，第4行是对第2行的中文翻译，第5行是用第1行进行英文造句，第6行时对第5行进行中文翻译；请根据规则补齐空行，按文本文件格式返回给我，不要任何标记，移除空行，后面的5组也是按一样方式处理；以下是6组词汇：cephalopod\nthe
   * group of animals to which the octopus belongs\npublicity stunt\nsomething a
   * company might do to grab your attention and promote its products\na common
   * ancestor\na distant relative from which two different species
   * evolved\ncomparable to\nsimilar to\nvertebrates\nanimals that have a
   * spine\nprotean\n(adjective) adaptable and changeable";
   */
  public static final String VOC_CN_PREFIX = "帮我实现如下功能：输入是是6组英文词汇，6行为1组；"
    + "第1行英文单词或词组，第2行是英文释义，第1行和第2行不要处理，按原始文本返回，"
    + "第3行是对第1行的中文简明翻译，翻译结果尽量少于20个字符串，"
    + "第4行是对第2行的中文翻译，第4行很重要，请严格按照柯林斯英汉双解大词典或牛津双语词典的释义进行翻译，"
    + "第5行是用第1行进行英文造句，我是要考雅思的考生，请优先选用《柯林斯英汉双解大词典》或《牛津双语词典》中的较难的例句，"
    + "第6行是对第5行进行中文翻译；"
    + "再次强调，第2行按原文输出，不要修改；"
    + "请根据规则补齐空行，按文本文件格式返回给我，不要任何标记，移除空行；"
    + "后面的5组也是按一样方式处理；以下是6组词汇：";

  public static final String SRC_TRANSLATE_PREFIX = "帮我把英文字幕翻译成中文字幕，返回英中文双语字幕；"
    + "尽量通顺，前后文一致；"
    + "你返回内容一定要和我给你的匹配，给你100行，返回200行，英文放在上面，中文放在下面，一行英文，一行中文；"
    + "以下是英文字幕：";


  public static final String VOC_EN_PREFIX = "帮我实现如下功能：输入是是6组英文词汇，6行为1组；第1行英文单词或词组，第2行是英文释义，第1行和第2行不要处理，按原始文本返回，第3行是对第1行的中文简明翻译，翻译结果尽量少于20个字符串，第4行是对第2行的中文翻译，第5行是用第1行进行英文造句，我是要考雅思的考生，请优先选用《柯林斯英汉双解大词典》或《牛津双语词典》中的较难的例句，第6行时对第5行进行中文翻译；请根据规则补齐空行，按文本文件格式返回给我，不要任何标记，移除空行，后面的5组也是按一样方式处理；以下是6组词汇：";

  // pdf扩展名
  public static final String PDF_EXTENSION = ".pdf";
  // txt扩展名
  public static final String TXT_EXTENSION = ".txt";
  // mp3扩展名
  public static final String MP3_EXTENSION = ".mp3";
  // srt扩展名
  public static final String SRT_EXTENSION = ".srt";

  /**
   * A:联想；B、小米；C、戴尔；D、三星
   */
  public static final String TEMPLATE_FLAG = "D";

  public static Integer SINGLE_SCRIPT_LENGTH = 65;

  public static String GEN_PHONETICS_TEXT = "解析下面的文本，给每行英文句子加上音标，"
    + "放到下一行，给的n行英文句子，返回2*n行数据给我，"
    + "以Scene开头的句子也都要加上音标，放到下一行，"
    + "句子的音标只在句首句尾加斜线，中间不要有斜线。"
    + "待添加音标的句子文本如下：";

  public static String GEN_PHONETICS_TEXT_V2 = "解析下面的文本，给每行英文句子的音标，"
    + "不用管每一行有几句话，把整行的音标放到同一行，不要另起一行，"
    + "句子的音标只在句首句尾加斜线，中间不要有斜线，如果有多余的斜线，返回前先去掉；"
    + "返回的内容不要增加序号，也不要加其他字符，如单引号，撇号，`等等，每行都以斜线开头，斜线结尾，中间不包含斜线；"
    + "返回前先统计一下行数，如果与输入的行数不一致，则重新生成后再返回给我，直到行数一致为止。"
    + "待添加音标的英文句子文本如下：";


  public static final String OS_WINDOWS = "Windows";
  public static final String OS_MAC = "Mac";
  public static final String OS_LINUX = "Linux";
  public static final String OS_UNKNOWN = "Unknown";

  public static final String KEYWORD_WINDOWS = "windows";
  public static final String KEYWORD_MAC1 = "mac";
  public static final String KEYWORD_MAC2 = "darwin";
  public static final String KEYWORD_LINUX1 = "linux";
  public static final String KEYWORD_LINUX2 = "unix";

  public static final String CODERDREAM_POSTS_FOLDER =
    OperatingSystem.getGitHubCoderDreamHexoFolder() + "source" + File.separator
      + "_posts";
  // getHalfHourEnglishHexoFolder
  public static final String HALF_HOUR_ENGLISH_POSTS_FOLDER =
    OperatingSystem.getHalfHourEnglishHexoFolder() + File.separator + "source"
      + File.separator
      + "_posts";
  public static final String TRANSLATE_PLATFORM_GEMINI = "gemini";

  public static final String TRANSLATE_PLATFORM_DEEP_SEEK = "deep_seek";

  public static final String TRANSLATE_PLATFORM_MSTTS = "mstts";

  public static final String AI_PROVIDER_GEMINI = "gemini";
  public static final String AI_PROVIDER_NVIDIA = "nvidia";
  public static final String AI_PROVIDER_CODEX = "codex";

  public static final String SUBTITLE_EN = "en";

  public static final String SUBTITLE_ZH_CN = "zh-CN";

  public static final String SUBTITLE_ZH_TW = "zh-TW";

  public static final String SUBTITLE_EN_ZH_CN = "en-zh-CN";
  public static final String SUBTITLE_EN_ZH_TW = "en-zh-TW";

  /**
   * Python 3.9 路径配置（用于 aeneas 模块）
   * Windows: C:\\Program Files\\Python39\\python.exe
   * Mac: /usr/local/bin/python3.9
   * Linux: /usr/bin/python3.9
   */
  public static final String PYTHON39_PATH_WINDOWS = "C:\\Program Files\\Python39\\python.exe";
  public static final String PYTHON39_PATH_MAC = "/usr/local/bin/python3.9";
  public static final String PYTHON39_PATH_LINUX = "/usr/bin/python3.9";

  // ==================== Claude API 配置 ====================

  /**
   * Claude Opus 4.5 模型（最强编码能力）
   */
  public static final String CLAUDE_MODEL_OPUS_45 = "claude-opus-4-5-20251101";

  /**
   * Claude Sonnet 4.5 模型（平衡性能和成本）
   */
  public static final String CLAUDE_MODEL_SONNET_45 = "claude-sonnet-4-5-20250929";

  /**
   * Claude Haiku 4.5 模型（轻量级，快速响应）
   */
  public static final String CLAUDE_MODEL_HAIKU_45 = "claude-haiku-4-5-20251001";

  /**
   * 默认使用的 Claude 模型
   */
  public static final String CLAUDE_DEFAULT_MODEL = CLAUDE_MODEL_SONNET_45;

  /**
   * Claude API 最大输出 Token 数
   */
  public static final int CLAUDE_MAX_TOKENS = 4096;

  /**
   * Claude 思考模式预算（Token 数）
   */
  public static final int CLAUDE_THINKING_BUDGET = 2048;

  /**
   * Claude 努力程度：高（最深入的推理）
   */
  public static final String CLAUDE_EFFORT_HIGH = "high";

  /**
   * Claude 努力程度：中（平衡推理）
   */
  public static final String CLAUDE_EFFORT_MEDIUM = "medium";

  /**
   * Claude 努力程度：低（快速响应）
   */
  public static final String CLAUDE_EFFORT_LOW = "low";

  /**
   * Claude API 版本
   */
  public static final String CLAUDE_API_VERSION = "2023-06-01";

  /**
   * Claude API 调用超时时间（毫秒）
   */
  public static final int CLAUDE_API_TIMEOUT = 120000;

  /**
   * Claude API 最大重试次数
   */
  public static final int CLAUDE_MAX_RETRIES = 3;

  /**
   * Claude API 重试延迟（毫秒）
   */
  public static final int CLAUDE_RETRY_DELAY = 2000;

  /**
   * MiniMax API 基础 URL
   */
  public static final String MINIMAX_BASE_URL = "https://api.minimax.chat";

  /**
   * MiniMax API 调用超时时间（毫秒）
   */
  public static final int MINIMAX_API_TIMEOUT = 120000;

  /**
   * MiniMax API 最大重试次数
   */
  public static final int MINIMAX_MAX_RETRIES = 3;

  /**
   * MiniMax API 重试延迟（毫秒）
   */
  public static final int MINIMAX_RETRY_DELAY = 2000;

  /**
   * MiniMax 默认模型
   */
  public static final String MINIMAX_DEFAULT_MODEL = "abab6.5s-chat";

  /**
   * Claude 工具调用循环最大迭代次数
   */
  public static final int CLAUDE_TOOL_LOOP_MAX_ITERATIONS = 10;

}
