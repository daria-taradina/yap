// controllers/PostManagementController.java
package com.yap.backend.controllers;

import com.yap.backend.dtos.*;
import com.yap.backend.services.PostManagementService;
import com.yap.backend.util.PaginationUtil;
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
    public ResponseEntity<List<PostSummary>> getFeed(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> feed = postManagementService.getFeedForUser(userId);
        return ResponseEntity.ok(paginateList(feed, normalizedPage, clampedSize));
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
    public ResponseEntity<List<PostSummary>> getPostsByTag(
            @PathVariable String tagName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getPostsByTag(tagName);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostSummary>> searchByTag(
            @RequestParam String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (q == null || q.isBlank()) return ResponseEntity.badRequest().build();
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.searchPostsByTag(q);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<List<PostSummary>> getProfilePosts(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getProfilePosts(userId);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/user/{userId}/community")
    public ResponseEntity<List<PostSummary>> getCommunityPostsByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getCommunityPostsByUser(userId);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/liked-by/{userId}")
    public ResponseEntity<List<PostSummary>> getPostsLikedByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getPostsLikedByUser(userId);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<PostSummary>> getPostsByCommunity(
            @PathVariable Integer communityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getPostsByCommunity(communityId);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/forums/{userId}")
    public ResponseEntity<List<PostSummary>> getForumsFeed(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getForumsFeedForUser(userId);
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    @GetMapping("/blogs")
    public ResponseEntity<List<PostSummary>> getAllBlogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<PostSummary> posts = postManagementService.getAllBlogPosts();
        return ResponseEntity.ok(paginateList(posts, normalizedPage, clampedSize));
    }

    private <T> List<T> paginateList(List<T> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, list.size());
        return list.subList(fromIndex, toIndex);
    }
}
