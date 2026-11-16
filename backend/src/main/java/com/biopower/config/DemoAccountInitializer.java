// PURPOSE: Optional demo tenants/logins. Disabled unless SWARM_SEED_DEMO_ACCOUNTS=true.
package com.biopower.config;

import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.User;
import com.biopower.model.enums.CompanyStatus;
import com.biopower.model.enums.PlantStatus;
import com.biopower.model.enums.PlantType;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.PlantRepository;
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
import java.util.UUID;

/**
 * Seeds abc / Rebuild Demo Co SUPER_ADMIN logins only when explicitly enabled.
 * Never creates CONTROL_CENTER_ADMIN and never overwrites existing users.
 */
@Component
@Order(3)
@ConditionalOnProperty(name = "biopower.seed.demo-accounts", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoAccountInitializer implements CommandLineRunner {

    static final UUID ABC_TENANT = UUID.fromString("a11ce000-0000-4000-8000-000000000001");
    static final UUID REBUILD_TENANT = UUID.fromString("a11ce000-0000-4000-8000-000000000002");
    static final String ABC_EMAIL = "abc@gmail.com";
    static final String REBUILD_EMAIL = "rebuild.demo@swarm.local";

    private final CompanyRepository companyRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${biopower.seed.demo-password:}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        String password = demoPassword == null ? "" : demoPassword;
        if (password.length() < 12) {
            log.error("SWARM_SEED_DEMO_ACCOUNTS is true but SWARM_SEED_DEMO_PASSWORD is missing or shorter than 12. Demo accounts were not created.");
            return;
        }
        Company abc = ensureCompany(ABC_TENANT, "abc");
        Company rebuild = ensureCompany(REBUILD_TENANT, "Rebuild Demo Co");
        Plant abcPlant = ensurePlant(abc, "we", PlantType.ORGANIC_WASTE, false);
        Plant rebuildPlant = ensurePlant(rebuild, "Rebuild Demo Plant", PlantType.BIOGAS, true);
        ensureSuperAdmin("abcd", ABC_EMAIL, "9663036755", abc, abcPlant, password);
        ensureSuperAdmin("Rebuild Demo Admin", REBUILD_EMAIL, "9999999999", rebuild, rebuildPlant, password);
        log.info("Demo accounts are present for {} and {}", ABC_EMAIL, REBUILD_EMAIL);
    }

    private Company ensureCompany(UUID id, String name) {
        return companyRepository.findById(id).orElseGet(() -> companyRepository.save(Company.builder()
                .id(id)
                .name(name)
                .status(CompanyStatus.ACTIVE)
                .build()));
    }

    private Plant ensurePlant(Company company, String plantName, PlantType type, boolean hmiEnabled) {
        return plantRepository.findByCompany(company).stream()
                .filter(plant -> plantName.equals(plant.getPlantName()))
                .findFirst()
                .orElseGet(() -> plantRepository.save(Plant.builder()
                        .plantName(plantName)
                        .plantType(type)
                        .status(PlantStatus.ACTIVE)
                        .hmiEnabled(hmiEnabled)
                        .company(company)
                        .build()));
    }

    private void ensureSuperAdmin(String name, String email, String mobile, Company company, Plant plant, String rawPassword) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        User user = User.builder()
                .name(name)
                .email(email.trim().toLowerCase())
                .mobile(mobile)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .assignedPlants(new HashSet<>())
                .assignedSensorNodes(new HashSet<>())
                .build();
        user.getAssignedPlants().add(plant);
        userRepository.save(user);
    }
}
