package com.examplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingQuestion {
    
    private Long id;
    private String title;
    private String difficulty;
    private String language;
    private String description;
    private List<Example> examples;
    private List<String> constraints;
    private String starterCode;
    private List<TestCase> testCases;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Example {
        private String input;
        private String output;
        private String explanation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private String input;
        private String expected;
        private boolean hidden;
    }
}
