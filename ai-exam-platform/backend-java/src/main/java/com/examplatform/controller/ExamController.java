package com.examplatform.controller;

import com.examplatform.model.CodingQuestion;
import com.examplatform.model.ExamReport;
import com.examplatform.model.ExamSession;
import com.examplatform.model.SecurityViolation;
import com.examplatform.security.CustomUserDetailsService;
import com.examplatform.service.CodeExecutionService;
import com.examplatform.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/start")
    public ResponseEntity<?> startExam(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username required"));
        }
        ExamSession session = examService.startExam(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("startTime", session.getStartTime());
        response.put("durationMinutes", session.getDurationMinutes());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<CodingQuestion>> getQuestions(@RequestParam(required = false) String testType) {
        String type = testType != null ? testType.toUpperCase() : "JAVA";
        return ResponseEntity.ok(examService.getQuestionsByType(type));
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeCode(@RequestBody ExecuteRequest request) {
        try {
            CodeExecutionService.ExecutionResult result = examService.executeCode(
                    request.getSessionId(),
                    request.getQuestionId(),
                    request.getCode()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/violation")
    public ResponseEntity<?> recordViolation(@RequestBody ViolationRequest request) {
        try {
            SecurityViolation violation = SecurityViolation.builder()
                    .type(request.getType())
                    .severity(SecurityViolation.Severity.valueOf(request.getSeverity()))
                    .questionIndex(request.getQuestionIndex())
                    .build();
            
            examService.recordViolation(request.getSessionId(), violation);
            return ResponseEntity.ok(Map.of("message", "Violation recorded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestBody SubmitRequest request) {
        try {
            List<SecurityViolation> violations = request.getViolations().stream()
                    .map(v -> SecurityViolation.builder()
                            .type(v.getType())
                            .severity(SecurityViolation.Severity.valueOf(v.getSeverity()))
                            .build())
                    .toList();
            
            ExamReport report = examService.submitExam(
                    request.getSessionId(),
                    request.getAnswers(),
                    violations,
                    request.getTimeTaken(),
                    request.getRiskScore(),
                    request.getSubmitReason()
            );
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public static class ExecuteRequest {
        private Long sessionId;
        private Long questionId;
        private String code;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class ViolationRequest {
        private Long sessionId;
        private String type;
        private String severity;
        private Integer questionIndex;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Integer getQuestionIndex() { return questionIndex; }
        public void setQuestionIndex(Integer questionIndex) { this.questionIndex = questionIndex; }
    }

    public static class ViolationDto {
        private String type;
        private String severity;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class SubmitRequest {
        private Long sessionId;
        private Map<String, String> answers;
        private List<ViolationDto> violations;
        private long timeTaken;
        private int riskScore;
        private String submitReason;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Map<String, String> getAnswers() { return answers; }
        public void setAnswers(Map<String, String> answers) { this.answers = answers; }
        public List<ViolationDto> getViolations() { return violations; }
        public void setViolations(List<ViolationDto> violations) { this.violations = violations; }
        public long getTimeTaken() { return timeTaken; }
        public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public String getSubmitReason() { return submitReason; }
        public void setSubmitReason(String submitReason) { this.submitReason = submitReason; }
    }
}
