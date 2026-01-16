package com.coderdream.util.gemini.api.entity;

import lombok.Data;

import java.util.List;

/**
 * Gemini API 请求的实体类
 */
@Data
public class GeminiApiRequest {
    private List<Content> contents;
//    private List<SafetySetting> safetySettings;
}
