package com.examplatform.service;

import com.examplatform.model.*;
import com.examplatform.model.ExamSession.Status;
import com.examplatform.repository.AnswerRepository;
import com.examplatform.repository.ExamSessionRepository;
import com.examplatform.repository.SecurityViolationRepository;
import com.examplatform.repository.UserRepository;
import com.examplatform.ai.AIFeedbackService;
import com.examplatform.plagiarism.PlagiarismDetectionService;
import com.examplatform.report.ReportGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SecurityViolationRepository violationRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Autowired
    private PlagiarismDetectionService plagiarismService;

    @Autowired
    private AIFeedbackService aiFeedbackService;

    @Autowired
    private ReportGeneratorService reportGenerator;

    @Autowired
    private CheatingDetectionEngine cheatingEngine;

    public ExamSession startExam(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if there's already an active session
        List<ExamSession> activeSessions = examSessionRepository.findByUserAndStatus(user, Status.IN_PROGRESS);
        if (!activeSessions.isEmpty()) {
            return activeSessions.get(0);
        }

        ExamSession session = ExamSession.builder()
                .user(user)
                .startTime(LocalDateTime.now())
                .durationMinutes(90) // 1.5 hours
                .status(Status.IN_PROGRESS)
                .riskScore(0)
                .build();

        return examSessionRepository.save(session);
    }

    public List<CodingQuestion> getQuestions() {
        return questionService.getAllQuestions();
    }

    public List<CodingQuestion> getQuestionsByType(String testType) {
        return questionService.getQuestionsByType(testType);
    }

    @Transactional
    public void recordViolation(Long sessionId, SecurityViolation violation) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        violation.setExamSession(session);
        violation.setTimestamp(LocalDateTime.now());
        violationRepository.save(violation);

        // Update risk score
        int riskIncrement = calculateRiskIncrement(violation.getSeverity());
        session.setRiskScore(Math.min(100, session.getRiskScore() + riskIncrement));
        examSessionRepository.save(session);

        logger.warn("Violation recorded for session {}: {} ({})", 
                sessionId, violation.getType(), violation.getSeverity());
    }

    private int calculateRiskIncrement(SecurityViolation.Severity severity) {
        return switch (severity) {
            case CRITICAL -> 30;
            case HIGH -> 15;
            case MEDIUM -> 8;
            case LOW -> 3;
        };
    }

    public CodeExecutionService.ExecutionResult executeCode(Long sessionId, Long questionId, String code) {
        CodingQuestion question = questionService.getQuestionById(questionId);
        if (question == null) {
            throw new RuntimeException("Question not found");
        }

        List<CodeExecutionService.TestCase> testCases = question.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .map(tc -> new CodeExecutionService.TestCase(tc.getInput(), tc.getExpected()))
                .toList();

        return codeExecutionService.executeJavaCode(code, testCases);
    }

    @Transactional
    public void saveAnswer(Long sessionId, Long questionId, String code, CodeExecutionService.ExecutionResult result) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        CodingQuestion question = questionService.getQuestionById(questionId);

        // Check plagiarism
        double plagiarismScore = 0.0;
        try {
            // Compare with existing answers
            List<Answer> existingAnswers = answerRepository.findAll();
            for (Answer existing : existingAnswers) {
                if (existing.getCode() != null && !existing.getExamSession().getId().equals(sessionId)) {
                    PlagiarismDetectionService.PlagiarismResult sim = 
                            plagiarismService.checkSimilarity(code, existing.getCode());
                    if (sim.getSimilarityScore() > plagiarismScore) {
                        plagiarismScore = sim.getSimilarityScore();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Plagiarism check error", e);
        }

        // Generate AI feedback
        String aiFeedback = aiFeedbackService.generateCodeFeedback(
                code, question, 
                result.getTestCasesPassed(), 
                result.getTotalTestCases()
        );

        // Calculate score
        int score = 0;
        if (result.getTotalTestCases() > 0) {
            score = (result.getTestCasesPassed() * 100) / result.getTotalTestCases();
        }

        Answer answer = answerRepository.findByExamSessionAndQuestionId(session, questionId)
                .orElse(new Answer());

        answer.setExamSession(session);
        answer.setQuestionId(questionId);
        answer.setCode(code);
        answer.setTestCasesPassed(result.getTestCasesPassed());
        answer.setTotalTestCases(result.getTotalTestCases());
        answer.setScore(score);
        answer.setPlagiarismScore(plagiarismScore);
        answer.setAiFeedback(aiFeedback);

        answerRepository.save(answer);
    }

    @Transactional
    public ExamReport submitExam(Long sessionId, Map<String, String> answers, 
                                   List<SecurityViolation> violations, 
                                   long timeTaken, int finalRiskScore, String reason) {
        
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Save any remaining answers
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Long questionId = Long.parseLong(entry.getKey());
            String code = entry.getValue();
            
            Answer answer = answerRepository.findByExamSessionAndQuestionId(session, questionId)
                    .orElse(null);
            
            if (answer == null) {
                // Execute and save
                CodeExecutionService.ExecutionResult result = executeCode(sessionId, questionId, code);
                saveAnswer(sessionId, questionId, code, result);
            }
        }

        // Record final violations
        for (SecurityViolation v : violations) {
            recordViolation(sessionId, v);
        }

        // Update session
        session.setEndTime(LocalDateTime.now());
        session.setRiskScore(finalRiskScore);
        session.setSubmitReason(reason);
        
        // Determine final status
        CheatingDetectionEngine.CheatingScore score = cheatingEngine.calculateRiskScore(
                violationRepository.findByExamSession(session), 
                finalRiskScore
        );
        
        if (score.isAutoDisqualified()) {
            session.setStatus(Status.AUTO_SUBMITTED);
        } else {
            session.setStatus(Status.COMPLETED);
        }

        examSessionRepository.save(session);

        return reportGenerator.generateReport(sessionId);
    }

    public ExamSession getSession(Long sessionId) {
        return examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }
}
