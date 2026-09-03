package com.biopower.util;

/**
 * emPOWER tenant id stored as partner_organizations.external_org_id.
 * Must match the helper on emPOWER Settings (“This tenant's emPOWER organization ID is …”),
 * e.g. {@code 001} for Vulcan — not invented slugs like {@code org_vulcan_mozambique}.
 */
public final class ExternalOrgIds {

    public static final String SANDBOX_ORG_ID = "org_biopower_pilot";

    private ExternalOrgIds() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    /** Human-invented {@code org_*} slugs except the local sandbox id. */
    public static boolean looksInventedSlug(String raw) {
        String id = normalize(raw);
        return id.regionMatches(true, 0, "org_", 0, 4) && !SANDBOX_ORG_ID.equalsIgnoreCase(id);
    }
}
