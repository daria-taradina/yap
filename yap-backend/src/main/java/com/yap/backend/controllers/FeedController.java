package com.yap.backend.controllers;

import com.yap.backend.dtos.PostSummary;
import com.yap.backend.entities.Post;
import com.yap.backend.entities.User;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.keys.PostLikeId;
import com.yap.backend.repositories.CommunityMemberRepository;
import com.yap.backend.repositories.PostLikeRepository;
import com.yap.backend.repositories.UserRepository;
import com.yap.backend.services.FeedService;
import com.yap.backend.util.PaginationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class FeedController {

    private final FeedService feedService;
    private final PostLikeRepository postLikeRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;

    public FeedController(FeedService feedService,
                          PostLikeRepository postLikeRepository,
                          CommunityMemberRepository communityMemberRepository,
                          UserRepository userRepository) {
        this.feedService = feedService;
        this.postLikeRepository = postLikeRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostSummary>> getFeed(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);

        Integer currentUserId = getAuthenticatedUserId();
        List<Post> posts;

        if (currentUserId != null) {
            posts = feedService.getPersonalizedFeed(currentUserId, normalizedPage);
        } else {
            posts = feedService.getPublicFeed(normalizedPage);
        }

        List<PostSummary> summaries = posts.stream()
                .map(post -> mapToSummary(post, currentUserId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(paginateList(summaries, normalizedPage, clampedSize));
    }

    @GetMapping("/hot")
    public ResponseEntity<List<PostSummary>> getHotPosts() {
        Integer currentUserId = getAuthenticatedUserId();
        List<Post> posts = feedService.getHotPosts();

        List<PostSummary> summaries = posts.stream()
                .map(post -> mapToSummary(post, currentUserId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/public")
    public ResponseEntity<List<PostSummary>> getPublicFeed(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);

        Integer currentUserId = getAuthenticatedUserId();
        List<Post> posts = feedService.getPublicFeed(normalizedPage);

        List<PostSummary> summaries = posts.stream()
                .map(post -> mapToSummary(post, currentUserId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(paginateList(summaries, normalizedPage, clampedSize));
    }

    /**
     * Returns the authenticated user's ID, or null if the request is unauthenticated.
     */
    private Integer getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        // Principal is a UserDetails — extract username and look up userId
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(User::getUserId)
                .orElse(null);
    }

    private PostSummary mapToSummary(Post post, Integer currentUserId) {
        List<String> tagNames = post.getTags().stream()
                .map(pt -> pt.getTag().getName())
                .collect(Collectors.toList());

        boolean liked = false;
        boolean canDelete = false;

        if (currentUserId != null) {
            liked = postLikeRepository.existsById(new PostLikeId(currentUserId, post.getPostId()));
            canDelete = post.getAuthor().getUserId().equals(currentUserId)
                    || (post.getCommunity() != null && communityMemberRepository
                    .existsById_CommunityIdAndId_UserIdAndRole(
                            post.getCommunity().getCommunityId(), currentUserId, CommunityMemberRole.MOD));
        }

        return new PostSummary(
                post.getPostId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getUserId(),
                post.getAuthor().getAvatarUrl(),
                post.getCommunity() != null ? post.getCommunity().getName() : null,
                post.getCommunity() != null ? post.getCommunity().getCommunityId() : null,
                post.getCommunity() != null ? post.getCommunity().getIconUrl() : null,
                post.getPostType(),
                post.getTitle(),
                post.getContentText(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                tagNames,
                liked,
                canDelete,
                post.getGifUrl(),
                post.getFlair()
        );
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
