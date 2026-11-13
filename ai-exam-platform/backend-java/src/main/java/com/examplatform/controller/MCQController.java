package com.examplatform.controller;

import com.examplatform.entity.MCQQuestion;
import com.examplatform.repository.MCQQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcq")
@CrossOrigin(origins = "*")
public class MCQController {
    
    @Autowired
    private MCQQuestionRepository mcqRepository;
    
    @GetMapping("/questions/{testType}")
    public ResponseEntity<?> getQuestions(@PathVariable String testType) {
        try {
            List<MCQQuestion> questions = mcqRepository.findByTestTypeOrderByQuestionNumberAsc(testType.toUpperCase());
            
            // Remove correct answers before sending to frontend
            List<Map<String, Object>> safeQuestions = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("questionNumber", q.getQuestionNumber());
                map.put("question", q.getQuestion());
                map.put("optionA", q.getOptionA());
                map.put("optionB", q.getOptionB());
                map.put("optionC", q.getOptionC());
                map.put("optionD", q.getOptionD());
                return map;
            }).toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", safeQuestions);
            response.put("count", safeQuestions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitMCQ(@RequestBody MCQSubmitRequest request) {
        try {
            List<MCQQuestion> questions = mcqRepository.findByTestTypeOrderByQuestionNumberAsc(request.getTestType().toUpperCase());
            
            int correct = 0;
            int total = questions.size();
            
            for (MCQQuestion q : questions) {
                String userAnswer = request.getAnswers().get(q.getId().toString());
                if (userAnswer != null && userAnswer.equals(q.getCorrectAnswer())) {
                    correct++;
                }
            }
            
            double score = (double) correct / total * 100;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("correct", correct);
            response.put("total", total);
            response.put("score", Math.round(score));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    public static class MCQSubmitRequest {
        private String testType;
        private Map<String, String> answers;
        
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
        public Map<String, String> getAnswers() { return answers; }
        public void setAnswers(Map<String, String> answers) { this.answers = answers; }
    }
}
