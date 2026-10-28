package com.sirji.homecare.service;

import com.sirji.homecare.domain.AdminUser;
import com.sirji.homecare.domain.UserRole;
import com.sirji.homecare.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AdminUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(
        AdminUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.bootstrap-admin.email:}") String email,
        @Value("${app.bootstrap-admin.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Set both bootstrap admin email and password, or leave both unset.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("Bootstrap admin password must contain at least 12 characters.");
        }
        if (userRepository.findByEmailIgnoreCase(email.trim()).isPresent()) return;

        AdminUser admin = new AdminUser();
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
    }
}
