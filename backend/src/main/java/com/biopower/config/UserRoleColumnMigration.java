package com.biopower.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(-10)
@RequiredArgsConstructor
@Slf4j
public class UserRoleColumnMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL");
            log.info("users.role widened to VARCHAR(32) for CONTROL_CENTER_ADMIN");
        } catch (Exception e) {
            log.debug("Skip users.role alter: {}", e.getMessage());
        }
    }
}
