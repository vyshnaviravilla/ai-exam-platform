package com.examplatform.controller;

import com.examplatform.model.ExamReport;
import com.examplatform.report.ReportGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportGeneratorService reportGenerator;

    @GetMapping("/{sessionId}")
    public ResponseEntity<ExamReport> getReport(@PathVariable Long sessionId) {
        ExamReport report = reportGenerator.generateReport(sessionId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{sessionId}/pdf")
    public ResponseEntity<byte[]> getPDFReport(@PathVariable Long sessionId) {
        byte[] pdfBytes = reportGenerator.generatePDFReport(sessionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "exam-report-" + sessionId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
