package com.examplatform.repository;

import com.examplatform.model.ExamSession;
import com.examplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    
    List<ExamSession> findByUserOrderByStartTimeDesc(User user);
    
    List<ExamSession> findByUserAndStatus(User user, ExamSession.Status status);
    
    Optional<ExamSession> findByIdAndUser(Long id, User user);
}
