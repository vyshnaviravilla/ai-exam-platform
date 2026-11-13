package com.examplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final long TIMEOUT_SECONDS = 10;

    public ExecutionResult executeJavaCode(String userCode, List<TestCase> testCases) {
        Path tempDir = null;
        
        try {
            tempDir = Files.createTempDirectory("exam_");
            
            // Write user code to Solution.java
            Path solutionFile = tempDir.resolve("Solution.java");
            Files.writeString(solutionFile, userCode);
            
            // Create a Main.java wrapper that can run the solution
            String mainCode = generateMainCode(userCode, testCases);
            Path mainFile = tempDir.resolve("Main.java");
            Files.writeString(mainFile, mainCode);
            
            // Compile both files
            ProcessBuilder compilePb = new ProcessBuilder("javac", "Solution.java", "Main.java");
            compilePb.directory(tempDir.toFile());
            compilePb.redirectErrorStream(true);
            
            Process compileProcess = compilePb.start();
            boolean compiled = compileProcess.waitFor(15, TimeUnit.SECONDS);
            
            if (!compiled) {
                compileProcess.destroyForcibly();
                return ExecutionResult.builder()
                        .success(false)
                        .error("Compilation timeout")
                        .build();
            }
            
            if (compileProcess.exitValue() != 0) {
                String error = new String(compileProcess.getInputStream().readAllBytes());
                return ExecutionResult.builder()
                        .success(false)
                        .error("Compilation failed:\n" + error)
                        .build();
            }
            
            // Run tests
            List<TestResult> results = new ArrayList<>();
            int passed = 0;
            
            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                TestResult result = runTestCase(tempDir, tc, i);
                results.add(result);
                if (result.isPassed()) passed++;
            }
            
            return ExecutionResult.builder()
                    .success(true)
                    .testResults(results)
                    .testCasesPassed(passed)
                    .totalTestCases(testCases.size())
                    .build();
            
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return ExecutionResult.builder()
                    .success(false)
                    .error("Execution error: " + e.getMessage())
                    .build();
        } finally {
            if (tempDir != null) {
                cleanup(tempDir);
            }
        }
    }
    
    private String generateMainCode(String userCode, List<TestCase> testCases) {
        // Extract method name from user code
        String methodName = extractMethodName(userCode);
        
        return """
            public class Main {
                public static void main(String[] args) {
                    if (args.length == 0) {
                        System.out.println("No input provided");
                        return;
                    }
                    try {
                        Solution sol = new Solution();
                        String input = String.join(" ", args);
                        
                        // Try to parse and call the solution method
                        Object result = callMethod(sol, input);
                        System.out.println(result);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                private static Object callMethod(Solution sol, String input) throws Exception {
                    // For twoSum: parse "[2,7,11,15], 9" format
                    if (input.contains("[") && input.contains("]")) {
                        // Extract array part
                        int arrayStart = input.indexOf('[');
                        int arrayEnd = input.indexOf(']');
                        String arrayStr = input.substring(arrayStart + 1, arrayEnd);
                        String[] parts = arrayStr.split(",");
                        int[] nums = new int[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            nums[i] = Integer.parseInt(parts[i].trim());
                        }
                        
                        // Extract target after the array
                        String afterArray = input.substring(arrayEnd + 1);
                        int target = 0;
                        if (afterArray.contains(",")) {
                            String targetStr = afterArray.substring(afterArray.indexOf(',') + 1).trim();
                            target = Integer.parseInt(targetStr);
                        }
                        
                        // Call twoSum via reflection
                        java.lang.reflect.Method method = Solution.class.getMethod("twoSum", int[].class, int.class);
                        int[] result = (int[]) method.invoke(sol, nums, target);
                        return java.util.Arrays.toString(result);
                    }
                    
                    // For reverseList and other methods with array input
                    if (input.startsWith("[") && input.endsWith("]")) {
                        String content = input.substring(1, input.length() - 1);
                        if (content.isEmpty()) {
                            // Empty array case
                            return "[]";
                        }
                        String[] parts = content.split(",");
                        int[] arr = new int[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            arr[i] = Integer.parseInt(parts[i].trim());
                        }
                        
                        // Try reverseList
                        try {
                            java.lang.reflect.Method method = Solution.class.getMethod("reverseList", ListNode.class);
                            ListNode head = arrayToListNode(arr);
                            ListNode result = (ListNode) method.invoke(sol, head);
                            return listNodeToString(result);
                        } catch (NoSuchMethodException e) {
                            // Method not found, return array as string
                            return java.util.Arrays.toString(arr);
                        }
                    }
                    
                    return "Input format not recognized: " + input;
                }
                
                private static ListNode arrayToListNode(int[] arr) {
                    if (arr == null || arr.length == 0) return null;
                    ListNode dummy = new ListNode(0);
                    ListNode current = dummy;
                    for (int val : arr) {
                        current.next = new ListNode(val);
                        current = current.next;
                    }
                    return dummy.next;
                }
                
                private static String listNodeToString(ListNode head) {
                    if (head == null) return "[]";
                    StringBuilder sb = new StringBuilder("[");
                    while (head != null) {
                        sb.append(head.val);
                        if (head.next != null) sb.append(",");
                        head = head.next;
                    }
                    sb.append("]");
                    return sb.toString();
                }
            }
            
            class ListNode {
                int val;
                ListNode next;
                ListNode(int x) { val = x; }
                ListNode(int val, ListNode next) { this.val = val; this.next = next; }
            }
            """;
    }
    
    private String extractMethodName(String code) {
        // Simple extraction - look for public method
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("public\\s+\\w+\\s+(\\w+)\\s*\\(");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "solution";
    }
    
    private TestResult runTestCase(Path workingDir, TestCase testCase, int index) {
        try {
            ProcessBuilder runPb = new ProcessBuilder("java", "Main", testCase.getInput());
            runPb.directory(workingDir.toFile());
            runPb.redirectErrorStream(true);
            
            Process runProcess = runPb.start();
            boolean finished = runProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                runProcess.destroyForcibly();
                return TestResult.builder()
                        .passed(false)
                        .expected(testCase.getExpected())
                        .actual("Timeout - execution took too long")
                        .build();
            }
            
            String output = new String(runProcess.getInputStream().readAllBytes()).trim();
            
            // Check if there's an error in output
            if (output.startsWith("Error:") || output.contains("Exception")) {
                return TestResult.builder()
                        .passed(false)
                        .expected(testCase.getExpected())
                        .actual(output)
                        .build();
            }
            
            boolean passed = normalizeOutput(output).equals(normalizeOutput(testCase.getExpected()));
            
            return TestResult.builder()
                    .passed(passed)
                    .expected(testCase.getExpected())
                    .actual(output)
                    .build();
            
        } catch (Exception e) {
            return TestResult.builder()
                    .passed(false)
                    .expected(testCase.getExpected())
                    .actual("Runtime error: " + e.getMessage())
                    .build();
        }
    }
    
    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.replaceAll("\\s+", "").replaceAll("'", "").replaceAll("\"", "").toLowerCase();
    }
    
    private void cleanup(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
    
    public static class TestCase {
        private String input;
        private String expected;
        
        public TestCase(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
        
        public String getInput() { return input; }
        public String getExpected() { return expected; }
    }
    
    public static class ExecutionResult {
        private boolean success;
        private String error;
        private List<TestResult> testResults;
        private int testCasesPassed;
        private int totalTestCases;
        
        public static Builder builder() { return new Builder(); }
        
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public List<TestResult> getTestResults() { return testResults; }
        public int getTestCasesPassed() { return testCasesPassed; }
        public int getTotalTestCases() { return totalTestCases; }
        
        public static class Builder {
            private ExecutionResult result = new ExecutionResult();
            
            public Builder success(boolean success) {
                result.success = success;
                return this;
            }
            
            public Builder error(String error) {
                result.error = error;
                return this;
            }
            
            public Builder testResults(List<TestResult> testResults) {
                result.testResults = testResults;
                return this;
            }
            
            public Builder testCasesPassed(int passed) {
                result.testCasesPassed = passed;
                return this;
            }
            
            public Builder totalTestCases(int total) {
                result.totalTestCases = total;
                return this;
            }
            
            public ExecutionResult build() { return result; }
        }
    }
    
    public static class TestResult {
        private boolean passed;
        private String expected;
        private String actual;
        
        public static Builder builder() { return new Builder(); }
        
        public boolean isPassed() { return passed; }
        public String getExpected() { return expected; }
        public String getActual() { return actual; }
        
        public static class Builder {
            private TestResult result = new TestResult();
            
            public Builder passed(boolean passed) {
                result.passed = passed;
                return this;
            }
            
            public Builder expected(String expected) {
                result.expected = expected;
                return this;
            }
            
            public Builder actual(String actual) {
                result.actual = actual;
                return this;
            }
            
            public TestResult build() { return result; }
        }
    }
}
