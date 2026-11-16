// PURPOSE: Shared backend helper used by SWARM services.
package com.biopower.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class DeviceSecretHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DeviceSecretHasher() {
    }

    public static String hash(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public static boolean matches(String rawSecret, String storedHash) {
        if (rawSecret == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return hash(rawSecret).equalsIgnoreCase(storedHash);
    }

    /** One-time plaintext secret returned at pair/rotate. Prefix swd_ for SWARM device. */
    public static String generateRawSecret() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return "swd_" + HexFormat.of().formatHex(bytes);
    }

    public static String generateDeviceUid(String chipId) {
        String suffix = chipId == null || chipId.isBlank()
                ? HexFormat.of().formatHex(randomBytes(4))
                : chipId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (suffix.length() > 12) {
            suffix = suffix.substring(suffix.length() - 12);
        }
        return "SWARM-HUB-" + suffix.toUpperCase();
    }

    private static byte[] randomBytes(int n) {
        byte[] bytes = new byte[n];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
