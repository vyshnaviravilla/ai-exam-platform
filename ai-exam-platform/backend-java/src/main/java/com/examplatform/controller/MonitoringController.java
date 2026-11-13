package com.examplatform.controller;

import com.examplatform.monitoring.ProcessMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private ProcessMonitoringService processMonitoringService;

    @GetMapping("/processes")
    public ResponseEntity<?> checkProcesses() {
        ProcessMonitoringService.ProcessScanResult result = processMonitoringService.scanForSuspiciousProcesses();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/terminate")
    public ResponseEntity<?> terminateProcess(@RequestBody Map<String, String> request) {
        String processName = request.get("processName");
        if (processName == null || processName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Process name required"));
        }
        
        boolean terminated = processMonitoringService.terminateProcess(processName);
        if (terminated) {
            return ResponseEntity.ok(Map.of("message", "Process terminated: " + processName));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not terminate process: " + processName));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
