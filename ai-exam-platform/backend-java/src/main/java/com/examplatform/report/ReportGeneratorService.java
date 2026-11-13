package com.examplatform.report;

import com.examplatform.model.*;
import com.examplatform.plagiarism.PlagiarismDetectionService;
import com.examplatform.repository.AnswerRepository;
import com.examplatform.repository.ExamSessionRepository;
import com.examplatform.repository.SecurityViolationRepository;
import com.examplatform.service.CheatingDetectionEngine;
import com.examplatform.ai.AIFeedbackService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorService.class);

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SecurityViolationRepository violationRepository;

    @Autowired
    private PlagiarismDetectionService plagiarismService;

    @Autowired
    private CheatingDetectionEngine cheatingEngine;

    @Autowired
    private AIFeedbackService aiFeedbackService;

    public ExamReport generateReport(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Answer> answers = answerRepository.findByExamSession(session);
        List<SecurityViolation> violations = violationRepository.findByExamSession(session);

        int totalScore = calculateTotalScore(answers);
        int testCasesPassed = answers.stream().mapToInt(a -> a.getTestCasesPassed() != null ? a.getTestCasesPassed() : 0).sum();
        int totalTestCases = answers.stream().mapToInt(a -> a.getTotalTestCases() != null ? a.getTotalTestCases() : 0).sum();
        
        CheatingDetectionEngine.CheatingScore cheatingScore = cheatingEngine.calculateRiskScore(violations, session.getRiskScore() != null ? session.getRiskScore() : 0);
        
        double avgPlagiarism = answers.stream()
                .filter(a -> a.getPlagiarismScore() != null)
                .mapToDouble(Answer::getPlagiarismScore)
                .average()
                .orElse(0.0);

        // Generate AI feedback
        String aiFeedback = generateAIFeedback(answers, violations, cheatingScore);
        String optimizationTips = generateOptimizationTips(answers);

        return ExamReport.builder()
                .sessionId(sessionId)
                .candidateName(session.getUser().getUsername())
                .score(totalScore)
                .totalScore(answers.size() * 100)
                .testCasesPassed(testCasesPassed)
                .totalTestCases(totalTestCases)
                .timeTaken(session.getEndTime() != null ? 
                    java.time.Duration.between(session.getStartTime(), session.getEndTime()).getSeconds() : 0)
                .riskScore(cheatingScore.getScore())
                .violationCount(violations.size())
                .submitReason(session.getSubmitReason())
                .completedAt(session.getEndTime())
                .violations(mapViolations(violations))
                .answers(mapAnswers(answers))
                .aiFeedback(aiFeedback)
                .optimizationTips(optimizationTips)
                .avgPlagiarismScore(avgPlagiarism)
                .build();
    }

    private int calculateTotalScore(List<Answer> answers) {
        if (answers.isEmpty()) return 0;
        
        int total = 0;
        for (Answer answer : answers) {
            if (answer.getTestCasesPassed() != null && answer.getTotalTestCases() != null 
                    && answer.getTotalTestCases() > 0) {
                int questionScore = (answer.getTestCasesPassed() * 100) / answer.getTotalTestCases();
                total += questionScore;
            }
        }
        return total / answers.size();
    }

    private List<ExamReport.ViolationInfo> mapViolations(List<SecurityViolation> violations) {
        return violations.stream()
                .map(v -> ExamReport.ViolationInfo.builder()
                        .type(v.getType())
                        .severity(v.getSeverity().name())
                        .timestamp(v.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Long, ExamReport.AnswerInfo> mapAnswers(List<Answer> answers) {
        Map<Long, ExamReport.AnswerInfo> map = new HashMap<>();
        for (Answer answer : answers) {
            map.put(answer.getQuestionId(), ExamReport.AnswerInfo.builder()
                    .questionId(answer.getQuestionId())
                    .testCasesPassed(answer.getTestCasesPassed())
                    .totalTestCases(answer.getTotalTestCases())
                    .score(answer.getScore())
                    .aiFeedback(answer.getAiFeedback())
                    .plagiarismScore(answer.getPlagiarismScore())
                    .build());
        }
        return map;
    }

    private String generateAIFeedback(List<Answer> answers, List<SecurityViolation> violations, 
                                       CheatingDetectionEngine.CheatingScore score) {
        StringBuilder feedback = new StringBuilder();
        
        // Performance feedback
        int avgScore = answers.isEmpty() ? 0 : answers.stream()
                .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                .sum() / answers.size();
        
        if (avgScore >= 90) {
            feedback.append("Excellent performance! Your solutions demonstrate strong algorithmic understanding. ");
        } else if (avgScore >= 70) {
            feedback.append("Good performance. Your solutions are correct but may benefit from optimization. ");
        } else if (avgScore >= 50) {
            feedback.append("Satisfactory performance. Some solutions need improvement in correctness or efficiency. ");
        } else {
            feedback.append("Performance needs improvement. Review the fundamental concepts and test your code thoroughly. ");
        }
        
        // Behavioral feedback
        if (!violations.isEmpty()) {
            feedback.append(" During the exam, ").append(violations.size())
                    .append(" security events were recorded. ");
            
            if (score.isFlaggedForReview()) {
                feedback.append("Your session has been flagged for manual review due to irregularities.");
            }
        } else {
            feedback.append(" Your exam session was clean with no security violations recorded.");
        }
        
        return feedback.toString();
    }

    private String generateOptimizationTips(List<Answer> answers) {
        StringBuilder tips = new StringBuilder();
        
        for (Answer answer : answers) {
            if (answer.getScore() != null && answer.getScore() < 100) {
                tips.append("Question ").append(answer.getQuestionId())
                        .append(": Consider optimizing your solution for better time/space complexity. ");
            }
        }
        
        if (tips.length() == 0) {
            tips.append("All solutions achieved full marks. Consider studying advanced optimization techniques for future challenges.");
        }
        
        return tips.toString();
    }

    public byte[] generatePDFReport(Long sessionId) {
        try {
            ExamReport report = generateReport(sessionId);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);

            // Title
            document.add(new Paragraph("AI Secure Exam Platform - Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            
            document.add(new Paragraph("\n"));

            // Candidate Info
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            infoTable.addCell(new Cell().add(new Paragraph("Candidate:").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(report.getCandidateName())));
            infoTable.addCell(new Cell().add(new Paragraph("Session ID:").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getSessionId()))));
            infoTable.addCell(new Cell().add(new Paragraph("Completed:").setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(report.getCompletedAt() != null ? 
                    report.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));
            document.add(infoTable);
            
            document.add(new Paragraph("\n"));

            // Score Section
            document.add(new Paragraph("Performance Summary").setBold().setFontSize(14));
            Table scoreTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
            scoreTable.addCell(new Cell().add(new Paragraph("Overall Score:")));
            scoreTable.addCell(new Cell().add(new Paragraph(report.getScore() + "%")));
            scoreTable.addCell(new Cell().add(new Paragraph("Test Cases Passed:")));
            scoreTable.addCell(new Cell().add(new Paragraph(report.getTestCasesPassed() + "/" + report.getTotalTestCases())));
            scoreTable.addCell(new Cell().add(new Paragraph("Time Taken:")));
            scoreTable.addCell(new Cell().add(new Paragraph(formatDuration(report.getTimeTaken()))));
            scoreTable.addCell(new Cell().add(new Paragraph("Risk Score:")));
            scoreTable.addCell(new Cell().add(new Paragraph(report.getRiskScore() + "/100")));
            document.add(scoreTable);

            document.add(new Paragraph("\n"));

            // AI Feedback
            document.add(new Paragraph("AI Feedback").setBold().setFontSize(14));
            document.add(new Paragraph(report.getAiFeedback()));
            
            document.add(new Paragraph("\n"));

            // Violations
            if (!report.getViolations().isEmpty()) {
                document.add(new Paragraph("Security Violations").setBold().setFontSize(14));
                for (ExamReport.ViolationInfo v : report.getViolations()) {
                    document.add(new Paragraph("• [" + v.getSeverity() + "] " + v.getType() + 
                            " at " + v.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                }
            }

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String formatDuration(Long seconds) {
        if (seconds == null) return "N/A";
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d min %d sec", mins, secs);
    }
}
