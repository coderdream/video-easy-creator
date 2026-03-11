package com.coderdream.util.minimax;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * MiniMax API 响应对象
 *
 * @author Claude Code
 * @since 2026-02-08
 */
@Data
@Accessors(chain = true)
public class MiniMaxResponse {

    /**
     * 请求 ID
     */
    private String id;

    /**
     * 对象类型
     */
    private String object;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * Token 使用统计
     */
    private Usage usage;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 选择对象
     */
    @Data
    @Accessors(chain = true)
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息内容
         */
        private Message message;

        /**
         * 完成原因
         * stop: 正常结束
         * length: 达到最大长度
         */
        private String finishReason;
    }

    /**
     * 消息对象
     */
    @Data
    @Accessors(chain = true)
    public static class Message {
        /**
         * 发送者类型
         */
        private String senderType;

        /**
         * 消息内容
         */
        private String text;
    }

    /**
     * Token 使用统计
     */
    @Data
    @Accessors(chain = true)
    public static class Usage {
        /**
         * 输入 Token 数
         */
        private Integer totalTokens;

        /**
         * 获取总 Token 数
         *
         * @return 总 Token 数
         */
        public Integer getTotalTokensCount() {
            return totalTokens != null ? totalTokens : 0;
        }
    }

    /**
     * 获取第一个选择的文本内容
     *
     * @return 文本内容，如果没有则返回空字符串
     */
    public String getFirstChoiceText() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice != null && firstChoice.getMessage() != null) {
                return firstChoice.getMessage().getText();
            }
        }
        return "";
    }

    /**
     * 获取完成原因
     *
     * @return 完成原因
     */
    public String getFinishReason() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice != null) {
                return firstChoice.getFinishReason();
            }
        }
        return null;
    }
}
