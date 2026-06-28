package com.yap.backend.services;

import com.yap.backend.entities.Post;
import com.yap.backend.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedService extends BaseService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Value("${app.feed.window-days:30}")
    private int feedWindowDays;

    private static final int PAGE_SIZE = 20;
    private static final int COLD_START_THRESHOLD = 3;
    private static final double AFFINITY_MULTIPLIER = 1.5;
    private static final int MAX_CONSECUTIVE_SAME_COMMUNITY = 2;
    private static final int VELOCITY_BOOST_THRESHOLD = 5;
    private static final double VELOCITY_BOOST_MULTIPLIER = 2.0;
    private static final int HOT_POSTS_LIMIT = 5;
    private static final int HOT_POSTS_HOURS = 48;

    public FeedService(PostRepository postRepository,
                       PostLikeRepository postLikeRepository,
                       CommunityMemberRepository communityMemberRepository,
                       UserRepository userRepository) {
        super(userRepository);
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.communityMemberRepository = communityMemberRepository;
    }

    /**
     * Personalized feed for authenticated users with 3+ joined spaces.
     * Posts from last N days from joined spaces, ranked by score with
     * affinity multiplier, diversity enforcement, and discovery injection.
     */
    @Transactional(readOnly = true)
    public List<Post> getPersonalizedFeed(Integer userId, int page) {
        LocalDateTime since = LocalDateTime.now().minusDays(feedWindowDays);

        // Get user's joined community IDs
        List<Integer> joinedCommunityIds = communityMemberRepository.findByUser_UserId(userId)
                .stream()
                .map(cm -> cm.getId().getCommunityId())
                .collect(Collectors.toList());

        if (joinedCommunityIds.isEmpty()) {
            return getPublicFeed(page);
        }

        // Cold start: user has <3 joined spaces → blend 70% public + 30% personalized
        if (joinedCommunityIds.size() < COLD_START_THRESHOLD) {
            return getColdStartFeed(userId, joinedCommunityIds, since, page);
        }

        // Get affinity community IDs (where user has liked or commented)
        Set<Integer> affinityCommunityIds = getAffinityCommunityIds(userId);

        // Fetch posts from joined communities
        List<Post> posts = postRepository.findFeedPostsFromCommunities(joinedCommunityIds, since);

        // Score posts
        List<ScoredPost> scoredPosts = posts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, affinityCommunityIds)))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .collect(Collectors.toList());

        // Apply diversity (max 2 consecutive from same community)
        List<Post> diversifiedPosts = applyDiversity(scoredPosts);

        // Apply discovery injection at positions 5 and 12
        List<Post> finalFeed = applyDiscoveryInjection(diversifiedPosts, joinedCommunityIds, since);

        // Paginate
        return paginate(finalFeed, page);
    }

    /**
     * Public feed for logged-out users.
     * All posts ranked by score, no affinity or discovery.
     */
    @Transactional(readOnly = true)
    public List<Post> getPublicFeed(int page) {
        LocalDateTime since = LocalDateTime.now().minusDays(feedWindowDays);

        List<Post> posts = postRepository.findAllPublicFeedPosts(since);

        // Score posts (no affinity)
        List<ScoredPost> scoredPosts = posts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, Collections.emptySet())))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .collect(Collectors.toList());

        List<Post> result = scoredPosts.stream()
                .map(ScoredPost::post)
                .collect(Collectors.toList());

        return paginate(result, page);
    }

    /**
     * Hot posts: top 5 by score from last 48 hours. No pagination.
     */
    @Transactional(readOnly = true)
    public List<Post> getHotPosts() {
        LocalDateTime since = LocalDateTime.now().minusHours(HOT_POSTS_HOURS);

        List<Post> posts = postRepository.findAllPublicFeedPosts(since);

        return posts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, Collections.emptySet())))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .limit(HOT_POSTS_LIMIT)
                .map(ScoredPost::post)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPERS
    // ---------------------------------------------------------------

    private double calculateScore(Post post, Set<Integer> affinityCommunityIds) {
        double baseScore = post.getLikeCount() + (post.getCommentCount() * 1.5);
        long hoursAge = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
        double decayFactor = Math.pow(0.5, hoursAge / 24.0);
        double score = baseScore * decayFactor;

        // Velocity boost: 5+ likes in last 60 min → score × 2
        long recentLikes = postLikeRepository.countByPost_PostIdAndCreatedAtAfter(
                post.getPostId(), LocalDateTime.now().minusHours(1));
        if (recentLikes >= VELOCITY_BOOST_THRESHOLD) {
            score *= VELOCITY_BOOST_MULTIPLIER;
        }

        // Affinity multiplier: posts from communities where user has engaged get 1.5×
        if (post.getCommunity() != null
                && affinityCommunityIds.contains(post.getCommunity().getCommunityId())) {
            score *= AFFINITY_MULTIPLIER;
        }

        return score;
    }

    private Set<Integer> getAffinityCommunityIds(Integer userId) {
        Set<Integer> affinityIds = new HashSet<>();
        affinityIds.addAll(postRepository.findCommunityIdsWherUserLiked(userId));
        affinityIds.addAll(postRepository.findCommunityIdsWhereUserCommented(userId));
        return affinityIds;
    }

    /**
     * Enforce max 2 consecutive posts from the same community.
     * If a 3rd consecutive post from the same community appears, it's pushed back.
     */
    private List<Post> applyDiversity(List<ScoredPost> scoredPosts) {
        List<Post> result = new ArrayList<>();
        List<ScoredPost> deferred = new ArrayList<>();
        int consecutiveCount = 0;
        Integer lastCommunityId = null;

        for (ScoredPost sp : scoredPosts) {
            Integer communityId = sp.post().getCommunity() != null
                    ? sp.post().getCommunity().getCommunityId() : null;

            if (communityId != null && communityId.equals(lastCommunityId)) {
                consecutiveCount++;
                if (consecutiveCount > MAX_CONSECUTIVE_SAME_COMMUNITY) {
                    deferred.add(sp);
                    continue;
                }
            } else {
                consecutiveCount = 1;
                lastCommunityId = communityId;
            }

            result.add(sp.post());
        }

        // Append deferred posts at the end
        for (ScoredPost sp : deferred) {
            result.add(sp.post());
        }

        return result;
    }

    /**
     * Inject discovery posts at positions 5 and 12 (0-indexed).
     * Discovery = top posts from highest-memberCount non-joined communities.
     */
    private List<Post> applyDiscoveryInjection(List<Post> feed, List<Integer> joinedCommunityIds,
                                                LocalDateTime since) {
        if (joinedCommunityIds.isEmpty()) {
            return feed;
        }

        List<Post> discoveryPosts = postRepository.findDiscoveryPosts(joinedCommunityIds, since);
        if (discoveryPosts.isEmpty()) {
            return feed;
        }

        // Score discovery posts and pick top ones
        List<Post> topDiscovery = discoveryPosts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, Collections.emptySet())))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .limit(2)
                .map(ScoredPost::post)
                .collect(Collectors.toList());

        List<Post> result = new ArrayList<>(feed);
        int[] injectionPositions = {5, 12};
        int injected = 0;

        for (int pos : injectionPositions) {
            if (injected >= topDiscovery.size()) break;
            if (pos <= result.size()) {
                result.add(pos, topDiscovery.get(injected));
                injected++;
            }
        }

        return result;
    }

    /**
     * Cold start blend: 70% public + 30% personalized for users with <3 joined spaces.
     */
    private List<Post> getColdStartFeed(Integer userId, List<Integer> joinedCommunityIds,
                                         LocalDateTime since, int page) {
        // Get public posts
        List<Post> publicPosts = postRepository.findAllPublicFeedPosts(since);
        List<ScoredPost> publicScored = publicPosts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, Collections.emptySet())))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .collect(Collectors.toList());

        // Get personalized posts from joined communities
        Set<Integer> affinityCommunityIds = getAffinityCommunityIds(userId);
        List<Post> personalizedPosts = postRepository.findFeedPostsFromCommunities(joinedCommunityIds, since);
        List<ScoredPost> personalizedScored = personalizedPosts.stream()
                .map(post -> new ScoredPost(post, calculateScore(post, affinityCommunityIds)))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .collect(Collectors.toList());

        // Blend: 70% public, 30% personalized
        int totalSize = PAGE_SIZE;
        int publicCount = (int) Math.round(totalSize * 0.7);
        int personalizedCount = totalSize - publicCount;

        // Calculate offset for this page
        int publicOffset = page * publicCount;
        int personalizedOffset = page * personalizedCount;

        List<Post> blended = new ArrayList<>();

        // Add public posts
        List<Post> publicPage = publicScored.stream()
                .map(ScoredPost::post)
                .skip(publicOffset)
                .limit(publicCount)
                .collect(Collectors.toList());
        blended.addAll(publicPage);

        // Add personalized posts
        List<Post> personalizedPage = personalizedScored.stream()
                .map(ScoredPost::post)
                .skip(personalizedOffset)
                .limit(personalizedCount)
                .collect(Collectors.toList());
        blended.addAll(personalizedPage);

        // Re-sort blended by score
        Set<Integer> finalAffinityIds = affinityCommunityIds;
        blended.sort((a, b) -> Double.compare(
                calculateScore(b, finalAffinityIds),
                calculateScore(a, finalAffinityIds)));

        return blended;
    }

    private List<Post> paginate(List<Post> posts, int page) {
        int start = page * PAGE_SIZE;
        if (start >= posts.size()) {
            return Collections.emptyList();
        }
        int end = Math.min(start + PAGE_SIZE, posts.size());
        return posts.subList(start, end);
    }

    private record ScoredPost(Post post, double score) {}
}
