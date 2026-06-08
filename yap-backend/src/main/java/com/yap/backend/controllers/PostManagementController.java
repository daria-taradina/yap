// controllers/PostManagementController.java
package com.yap.backend.controllers;

import com.yap.backend.dtos.*;
import com.yap.backend.services.PostManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostManagementController {

    private final PostManagementService postManagementService;

    public PostManagementController(PostManagementService postManagementService) {
        this.postManagementService = postManagementService;
    }

    @PostMapping
    public ResponseEntity<PostSummary> createPost(@RequestBody @Valid PostCreate dto) {
        return ResponseEntity.status(201).body(postManagementService.createPost(dto));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        postManagementService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostSummary> getPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(postManagementService.getPost(postId));
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<PostSummary>> getFeed(@PathVariable Integer userId) {
        return ResponseEntity.ok(postManagementService.getFeedForUser(userId));
    }

    @GetMapping("/feed/{userId}/type")
    public ResponseEntity<Map<String, String>> getFeedType(@PathVariable Integer userId) {
        return ResponseEntity.ok(Map.of("type", postManagementService.getFeedType(userId)));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingTag>> getTrendingTags() {
        return ResponseEntity.ok(postManagementService.getTrendingTags());
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<PostSummary>> getPostsByTag(@PathVariable String tagName) {
        return ResponseEntity.ok(postManagementService.getPostsByTag(tagName));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostSummary>> searchByTag(@RequestParam String q) {
        if (q == null || q.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(postManagementService.searchPostsByTag(q));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<List<PostSummary>> getProfilePosts(@PathVariable Integer userId) {
        return ResponseEntity.ok(postManagementService.getProfilePosts(userId));
    }

    @GetMapping("/user/{userId}/community")
    public ResponseEntity<List<PostSummary>> getCommunityPostsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(postManagementService.getCommunityPostsByUser(userId));
    }

    @GetMapping("/liked-by/{userId}")
    public ResponseEntity<List<PostSummary>> getPostsLikedByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(postManagementService.getPostsLikedByUser(userId));
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<PostSummary>> getPostsByCommunity(@PathVariable Integer communityId) {
        return ResponseEntity.ok(postManagementService.getPostsByCommunity(communityId));
    }
}