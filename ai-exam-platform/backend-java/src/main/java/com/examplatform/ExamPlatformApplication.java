package com.examplatform;

import com.examplatform.model.User;
import com.examplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@SpringBootApplication
public class ExamPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamPlatformApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default candidate user if not exists
            if (!userRepository.existsByUsername("candidate")) {
                User candidate = User.builder()
                        .username("candidate")
                        .password(passwordEncoder.encode("password"))
                        .email("candidate@example.com")
                        .role(User.Role.CANDIDATE)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(candidate);
                System.out.println("✓ Default user created: candidate / password");
            }

            // Create default admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .role(User.Role.ADMIN)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                System.out.println("✓ Default admin created: admin / admin123");
            }
        };
    }
}
