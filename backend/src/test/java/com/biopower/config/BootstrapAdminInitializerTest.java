// PURPOSE: Ensures env bootstrap creates the first CC admin and never overwrites existing accounts.
package com.biopower.config;

import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminInitializerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private BootstrapAdminInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new BootstrapAdminInitializer(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(initializer, "bootstrapAdminEmail", "ops@example.invalid");
        ReflectionTestUtils.setField(initializer, "bootstrapAdminPassword", "twelvechars!!");
    }

    @Test
    void skipsWhenControlCenterAdminAlreadyExists() {
        when(userRepository.findByRole(UserRole.CONTROL_CENTER_ADMIN)).thenReturn(List.of(new User()));

        initializer.run();

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createsFirstAdminFromEnvironment() {
        when(userRepository.findByRole(UserRole.CONTROL_CENTER_ADMIN)).thenReturn(List.of());
        when(userRepository.findByEmailIgnoreCase("ops@example.invalid")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("twelvechars!!")).thenReturn("$2a$12$encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("ops@example.invalid", saved.getEmail());
        assertEquals(UserRole.CONTROL_CENTER_ADMIN, saved.getRole());
        assertEquals("$2a$12$encoded-hash", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2a$"));
    }

    @Test
    void refusesToOverwriteExistingEmail() {
        when(userRepository.findByRole(UserRole.CONTROL_CENTER_ADMIN)).thenReturn(List.of());
        when(userRepository.findByEmailIgnoreCase("ops@example.invalid")).thenReturn(Optional.of(new User()));

        initializer.run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void refusesShortPassword() {
        ReflectionTestUtils.setField(initializer, "bootstrapAdminPassword", "short");
        when(userRepository.findByRole(UserRole.CONTROL_CENTER_ADMIN)).thenReturn(List.of());

        initializer.run();

        verify(userRepository, never()).save(any());
    }
}
