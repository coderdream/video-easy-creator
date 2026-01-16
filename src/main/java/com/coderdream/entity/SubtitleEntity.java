package com.coderdream.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CoderDream
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubtitleEntity implements Serializable {

    /**
     * 字幕序号
     */
    private Integer subIndex;

    /**
     * 时间字符串 00:00:50,280 --> 00:00:52,800
     */
    private String timeStr;

    /**
     * 第一字幕内容
     */
    private String subtitle;

    /**
     * 第二字幕内容
     */
    private String secondSubtitle;

}
