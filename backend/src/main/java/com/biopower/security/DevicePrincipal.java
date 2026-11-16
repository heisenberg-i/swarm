// PURPOSE: Authentication/authorization component for SWARM API security.
package com.biopower.security;

import com.biopower.model.enums.DeviceKind;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

@Getter
public class DevicePrincipal extends AbstractAuthenticationToken {

    private final Long deviceId;
    private final String deviceUid;
    private final Long plantId;
    private final UUID companyId;
    private final String chipId;
    private final DeviceKind deviceKind;
    private final boolean legacyCompat;
    private final int configVersion;

    public DevicePrincipal(Long deviceId, String deviceUid, Long plantId, UUID companyId,
                           String chipId, DeviceKind deviceKind, boolean legacyCompat, int configVersion) {
        super(AuthorityUtils.createAuthorityList("ROLE_DEVICE"));
        this.deviceId = deviceId;
        this.deviceUid = deviceUid;
        this.plantId = plantId;
        this.companyId = companyId;
        this.chipId = chipId;
        this.deviceKind = deviceKind;
        this.legacyCompat = legacyCompat;
        this.configVersion = configVersion;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return deviceUid;
    }
}
