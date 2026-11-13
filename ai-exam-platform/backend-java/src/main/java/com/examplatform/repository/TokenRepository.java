package com.examplatform.repository;

import com.examplatform.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenValue(String tokenValue);
    
    Optional<Token> findByUsernameAndIsUsedFalse(String username);
    
    boolean existsByTokenValue(String tokenValue);
}
