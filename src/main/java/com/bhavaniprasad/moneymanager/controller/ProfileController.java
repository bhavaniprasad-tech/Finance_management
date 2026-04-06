package com.bhavaniprasad.moneymanager.controller;

import com.bhavaniprasad.moneymanager.dto.AuthDTO;
import com.bhavaniprasad.moneymanager.dto.ProfileDTO;
import com.bhavaniprasad.moneymanager.dto.UserAccessUpdateDTO;
import com.bhavaniprasad.moneymanager.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(
            @Valid @RequestBody ProfileDTO profileDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.registerProfile(profileDTO));
    }


    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        if (isActivated) {
            return ResponseEntity.ok("Profile activated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthDTO authDTO) {
        Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getPublicProfile() {
        ProfileDTO profileDTO = profileService.getPublicProfile(null);
        return ResponseEntity.ok(profileDTO);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<ProfileDTO>> getAllUsers() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @PutMapping("/admin/users/{userId}/access")
    public ResponseEntity<ProfileDTO> updateUserAccess(
            @PathVariable Long userId,
            @Valid @RequestBody UserAccessUpdateDTO accessUpdateDTO
    ) {
        return ResponseEntity.ok(profileService.updateUserAccess(userId, accessUpdateDTO));
    }
}
