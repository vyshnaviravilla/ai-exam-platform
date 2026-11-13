package com.examplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_violations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityViolation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;
    
    @Column(nullable = false)
    private String type;
    
    @Enumerated(EnumType.STRING)
    private Severity severity;
    
    private LocalDateTime timestamp;
    
    private Integer questionIndex;
    
    private String details;
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
