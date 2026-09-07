package com.biopower.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(-9)
@RequiredArgsConstructor
@Slf4j
public class HmiEnabledColumnMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE plants ADD COLUMN hmi_enabled TINYINT(1) NOT NULL DEFAULT 0");
            log.info("Added plants.hmi_enabled (Control Center HMI entitlement)");
        } catch (Exception e) {
            log.debug("Skip plants.hmi_enabled add: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE plants MODIFY COLUMN hmi_enabled TINYINT(1) NOT NULL DEFAULT 0");
            log.info("Normalized plants.hmi_enabled to TINYINT(1)");
        } catch (Exception e) {
            log.debug("Skip plants.hmi_enabled modify: {}", e.getMessage());
        }
    }
}
