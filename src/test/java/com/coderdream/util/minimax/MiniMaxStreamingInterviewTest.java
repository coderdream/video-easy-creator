package com.coderdream.util.minimax;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MiniMax 流式提问测试 - Java面试题层层递进
 * <p>
 * 本测试类演示如何使用 MiniMax API 进行连续对话，
 * 每个问题都基于前一个问题的回答来生成，模拟真实的技术面试场景。
 *
 * @author Claude Code
 * @since 2026-03-12
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MiniMaxStreamingInterviewTest {

    /**
     * 测试1: 流式提问 — Java面试题层层递进（3轮对话）
     * <p>
     * 场景：模拟技术面试，根据候选人的回答逐步深入提问
     * <ul>
     *   <li>第1轮：基础问题 - Java集合框架</li>
     *   <li>第2轮：根据第1轮回答，深入某个具体集合类的实现原理</li>
     *   <li>第3轮：根据第2轮回答，追问并发场景或性能优化</li>
     * </ul>
     */
    @Test
    @Order(1)
    public void test01StreamingInterviewQuestions() {
        log.info("========== 测试1: 流式提问 — Java面试题层层递进 ==========");

        // ===== 第1轮：基础问题 =====
        log.info("\n【第1轮提问】基础问题");
        String question1 = "请简要介绍Java集合框架的主要接口和实现类，以及它们的特点。";
        log.info("问题1: {}", question1);

        String answer1 = MiniMaxUtil.callWithFallback(question1);
        log.info("回答1:\n{}", answer1);

        assertNotNull(answer1, "第1轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer1), "第1轮回答不应为错误信息");

        // ===== 第2轮：根据第1轮回答深入提问 =====
        log.info("\n【第2轮提问】根据第1轮回答深入");

        // 构造第2轮提问，基于第1轮的回答内容
        String question2 = String.format(
                "你刚才提到了Java集合框架的内容。现在请详细解释HashMap的底层实现原理，" +
                "包括数据结构、哈希冲突解决方案、扩容机制等。\n\n" +
                "（参考你之前的回答：%s）",
                answer1.length() > 200 ? answer1.substring(0, 200) + "..." : answer1
        );
        log.info("问题2: {}", question2);

        String answer2 = MiniMaxUtil.callWithFallback(question2);
        log.info("回答2:\n{}", answer2);

        assertNotNull(answer2, "第2轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer2), "第2轮回答不应为错误信息");

        // ===== 第3轮：根据第2轮回答追问并发或性能问题 =====
        log.info("\n【第3轮提问】根据第2轮回答追问并发场景");

        // 构造第3轮提问，基于第2轮的回答内容
        String question3 = String.format(
                "你刚才详细解释了HashMap的实现原理。现在请回答：\n" +
                "1. HashMap在多线程环境下会出现什么问题？\n" +
                "2. ConcurrentHashMap是如何解决这些问题的？\n" +
                "3. 在高并发场景下，如何选择合适的Map实现？\n\n" +
                "（参考你之前关于HashMap的回答：%s）",
                answer2.length() > 200 ? answer2.substring(0, 200) + "..." : answer2
        );
        log.info("问题3: {}", question3);

        String answer3 = MiniMaxUtil.callWithFallback(question3);
        log.info("回答3:\n{}", answer3);

        assertNotNull(answer3, "第3轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer3), "第3轮回答不应为错误信息");

        // ===== 总结 =====
        log.info("\n========== 流式提问测试完成 ==========");
        log.info("第1轮回答长度: {} 字符", answer1.length());
        log.info("第2轮回答长度: {} 字符", answer2.length());
        log.info("第3轮回答长度: {} 字符", answer3.length());
        log.info("总计3轮对话，层层递进，模拟真实技术面试场景");

        // 验证每轮回答都包含中文内容
        assertTrue(answer1.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第1轮回答应包含中文字符");
        assertTrue(answer2.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第2轮回答应包含中文字符");
        assertTrue(answer3.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第3轮回答应包含中文字符");

        log.info("test01 通过，所有回答均有效且包含中文内容");
    }

    /**
     * 测试2: 简化版流式提问 — 使用更短的问题避免超时
     * <p>
     * 场景：模拟技术面试，使用简短问题进行3轮对话
     * <ul>
     *   <li>第1轮：ArrayList和LinkedList的区别</li>
     *   <li>第2轮：根据第1轮回答，询问使用场景</li>
     *   <li>第3轮：根据第2轮回答，询问性能优化建议</li>
     * </ul>
     */
    @Test
    @Order(2)
    public void test02SimplifiedStreamingInterview() {
        log.info("========== 测试2: 简化版流式提问 ==========");

        // ===== 第1轮：简短基础问题 =====
        log.info("\n【第1轮提问】ArrayList和LinkedList的区别");
        String question1 = "请用3-5句话说明ArrayList和LinkedList的主要区别。";
        log.info("问题1: {}", question1);

        String answer1 = MiniMaxUtil.callWithFallback(question1);
        log.info("回答1:\n{}", answer1);

        assertNotNull(answer1, "第1轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer1), "第1轮回答不应为错误信息");

        // ===== 第2轮：基于第1轮回答询问使用场景 =====
        log.info("\n【第2轮提问】询问使用场景");

        String question2 = String.format(
                "根据你刚才说的区别，请简要说明在什么场景下应该使用ArrayList，什么场景下使用LinkedList？\n\n" +
                "（你之前提到：%s）",
                answer1.length() > 100 ? answer1.substring(0, 100) + "..." : answer1
        );
        log.info("问题2: {}", question2);

        String answer2 = MiniMaxUtil.callWithFallback(question2);
        log.info("回答2:\n{}", answer2);

        assertNotNull(answer2, "第2轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer2), "第2轮回答不应为错误信息");

        // ===== 第3轮：询问性能优化建议 =====
        log.info("\n【第3轮提问】性能优化建议");

        String question3 = String.format(
                "基于你刚才说的使用场景，请给出2-3条使用ArrayList时的性能优化建议。\n\n" +
                "（你之前提到的场景：%s）",
                answer2.length() > 100 ? answer2.substring(0, 100) + "..." : answer2
        );
        log.info("问题3: {}", question3);

        String answer3 = MiniMaxUtil.callWithFallback(question3);
        log.info("回答3:\n{}", answer3);

        assertNotNull(answer3, "第3轮回答不能为 null");
        assertFalse(MiniMaxUtil.isFailed(answer3), "第3轮回答不应为错误信息");

        // ===== 总结 =====
        log.info("\n========== 简化版流式提问测试完成 ==========");
        log.info("第1轮回答长度: {} 字符", answer1.length());
        log.info("第2轮回答长度: {} 字符", answer2.length());
        log.info("第3轮回答长度: {} 字符", answer3.length());
        log.info("总计3轮对话，使用简短问题避免超时");

        // 验证每轮回答都包含中文内容
        assertTrue(answer1.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第1轮回答应包含中文字符");
        assertTrue(answer2.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第2轮回答应包含中文字符");
        assertTrue(answer3.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF),
                "第3轮回答应包含中文字符");

        log.info("test02 通过，所有回答均有效且包含中文内容");
    }
}
