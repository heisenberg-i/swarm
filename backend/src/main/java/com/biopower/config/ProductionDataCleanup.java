package com.biopower.config;

import com.biopower.model.entity.Plant;
import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import com.biopower.repository.*;
import com.biopower.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes legacy demo plants/users and ensures the Control Center admin exists.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "biopower.deployment.role", havingValue = "primary", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ProductionDataCleanup implements CommandLineRunner {

    public static final String PLATFORM_ADMIN_EMAIL = "swarm.nfi@gmail.com";
    public static final String PLATFORM_ADMIN_PASSWORD = "Swarm@2026";
    private static final String CLEANUP_FLAG = "production.cleanup.v1.done";

    private static final Set<String> LEGACY_USER_EMAILS = Set.of(
            "admin@biopower.com",
            "manager@biopower.com",
            "operator@biopower.com",
            "tata.admin@tatasteel.com",
            "tata.operator@tatasteel.com"
    );

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final SettingsService settingsService;

    @Override
    @Transactional
    public void run(String... args) {
        boolean firstMigration = !settingsService.getSettingsMap().containsKey(CLEANUP_FLAG);

        removeLegacyUsers();
        renameTataBranding();
        ensurePlatformAdmin(firstMigration);

        if (firstMigration) {
            com.biopower.dto.request.SettingsRequest flag = new com.biopower.dto.request.SettingsRequest();
            flag.setSettingKey(CLEANUP_FLAG);
            flag.setSettingValue("true");
            flag.setCategory("SYSTEM");
            flag.setDescription("Production cleanup migrated dummy plants and platform admin");
            settingsService.saveSetting(flag);
            log.info("Production data cleanup completed (first migration).");
        }
    }

    private void removeLegacyUsers() {
        for (String email : LEGACY_USER_EMAILS) {
            userRepository.findByEmail(email).ifPresent(user -> {
                log.info("Removing legacy demo user: {}", email);
                if (user.getAssignedPlants() != null) {
                    user.getAssignedPlants().clear();
                }
                if (user.getAssignedSensorNodes() != null) {
                    user.getAssignedSensorNodes().clear();
                }
                userRepository.save(user);
                userRepository.delete(user);
            });
        }
    }

    private void renameTataBranding() {
        for (Plant plant : plantRepository.findAll()) {
            if (plant.getPlantName() != null && plant.getPlantName().toLowerCase().contains("tata")) {
                String renamed = plant.getPlantName().replaceAll("(?i)tata steel\\s*", "").trim();
                if (renamed.isBlank()) {
                    renamed = "Biomethanation Plant";
                }
                log.info("Renaming plant '{}' -> '{}'", plant.getPlantName(), renamed);
                plant.setPlantName(renamed);
                plantRepository.save(plant);
            }
        }
        for (var company : companyRepository.findAll()) {
            if (company.getName() != null && company.getName().toLowerCase().contains("tata")) {
                String renamed = company.getName().replaceAll("(?i)tata steel\\s*", "").trim();
                if (renamed.isBlank()) {
                    renamed = "Customer";
                }
                log.info("Renaming company '{}' -> '{}'", company.getName(), renamed);
                company.setName(renamed);
                companyRepository.save(company);
            }
        }
    }

    private void ensurePlatformAdmin(boolean setPassword) {
        userRepository.findByEmail(PLATFORM_ADMIN_EMAIL).ifPresentOrElse(user -> {
            user.setRole(UserRole.CONTROL_CENTER_ADMIN);
            user.setStatus(UserStatus.ACTIVE);
            user.setName("SWARM Admin");
            user.setCompany(null);
            user.getAssignedPlants().clear();
            if (setPassword) {
                user.setPassword(passwordEncoder.encode(PLATFORM_ADMIN_PASSWORD));
            }
            userRepository.save(user);
            log.info("Control Center admin updated: {}", PLATFORM_ADMIN_EMAIL);
        }, () -> {
            User admin = User.builder()
                    .name("SWARM Admin")
                    .email(PLATFORM_ADMIN_EMAIL)
                    .password(passwordEncoder.encode(PLATFORM_ADMIN_PASSWORD))
                    .role(UserRole.CONTROL_CENTER_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .assignedPlants(new HashSet<>())
                    .build();
            userRepository.save(admin);
            log.info("Control Center admin created: {}", PLATFORM_ADMIN_EMAIL);
        });
    }
}
