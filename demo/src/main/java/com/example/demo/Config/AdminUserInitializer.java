package com.example.demo.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

@Configuration
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin_password"));
                admin.setEmail("admin@example.com");
                admin.setRole("ADMIN");
                userRepository.save(admin);
                System.out.println("Admin user created.");
            }
        };
    }
}