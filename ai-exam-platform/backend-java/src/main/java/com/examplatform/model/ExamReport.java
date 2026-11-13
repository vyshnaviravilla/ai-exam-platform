package com.examplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamReport {
    
    private Long sessionId;
    private String candidateName;
    private Integer score;
    private Integer totalScore;
    private Integer testCasesPassed;
    private Integer totalTestCases;
    private Long timeTaken;
    private Integer riskScore;
    private Integer violationCount;
    private String submitReason;
    private LocalDateTime completedAt;
    private List<ViolationInfo> violations;
    private Map<Long, AnswerInfo> answers;
    private String aiFeedback;
    private String optimizationTips;
    private Double avgPlagiarismScore;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViolationInfo {
        private String type;
        private String severity;
        private LocalDateTime timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerInfo {
        private Long questionId;
        private Integer testCasesPassed;
        private Integer totalTestCases;
        private Integer score;
        private String aiFeedback;
        private Double plagiarismScore;
    }
}
