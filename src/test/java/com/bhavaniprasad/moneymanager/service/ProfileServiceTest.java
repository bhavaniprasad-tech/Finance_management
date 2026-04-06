package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.ProfileDTO;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.entity.UserRole;
import com.bhavaniprasad.moneymanager.repository.ProfileRepository;
import com.bhavaniprasad.moneymanager.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private BrevoEmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AppUserDetailsService appUserDetailsService;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void toEntity_defaultsRoleToAnalystWhenRoleMissing() {
        ProfileDTO dto = ProfileDTO.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("plain-password")
                .build();
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded");

        ProfileEntity entity = profileService.toEntity(dto);

        assertThat(entity.getRole()).isEqualTo(UserRole.ANALYST);
        assertThat(entity.getPassword()).isEqualTo("encoded");
    }

    @Test
    void toDTO_mapsRoleAndStatus() {
        ProfileEntity entity = ProfileEntity.builder()
                .id(11L)
                .fullName("Admin User")
                .email("admin@example.com")
                .isActive(true)
                .role(UserRole.ADMIN)
                .build();

        ProfileDTO dto = profileService.toDTO(entity);

        assertThat(dto.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getEmail()).isEqualTo("admin@example.com");
    }
}

