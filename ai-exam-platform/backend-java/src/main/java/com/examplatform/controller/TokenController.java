package com.examplatform.controller;

import com.examplatform.entity.Token;
import com.examplatform.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
@CrossOrigin(origins = "*")
public class TokenController {
    
    @Autowired
    private TokenService tokenService;
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateToken(@RequestBody TokenRequest request) {
        try {
            Token token = tokenService.generateToken(request.getUsername(), request.getTestType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token.getTokenValue());
            response.put("expiresAt", token.getExpiresAt().toString());
            response.put("message", "Token generated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody ValidateRequest request) {
        Map<String, Object> result = tokenService.validateToken(
            request.getToken(), 
            request.getUsername(), 
            request.getTestType()
        );
        
        return ResponseEntity.ok(result);
    }
    
    public static class TokenRequest {
        private String username;
        private String testType;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
    }
    
    public static class ValidateRequest {
        private String token;
        private String username;
        private String testType;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
    }
}
