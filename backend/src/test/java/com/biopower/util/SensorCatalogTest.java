// PURPOSE: Automated test covering SWARM SensorCatalogTest.
package com.biopower.util;

import com.biopower.dto.response.SensorTypeInfo;
import com.biopower.model.enums.SensorType;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SensorCatalogTest {

    @Test
    void catalogCoversEverySensorType() {
        Set<String> codes = SensorCatalog.all().stream().map(SensorTypeInfo::getCode).collect(Collectors.toSet());
        for (SensorType type : SensorType.values()) {
            assertTrue(codes.contains(type.name()), "missing catalog entry for " + type);
            assertNotNull(SensorCatalog.label(type));
            assertNotNull(SensorCatalog.defaultUnit(type));
        }
        assertEquals(SensorType.values().length, SensorCatalog.all().size());
    }
}
