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
public class UserStatusColumnMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN status VARCHAR(32) NOT NULL");
            log.info("users.status widened to VARCHAR(32) for PENDING");
        } catch (Exception e) {
            log.debug("Skip users.status alter: {}", e.getMessage());
        }
    }
}
