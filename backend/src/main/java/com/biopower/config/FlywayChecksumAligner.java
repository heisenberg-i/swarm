// PURPOSE: Aligns known legacy Flyway checksums before migrate; does not re-run applied versions.
package com.biopower.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * V1/V2 bytes changed after those versions were already applied (invalid {@code # PURPOSE} header,
 * then removal of hardcoded administrator inserts). Flyway must not re-execute them. Only rewrite
 * checksums that match the known previous files.
 */
@Configuration
@Slf4j
public class FlywayChecksumAligner {

    static final String V1_RESOURCE = "db/migration/V1__swarm_app_schema.sql";
    static final String V2_RESOURCE = "db/migration/V2__seed_app_and_h2_data.sql";

    /** Dump / JAR checksum for V1 that started with {@code # PURPOSE}. */
    static final Set<Integer> LEGACY_V1_CHECKSUMS = Set.of(841724909);
    /** Dump checksum ({@code # PURPOSE}) and post-comment-fix checksum that still seeded a CC admin. */
    static final Set<Integer> LEGACY_V2_CHECKSUMS = Set.of(-301239876, 1381351303);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return this::alignThenMigrate;
    }

    void alignThenMigrate(Flyway flyway) {
        DataSource dataSource = flyway.getConfiguration().getDataSource();
        align(dataSource);
        flyway.migrate();
    }

    void align(DataSource dataSource) {
        int v1 = checksumClasspathResource(V1_RESOURCE);
        int v2 = checksumClasspathResource(V2_RESOURCE);
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, "flyway_schema_history")) {
                return;
            }
            int updatedV1 = updateLegacyChecksum(connection, "1", LEGACY_V1_CHECKSUMS, v1);
            int updatedV2 = updateLegacyChecksum(connection, "2", LEGACY_V2_CHECKSUMS, v2);
            if (updatedV1 > 0 || updatedV2 > 0) {
                log.info("Aligned Flyway checksums for already-applied migrations (V1 rows {}, V2 rows {})",
                        updatedV1, updatedV2);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to align Flyway checksums", ex);
        }
    }

    static int checksumClasspathResource(String resource) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = FlywayChecksumAligner.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing Flyway resource " + resource);
            }
            return flywayLineCrc32(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to checksum " + resource, ex);
        }
    }

    static int flywayLineCrc32(InputStream in) throws IOException {
        CRC32 crc32 = new CRC32();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            boolean first = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                        line = line.substring(1);
                    }
                    first = false;
                }
                crc32.update(line.getBytes(StandardCharsets.UTF_8));
            }
        }
        return (int) crc32.getValue();
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int updateLegacyChecksum(Connection connection, String version,
                                            Set<Integer> legacyChecksums, int current) throws SQLException {
        int updated = 0;
        for (Integer legacy : legacyChecksums) {
            if (legacy == current) {
                continue;
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE flyway_schema_history SET checksum = ? WHERE version = ? AND checksum = ?")) {
                ps.setInt(1, current);
                ps.setString(2, version);
                ps.setInt(3, legacy);
                updated += ps.executeUpdate();
            }
        }
        return updated;
    }
}
