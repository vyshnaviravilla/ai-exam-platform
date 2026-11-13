package com.examplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;
    
    @Column(nullable = false)
    private Long questionId;
    
    @Column(columnDefinition = "TEXT")
    private String code;
    
    private Integer testCasesPassed;
    
    private Integer totalTestCases;
    
    private Integer score;
    
    private String compilationOutput;
    
    private Double plagiarismScore;
    
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;
}
