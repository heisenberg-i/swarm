// PURPOSE: Regression tests for LAN/CORS origin matching that previously returned HTTP 403.
package com.biopower.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorsOriginPatternsTest {

    @Test
    void lanOriginIsAllowedWhenAnyHttpOriginIsEnabled() {
        CorsConfiguration config = configuration("http://localhost", true);
        assertEquals("http://192.168.29.229", config.checkOrigin("http://192.168.29.229"));
        assertEquals("http://10.0.0.5:8080", config.checkOrigin("http://10.0.0.5:8080"));
        assertEquals("http://localhost", config.checkOrigin("http://localhost"));
    }

    @Test
    void lanOriginIsRejectedWhenLockedDown() {
        CorsConfiguration config = configuration("http://localhost", false);
        assertNull(config.checkOrigin("http://192.168.29.229"));
        assertEquals("http://localhost", config.checkOrigin("http://localhost"));
    }

    private static CorsConfiguration configuration(String allowedOrigins, boolean allowAny) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(CorsOriginPatterns.build(allowedOrigins, allowAny));
        config.setAllowCredentials(true);
        return config;
    }
}
