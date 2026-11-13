package com.examplatform.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMonitoringService.class);
    
    private static final Set<String> SUSPICIOUS_PROCESSES = Set.of(
            // Remote desktop / screen sharing
            "anydesk", "teamviewer", "chrome remote desktop", "microsoft remote assistance",
            "ultraviewer", "supremo", "showmypc", "screenleap", "join.me",
            
            // Screen recording
            "obs", "camtasia", "snagit", "bandicam", "fraps", "screencast-o-matic",
            "loom", "screencastify", "apowersoft", "movavi", "icecream",
            
            // Communication/Voice
            "discord", "skype", "zoom", "webex", "gotomeeting", "slack",
            
            // VM/Container (potential sandbox evasion)
            "vmware", "virtualbox", "parallels", "hyper-v", "docker", "qemu",
            
            // Browser instances (for external searches)
            "chrome.exe", "firefox.exe", "msedge.exe", "safari.exe",
            
            // IDE/Code editors (for copying)
            "idea", "pycharm", "vscode", "sublime_text", "atom", "eclipse",
            
            // AI assistants
            "chatgpt", "claude", "copilot", "bard", "codeium", "tabnine",
            "github copilot", "amazon q", "blackbox"
    );

    public ProcessScanResult scanForSuspiciousProcesses() {
        try {
            List<String> suspicious = detectRunningProcesses();
            
            return ProcessScanResult.builder()
                    .suspiciousFound(!suspicious.isEmpty())
                    .processes(suspicious)
                    .timestamp(new Date())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error scanning processes", e);
            return ProcessScanResult.builder()
                    .suspiciousFound(false)
                    .processes(Collections.emptyList())
                    .error(e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    private List<String> detectRunningProcesses() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        List<String> processes = new ArrayList<>();
        
        if (os.contains("win")) {
            // Windows
            Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String processName = parseWindowsProcess(line);
                    if (processName != null && isSuspicious(processName)) {
                        processes.add(processName);
                    }
                }
            }
        } else {
            // Linux/Mac
            Process process = Runtime.getRuntime().exec("ps aux");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String processName = parseUnixProcess(line);
                    if (processName != null && isSuspicious(processName)) {
                        processes.add(processName);
                    }
                }
            }
        }
        
        return processes.stream().distinct().collect(Collectors.toList());
    }

    private String parseWindowsProcess(String csvLine) {
        String[] parts = csvLine.split("\"");
        if (parts.length >= 2) {
            return parts[1].toLowerCase();
        }
        return null;
    }

    private String parseUnixProcess(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 11) {
            return parts[10].toLowerCase();
        }
        return null;
    }

    private boolean isSuspicious(String processName) {
        String lower = processName.toLowerCase();
        return SUSPICIOUS_PROCESSES.stream()
                .anyMatch(suspicious -> lower.contains(suspicious.toLowerCase()));
    }

    public boolean terminateProcess(String processName) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            
            if (os.contains("win")) {
                // Windows: taskkill /F /IM process.exe
                process = Runtime.getRuntime().exec("taskkill /F /IM " + processName);
            } else {
                // Linux/Mac: killall process
                process = Runtime.getRuntime().exec("killall -9 " + processName);
            }
            
            boolean exited = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            int exitCode = process.exitValue();
            
            if (exited && exitCode == 0) {
                logger.info("Successfully terminated process: {}", processName);
                return true;
            } else {
                logger.warn("Failed to terminate process: {} (exit code: {})", processName, exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error terminating process: " + processName, e);
            return false;
        }
    }

    public static class ProcessScanResult {
        private boolean suspiciousFound;
        private List<String> processes;
        private String error;
        private Date timestamp;

        public static Builder builder() {
            return new Builder();
        }

        public boolean isSuspiciousFound() { return suspiciousFound; }
        public List<String> getProcesses() { return processes; }
        public String getError() { return error; }
        public Date getTimestamp() { return timestamp; }

        public static class Builder {
            private ProcessScanResult result = new ProcessScanResult();

            public Builder suspiciousFound(boolean found) {
                result.suspiciousFound = found;
                return this;
            }

            public Builder processes(List<String> processes) {
                result.processes = processes;
                return this;
            }

            public Builder error(String error) {
                result.error = error;
                return this;
            }

            public Builder timestamp(Date timestamp) {
                result.timestamp = timestamp;
                return this;
            }

            public ProcessScanResult build() {
                return result;
            }
        }
    }
}
