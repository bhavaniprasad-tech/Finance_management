package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.AuthDTO;
import com.bhavaniprasad.moneymanager.dto.ProfileDTO;
import com.bhavaniprasad.moneymanager.dto.UserAccessUpdateDTO;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.entity.UserRole;
import com.bhavaniprasad.moneymanager.repository.ProfileRepository;
import com.bhavaniprasad.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final BrevoEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppUserDetailsService appUserDetailsService;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        if (profileRepository.existsByEmail(profileDTO.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email is already registered");
        }
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        //send activation email
        String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your money manager account";
        String body = "Click on the following link to activate your money manager account." + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .isActive(profileDTO.getIsActive())
                .role(profileDTO.getRole() != null ? profileDTO.getRole() : UserRole.ANALYST)
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .isActive(profileEntity.getIsActive())
                .role(profileEntity.getRole())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email"+authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        }else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email"+email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .isActive(currentUser.getIsActive())
                .role(currentUser.getRole())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public List<ProfileDTO> getAllProfiles() {
        return profileRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDTO).toList();
    }

    public ProfileDTO updateUserAccess(Long userId, UserAccessUpdateDTO accessUpdateDTO) {
        ProfileEntity current = getCurrentProfile();
        ProfileEntity target = profileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (current.getId().equals(target.getId()) && Boolean.FALSE.equals(accessUpdateDTO.getIsActive())) {
            throw new ResponseStatusException(BAD_REQUEST, "You cannot deactivate your own account");
        }
        if (current.getId().equals(target.getId()) && accessUpdateDTO.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(BAD_REQUEST, "You cannot downgrade your own admin role");
        }

        target.setRole(accessUpdateDTO.getRole());
        target.setIsActive(accessUpdateDTO.getIsActive());
        return toDTO(profileRepository.save(target));
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            // Authenticate using email & password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getEmail(),
                            authDTO.getPassword()
                    )
            );
            // Load user details
            UserDetails userDetails =
                    appUserDetailsService.loadUserByUsername(authDTO.getEmail());
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );

        } catch (DisabledException e) {
            throw new ResponseStatusException(FORBIDDEN, "Account is inactive");
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid email or password");
        }
    }

}
