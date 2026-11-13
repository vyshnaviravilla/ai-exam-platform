package com.examplatform.service;

import com.examplatform.entity.Token;
import com.examplatform.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {
    
    @Autowired
    private TokenRepository tokenRepository;
    
    public Token generateToken(String username, String testType) {
        // Invalidate any existing unused tokens for this user
        Optional<Token> existingToken = tokenRepository.findByUsernameAndIsUsedFalse(username);
        existingToken.ifPresent(token -> {
            token.setIsUsed(true);
            tokenRepository.save(token);
        });
        
        // Generate new token
        Token token = new Token(username, testType);
        return tokenRepository.save(token);
    }
    
    public Map<String, Object> validateToken(String tokenValue, String username, String testType) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
        
        if (tokenOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "Invalid token");
            return result;
        }
        
        Token token = tokenOpt.get();
        
        if (!token.getUsername().equals(username)) {
            result.put("valid", false);
            result.put("message", "Token does not belong to you");
            return result;
        }
        
        if (!token.getTestType().equalsIgnoreCase(testType)) {
            result.put("valid", false);
            result.put("message", "Token is for different test type");
            return result;
        }
        
        if (token.isExpired()) {
            result.put("valid", false);
            result.put("message", "Token has expired");
            return result;
        }
        
        if (token.getIsUsed()) {
            result.put("valid", false);
            result.put("message", "Token has already been used");
            return result;
        }
        
        // Mark token as used
        token.setIsUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        
        result.put("valid", true);
        result.put("message", "Token validated successfully");
        result.put("token", token);
        return result;
    }
    
    public Optional<Token> getTokenByValue(String tokenValue) {
        return tokenRepository.findByTokenValue(tokenValue);
    }
    
    public void incrementViolation(String tokenValue) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
        tokenOpt.ifPresent(token -> {
            token.setViolationCount(token.getViolationCount() + 1);
            tokenRepository.save(token);
        });
    }
}
