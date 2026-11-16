// PURPOSE: Creates the first Control Center admin from environment variables.
package com.biopower.config;

import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import com.biopower.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * Creates the first Control Center admin from environment variables when the user table has none.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "biopower.deployment.role", havingValue = "primary", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${biopower.bootstrap.admin-email:}")
    private String bootstrapAdminEmail;

    @Value("${biopower.bootstrap.admin-password:}")
    private String bootstrapAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.findByRole(UserRole.CONTROL_CENTER_ADMIN).isEmpty()) {
            return;
        }
        String email = bootstrapAdminEmail == null ? "" : bootstrapAdminEmail.trim().toLowerCase();
        String password = bootstrapAdminPassword == null ? "" : bootstrapAdminPassword;
        if (email.isBlank() || password.isBlank()) {
            log.error("No CONTROL_CENTER_ADMIN exists. Set SWARM_BOOTSTRAP_ADMIN_EMAIL and "
                    + "SWARM_BOOTSTRAP_ADMIN_PASSWORD for first-boot provisioning.");
            return;
        }
        if (password.length() < 12) {
            log.error("Bootstrap Control Center password is too short (minimum 12). Admin was not created.");
            return;
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            log.error("Bootstrap email already belongs to an existing user. Refusing to overwrite that account.");
            return;
        }
        User admin = User.builder()
                .name("SWARM Admin")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.CONTROL_CENTER_ADMIN)
                .status(UserStatus.ACTIVE)
                .assignedPlants(new HashSet<>())
                .build();
        userRepository.save(admin);
        log.info("Control Center admin created for {}", email);
    }
}
