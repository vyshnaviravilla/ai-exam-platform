package com.examplatform.repository;

import com.examplatform.model.SecurityViolation;
import com.examplatform.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityViolationRepository extends JpaRepository<SecurityViolation, Long> {
    
    List<SecurityViolation> findByExamSession(ExamSession examSession);
    
    List<SecurityViolation> findByExamSessionAndSeverity(ExamSession examSession, SecurityViolation.Severity severity);
}
