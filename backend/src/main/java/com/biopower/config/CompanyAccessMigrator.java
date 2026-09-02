package com.biopower.config;

import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.User;
import com.biopower.model.enums.CompanyStatus;
import com.biopower.model.enums.UserRole;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Links existing Super Admins and plants into companies after Control Center rollout.
 */
@Component
@Order(80)
@RequiredArgsConstructor
@Slf4j
public class CompanyAccessMigrator implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (User user : userRepository.findByRole(UserRole.SUPER_ADMIN)) {
            if (user.getCompany() != null) {
                continue;
            }
            String companyName = uniqueCompanyName(user.getName() != null ? user.getName() : user.getEmail());
            Company company = companyRepository.save(Company.builder()
                    .name(companyName)
                    .status(CompanyStatus.ACTIVE)
                    .build());
            user.getAssignedPlants().size();
            user.setCompany(company);
            userRepository.save(user);
            for (Plant plant : user.getAssignedPlants()) {
                if (plant.getCompany() == null) {
                    plant.setCompany(company);
                    plantRepository.save(plant);
                }
            }
            log.info("Migrated Super Admin {} into company {}", user.getEmail(), companyName);
        }
        for (User user : userRepository.findAll()) {
            if (user.getRole() == UserRole.CONTROL_CENTER_ADMIN || user.getCompany() != null) {
                continue;
            }
            user.getAssignedPlants().size();
            Company fromPlant = null;
            for (Plant plant : user.getAssignedPlants()) {
                if (plant.getCompany() != null) {
                    fromPlant = plant.getCompany();
                    break;
                }
            }
            if (fromPlant != null) {
                user.setCompany(fromPlant);
                userRepository.save(user);
                log.info("Linked {} to company {}", user.getEmail(), fromPlant.getName());
            }
        }
        log.info("Company access migration complete. Unassigned plants remain for Control Center.");
    }

    private String uniqueCompanyName(String base) {
        String name = (base == null || base.isBlank() ? "Company" : base.trim()) + " Company";
        if (!companyRepository.existsByNameIgnoreCase(name)) {
            return name;
        }
        int i = 2;
        while (companyRepository.existsByNameIgnoreCase(name + " " + i)) {
            i++;
        }
        return name + " " + i;
    }
}
