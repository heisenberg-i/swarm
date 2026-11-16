// PURPOSE: Demo logins stay opt-in and never become Control Center administrators.
package com.biopower.config;

import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoAccountInitializerTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DemoAccountInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DemoAccountInitializer(companyRepository, plantRepository, userRepository, passwordEncoder);
        ReflectionTestUtils.setField(initializer, "demoPassword", "demo-password-ok");
    }

    @Test
    void skipsWhenPasswordTooShort() {
        ReflectionTestUtils.setField(initializer, "demoPassword", "short");
        initializer.run();
        verify(userRepository, never()).save(any());
    }

    @Test
    void createsSuperAdminsNotControlCenter() {
        when(companyRepository.findById(any())).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(plantRepository.findByCompany(any())).thenReturn(List.of());
        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$demo");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());
        for (User user : captor.getAllValues()) {
            assertEquals(UserRole.SUPER_ADMIN, user.getRole());
            assertNotEquals(UserRole.CONTROL_CENTER_ADMIN, user.getRole());
        }
    }
}
