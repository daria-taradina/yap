package com.yap.backend.services;

import com.yap.backend.dtos.ProfileData;
import com.yap.backend.entities.User;
import com.yap.backend.exceptions.ResourceNotFoundException;
import com.yap.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService extends BaseService {

    public ProfileService(UserRepository userRepository) {
        super(userRepository);
    }

    public ProfileData getMyProfile() {
        return mapToProfileData(getAuthenticatedUser());
    }

    public ProfileData getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        ProfileData data = mapToProfileData(user);
        data.setEmail(null); // don't expose other users' emails
        return data;
    }

    @Transactional
    public void updateBio(String bio) {
        User user = getAuthenticatedUser();
        user.setBio(bio);
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(String url) {
        User user = getAuthenticatedUser();
        user.setAvatarUrl(url);
        userRepository.save(user);
    }

    private ProfileData mapToProfileData(User user) {
        ProfileData data = new ProfileData();
        data.setUserId(user.getUserId());
        data.setUsername(user.getUsername());
        data.setEmail(user.getEmail());
        data.setBio(user.getBio());
        data.setAvatarUrl(user.getAvatarUrl());
        data.setBannerUrl(user.getBannerUrl());
        data.setFollowerCount(user.getFollowerCount());
        data.setFollowingCount(user.getFollowingCount());
        data.setCommunityCount(user.getCommunityCount());
        data.setRole(user.getRole().name());
        return data;
    }
}