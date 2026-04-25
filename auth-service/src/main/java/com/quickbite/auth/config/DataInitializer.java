package com.quickbite.auth.config;

import com.quickbite.auth.entity.Role;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.findByEmail("admin@quickbite.com").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                    System.out.println("Admin password verified and reset to: admin123");
                },
                () -> {
                    User admin = User.builder()
                            .name("Super Admin")
                            .email("admin@quickbite.com")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Default Admin user created: admin@quickbite.com / admin123");
                }
            );
        };
    }
}
