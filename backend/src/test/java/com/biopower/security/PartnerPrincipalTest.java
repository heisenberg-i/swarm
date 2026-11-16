// PURPOSE: Automated test covering SWARM PartnerPrincipalTest.
package com.biopower.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartnerPrincipalTest {

    @Test
    void emptyAllowListDeniesEveryPlant() {
        PartnerPrincipal principal = new PartnerPrincipal(1L, "001", "Org", 9L, Set.of());
        assertFalse(principal.hasPlantAllowList());
        assertFalse(principal.canAccessPlant(1L));
    }

    @Test
    void nullAllowListDeniesEveryPlant() {
        PartnerPrincipal principal = new PartnerPrincipal(1L, "001", "Org", 9L, null);
        assertFalse(principal.hasPlantAllowList());
        assertFalse(principal.canAccessPlant(1L));
    }

    @Test
    void allowListPermitsOnlyListedPlants() {
        PartnerPrincipal principal = new PartnerPrincipal(1L, "001", "Org", 9L, Set.of(3L));
        assertTrue(principal.hasPlantAllowList());
        assertTrue(principal.canAccessPlant(3L));
        assertFalse(principal.canAccessPlant(1L));
    }
}
