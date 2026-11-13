package com.examplatform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mcq_questions")
public class MCQQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String testType; // JAVA or PYTHON
    
    @Column(nullable = false)
    private Integer questionNumber;
    
    @Column(nullable = false, length = 1000)
    private String question;
    
    @Column(nullable = false)
    private String optionA;
    
    @Column(nullable = false)
    private String optionB;
    
    @Column(nullable = false)
    private String optionC;
    
    @Column(nullable = false)
    private String optionD;
    
    @Column(nullable = false)
    private String correctAnswer; // A, B, C, or D
    
    @Column
    private String explanation;
    
    public MCQQuestion() {}
    
    public MCQQuestion(String testType, Integer questionNumber, String question, 
                       String optionA, String optionB, String optionC, String optionD, 
                       String correctAnswer, String explanation) {
        this.testType = testType;
        this.questionNumber = questionNumber;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
