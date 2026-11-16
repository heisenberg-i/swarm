// PURPOSE: Automated test covering SWARM DeviceSecretHasherTest.
package com.biopower.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceSecretHasherTest {

    @Test
    void hashIsStableAndMatches() {
        String raw = DeviceSecretHasher.generateRawSecret();
        assertTrue(raw.startsWith("swd_"));
        String hash = DeviceSecretHasher.hash(raw);
        assertTrue(DeviceSecretHasher.matches(raw, hash));
        assertFalse(DeviceSecretHasher.matches(raw + "x", hash));
    }

    @Test
    void deviceUidFromChipIsDeterministicShape() {
        String uid = DeviceSecretHasher.generateDeviceUid("a1b2c3d4e5");
        assertTrue(uid.startsWith("SWARM-HUB-"));
    }
}
