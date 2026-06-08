package com.yap.backend.controllers;

import com.yap.backend.dtos.ProfileData;
import com.yap.backend.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileData> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileData> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getProfileByUsername(username));
    }

    @PatchMapping("/me/bio")
    public ResponseEntity<Void> updateBio(@RequestBody Map<String, String> body) {
        profileService.updateBio(body.get("bio"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<Void> updateAvatar(@RequestBody Map<String, String> body) {
        profileService.updateAvatar(body.get("url"));
        return ResponseEntity.ok().build();
    }
}