package com.coderdream.util.youtube.demo03;

import cn.hutool.core.util.StrUtil;
import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.youtube.YouTubeApiUtil;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class YoutubeThumbnailFetcher {

  private static final String API_KEY = CdConstants.YOUTUBE_API_KEY; // 替换为你的 API 密钥
//  private static final String VIDEO_ID = "5yjf1RvhflI"; // 示例视频ID
//  private static final String APPLICATION_NAME = "YoutubeThumbnailFetcher";
//  private static final String SAVE_DIRECTORY = "thumbnails"; // 保存图片的目录
//  private static final String IMAGE_FORMAT = "jpg"; // 图片格式

  public static String getYoutubeThumbnailUrl(String videoId) {
    // 【关键修改】在调用API前检查API_KEY是否存在
    if (StrUtil.isBlank(API_KEY)) {
      log.error("YouTube API 密钥 (API_KEY) 未配置或为空，请在 CdConstants.YOUTUBE_API_KEY 中进行配置。");
      return null;
    }

    try {
      NetHttpTransport transport = new NetHttpTransport();
      JacksonFactory jsonFactory = new JacksonFactory();
      YouTube youtubeService = YouTubeApiUtil.createYoutubeService(transport,
        jsonFactory);

      assert youtubeService != null;
      YouTube.Videos.List request = youtubeService.videos().list("snippet");
      request.setId(videoId);
      request.setKey(API_KEY); // 使用常量API_KEY
      VideoListResponse response = request.execute();

      List<Video> videos = response.getItems();
      if (videos != null && !videos.isEmpty()) {
        Video video = videos.get(0);
        ThumbnailDetails thumbnails = video.getSnippet().getThumbnails();

        // 封面优先级: maxres, standard, high, medium
        if (thumbnails.getMaxres() != null) {
          return thumbnails.getMaxres().getUrl();
        } else if (thumbnails.getStandard() != null) {
          return thumbnails.getStandard().getUrl();
        } else if (thumbnails.getHigh() != null) {
          return thumbnails.getHigh().getUrl();
        } else if (thumbnails.getMedium() != null) {
          return thumbnails.getMedium().getUrl();
        } else {
          return null; // 没有找到任何合适的封面
        }
      } else {
        return null; // 视频不存在
      }

    } catch (Exception e) {
      log.error("发生错误:{} ", e.getMessage());
      return null;
    }
  }

  public static void downloadThumbnail(String thumbnailUrl,
    String thumbnailPath, String thumbnailFileName) {
    if (thumbnailUrl == null) {
      log.info("封面 URL 为空，无法下载。");
      return;
    }

    try {
      // 创建保存目录如果不存在
      Files.createDirectories(Paths.get(thumbnailPath));

      // 构建文件名
      // String fileName = thumbnailFileName;//videoId + "." + IMAGE_FORMAT;
      String filePath = Paths.get(thumbnailPath, thumbnailFileName).toString();

      // 下载图片
      URL url = new URL(thumbnailUrl);
      URLConnection connection = url.openConnection();
      InputStream inputStream = connection.getInputStream();
      FileOutputStream outputStream = new FileOutputStream(filePath);

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      outputStream.close();
      inputStream.close();

      log.info("成功下载封面到: " + filePath);

    } catch (IOException e) {
      log.error("下载图片时发生错误: " + e.getMessage());
    }
  }

  public static void downloadThumbnail(String thumbnailUrl,String thumbnailFileName) {
    if (thumbnailUrl == null) {
      log.info("封面 URL 为空，无法下载。");
      return;
    }

    try {

      // 下载图片
      URL url = new URL(thumbnailUrl);
      URLConnection connection = url.openConnection();
      InputStream inputStream = connection.getInputStream();
      FileOutputStream outputStream = new FileOutputStream(thumbnailFileName);

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      outputStream.close();
      inputStream.close();

      log.info("成功下载封面到: " + thumbnailFileName);

    } catch (IOException e) {
      log.error("下载图片时发生错误: " + e.getMessage());
    }
  }

  public static void getThumbnail(String videoUrl, String thumbnailPath,
    String thumbnailFileName) {
    String videoId = "";
    String[] split = videoUrl.split("=");
    if (split.length > 1) {
      videoId = split[1];
    } else {
      log.info("视频链接格式不正确，无法提取视频ID");
      return;
    }

    String thumbnailUrl = getYoutubeThumbnailUrl(videoId); // 不再需要传递API_KEY
    log.info("视频 {},  的封面 URL: {}", videoId, thumbnailUrl);
    if (thumbnailUrl != null) {
      log.info("视频 {} 的封面 URL: {}", videoId, thumbnailUrl);
      // 下载图片
      downloadThumbnail(thumbnailUrl, thumbnailPath, thumbnailFileName);
    } else {
      log.info("无法获取视频 " + videoId + " 的封面 URL");
    }
  }


  public static void getThumbnail(String videoUrl, String thumbnailFileName) {
    String videoId = "";
    String[] split = videoUrl.split("=");
    if (split.length > 1) {
      videoId = split[1];
    } else {
      log.info("视频链接格式不正确，无法提取视频ID");
      return;
    }

    String thumbnailUrl = getYoutubeThumbnailUrl(videoId); // 不再需要传递API_KEY
    log.info("视频 {},  的封面 URL: {}", videoId, thumbnailUrl);
    if (thumbnailUrl != null) {
      log.info("视频 {} 的封面 URL: {}", videoId, thumbnailUrl);
      // 下载图片
      downloadThumbnail(thumbnailUrl, thumbnailFileName);
    } else {
      log.info("无法获取视频 " + videoId + " 的封面 URL");
    }
  }

  public static void main(String[] args) {
//    String thumbnailUrl = getYoutubeThumbnailUrl(VIDEO_ID); // 不再需要传递API_KEY
//    log.info("视频 {},  的封面 URL: {}", VIDEO_ID, thumbnailUrl);
//    if (thumbnailUrl != null) {
//      log.info("视频 " + VIDEO_ID + " 的封面 URL: " + thumbnailUrl);
//      downloadThumbnail(thumbnailUrl, VIDEO_ID); // 下载图片
//    } else {
//      log.info("无法获取视频 " + VIDEO_ID + " 的封面 URL");
//    }
  }
}
