// controllers/PostInteractionController.java
package com.yap.backend.controllers;

import com.yap.backend.dtos.*;
import com.yap.backend.services.PostInteractionService;
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
    public ResponseEntity<List<CommentSummary>> getComments(@PathVariable Integer postId) {
        return ResponseEntity.ok(postInteractionService.getCommentsForPost(postId));
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