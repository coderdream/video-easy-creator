//package com.coderdream.util.gemini;
//
//import com.coderdream.util.cd.CdConstants;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.InetSocketAddress;
//import java.net.ProxySelector;
//import java.net.http.HttpClient;
//import java.util.Base64;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import com.coderdream.util.proxy.OperatingSystem;
//import lombok.extern.slf4j.Slf4j;
//import swiss.ameri.gemini.api.*;
//import swiss.ameri.gemini.api.GenAi.GeneratedContent;
//import swiss.ameri.gemini.api.GenAi.Model;
//import swiss.ameri.gemini.gson.GsonJsonParser;
//import swiss.ameri.gemini.spi.JsonParser;
//
///**
// * Gemini API 工具类，封装了 Gemini API 的调用逻辑。 用户只需要传入内容参数，无需关注内部细节。
// */
//@Slf4j
//public class GeminiApiUtil1 {
//
//  private static final JsonParser parser = new GsonJsonParser();
//  private static final HttpClient proxyClient;
//  private static final GenAi genAi;
//
//  static {
//    // 创建 HttpClient， 设置代理
//    proxyClient = HttpClient.newBuilder()
//      .proxy(ProxySelector.of(new InetSocketAddress(CdConstants.PROXY_HOST, OperatingSystem.getProxyPort())))
//      .build();
//    // 创建 GenAi 实例
//    String apiKey = CdConstants.GEMINI_API_KEY;
//    genAi = new GenAi(apiKey, parser, proxyClient);
//  }
//
//
//  /**
//   * 禁止实例化工具类
//   */
//  private GeminiApiUtil1() {
//    throw new AssertionError("禁止实例化");
//  }
//
//
//  /**
//   * 列出所有可用的模型
//   *
//   * @return 模型列表
//   */
//  public static List<Model> listModels() {
//    log.info("----- 列出所有可用模型");
//    return genAi.listModels();
//  }
//
//  /**
//   * 获取指定模型的信息
//   *
//   * @param modelVariant 模型枚举
//   * @return 模型信息
//   */
//  public static Model getModel(ModelVariant modelVariant) {
//    log.info("----- 获取模型信息：{}", modelVariant);
//    return genAi.getModel(modelVariant);
//  }
//
//
//  /**
//   * 统计文本标记的数量
//   *
//   * @param content 要统计的文本内容
//   * @return 标记数量
//   */
//  public static Long countTokens(String content) {
//    log.info("----- 统计文本标记数量：{}", content);
//    var model = createModel(content);
//    return genAi.countTokens(model).join();
//  }
//
//
//  /**
//   * 生成文本内容（阻塞式）
//   *
//   * @param content 输入的文本内容
//   * @return 生成的内容
//   * @throws InterruptedException 中断异常
//   * @throws ExecutionException   执行异常
//   * @throws TimeoutException     超时异常
//   */
//  public static GeneratedContent generateContent(String content)
//    throws InterruptedException, ExecutionException, TimeoutException {
//    // 打印content 的前100个字符
//    if (content.length() > 100) {
//      log.info("----- content的前100个字符: {}", content.substring(0, 100));
//    } else {
//      log.info("----- content的长度小于等于100，直接打印：{}", content);
//    }
////    log.info("----- 生成文本内容（阻塞式）: {}", content);
//    var model = createModel(content);
//
//    return genAi.generateContent(model).get(120000, TimeUnit.SECONDS);
//  }
//
//  /**
//   * 生成文本内容（流式）
//   *
//   * @param content 输入的文本内容
//   * @return 生成的内容流
//   */
//  public static List<GeneratedContent> generateContentStream(
//    String content) {
//    log.info("----- 生成文本内容（流式）: {}", content);
//    var model = createModel(content);
//    List<GeneratedContent> list = genAi.generateContentStream(model).toList();
//    return list;
//  }
//
//  /**
//   * 生成内容（阻塞式），指定响应模式
//   *
//   * @param content 输入的文本内容
//   * @param schema  响应的模式
//   * @return 生成的内容
//   * @throws InterruptedException 中断异常
//   * @throws ExecutionException   执行异常
//   * @throws TimeoutException     超时异常
//   */
//  public static GeneratedContent generateContentWithResponseSchema(
//    String content, Schema schema)
//    throws InterruptedException, ExecutionException, TimeoutException {
//    log.info("----- 生成内容（阻塞式），指定响应模式: {}, schema: {}", content,
//      schema);
//    var model = createResponseSchemaModel(content, schema);
//    GeneratedContent generatedContent = genAi.generateContent(model)
//      .get(20, TimeUnit.SECONDS);
//    return generatedContent;
//  }
//
//  /**
//   * 生成内容（流式），指定响应模式
//   *
//   * @param content 输入的文本内容
//   * @param schema  响应的模式
//   * @return 生成的内容流
//   */
//  public static List<GeneratedContent> generateContentStreamWithResponseSchema(
//    String content, Schema schema) {
//    log.info("----- 生成内容（流式），指定响应模式: {}, schema: {}", content,
//      schema);
//    var model = createResponseSchemaModel(content, schema);
//    List<GeneratedContent> list = genAi.generateContentStream(model).toList();
//    return list;
//  }
//
//  /**
//   * 多轮对话
//   *
//   * @param contents 多轮对话的文本内容列表
//   * @return 生成的内容流
//   */
//  public static List<GeneratedContent> multiChatTurn(
//    List<String> contents) {
//    log.info("----- 多轮对话：{}", contents);
//    GenerativeModel chatModel = createMultiTurnChatModel(contents);
//    List<GeneratedContent> list = genAi.generateContentStream(chatModel)
//      .toList();
//    return list;
//  }
//
//  /**
//   * 文本和图片内容
//   *
//   * @param text      文本内容
//   * @param imagePath 图片路径
//   * @return 生成的内容
//   * @throws IOException          IO异常
//   * @throws ExecutionException   执行异常
//   * @throws InterruptedException 中断异常
//   * @throws TimeoutException     超时异常
//   */
//  public static GeneratedContent textAndImage(String text,
//    String imagePath)
//    throws IOException, ExecutionException, InterruptedException, TimeoutException {
//    log.info("----- 文本和图片内容: text: {}, imagePath: {}", text, imagePath);
//    var model = createTextAndImageModel(text, imagePath);
//
//    return genAi.generateContent(model).get(20, TimeUnit.SECONDS);
//  }
//
//  /**
//   * 内容嵌入
//   *
//   * @param content 要嵌入的文本内容
//   * @return 嵌入列表
//   */
//  public static List<GenAi.ContentEmbedding> embedContents(String content) {
//    log.info("----- 内容嵌入: {}", content);
//    var model = createEmbedModel(content);
//    return genAi.embedContents(model, null, null, null).join();
//  }
//
//  /**
//   * 加载 scones.png 图片，并将其转换为 Base64 字符串。
//   *
//   * @return Base64 字符串
//   * @throws IOException 如果加载图片失败
//   */
//  private static String loadSconesImage(String imagePath) throws IOException {
//    // 加载 scones.png 图片，并将其转换为 Base64 字符串
//    try (InputStream is = GeminiApiUtil1.class.getClassLoader()
//      .getResourceAsStream(imagePath)) {
//      if (is == null) {
//        throw new IllegalStateException("Image not found! ");
//      }
//      return Base64.getEncoder().encodeToString(is.readAllBytes());
//    }
//  }
//
//  /**
//   * 创建用于生成故事的 GenerativeModel 对象。
//   *
//   * @param content 文本内容
//   * @return GenerativeModel 对象
//   */
//  private static GenerativeModel createModel(String content) {
//    return GenerativeModel.builder()
//      .modelName(ModelVariant.GEMINI_2_0_FLASH_EXP) // 指定模型
//      .addContent(Content.textContent( // 添加用户内容
//        Content.Role.USER,
//        content
//      ))
//      .addSafetySetting(SafetySetting.of( // 添加安全设置
//        SafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
//        SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH
//      ))
//      .generationConfig(new GenerationConfig( // 配置生成参数
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null
//      ))
//      .build();
//  }
//
//  /**
//   * 创建指定响应模式的 GenerativeModel 对象。
//   *
//   * @param content 文本内容
//   * @param schema  响应模式
//   * @return GenerativeModel 对象
//   */
//  private static GenerativeModel createResponseSchemaModel(String content,
//    Schema schema) {
//    // 创建 GenerativeModel 对象，指定响应模式
//    return GenerativeModel.builder()
//      .modelName(ModelVariant.GEMINI_1_5_FLASH) // 指定模型
//      .addContent(Content.textContent( // 添加用户内容
//        Content.Role.USER,
//        content
//      ))
//      .addSafetySetting(SafetySetting.of( // 添加安全设置
//        SafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
//        SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH
//      ))
//      .generationConfig(new GenerationConfig( // 配置生成参数，指定响应模式为 JSON
//        null,
//        "application/json",
//        schema,
//        null,
//        null,
//        null,
//        null
//      ))
//      .build();
//  }
//
//  /**
//   * 创建多轮对话的 GenerativeModel
//   *
//   * @param contents 多轮对话的内容列表
//   * @return GenerativeModel 对象
//   */
//  private static GenerativeModel createMultiTurnChatModel(
//    List<String> contents) {
//    var builder = GenerativeModel.builder()
//      .modelName(ModelVariant.GEMINI_1_5_PRO);
//    for (int i = 0; i < contents.size(); i++) {
//      String role = i % 2 == 0 ? Content.Role.USER.roleName()
//        : Content.Role.MODEL.roleName();
//      builder.addContent(new Content.TextContent(role, contents.get(i)));
//    }
//    return builder.build();
//  }
//
//  /**
//   * 创建包含文本和图像内容的 GenerativeModel
//   *
//   * @param text      文本内容
//   * @param imagePath 图片路径
//   * @return GenerativeModel 对象
//   * @throws IOException 加载图片失败
//   */
//  private static GenerativeModel createTextAndImageModel(String text,
//    String imagePath) throws IOException {
//    return GenerativeModel.builder()
//      .modelName(ModelVariant.GEMINI_1_5_FLASH) // 指定模型
//      .addContent(
//        Content.textAndMediaContentBuilder()
//          .role(Content.Role.USER) // 指定角色为用户
//          .text(text) // 添加文本内容
//          .addMedia(new Content.MediaData( // 添加媒体内容
//            "image/png",
//            loadSconesImage(imagePath)
//          ))
//          .build()
//      ).build();
//  }
//
//  /**
//   * 创建用于内容嵌入的 GenerativeModel 对象。
//   *
//   * @param content 文本内容
//   * @return GenerativeModel 对象
//   */
//  private static GenerativeModel createEmbedModel(String content) {
//    return GenerativeModel.builder()
//      .modelName(ModelVariant.TEXT_EMBEDDING_004) // 指定模型
//      .addContent(Content.textContent( // 添加用户内容
//        Content.Role.USER,
//        content
//      ))
//      .addSafetySetting(SafetySetting.of( // 添加安全设置
//        SafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
//        SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH
//      ))
//      .generationConfig(new GenerationConfig( // 配置生成参数
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null
//      ))
//      .build();
//  }
//}
