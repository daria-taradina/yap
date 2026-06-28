// controllers/PostInteractionController.java
package com.yap.backend.controllers;

import com.yap.backend.dtos.*;
import com.yap.backend.services.PostInteractionService;
import com.yap.backend.util.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    public PostInteractionController(PostInteractionService postInteractionService) {
        this.postInteractionService = postInteractionService;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Integer postId) {
        postInteractionService.likePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Integer postId) {
        postInteractionService.unlikePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentSummary> createComment(@RequestBody @Valid CommentCreate dto) {
        return ResponseEntity.status(201).body(postInteractionService.createComment(dto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        postInteractionService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentSummary>> getComments(
            @PathVariable Integer postId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<CommentSummary> comments = postInteractionService.getCommentsForPost(postId);
        int fromIndex = normalizedPage * clampedSize;
        if (fromIndex >= comments.size()) {
            return ResponseEntity.ok(List.of());
        }
        int toIndex = Math.min(fromIndex + clampedSize, comments.size());
        return ResponseEntity.ok(comments.subList(fromIndex, toIndex));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Integer commentId) {
        postInteractionService.likeComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(@PathVariable Integer commentId) {
        postInteractionService.unlikeComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
