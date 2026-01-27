package com.coderdream.util.network;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.cd.CdTimeUtil;
import com.coderdream.util.network.QuarkDiskUtil.QuarkDiskResponse.FileList;
import com.coderdream.util.proxy.OperatingSystem;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuarkDiskUtil {

  private static final String URL = "https://drive-pc.quark.cn/1/clouddrive/file/sort";
  private static final String SHARE_URL =
    "https://drive-pc.quark.cn/1/clouddrive/share?pr=ucpro&fr=pc&uc_param_str=";
  private static final String TASK_URL = "https://drive-pc.quark.cn/1/clouddrive/task";
  private static final String SHARE_PASSWORD_URL =
    "https://drive-pc.quark.cn/1/clouddrive/share/password?pr=ucpro&fr=pc&uc_param_str=";

  private static final int BEFORE_CALL_SLEEP_MS = 2000; // 3 秒
  private static final int BEFORE_RETRY_SLEEP_MS = 3000; // 5 秒

  /**
   * 文件排序参数对象
   */
  @Data
  public static class FileSortParams {

    private String pr = "ucpro";
    private String fr = "pc";
    private String uc_param_str = "";
    private String pdir_fid; // 移除固定值
    private int _page = 1;
    private int _size = 60; // 修改为 100
    private int _fetch_total = 1;
    private int _fetch_sub_dirs = 0;
    private String _sort = "file_type:asc,file_name:asc";
  }

  /**
   * 响应数据对象 - 简化示例，请根据实际情况调整
   */
  @Data
  public static class QuarkDiskResponse {

    private int status;
    private int code;
    private String message;
    private long timestamp;
    private Data data;
    private Metadata metadata;

    @lombok.Data
    public static class Data {

      private List<FileList> list;
    }

    @lombok.Data
    public static class FileList {

      private String fid;
      private String file_name;
      private String pdir_fid;
      private Integer category;
      private Integer file_type;
      private Integer size;
      private String format_type;
      private Integer status;
      private String tags;
      private Long l_created_at;
      private Long l_updated_at;
      private String source;
      private String file_source;
      private Integer name_space;
      private Long l_shot_at;
      private String source_display;
      private Integer include_items;
      private Boolean series_dir;
      private Boolean album_dir;
      private Boolean more_than_one_layer;
      private Boolean upload_camera_root_dir;
      private Double fps;
      private Integer like;
      private Long operated_at;
      private String sort_type;
      private String sort_range;
      private Integer risk_type;
      private Integer backup_sign;
      private Integer file_name_hl_start;
      private Integer file_name_hl_end;
      private FileStruct file_struct;
      private Integer duration;
      private Map<String, Object> event_extra;
      private String file_local_path;
      private String backup_file_local_path;
      private Integer scrape_status;
      private Boolean ban;
      private Boolean save_as_source;
      private Boolean backup_source;
      private Boolean offline_source;
      private Integer owner_drive_type_or_default;
      private Integer raw_name_space;
      private Integer cur_version_or_default;
      private Boolean dir;
      private Boolean file;
      private Long created_at;
      private Long updated_at;
      private Map<String, Object> _extra;
    }

    @lombok.Data
    public static class FileStruct {

      private String fir_source;
      private String sec_source;
      private String thi_source;
      private String platform_source;
    }

    @lombok.Data
    public static class Metadata {

      private Integer _size;
      private String req_id;
      private Integer _page;
      private Integer _count;
      private Integer _total;
      private Integer tq_gap;
    }
  }

  /**
   * 分享请求参数对象
   */
  @Data
  public static class ShareParams {

    private List<String> fid_list;
    private String title;
    private Integer url_type;
    private Integer expired_type;
  }

  @Data
  public static class ShareResponse {

    private int status;
    private int code;
    private String message;
    private long timestamp;
    private ShareData data;

    @Data
    public static class ShareData {

      private String task_id;
      private Boolean task_sync;
    }
  }

  @Data
  public static class TaskResponse {

    private int status;
    private int code;
    private String message;
    private long timestamp;
    private TaskData data;
    private QuarkDiskResponse.Metadata metadata;

    @Data
    public static class TaskData {

      private String task_id;
      private String event_id;
      private Integer task_type;
      private String task_title;
      private Integer status;
      private Long created_at;
      private Long finished_at;
      private String share_id;
      private Map<String, Object> share;
      private SaveAs save_as;
      private CreationSnapshot creation_snapshot;
    }

    @Data
    public static class SaveAs {

      private List<String> save_as_select_top_fids;
      private String is_pack;
      private List<String> save_as_top_fids;
    }

    @Data
    public static class CreationSnapshot {

      private Integer success_count;
      private Integer failed_count;
    }
  }

  @Data
  public static class SharePasswordParams {

    private String share_id;
  }

  @Data
  public static class SharePasswordResponse {

    private int status;
    private int code;
    private String message;
    private long timestamp;
    private SharePasswordData data;

    @Data
    public static class SharePasswordData {

      private String title;
      private String sub_title;
      private Integer share_type;
      private String pwd_id;
      private String share_url;
      private Integer url_type;
      private Integer expired_type;
      private Integer file_num;
      private Long expired_at;
      private FirstFile first_file;
      private String path_info;
      private Boolean partial_violation;
      private List<Integer> first_layer_file_categories;
      private Boolean download_pvlimited;
    }

    @Data
    public static class FirstFile {

      private String fid;
      private Integer category;
      private Integer file_type;
      private String format_type;
      private Integer name_space;
      private Boolean series_dir;
      private Boolean album_dir;
      private Boolean more_than_one_layer;
      private Boolean upload_camera_root_dir;
      private Double fps;
      private Integer like;
      private Integer risk_type;
      private Integer file_name_hl_start;
      private Integer file_name_hl_end;
      private Integer duration;
      private Integer scrape_status;
      private Boolean ban;
      private Integer cur_version_or_default;
      private Boolean save_as_source;
      private Boolean backup_source;
      private Boolean offline_source;
      private Integer owner_drive_type_or_default;
      private Boolean dir;
      private Boolean file;
      private Map<String, Object> _extra;
    }
  }

  /**
   * 从文件中读取请求头
   */
  private static Map<String, String> readHeadersFromFile(String filePath) {
    Map<String, String> headers = new HashMap<>();
    List<String> lines = FileUtil.readLines(new File(filePath),
      StandardCharsets.UTF_8);
    for (String line : lines) {
      if (StrUtil.isBlank(line)) {
        continue;
      }

      String key;
      String value;

      if (line.startsWith(":")) {
        // 特殊处理以冒号开头的行
        int firstColonIndex = line.indexOf(":", 1); // 查找第二个冒号的位置
        if (firstColonIndex > 0) {
          key = line.substring(0, firstColonIndex).trim(); // 提取 key
          value = line.substring(firstColonIndex + 1).trim(); // 提取 value
        } else {
          // log.error("Invalid header line (starts with ':' but no second colon): {}", line);
          continue;
        }
      } else {
        List<String> parts = StrUtil.split(line, ':', 2);
        if (parts == null || parts.size() != 2) {
          // log.error("Invalid header line: {}", line);
          continue; // 或者抛出异常
        }
        key = parts.get(0).trim();
        value = parts.get(1).trim();
      }

      if (headers.containsKey(key)) {
        // log.error("Duplicate header key: {}", key);
        continue;
      }
      headers.put(key, value);
    }
    return headers;
  }

  /**
   * 发起GET请求
   */
  public static QuarkDiskResponse getFileSort(FileSortParams params,
    String headerFilePath) {
    sleepBeforeCall(); // 休眠 3 秒
    Map<String, String> headers = readHeadersFromFile(headerFilePath);
    Map<String, Object> paramMap = BeanUtil.beanToMap(params);
    String url = URL + "?" + HttpUtil.toParams(paramMap); // 手动拼接URL参数

    // 打印请求详情
    log.info("========== GET File Sort Request ==========");
    log.info("Request URL: {}", url);
    log.info("Request Headers:");
    headers.forEach((key, value) -> log.info("  {} : {}", key, value));
    log.info("Request Params: {}", JSONUtil.toJsonStr(paramMap));

    HttpRequest request = HttpUtil.createGet(url);
    headers.forEach(request::header); // 添加请求头
    try (HttpResponse response = request.execute()) { // 使用 try-with-resources
      String body = response.body();

      // 打印响应详情
      log.info("========== GET File Sort Response ==========");
      log.info("Response Status: {}", response.getStatus());
      log.info("Response Headers:");
      response.headers().forEach((key, values) ->
        log.info("  {} : {}", key, String.join(", ", values)));
      log.info("Response Body: {}", body);
      log.info("===========================================");

      if (response.isOk()) {
        return JSONUtil.toBean(body, QuarkDiskResponse.class);
      } else {
        log.error("GET File Sort Request failed with status code: {}", response.getStatus());
        return null; // 或者抛出异常
      }
    } catch (Exception e) {
      log.error("GET File Sort Request error:{}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * 发起 POST 请求 (Share)
   */
  public static ShareResponse postShare(ShareParams params,
    String headerFilePath) {
    sleepBeforeCall(); // 休眠 3 秒
    Map<String, String> headers = readHeadersFromFile(headerFilePath);
    String jsonBody = JSONUtil.toJsonStr(params);

    // 打印请求详情
    log.info("========== POST Share Request ==========");
    log.info("Request URL: {}", SHARE_URL);
    log.info("Request Headers:");
    headers.forEach((key, value) -> log.info("  {} : {}", key, value));
    log.info("Request Body: {}", jsonBody);

    HttpRequest request = HttpUtil.createPost(SHARE_URL).body(jsonBody);
    headers.forEach(request::header);

    try (HttpResponse response = request.execute()) {
      String body = response.body();

      // 打印响应详情
      log.info("========== POST Share Response ==========");
      log.info("Response Status: {}", response.getStatus());
      log.info("Response Headers:");
      response.headers().forEach((key, values) ->
        log.info("  {} : {}", key, String.join(", ", values)));
      log.info("Response Body: {}", body);
      log.info("===========================================");

      if (response.isOk()) {
        return JSONUtil.toBean(body, ShareResponse.class);
      } else {
        log.error("POST Share Request failed with status code: {}", response.getStatus());
        return null;
      }
    } catch (Exception e) {
      log.error("POST Share Request error:{}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * 发起 GET 请求 (Task)，增加重试机制
   */
  public static TaskResponse getTask(String taskId, String headerFilePath) {
    int maxRetries = 10;
    for (int retry = 1; retry <= maxRetries; retry++) {
      Map<String, String> headers = readHeadersFromFile(headerFilePath);
      String url =
        TASK_URL
          + "?pr=ucpro&fr=pc&uc_param_str=&task_id="
          + taskId
          + "&retry_index="
          + retry;

      // 打印请求详情
      log.info("========== GET Task Request (Retry {}/{}) ==========", retry, maxRetries);
      log.info("Request URL: {}", url);
      log.info("Request Headers:");
      headers.forEach((key, value) -> log.info("  {} : {}", key, value));

      HttpRequest request = HttpUtil.createGet(url);
      headers.forEach(request::header);

      try (HttpResponse response = request.execute()) {
        String body = response.body();

        // 打印响应详情
        log.info("========== GET Task Response (Retry {}/{}) ==========", retry, maxRetries);
        log.info("Response Status: {}", response.getStatus());
        log.info("Response Headers:");
        response.headers().forEach((key, values) ->
          log.info("  {} : {}", key, String.join(", ", values)));
        log.info("Response Body: {}", body);
        log.info("===========================================");

        if (response.isOk()) {
          TaskResponse taskResponse = JSONUtil.toBean(body, TaskResponse.class);
          if (taskResponse != null
            && taskResponse.getData() != null
            && !StrUtil.isBlank(taskResponse.getData().getShare_id())) {
            log.info("Successfully extracted shareId on retry: {}", retry);
            return taskResponse; // 成功获取 shareId，返回
          } else {
            log.warn("shareId is blank on retry {}. Full response: {}", retry, body);
          }
        } else {
          log.error("Task GET request failed with status code: {}", response.getStatus());
        }
      } catch (Exception e) {
        log.error("Task GET request error:{}", e.getMessage(), e);
      }

      // 重试前休眠 5 秒
      try {
        Thread.sleep(BEFORE_RETRY_SLEEP_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Thread interrupted: {}", e.getMessage());
        return null;
      }
    }

    log.error("Failed to extract shareId after {} retries for taskId: {}", maxRetries, taskId);
    return null; // 超过最大重试次数，返回 null
  }

  /**
   * 发起 POST 请求 (Share Password)
   */
  public static SharePasswordResponse postSharePassword(
    String shareId, String headerFilePath) {
    sleepBeforeCall(); // 休眠 3 秒
    Map<String, String> headers = readHeadersFromFile(headerFilePath);
    SharePasswordParams params = new SharePasswordParams();
    params.setShare_id(shareId);

    String jsonBody = JSONUtil.toJsonStr(params);

    // 打印请求详情
    log.info("========== POST Share Password 请求 ==========");
    log.info("请求 URL: {}", SHARE_PASSWORD_URL);
    log.info("请求 Headers:");
    headers.forEach((key, value) -> log.info("  {} : {}", key, value));
    log.info("请求 Body: {}", jsonBody);

    HttpRequest request = HttpUtil.createPost(SHARE_PASSWORD_URL)
      .body(jsonBody);
    headers.forEach(request::header);

    try (HttpResponse response = request.execute()) {
      String body = response.body();

      // 打印响应详情
      log.info("========== POST Share Password 响应 ==========");
      log.info("响应状态码: {}", response.getStatus());
      log.info("响应 Headers:");
      response.headers().forEach((key, values) ->
        log.info("  {} : {}", key, String.join(", ", values)));
      log.info("响应 Body: {}", body);
      log.info("===========================================");

      if (response.isOk()) {
        return JSONUtil.toBean(body, SharePasswordResponse.class);
      } else {
        log.error("Share Password POST 请求失败，状态码: {}", response.getStatus());
        return null;
      }
    } catch (Exception e) {
      log.error("Share Password POST 请求异常:{}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * 递归处理每个 FileList 对象
   *
   * @param fileList       FileList 对象
   * @param headerFilePath 请求头文件路径
   */
  public static String processFileList(FileList fileList,
    String headerFilePath) {
    // log.info("Processing file: {} with fid: {}", fileList.getFile_name(), fileList.getFid());

    String fid = fileList.getFid();
    String title = fileList.getFile_name();

    // 2. POST Share
    ShareParams shareParams = new ShareParams();
    shareParams.setFid_list(List.of(fid));
    shareParams.setTitle(title);
    shareParams.setUrl_type(1);
    shareParams.setExpired_type(1);

    ShareResponse shareResponse = QuarkDiskUtil.postShare(shareParams,
      headerFilePath);
    String taskId;

    if (shareResponse != null) {
      taskId = shareResponse.getData().getTask_id();
      // log.info("Extracted taskId for GET Task: {}", taskId);
    } else {
      // log.error("POST Share Request failed for fid: {}", fid);
      return ""; // 如果第二个请求失败，直接退出当前循环
    }

    // 3. GET Task
    TaskResponse taskResponse = getTask(taskId,
      headerFilePath); // 使用更新后的 getTask 方法
    String shareId = null;

    if (taskResponse != null && taskResponse.getData() != null) {
      shareId = taskResponse.getData().getShare_id();
      // log.info("Extracted shareId for POST Share Password: {}", shareId);
    } else {
      // log.error("GET Task Request failed for fid: {}", fid);
      return ""; // 如果第三个请求失败，直接退出当前循环
    }

    // 4. POST Share Password
    SharePasswordResponse sharePasswordResponse =
      postSharePassword(shareId, headerFilePath);
//    String title = "";
    String text = "";
    if (sharePasswordResponse != null) {
      log.error(
        "Share URL for title {} : {}",
        sharePasswordResponse.getData().getTitle(),
        sharePasswordResponse.getData().getShare_url()); //
      text = sharePasswordResponse.getData().getTitle() + " : "
        + sharePasswordResponse.getData().getShare_url();
    } else {
      // log.error("POST Share Password Request failed for fid: {}", fid);
    }

    if (title.length() > 1) {
      title = title.substring(0, 2);
    }

    // 5. 下载文件
    //   CdFileUtil.writeToFile("D:\\output\\20" + title + ".txt", textList);
    return text;
  }

  /**
   * 休眠
   */
  private static void sleepBeforeCall() {
    try {
      Thread.sleep(BEFORE_CALL_SLEEP_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      // log.error("Thread interrupted: {}", e.getMessage());
    }
  }

  public static void main(String[] args) {
//    String year = "2023";
//    List<String> years = List.of("2018", "2019", "2020", "2021", "2022", "2023");
    List<String> years = List.of("2026");
    for (String year : years) {
      process(year);
    }
  }

  public static void process(String year) {
    long startTime = System.currentTimeMillis(); // 记录开始时间
    String quarkPath =
      OperatingSystem.getBaseFolder() + File.separator + "quark";
    List<String> list = FileUtil.readLines(
      quarkPath
        + File.separator + "input"
        + File.separator + "quark_share_year_fid.txt",
      StandardCharsets.UTF_8);
    Map<String, String> map = new HashMap<>();
    for (String line : list) {
      if (line.contains(":")) {
        String[] split = line.split(":");
        map.put(split[0].trim(), split[1].trim());
      } else {
        log.error("line is not valid: {}", line);
        return;
      }
    }
    String fid = map.get(year);
    if (fid == null) {
      log.error("fid is null for year: {}", year);
      return;
    }

    String headerFilePath =
      quarkPath + File.separator + "input"
        + File.separator + "quark_cookie.txt"; // 修改后的文件路径
    FileSortParams fileSortParams = new FileSortParams();
//    fileSortParams.setPdir_fid("df5fa55ae8c34cd08e50e76cc57da28a"); // 设置 pdir_fid
    fileSortParams.setPdir_fid(fid); // 设置 pdir_fid

    // 1. GET File Sort
    QuarkDiskResponse fileSortResponse = getFileSort(fileSortParams,
      headerFilePath);

    if (fileSortResponse != null
      && fileSortResponse.getData() != null
      && fileSortResponse.getData().getList() != null
      && !fileSortResponse.getData().getList().isEmpty()) {
      String quarkShareFileName =quarkPath + File.separator + "output"
        + File.separator + "quark_share_" + year + ".txt";
      List<String> textList = new LinkedList<>();
      Set<String> fileNameSet = new LinkedHashSet<>();
      if(!CdFileUtil.isFileEmpty(quarkShareFileName)) {
        List<String> oldTextList = FileUtil.readLines(quarkShareFileName,
          StandardCharsets.UTF_8);
        for (String line : oldTextList) {
          String[] split = line.split(" : ");
          if (split.length == 2) {
            fileNameSet.add(split[0].trim());
            textList.add(line);
          }
        }
      }

      // 循环处理 list 中的每个 FileList 对象
      List<QuarkDiskResponse.FileList> fileList = fileSortResponse.getData()
        .getList();
      for (QuarkDiskResponse.FileList file : fileList) {
        String fileName = file.getFile_name();
        if(fileNameSet.isEmpty() || !fileNameSet.contains(fileName)){
          textList.add(QuarkDiskUtil.processFileList(file, headerFilePath));
        } else {
          log.info("分享链接已存在，跳过: {}", fileName);
        }
      }
      textList = textList.stream().sorted().collect(Collectors.toList());

      CdFileUtil.writeToFile(quarkShareFileName,    textList);
    } else {
      // log.error("GET File Sort Request failed.");
    }
    long endTime = System.currentTimeMillis(); // 记录视频生成结束时间
    long durationMillis = endTime - startTime; // 计算耗时（毫秒）
    log.info("{} 年分享文件创建成功，耗时: {}", year,
      CdTimeUtil.formatDuration(durationMillis));
  }
}
