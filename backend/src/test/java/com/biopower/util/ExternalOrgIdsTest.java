// PURPOSE: Automated test covering SWARM ExternalOrgIdsTest.
package com.biopower.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalOrgIdsTest {

    @Test
    void normalizeTrimsWhitespace() {
        assertEquals("001", ExternalOrgIds.normalize("  001  "));
    }

    @Test
    void inventedSlugIsRejectedInUiGuidance() {
        assertTrue(ExternalOrgIds.looksInventedSlug("org_vulcan_mozambique"));
        assertFalse(ExternalOrgIds.looksInventedSlug("001"));
        assertFalse(ExternalOrgIds.looksInventedSlug("org_biopower_pilot"));
    }
}
