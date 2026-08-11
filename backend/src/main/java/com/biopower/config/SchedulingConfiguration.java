package com.biopower.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "biopower.deployment.role", havingValue = "primary", matchIfMissing = true)
public class SchedulingConfiguration {
}
