// controllers/UserFollowController.java
package com.yap.backend.controllers;

import com.yap.backend.dtos.UserSummary;
import com.yap.backend.services.UserFollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserFollowController {

    private final UserFollowService userFollowService;

    public UserFollowController(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> follow(@PathVariable Integer userId) {
        return ResponseEntity.ok(userFollowService.followUser(userId));
    }

    @DeleteMapping("/{userId}/unfollow")
    public ResponseEntity<String> unfollow(@PathVariable Integer userId) {
        return ResponseEntity.ok(userFollowService.unfollowUser(userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummary>> getFollowers(@PathVariable Integer userId) {
        return ResponseEntity.ok(userFollowService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummary>> getFollowing(@PathVariable Integer userId) {
        return ResponseEntity.ok(userFollowService.getFollowing(userId));
    }
}