package com.examplatform.repository;

import com.examplatform.model.Answer;
import com.examplatform.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    List<Answer> findByExamSession(ExamSession examSession);
    
    Optional<Answer> findByExamSessionAndQuestionId(ExamSession examSession, Long questionId);
}
