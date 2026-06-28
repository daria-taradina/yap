// services/PostManagementService.java
package com.yap.backend.services;

import com.yap.backend.dtos.*;
import com.yap.backend.entities.*;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.enums.PostType;
import com.yap.backend.exceptions.*;
import com.yap.backend.keys.*;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostManagementService extends BaseService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final PostTagService postTagService;
    private final PostLikeRepository postLikeRepository;
    private final UserFollowRepository userFollowRepository;

    public PostManagementService(PostRepository postRepository,
                                  CommunityRepository communityRepository,
                                  CommunityMemberRepository communityMemberRepository,
                                  PostTagService postTagService,
                                  PostLikeRepository postLikeRepository,
                                  UserFollowRepository userFollowRepository,
                                  UserRepository userRepository) {
        super(userRepository);
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.postTagService = postTagService;
        this.postLikeRepository = postLikeRepository;
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional
    public PostSummary createPost(PostCreate dto) {
        User currentUser = getAuthenticatedUser();

        Post post = new Post();
        post.setAuthor(currentUser);
        post.setContentText(dto.getContentText());
        post.setGifUrl(dto.getGifUrl());
        post.setFlair(dto.getFlair());
        post.setPostType(dto.getPostType() != null ? dto.getPostType() : PostType.DISCUSSION);

        boolean isBlog = post.getPostType() == PostType.BLOG;

        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new InvalidInputException("Title is required");

        post.setTitle(dto.getTitle());

        if (!isBlog) {
            // DISCUSSION posts must belong to a community
            if (dto.getCommunityId() == null)
                throw new InvalidInputException("A space is required for discussion posts");

            Community community = communityRepository.findById(dto.getCommunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + dto.getCommunityId()));

            boolean isMember = communityMemberRepository.existsById(
                new CommunityMemberId(dto.getCommunityId(), currentUser.getUserId()));
            if (!isMember) throw new UnauthorizedException("You must be a member to post here");

            post.setCommunity(community);
        }
        // BLOG posts have no community — community stays null

        Post saved = postRepository.save(post);
        List<String> tagNames = postTagService.saveTags(saved, dto.getTags());
        return mapToSummary(saved, tagNames, currentUser.getUserId());
    }

    @Transactional
    public void deletePost(Integer postId) {
        User currentUser = getAuthenticatedUser();
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (post.isDeleted()) throw new ResourceNotFoundException("Post not found: " + postId);

        boolean isAuthor = post.getAuthor().getUserId().equals(currentUser.getUserId());
        if (!isAuthor) {
            if (post.getCommunity() == null) throw new UnauthorizedException("Not allowed");
            boolean isMod = communityMemberRepository.existsById_CommunityIdAndId_UserIdAndRole(
                post.getCommunity().getCommunityId(), currentUser.getUserId(), CommunityMemberRole.MOD);
            if (!isMod) throw new UnauthorizedException("Not allowed");
        }

        post.setDeleted(true);
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostSummary getPost(Integer postId) {
        User currentUser = getAuthenticatedUser();
        Post post = postRepository.findByIdWithTags(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (post.isDeleted()) throw new ResourceNotFoundException("Post not found: " + postId);
        return mapToSummary(post, currentUser.getUserId());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getFeedForUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        boolean hasFollows = !userFollowRepository.findByFollower_UserId(userId).isEmpty();
        boolean hasCommunities = !communityMemberRepository.findByUser_UserId(userId).isEmpty();
        if (!hasFollows && !hasCommunities) return getTrendingFeed(currentUser);
        return getPersonalizedFeed(currentUser);
    }

    public String getFeedType(Integer userId) {
        boolean hasFollows = !userFollowRepository.findByFollower_UserId(userId).isEmpty();
        boolean hasCommunities = !communityMemberRepository.findByUser_UserId(userId).isEmpty();
        return (!hasFollows && !hasCommunities) ? "suggested" : "feed";
    }

    private List<PostSummary> getTrendingFeed(User currentUser) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<String> tags = postRepository.findTrendingTagsRaw(since)
            .stream().map(row -> (String) row[0]).collect(Collectors.toList());
        if (tags.isEmpty()) return List.of();
        return postRepository.findPostsByTrendingTags(tags)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    private List<PostSummary> getPersonalizedFeed(User currentUser) {
        return postRepository.findFeedPostsForUser(currentUser.getUserId())
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrendingTag> getTrendingTags() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return postRepository.findTrendingTagsRaw(since)
            .stream().map(row -> new TrendingTag((String) row[0], (Long) row[1])).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getPostsByTag(String tagName) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findPostsByExactTag(tagName)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> searchPostsByTag(String query) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findPostsByTagContaining(query.trim())
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getProfilePosts(Integer userId) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findProfilePostsByUser(userId)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getCommunityPostsByUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findCommunityPostsByUser(userId)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getPostsByCommunity(Integer communityId) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findPostsByCommunity(communityId)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getPostsLikedByUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findPostsLikedByUser(userId)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getForumsFeedForUser(Integer userId) {
        User currentUser = getAuthenticatedUser();
        return postRepository.findDiscussionPostsForUser(userId)
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummary> getAllBlogPosts() {
        User currentUser = getAuthenticatedUser();
        return postRepository.findAllBlogPosts()
            .stream().map(p -> mapToSummary(p, currentUser.getUserId())).collect(Collectors.toList());
    }

    private PostSummary mapToSummary(Post post, Integer currentUserId) {
        List<String> tagNames = post.getTags().stream()
            .map(pt -> pt.getTag().getName()).collect(Collectors.toList());
        return mapToSummary(post, tagNames, currentUserId);
    }

    private PostSummary mapToSummary(Post post, List<String> tagNames, Integer currentUserId) {
        boolean liked = postLikeRepository.existsById(new PostLikeId(currentUserId, post.getPostId()));
        boolean canDelete = post.getAuthor().getUserId().equals(currentUserId)
            || (post.getCommunity() != null && communityMemberRepository
                .existsById_CommunityIdAndId_UserIdAndRole(
                    post.getCommunity().getCommunityId(), currentUserId, CommunityMemberRole.MOD));

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
}