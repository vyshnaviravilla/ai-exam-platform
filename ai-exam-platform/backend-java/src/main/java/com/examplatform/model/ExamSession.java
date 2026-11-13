package com.examplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer durationMinutes;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private Integer totalScore;
    
    private Integer riskScore;
    
    private String submitReason;
    
    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>();
    
    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityViolation> violations = new ArrayList<>();
    
    public enum Status {
        IN_PROGRESS, COMPLETED, AUTO_SUBMITTED, CANCELLED
    }
}
