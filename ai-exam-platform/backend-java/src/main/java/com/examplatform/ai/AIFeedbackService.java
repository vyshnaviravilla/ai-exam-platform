package com.examplatform.ai;

import com.examplatform.model.Answer;
import com.examplatform.model.CodingQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(AIFeedbackService.class);

    @Value("${llm.api.url:}")
    private String llmApiUrl;

    @Value("${llm.api.key:}")
    private String llmApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String generateCodeFeedback(String code, CodingQuestion question, int testCasesPassed, int totalTestCases) {
        try {
            if (llmApiUrl.isEmpty()) {
                return generateLocalFeedback(code, question, testCasesPassed, totalTestCases);
            }
            
            return callExternalLLM(code, question, testCasesPassed, totalTestCases);
        } catch (Exception e) {
            logger.error("Error generating AI feedback", e);
            return generateLocalFeedback(code, question, testCasesPassed, totalTestCases);
        }
    }

    private String generateLocalFeedback(String code, CodingQuestion question, int passed, int total) {
        StringBuilder feedback = new StringBuilder();
        
        // Check correctness
        if (passed == total) {
            feedback.append("✓ All test cases passed! ");
        } else {
            feedback.append(String.format("⚠ Only %d/%d test cases passed. ", passed, total));
        }
        
        // Analyze code patterns
        if (code.contains("HashMap") || code.contains("HashSet")) {
            feedback.append("Good use of hash-based data structures for O(1) lookups. ");
        }
        
        if (code.contains("for") && code.contains("for")) {
            long loopCount = code.split("for").length - 1;
            if (loopCount > 2) {
                feedback.append("Multiple nested loops detected. Consider if O(n²) or higher complexity is necessary. ");
            }
        }
        
        if (!code.contains("//") && question.getDifficulty().equals("Hard")) {
            feedback.append("Consider adding comments to explain complex logic. ");
        }
        
        // Edge cases
        if (!code.contains("null") && question.getTitle().toLowerCase().contains("list")) {
            feedback.append("Remember to handle null/empty input cases. ");
        }
        
        return feedback.toString();
    }

    private String callExternalLLM(String code, CodingQuestion question, int passed, int total) {
        try {
            String prompt = buildPrompt(code, question, passed, total);
            
            String requestBody = String.format("""
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {"role": "system", "content": "You are a coding interview expert. Provide concise, actionable feedback."},
                        {"role": "user", "content": "%s"}
                    ],
                    "max_tokens": 200,
                    "temperature": 0.3
                }
                """, escapeJson(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseLLMResponse(response.body());
            }
            
            return generateLocalFeedback(code, question, passed, total);
            
        } catch (Exception e) {
            logger.error("LLM API call failed", e);
            return generateLocalFeedback(code, question, passed, total);
        }
    }

    private String buildPrompt(String code, CodingQuestion question, int passed, int total) {
        return String.format("""
            Analyze this Java solution for the problem: %s
            
            Test Results: %d/%d passed
            
            Code:
            ```java
            %s
            ```
            
            Provide 2-3 sentences of feedback on:
            1. Time/space complexity
            2. Code quality
            3. Any issues or improvements
            Keep feedback constructive and specific.
            """, question.getTitle(), passed, total, code);
    }

    private String parseLLMResponse(String json) {
        // Simple parsing - in production use a proper JSON library
        try {
            int start = json.indexOf("\"content\":\"");
            if (start > 0) {
                start += 11;
                int end = json.indexOf("\"", start);
                return json.substring(start, end).replace("\\n", " ").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            logger.error("Error parsing LLM response", e);
        }
        return "AI analysis completed.";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public Map<String, Object> analyzeBehavior(long tabSwitchCount, long violationCount, int riskScore) {
        Map<String, Object> analysis = new HashMap<>();
        
        StringBuilder behavioralFeedback = new StringBuilder();
        
        if (tabSwitchCount > 5) {
            behavioralFeedback.append("Frequent tab switching detected during exam. ");
        }
        
        if (violationCount > 3) {
            behavioralFeedback.append("Multiple security violations recorded. ");
        }
        
        if (riskScore > 50) {
            behavioralFeedback.append("Elevated risk profile due to irregular exam behavior.");
        }
        
        analysis.put("behavioralFeedback", behavioralFeedback.length() > 0 ? 
                behavioralFeedback.toString() : "Normal exam behavior observed.");
        analysis.put("riskLevel", riskScore > 70 ? "HIGH" : riskScore > 40 ? "MEDIUM" : "LOW");
        analysis.put("recommendations", generateRecommendations(tabSwitchCount, violationCount));
        
        return analysis;
    }

    private String generateRecommendations(long tabSwitches, long violations) {
        if (tabSwitches > 5 || violations > 3) {
            return "Consider reviewing exam protocols and ensuring candidate understands rules.";
        }
        return "Candidate followed exam guidelines appropriately.";
    }
}
