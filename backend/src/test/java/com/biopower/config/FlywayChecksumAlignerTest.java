// PURPOSE: Guards V2 against hardcoded admin credentials and verifies Flyway line CRC32.
package com.biopower.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayChecksumAlignerTest {

    @Test
    void v2DoesNotInsertUsableAdministratorCredentials() throws IOException {
        String sql = readResource(FlywayChecksumAligner.V2_RESOURCE);
        assertFalse(sql.contains("CONTROL_CENTER_ADMIN"));
        assertFalse(sql.contains("$2a$"));
        assertFalse(sql.contains("swarm.nfi@gmail.com"));
        assertFalse(sql.contains("INSERT INTO users"));
        assertTrue(sql.contains("SWARM_BOOTSTRAP_ADMIN_EMAIL"));
    }

    @Test
    void currentMigrationChecksumsUseFlywayLineCrc32() {
        int v1 = FlywayChecksumAligner.checksumClasspathResource(FlywayChecksumAligner.V1_RESOURCE);
        int v2 = FlywayChecksumAligner.checksumClasspathResource(FlywayChecksumAligner.V2_RESOURCE);
        assertEquals(-729282543, v1);
        assertEquals(2127360042, v2);
        assertFalse(FlywayChecksumAligner.LEGACY_V2_CHECKSUMS.contains(v2));
    }

    private static String readResource(String resource) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("missing " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
