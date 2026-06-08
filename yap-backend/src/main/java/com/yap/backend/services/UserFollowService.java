// services/UserFollowService.java
package com.yap.backend.services;

import com.yap.backend.dtos.UserSummary;
import com.yap.backend.entities.*;
import com.yap.backend.exceptions.*;
import com.yap.backend.keys.UserFollowId;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFollowService extends BaseService {

    private final UserFollowRepository userFollowRepository;

    public UserFollowService(UserRepository userRepository, UserFollowRepository userFollowRepository) {
        super(userRepository);
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional
    public String followUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser.getUserId().equals(userId)) throw new InvalidInputException("Cannot follow yourself");

        User following = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserFollowId followId = new UserFollowId(currentUser.getUserId(), userId);
        if (userFollowRepository.existsById(followId)) throw new ResourceAlreadyExistsException("Already following");

        UserFollow follow = new UserFollow();
        follow.setId(followId); follow.setFollower(currentUser); follow.setFollowing(following);
        userFollowRepository.save(follow);

        currentUser.setFollowingCount(currentUser.getFollowingCount() + 1);
        userRepository.save(currentUser);
        following.setFollowerCount(following.getFollowerCount() + 1);
        userRepository.save(following);
        return "Followed";
    }

    @Transactional
    public String unfollowUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        UserFollowId followId = new UserFollowId(currentUser.getUserId(), userId);
        if (!userFollowRepository.existsById(followId)) throw new ResourceNotFoundException("Not following");

        userFollowRepository.deleteById(followId);
        User following = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        currentUser.setFollowingCount(Math.max(0, currentUser.getFollowingCount() - 1));
        userRepository.save(currentUser);
        following.setFollowerCount(Math.max(0, following.getFollowerCount() - 1));
        userRepository.save(following);
        return "Unfollowed";
    }

    public List<UserSummary> getFollowing(Integer userId) {
        return userFollowRepository.findByFollower_UserId(userId)
            .stream().map(f -> toSummary(f.getFollowing())).collect(Collectors.toList());
    }

    public List<UserSummary> getFollowers(Integer userId) {
        return userFollowRepository.findByFollowing_UserId(userId)
            .stream().map(f -> toSummary(f.getFollower())).collect(Collectors.toList());
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(user.getUserId(), user.getUsername(), user.getAvatarUrl());
    }
}