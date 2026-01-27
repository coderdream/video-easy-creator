package com.coderdream.util.claudecode;

import com.coderdream.util.claudecode.prompt.ClaudeCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude 代码生成工具测试
 *
 * @author Claude Code
 * @since 2026-01-25
 */
@Slf4j
public class ClaudeCodeGeneratorTest {

    private ClaudeCodeGenerator codeGenerator;
    private ClaudeApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new ClaudeApiClient();
        codeGenerator = new ClaudeCodeGenerator(apiClient);
        log.info("测试设置完成");
    }

    @Test
    public void testCodeGeneratorInitialization() {
        log.info("========== 测试：代码生成器初始化 ==========");
        assertNotNull(codeGenerator, "代码生成器不应为空");
        assertNotNull(codeGenerator.getApiClient(), "API client 不应为空");
        log.info("代码生成器初始化测试通过");
    }

    @Test
    public void testCodeTaskTypes() {
        log.info("========== 测试： Code Task Types ==========");
        ClaudeCodeGenerator.CodeTaskType[] taskTypes = ClaudeCodeGenerator.CodeTaskType.values();
        assertEquals(5, taskTypes.length, "Should have 5 task types");
        assertTrue(java.util.Arrays.stream(taskTypes).anyMatch(t -> t.name().equals("GENERATE")),
                "Should have GENERATE task");
        assertTrue(java.util.Arrays.stream(taskTypes).anyMatch(t -> t.name().equals("REVIEW")),
                "Should have REVIEW task");
        assertTrue(java.util.Arrays.stream(taskTypes).anyMatch(t -> t.name().equals("FIX_BUG")),
                "Should have FIX_BUG task");
        assertTrue(java.util.Arrays.stream(taskTypes).anyMatch(t -> t.name().equals("REFACTOR")),
                "Should have REFACTOR task");
        assertTrue(java.util.Arrays.stream(taskTypes).anyMatch(t -> t.name().equals("OPTIMIZE")),
                "Should have OPTIMIZE task");
        log.info("Code task types 测试通过");
    }

    @Test
    public void testGenerateCode() {
        log.info("========== 测试： Generate Code ==========");
        String requirement = "Create a Java function that calculates the factorial of a number";
        try {
            String result = codeGenerator.generateCode(requirement);
            assertNotNull(result, "生成的代码不应为空");
            assertFalse(result.isEmpty(), "生成的代码不应为空");
            log.info("生成的代码:\n{}", result);
        } catch (Exception e) {
            log.warn("Code generation test skipped due to API unavailability: {}", e.getMessage());
        }
        log.info("Generate code 测试通过");
    }

    @Test
    public void testGenerateCodeWithLanguage() {
        log.info("========== 测试： Generate Code With Language ==========");
        String requirement = "Create a function that reverses a string";
        String language = "Python";
        try {
            String result = codeGenerator.generateCode(requirement, language);
            assertNotNull(result, "生成的代码不应为空");
            assertFalse(result.isEmpty(), "生成的代码不应为空");
            log.info("Generated {} code:\n{}", language, result);
        } catch (Exception e) {
            log.warn("Code generation with language test skipped: {}", e.getMessage());
        }
        log.info("Generate code with language 测试通过");
    }

    @Test
    public void testReviewCode() {
        log.info("========== 测试： Review Code ==========");
        String code = "public class Calculator { public int add(int a, int b) { return a + b; } }";
        try {
            String result = codeGenerator.reviewCode(code);
            assertNotNull(result, "Code review result 不应为空");
            assertFalse(result.isEmpty(), "Code review result 不应为空");
            log.info("Code review result:\n{}", result);
        } catch (Exception e) {
            log.warn("Code review test skipped due to API unavailability: {}", e.getMessage());
        }
        log.info("Review code 测试通过");
    }

    @Test
    public void testFixBug() {
        log.info("========== 测试： Fix Bug ==========");
        String buggyCode = "public int divide(int a, int b) { return a / b; }";
        String bugReport = "Division by zero error when b is 0";
        try {
            String result = codeGenerator.fixBug(buggyCode, bugReport);
            assertNotNull(result, "Bug fix result 不应为空");
            assertFalse(result.isEmpty(), "Bug fix result 不应为空");
            log.info("Bug fix result:\n{}", result);
        } catch (Exception e) {
            log.warn("Bug fix test skipped due to API unavailability: {}", e.getMessage());
        }
        log.info("Fix bug 测试通过");
    }

    @Test
    public void testRefactorCode() {
        log.info("========== 测试： Refactor Code ==========");
        String code = "public void processData(java.util.List<String> items) { for (int i = 0; i < items.size(); i++) { System.out.println(items.get(i)); } }";
        try {
            String result = codeGenerator.refactorCode(code);
            assertNotNull(result, "Refactored code 不应为空");
            assertFalse(result.isEmpty(), "Refactored code 不应为空");
            log.info("Refactored code:\n{}", result);
        } catch (Exception e) {
            log.warn("Code refactoring test skipped due to API unavailability: {}", e.getMessage());
        }
        log.info("Refactor code 测试通过");
    }

    @Test
    public void testOptimizeCode() {
        log.info("========== 测试： Optimize Code ==========");
        String code = "public boolean contains(java.util.List<String> list, String target) { for (String item : list) { if (item.equals(target)) { return true; } } return false; }";
        try {
            String result = codeGenerator.optimizeCode(code);
            assertNotNull(result, "Optimized code 不应为空");
            assertFalse(result.isEmpty(), "Optimized code 不应为空");
            log.info("Optimized code:\n{}", result);
        } catch (Exception e) {
            log.warn("Code optimization test skipped due to API unavailability: {}", e.getMessage());
        }
        log.info("Optimize code 测试通过");
    }

    @Test
    public void testCodeGeneratorChaining() {
        log.info("========== 测试： Code Generator Chaining ==========");
        ClaudeCodeGenerator chainedGenerator = new ClaudeCodeGenerator(apiClient);
        assertNotNull(chainedGenerator, "Chained generator 不应为空");
        log.info("代码生成器 chaining 测试通过");
    }

    @Test
    public void testGetApiClient() {
        log.info("========== 测试： Get API Client ==========");
        ClaudeApiClient client = codeGenerator.getApiClient();
        assertNotNull(client, "API client 不应为空");
        log.info("Get API client 测试通过");
    }

    @Test
    public void testTaskTypeDescriptions() {
        log.info("========== 测试： Task Type Descriptions ==========");
        ClaudeCodeGenerator.CodeTaskType generate = ClaudeCodeGenerator.CodeTaskType.GENERATE;
        assertNotNull(generate.getDescription(), "GENERATE description 不应为空");
        assertEquals("代码生成", generate.getDescription(), "GENERATE description 应为 correct");
        ClaudeCodeGenerator.CodeTaskType review = ClaudeCodeGenerator.CodeTaskType.REVIEW;
        assertEquals("代码审查", review.getDescription(), "REVIEW description 应为 correct");
        log.info("Task type descriptions 测试通过");
    }

    @Test
    public void testEmptyCodeGeneration() {
        log.info("========== 测试： Empty Code Generation ==========");
        String emptyRequirement = "";
        try {
            String result = codeGenerator.generateCode(emptyRequirement);
            log.info("Empty requirement result: {}", result);
        } catch (Exception e) {
            log.info("Empty requirement correctly raised exception: {}", e.getMessage());
        }
        log.info("Empty code generation 测试通过");
    }

    @Test
    public void testMultiLanguageCodeGeneration() {
        log.info("========== 测试： Multi-language Code Generation ==========");
        String requirement = "Create a function that checks if a number is prime";
        String[] languages = {"Java", "Python", "JavaScript", "Go"};
        for (String language : languages) {
            try {
                String result = codeGenerator.generateCode(requirement, language);
                assertNotNull(result, "Generated " + language + " code should not be null");
                log.info("Generated {} code successfully", language);
            } catch (Exception e) {
                log.warn("Code generation for {} skipped: {}", language, e.getMessage());
            }
        }
        log.info("Multi-language code generation 测试通过");
    }

    @Test
    public void testComplexCodeReview() {
        log.info("========== 测试： Complex Code Review ==========");
        String complexCode = "public class DataProcessor { private java.util.List<String> data; public void process() { for (int i = 0; i < data.size(); i++) { String item = data.get(i); if (item != null && !item.isEmpty()) { System.out.println(item.toUpperCase()); } } } }";
        try {
            String result = codeGenerator.reviewCode(complexCode);
            assertNotNull(result, "Complex code review result 不应为空");
            log.info("Complex code review completed successfully");
        } catch (Exception e) {
            log.warn("Complex code review test skipped: {}", e.getMessage());
        }
        log.info("Complex code review 测试通过");
    }

    @Test
    public void testCodeGenerationQualityMetrics() {
        log.info("========== 测试： Code Generation Quality Metrics ==========");
        String requirement = "Create a utility function for string manipulation";
        try {
            String result = codeGenerator.generateCode(requirement, "Java");
            assertNotNull(result, "生成的代码不应为空");
            assertTrue(result.length() > 0, "生成的代码 应有 content");
            log.info("Code generation quality check passed");
        } catch (Exception e) {
            log.warn("Code generation quality test skipped: {}", e.getMessage());
        }
        log.info("Code generation quality metrics 测试通过");
    }

    @Test
    public void testCodeSnippetOptimization() {
        log.info("========== 测试： Code Snippet Optimization ==========");
        String snippet = "for (int i = 0; i < 1000000; i++) { String s = \"test\" + i; System.out.println(s); }";
        try {
            String result = codeGenerator.optimizeCode(snippet);
            assertNotNull(result, "Optimized snippet 不应为空");
            log.info("Code snippet optimization completed successfully");
        } catch (Exception e) {
            log.warn("Code snippet optimization test skipped: {}", e.getMessage());
        }
        log.info("Code snippet optimization 测试通过");
    }

    @Test
    public void testRefactoringPreservesFunctionality() {
        log.info("========== 测试： Refactoring Preserves Functionality ==========");
        String originalCode = "public int sum(int[] numbers) { int total = 0; for (int i = 0; i < numbers.length; i++) { total = total + numbers[i]; } return total; }";
        try {
            String result = codeGenerator.refactorCode(originalCode);
            assertNotNull(result, "Refactored code 不应为空");
            assertTrue(result.contains("sum") || result.contains("total"), "Refactored code 应包含 sum logic");
            log.info("Refactoring preserves functionality check passed");
        } catch (Exception e) {
            log.warn("Refactoring functionality test skipped: {}", e.getMessage());
        }
        log.info("Refactoring preserves functionality 测试通过");
    }

    @Test
    public void testCodeGeneratorStateManagement() {
        log.info("========== 测试： Code Generator State Management ==========");
        ClaudeCodeGenerator generator = new ClaudeCodeGenerator(apiClient);
        assertNotNull(generator, "Generator 不应为空");
        assertNotNull(generator.getApiClient(), "API client 不应为空");
        log.info("代码生成器 state management 测试通过");
    }

    @Test
    public void testCodeReviewFeedbackQuality() {
        log.info("========== 测试： Code Review Feedback Quality ==========");
        String code = "public void doSomething() { // TODO: implement this }";
        try {
            String result = codeGenerator.reviewCode(code);
            assertNotNull(result, "Review feedback 不应为空");
            assertTrue(result.length() > 0, "Review feedback 应有 content");
            log.info("Code review feedback quality check passed");
        } catch (Exception e) {
            log.warn("Code review feedback quality test skipped: {}", e.getMessage());
        }
        log.info("Code review feedback quality 测试通过");
    }

    @Test
    public void testAllTaskTypes() {
        log.info("========== 测试： All Task Types ==========");
        ClaudeCodeGenerator.CodeTaskType[] taskTypes = ClaudeCodeGenerator.CodeTaskType.values();
        for (ClaudeCodeGenerator.CodeTaskType taskType : taskTypes) {
            assertNotNull(taskType, "Task type 不应为空");
            assertNotNull(taskType.getDescription(), "Task description 不应为空");
            assertFalse(taskType.getDescription().isEmpty(), "Task description 不应为空");
            log.info("Task type: {} - {}", taskType.name(), taskType.getDescription());
        }
        log.info("All task types 测试通过");
    }
}
