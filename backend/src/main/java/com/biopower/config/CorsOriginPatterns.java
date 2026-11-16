// PURPOSE: Builds Spring CORS origin patterns for localhost, LAN, and configured hosts.
package com.biopower.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Browser Origin for a same-host nginx SPA on a LAN IP is {@code http://192.168.x.x}.
 * Spring CORS rejects that with HTTP 403 {@code Invalid CORS request} unless a pattern matches.
 */
public final class CorsOriginPatterns {

    private CorsOriginPatterns() {
    }

    public static List<String> build(String allowedOrigins, boolean allowAnyHttpOrigin) {
        Set<String> patterns = new LinkedHashSet<>();
        if (allowedOrigins != null) {
            for (String origin : allowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    patterns.add(trimmed);
                }
            }
        }
        patterns.add("https://localhost");
        patterns.add("https://localhost:*");
        patterns.add("capacitor://localhost");
        if (allowAnyHttpOrigin) {
            patterns.add("http://*");
            patterns.add("http://*:*");
            patterns.add("https://*");
            patterns.add("https://*:*");
        }
        return new ArrayList<>(patterns);
    }
}
