package com.coderdream.util.callapi;

//import ch.qos.logback.core.util.CloseUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {


  /**
   * get请求
   *
   * @param param
   * @param head
   * @param url
   * @param retryTimes
   * @return
   */
  public static List<String> getText(Map<String, Object> param,
    Map<String, String> head, String url,
    Integer retryTimes) {
    List<String> result = new ArrayList<>();
    HttpRequest request = HttpRequest.get(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = form(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(5000).execute();
      if (response != null && response.isOk()) {
        String body = response.body();
        body = body.replaceAll("\r", " ");
//                System.out.println("XXXXXXXXXXX" + body);
        result.addAll(Arrays.asList(body.split("\n")));
        return result;
//                return JSON.parseObject(body, t);
      } else {
        throw new RuntimeException("返回状态:" + response.getStatus());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      if (retryTimes < 0) {
        log.error("调用远程服务失败:" + url);
        throw new RuntimeException("调用远程服务失败");
      } else {
        log.error("调用次数，{} 调用远程服务失败: {}", retryTimes, url);
        return null;
//                return get(param, head, url, t, --retryTimes);
      }
    } finally {
      if (response != null) {
        //关闭对应的流
        response.close();
      }
    }

//        return null;
  }


  /**
   * get请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T get(Map<String, Object> param, Map<String, String> head,
    String url, Class<T> t,
    Integer retryTimes) {
    HttpRequest request = HttpRequest.get(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = form(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(5000).execute();
      if (response != null && response.isOk()) {
        String body = response.body();
        System.out.println("#####  " + body);
        return JSON.parseObject(body, t);
      } else {
        throw new RuntimeException("返回状态:" + response.getStatus());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      if (retryTimes < 0) {
        log.error("调用远程服务失败:" + url);
        throw new RuntimeException("调用远程服务失败");
      } else {
        return get(param, head, url, t, --retryTimes);
      }
    } finally {
      if (response != null) {
        //关闭对应的流
        response.close();
      }
    }
  }

  /**
   * get请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T get(Map<String, Object> param, Map<String, String> head,
    String url, Class<T> t) {
    HttpRequest request = HttpRequest.get(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = form(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(15000).execute();
      if (response != null && response.isOk()) {
        String body = response.body();
        return JSON.parseObject(body, t);
      } else {
        throw new RuntimeException("返回状态:" + response.getStatus());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException("调用远程服务失败");
    } finally {
      if (response != null) {
        //关闭对应的流
        response.close();
      }
    }
  }
//
//    /**
//     * get请求
//     *
//     * @param s
//     * @param head
//     * @param url
//     * @param t
//     * @param <T>
//     * @param <S>
//     * @return
//     */
//    public static <T, S> T get(S s, Map<String, String> head, String url, Class<T> t) {
//        Map<String, Object> param = BeanMapper.map(s, Map.class);
//        return get(param, head, url, t);
//    }

  /**
   * post请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T post(Map<String, Object> param, Map<String, String> head,
    String url,
    Class<T> t) {
    HttpRequest request = HttpRequest.post(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = body(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(9000).execute();
      if (response != null && response.getStatus() == 200) {
        String body = response.body();
        return JSON.parseObject(body, t);
      } else {
        throw new RuntimeException("返回状态:" + response.getStatus());
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      //关闭对应的流
//            CloseUtil.closeQuietly(response);
    }
  }

  /**
   * post请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T postWithJson(Map<String, Object> param,
    Map<String, String> head,
    String url, Class<T> t) {
    head.put("Content-Type", ContentType.JSON.getValue());
    HttpRequest request = HttpRequest.post(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = body(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(6000).execute();
//            response = request.charset("utf-8").timeout(6000).execute();
      if (response != null && response.getStatus() == 200) {
        String body = response.body();
        return JSON.parseObject(body, t);
      }
    } catch (Exception e) {
      throw new RuntimeException("调用远程服务失败");
    } finally {
      //关闭对应的流
//            CloseUtil.closeQuietly(response);
    }
    return null;
  }

  /**
   * post请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T postWithForm(Map<String, Object> param,
    Map<String, String> head,
    String url, Class<T> t) {
    head.put("Content-Type", ContentType.FORM_URLENCODED.getValue());
    HttpRequest request = HttpRequest.post(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = form(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(6000).execute();
      if (response != null && response.getStatus() == 200) {
        String body = response.body();
        return JSON.parseObject(body, t);
      }
    } catch (Exception e) {
      throw new RuntimeException("调用远程服务失败");
    } finally {
      //关闭对应的流
//            CloseUtil.closeQuietly(response);
    }
    return null;
  }

  /**
   * post请求
   *
   * @param param
   * @param head
   * @param url
   * @param t
   * @param <T>
   * @return
   */
  public static <T> T postWithForm(Map<String, Object> param,
    Map<String, String> head, String url, Class<T> t,
    Integer retryTimes) {
//        head.put("Content-Type", ContentType.FORM_URLENCODED.getValue());
    HttpRequest request = HttpRequest.post(url);
    //设置头参数
    request = header(request, head);
    //设置请求参数
    request = form(request, param);
    HttpResponse response = null;
    try {
      response = request.charset("utf-8").timeout(6000).execute();
      if (response != null && response.getStatus() == 200) {
        String body = response.body();
        return JSON.parseObject(body, t);
      }
      if (retryTimes < 0) {
        throw new RuntimeException("调用远程服务失败");
      } else {
        return postWithForm(param, head, url, t, --retryTimes);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      if (retryTimes < 0) {
        throw new RuntimeException("调用远程服务失败");
      } else {
        return postWithForm(param, head, url, t, --retryTimes);
      }
    } finally {
      //关闭对应的流
//            CloseUtil.closeQuietly(response);
    }
  }

//    public static void testHutoolPost(String cameraId) {
//        JSONObject jsonObject = JSONUtil.createObj();
//        jsonObject.put("cameraId", cameraId);
//        jsonObject.put("startTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        jsonObject.put("callback", "http://www.baidu.com");
//        String postResult = HttpRequest
//            .post("http://localhost:8080/v1/platedetect/tasks")
//            .header("Content-Type","application/json")
//            .body(jsonObject)
//            .execute()
//            .body();
//        log.info("postResult:"+postResult);
//    }

//    /**
//     * post请求
//     *
//     * @param s
//     * @param head
//     * @param url
//     * @param t
//     * @param <T>
//     * @param <S>
//     * @return
//     */
//    public static <T, S> T post(S s, Map<String, String> head, String url, Class<T> t) {
//        Map<String, Object> param = BeanMapper.map(s, Map.class);
//        return post(param, head, url, t);
//    }

  /**
   * 构建头参数
   *
   * @param request
   * @param head
   * @return
   */
  private static HttpRequest header(HttpRequest request,
    Map<String, String> head) {
    if (CollectionUtil.isNotEmpty(head)) {
      for (Map.Entry<String, String> entry : head.entrySet()) {
        if (StrUtil.isNotBlank(entry.getValue())) {
          request = request.header(entry.getKey(), entry.getValue());
        }
      }
    }
    return request;
  }

  /**
   * post方法的请求参数
   *
   * @param request
   * @param param
   * @return
   */
  private static HttpRequest body(HttpRequest request,
    Map<String, Object> param) {
    if (CollectionUtil.isNotEmpty(param)) {
      request = request.body(JSON.toJSONString(param));
    }
    return request;
  }

  /**
   * get方法请求参数
   *
   * @param request
   * @param param
   * @return
   */
  private static HttpRequest form(HttpRequest request,
    Map<String, Object> param) {
    if (CollectionUtil.isNotEmpty(param)) {
      request = request.form(param);
    }
    return request;
  }


  /* ========================================================= */


  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url       请求地址
   * @param jsonForm  请求参数
   * @param headerMap 请求头
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, JSONObject jsonForm,
    Map<String, String> headerMap) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.post(url)
      .form(jsonForm)
      .addHeaders(headerMap)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
//      log.info("httpPost请求地址: {} 参数: {} 返回结果: {}", url, jsonForm,
//        responseBody);

      return responseBody;
    } catch (Exception e) {
      // 捕获请求中的异常并记录日志
      log.error("HTTP POST 请求失败，URL: {}，错误信息: {}", url, e.getMessage(),
        e);
      return null;  // 或者根据业务需求返回默认值
    }
  }

  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url       请求地址
   * @param jsonForm  请求参数
   * @param headerMap 请求头
   * @param proxyHost 代理服务器地址
   * @param proxyPort 代理服务器端口
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, JSONObject jsonForm,
    Map<String, String> headerMap, String proxyHost, int proxyPort) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.post(url)
      .form(jsonForm)
      .addHeaders(headerMap)
      .setHttpProxy(proxyHost, proxyPort)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
//      log.info("httpPost请求地址: {} 参数: {} 返回结果: {}", url, jsonForm,
//        responseBody);

      return responseBody;
    } catch (Exception e) {
      // 捕获请求中的异常并记录日志
      log.error("HTTP POST 请求失败，URL: {}，错误信息: {}", url, e.getMessage(),
        e);
      return null;  // 或者根据业务需求返回默认值
    }
  }

  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url       请求地址
   * @param jsonStr  请求参数
   * @param headerMap 请求头
   * @param proxyHost 代理服务器地址
   * @param proxyPort 代理服务器端口
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, String jsonStr,
    Map<String, String> headerMap, String proxyHost, int proxyPort) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.post(url)
      .addHeaders(headerMap)
      .contentType("application/json")
      .body(jsonStr)
      .setHttpProxy(proxyHost, proxyPort)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
//      log.info("httpPost请求地址: {} 参数: {} 返回结果: {}", url, jsonStr,
//        responseBody);

      return responseBody;
    } catch (Exception e) {
      // 捕获请求中的异常并记录日志
      log.error("HTTP POST 请求失败，URL: {}，错误信息: {}", url, e.getMessage(),
        e);
      return null;  // 或者根据业务需求返回默认值
    }
  }

  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url      请求地址
   * @param jsonStr 请求参数
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, String jsonStr, String proxyHost, int proxyPort) {
    return httpHutoolPost(url, jsonStr, null, proxyHost, proxyPort);
  }

  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url      请求地址
   * @param jsonForm 请求参数
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, JSONObject jsonForm) {
    return httpHutoolPost(url, jsonForm, null);
  }

  /**
   * Hutool的HttpPost请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url       请求地址
   * @param jsonForm  请求参数
   * @param proxyHost 代理服务器地址
   * @param proxyPort 代理服务器端口
   * @return 返回响应体
   */
  public static String httpHutoolPost(String url, JSONObject jsonForm,
    String proxyHost, int proxyPort) {
    return httpHutoolPost(url, jsonForm, null, proxyHost, proxyPort);
  }

  /**
   * Hutool的HttpGet请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url 请求地址
   * @param map 请求头
   * @return 返回响应体
   */
  public static String httpHutoolGet(String url, Map<String, String> map) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.get(url)
      .addHeaders(map)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
      log.info("httpGet请求地址: {} 返回结果: {}", url, responseBody);

      return responseBody;
    } catch (Exception e) {
      // 捕获请求中的异常并记录日志
      log.error("HTTP GET 请求失败，URL: {}，错误信息: {}", url, e.getMessage(),
        e);
      return null;  // 或者根据业务需求返回默认值
    }
  }

  /**
   * Hutool的HttpGet请求方法，确保请求的响应被正确关闭并捕获异常。
   *
   * @param url       请求地址
   * @param map       请求头
   * @param proxyHost 代理服务器地址
   * @param proxyPort 代理服务器端口
   * @return 返回响应体
   */
  public static String httpHutoolGet(String url, Map<String, String> map, String proxyHost, int proxyPort) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.get(url)
      .addHeaders(map)
      .setHttpProxy(proxyHost, proxyPort)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
      log.info("httpGet请求地址: {} 返回结果: {}", url, responseBody);

      return responseBody;
    } catch (Exception e) {
      // 捕获请求中的异常并记录日志
      log.error("HTTP GET 请求失败，URL: {}，错误信息: {}", url, e.getMessage(),
        e);
      return null;  // 或者根据业务需求返回默认值
    }
  }

  /**
   * hutool的httputilPUT请求方法
   *
   * @param jsons
   * @return
   */
  public static String httpHutoolPut(String url, Map<String, String> map,
    JSONObject jsons) {
    // 使用 try-with-resources 确保 HttpResponse 被自动关闭
    try (HttpResponse response = HttpRequest.put(url)
      .addHeaders(map)
      .form(jsons)
      .execute()) {

      // 获取响应体
      String responseBody = response.body();

      // 打印日志，避免直接输出敏感数据
      log.info("httpPUT请求地址: {} 参数: {} 返回结果: {}", url, jsons,
        responseBody);

      // 返回响应体内容
      return responseBody;
    } catch (Exception e) {
      log.error("请求失败，URL: {}，错误信息: {}", url, e.getMessage(), e);
      return null;  // 或者根据业务需求返回默认值
    }
  }
}
