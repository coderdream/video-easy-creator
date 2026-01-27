package com.coderdream.util.codex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.Test;

class CodexApiClientTest {

  @Test
  void generateContent() {
    String result = CodexApiClient.generateContent("Reply with OK only.");
    assertNotNull(result);
    assumeFalse(result.startsWith("API call failed"),
      "Codex API unavailable: " + result);
    assertFalse(result.trim().isEmpty());
    assertFalse(result.startsWith("API call failed"));
    assertTrue(result.trim().startsWith("OK"));
  }
}
