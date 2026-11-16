// PURPOSE: Automated test covering SWARM Slice0SecurityIT.
package com.biopower.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class Slice0SecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"x\",\"newPassword\":\"abcdefghijkl\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ssoConfigRemainsPublic() throws Exception {
        mockMvc.perform(get("/auth/sso/config")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CONTROL_CENTER_ADMIN")
    void partnerAdminForbiddenForControlCenter() throws Exception {
        mockMvc.perform(get("/admin/partner/organizations")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void reportsWithoutPlantIdAreRejected() throws Exception {
        mockMvc.perform(get("/reports")).andExpect(status().isBadRequest());
    }
}
