package com.coderdream.util.nvidia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coderdream.util.cd.CdConstants;
import com.coderdream.util.cd.ChineseCharacterUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "RUN_NVIDIA_API_TESTS", matches = "true")
class NvidiaTranslateUtilTest {

  @Test
  void requestCompletion() {
    String result = NvidiaTranslateUtil.requestCompletion("Reply with OK only.");
    System.out.println("NVIDIA API result: " + result);
    assertNotNull(result);
    assertFalse(result.trim().isEmpty());
    assertFalse(result.contains("<think>"));
    assertFalse(result.contains("</think>"));
    assertTrue(result.trim().toUpperCase().startsWith("OK"));
    assertFalse(result.startsWith("API "));
  }

  @Test
  void requestVocTranslation() {
    List<String> inputLines = List.of(
      "cephalopod",
      "the group of animals to which the octopus belongs",
      "publicity stunt",
      "something a company might do to grab your attention and promote its products",
      "a common ancestor",
      "a distant relative from which two different species evolved",
      "comparable to",
      "similar to",
      "vertebrates",
      "animals that have a spine",
      "protean",
      "(adjective) adaptable and changeable"
    );
    String prompt = CdConstants.VOC_CN_PREFIX + String.join("\n", inputLines);
    String result = NvidiaTranslateUtil.requestCompletion(prompt);

    assertNotNull(result);
    assertFalse(result.trim().isEmpty());
    assertFalse(result.startsWith("API "));
    assertFalse(result.contains("<think>"));
    assertFalse(result.contains("</think>"));

    List<String> lines = Arrays.stream(result.split("\\R"))
      .map(String::trim)
      .filter(line -> !line.isEmpty())
      .collect(Collectors.toList());
    assertEquals(36, lines.size());

    for (int i = 0; i < 6; i++) {
      int inputIndex = i * 2;
      int outputIndex = i * 6;
      assertEquals(inputLines.get(inputIndex), lines.get(outputIndex));
      assertEquals(inputLines.get(inputIndex + 1), lines.get(outputIndex + 1));
    }
  }

  @Test
  void requestPhonetics() {
    List<String> sentences = List.of(
      "Hello world.",
      "How are you today?"
    );
    String prompt = CdConstants.GEN_PHONETICS_TEXT_V2 + String.join("\n",
      sentences);
    String result = NvidiaTranslateUtil.requestCompletion(prompt);

    assertNotNull(result);
    assertFalse(result.trim().isEmpty());
    assertFalse(result.startsWith("API "));
    assertFalse(result.contains("<think>"));
    assertFalse(result.contains("</think>"));

    List<String> lines = Arrays.stream(result.split("\\R"))
      .map(String::trim)
      .filter(line -> !line.isEmpty())
      .collect(Collectors.toList());
    assertEquals(sentences.size(), lines.size());

    for (String line : lines) {
      assertTrue(line.startsWith("/"));
      assertTrue(line.endsWith("/"));
      long slashCount = line.chars().filter(ch -> ch == '/').count();
      assertEquals(2, slashCount);
    }
  }

  @Test
  void requestDescription() {
    String prompt = "解析下面的文本，帮我写文章，用来发哔哩哔哩（B站）、快手、小红书和公众号，"
      + "要根据不同的平台特性生成不同风格的文章，B站的文章字数在1500~2000之间，包含词汇和例句，"
      + "快手的文章字数在500~600之间，小红书不超过800字，公众号不超过200字；"
      + "另外，帮我每个平台取3个疑问句的标题，标题中间不要有任何标点符号、表情符号且不超过20个字，"
      + "快手加入一些表情符号，生成的内容要直接可用，不要让我填空。文本如下：";
    String content = String.join("\n",
      "Hello. This is 6 Minute English from BBC Learning English.",
      "Today we talk about keeping promises and how habits change over time.",
      "We will hear examples and a short quiz to check understanding."
    );
    String result = NvidiaTranslateUtil.requestCompletion(prompt + content);

    assertNotNull(result);
    assertFalse(result.trim().isEmpty());
    assertFalse(result.startsWith("API "));
    assertFalse(result.contains("<think>"));
    assertFalse(result.contains("</think>"));
    assertTrue(ChineseCharacterUtil.containsChinese(result));
  }
}
