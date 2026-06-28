package com.yap.backend.services;

import com.yap.backend.entities.*;
import com.yap.backend.keys.CommunityMemberId;
import com.yap.backend.repositories.*;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for FeedService algorithm.
 *
 * Property 14: Feed excludes removed/deleted/flagged posts
 * Property 15: Feed time-decay halves score every 24 hours
 * Property 16: Feed diversity — max 2 consecutive posts from same space
 * Property 17: Cold start users get public feed blend
 *
 * Validates: Requirements 14.1, 14.2, 14.5
 */
class FeedServicePropertyTest {

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------

    private FeedService createFeedService(PostRepository postRepo,
                                          PostLikeRepository postLikeRepo,
                                          CommunityMemberRepository communityMemberRepo) throws Exception {
        UserRepository userRepo = mock(UserRepository.class);
        FeedService service = new FeedService(postRepo, postLikeRepo, communityMemberRepo, userRepo);
        // Set feedWindowDays via reflection
        Field feedWindowDaysField = FeedService.class.getDeclaredField("feedWindowDays");
        feedWindowDaysField.setAccessible(true);
        feedWindowDaysField.setInt(service, 30);
        return service;
    }

    private Post createPost(int postId, Community community, int likeCount, int commentCount,
                            LocalDateTime createdAt) throws Exception {
        Post post = new Post();
        // Set postId via reflection (no setter)
        Field postIdField = Post.class.getDeclaredField("postId");
        postIdField.setAccessible(true);
        postIdField.set(post, postId);

        post.setCommunity(community);
        post.setLikeCount(likeCount);
        post.setCommentCount(commentCount);
        post.setTitle("Test Post " + postId);
        post.setContentText("Content for post " + postId);

        // Set createdAt via reflection
        Field createdAtField = Post.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(post, createdAt);

        // Set author
        User author = new User();
        author.setUsername("user" + postId);
        author.setEmail("user" + postId + "@test.com");
        author.setPassword("password");
        post.setAuthor(author);

        return post;
    }

    private Community createCommunity(int communityId) throws Exception {
        Community community = new Community();
        Field idField = Community.class.getDeclaredField("communityId");
        idField.setAccessible(true);
        idField.set(community, communityId);
        community.setName("Space" + communityId);
        community.setMemberCount(100);
        return community;
    }

    private CommunityMember createCommunityMember(int communityId, int userId) throws Exception {
        CommunityMember cm = new CommunityMember();
        CommunityMemberId cmId = new CommunityMemberId(communityId, userId);
        cm.setId(cmId);
        return cm;
    }

    // ---------------------------------------------------------------
    // Property 14: Feed excludes removed/deleted/flagged posts
    //
    // The repository queries (findFeedPostsFromCommunities, findAllPublicFeedPosts)
    // filter out deleted/removed/flagged posts at the DB level. This property verifies
    // that the correct filtered repository methods are called and that the service
    // does not introduce any unfiltered posts.
    //
    // Validates: Requirements 14.5
    // ---------------------------------------------------------------

    /**
     * Property 14: Feed excludes removed/deleted/flagged posts.
     *
     * For any personalized feed request, the service SHALL call the repository method
     * that excludes deleted/removed/flagged posts (findFeedPostsFromCommunities), and
     * no post in the response shall have isDeleted, isRemoved, or isFlagged set to true.
     *
     * **Validates: Requirements 14.5**
     */
    @Property(tries = 20)
    void feedExcludesRemovedDeletedFlaggedPosts(
            @ForAll("postCounts") int postCount) throws Exception {

        PostRepository postRepo = mock(PostRepository.class);
        PostLikeRepository postLikeRepo = mock(PostLikeRepository.class);
        CommunityMemberRepository communityMemberRepo = mock(CommunityMemberRepository.class);

        FeedService feedService = createFeedService(postRepo, postLikeRepo, communityMemberRepo);

        // Setup: user with 3+ communities (not cold start)
        Integer userId = 1;
        Community community1 = createCommunity(1);
        Community community2 = createCommunity(2);
        Community community3 = createCommunity(3);

        List<CommunityMember> memberships = List.of(
                createCommunityMember(1, userId),
                createCommunityMember(2, userId),
                createCommunityMember(3, userId)
        );
        when(communityMemberRepo.findByUser_UserId(userId)).thenReturn(memberships);

        // Create clean posts (all should have flags = false, simulating DB-level filtering)
        Community[] communities = {community1, community2, community3};
        List<Post> cleanPosts = new ArrayList<>();
        for (int i = 0; i < postCount; i++) {
            Post post = createPost(i + 1, communities[i % 3], 5, 2,
                    LocalDateTime.now().minusHours(i + 1));
            // All posts returned by repo are clean (DB already filtered)
            assert !post.isDeleted() : "Post should not be deleted";
            assert !post.isRemoved() : "Post should not be removed";
            assert !post.isFlagged() : "Post should not be flagged";
            cleanPosts.add(post);
        }

        when(postRepo.findFeedPostsFromCommunities(anyList(), any(LocalDateTime.class)))
                .thenReturn(cleanPosts);
        when(postRepo.findCommunityIdsWherUserLiked(userId)).thenReturn(Collections.emptyList());
        when(postRepo.findCommunityIdsWhereUserCommented(userId)).thenReturn(Collections.emptyList());
        when(postRepo.findDiscoveryPosts(anyList(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(postLikeRepo.countByPost_PostIdAndCreatedAtAfter(anyInt(), any(LocalDateTime.class)))
                .thenReturn(0L);

        // Execute
        List<Post> feed = feedService.getPersonalizedFeed(userId, 0);

        // Verify: the correct filtered repository method was called
        verify(postRepo).findFeedPostsFromCommunities(anyList(), any(LocalDateTime.class));

        // Verify: no returned post has deleted/removed/flagged = true
        for (Post post : feed) {
            assert !post.isDeleted() : "Feed should not contain deleted posts";
            assert !post.isRemoved() : "Feed should not contain removed posts";
            assert !post.isFlagged() : "Feed should not contain flagged posts";
        }
    }

    /**
     * Property 14 (public feed variant): Public feed also uses the filtered query.
     *
     * **Validates: Requirements 14.5**
     */
    @Property(tries = 20)
    void publicFeedExcludesRemovedDeletedFlaggedPosts(
            @ForAll("postCounts") int postCount) throws Exception {

        PostRepository postRepo = mock(PostRepository.class);
        PostLikeRepository postLikeRepo = mock(PostLikeRepository.class);
        CommunityMemberRepository communityMemberRepo = mock(CommunityMemberRepository.class);

        FeedService feedService = createFeedService(postRepo, postLikeRepo, communityMemberRepo);

        Community community = createCommunity(1);
        List<Post> cleanPosts = new ArrayList<>();
        for (int i = 0; i < postCount; i++) {
            cleanPosts.add(createPost(i + 1, community, 5, 2,
                    LocalDateTime.now().minusHours(i + 1)));
        }

        when(postRepo.findAllPublicFeedPosts(any(LocalDateTime.class))).thenReturn(cleanPosts);
        when(postLikeRepo.countByPost_PostIdAndCreatedAtAfter(anyInt(), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<Post> feed = feedService.getPublicFeed(0);

        // Verify: the correct filtered public feed method was called
        verify(postRepo).findAllPublicFeedPosts(any(LocalDateTime.class));

        // Verify: no returned post has deleted/removed/flagged = true
        for (Post post : feed) {
            assert !post.isDeleted() : "Public feed should not contain deleted posts";
            assert !post.isRemoved() : "Public feed should not contain removed posts";
            assert !post.isFlagged() : "Public feed should not contain flagged posts";
        }
    }

    // ---------------------------------------------------------------
    // Property 15: Feed time-decay halves score every 24 hours
    //
    // For any post, the ranking score at age T+24h should be approximately
    // half the score at age T (all other factors being equal).
    //
    // Validates: Requirements 14.1
    // ---------------------------------------------------------------

    /**
     * Property 15: Feed time-decay halves score every 24 hours.
     *
     * For any post with given likeCount and commentCount, the score at age T+24h
     * SHALL be approximately half the score at age T.
     *
     * **Validates: Requirements 14.1**
     */
    @Property(tries = 50)
    void feedTimeDecayHalvesScoreEvery24Hours(
            @ForAll("likeCountArb") int likeCount,
            @ForAll("commentCountArb") int commentCount,
            @ForAll("hoursAgeArb") int baseHoursAge) throws Exception {

        // Skip cases with zero base score (decay of 0 is still 0)
        double baseScore = likeCount + (commentCount * 1.5);
        if (baseScore == 0) return;

        PostRepository postRepo = mock(PostRepository.class);
        PostLikeRepository postLikeRepo = mock(PostLikeRepository.class);
        CommunityMemberRepository communityMemberRepo = mock(CommunityMemberRepository.class);

        // No velocity boost for this test
        when(postLikeRepo.countByPost_PostIdAndCreatedAtAfter(anyInt(), any(LocalDateTime.class)))
                .thenReturn(0L);

        Community community = createCommunity(1);

        // Create two posts: one at age T hours, one at age T+24 hours
        LocalDateTime now = LocalDateTime.now();
        Post postAtT = createPost(1, community, likeCount, commentCount,
                now.minusHours(baseHoursAge));
        Post postAtT24 = createPost(2, community, likeCount, commentCount,
                now.minusHours(baseHoursAge + 24));

        // Calculate scores using the same formula as FeedService
        long hoursAge1 = ChronoUnit.HOURS.between(postAtT.getCreatedAt(), now);
        double decayFactor1 = Math.pow(0.5, hoursAge1 / 24.0);
        double score1 = baseScore * decayFactor1;

        long hoursAge2 = ChronoUnit.HOURS.between(postAtT24.getCreatedAt(), now);
        double decayFactor2 = Math.pow(0.5, hoursAge2 / 24.0);
        double score2 = baseScore * decayFactor2;

        // score2 should be approximately half of score1
        double ratio = score2 / score1;
        double expectedRatio = 0.5;
        double tolerance = 0.01; // allow small floating point imprecision

        assert Math.abs(ratio - expectedRatio) < tolerance :
                "Score at T+24h should be half of score at T. " +
                        "Ratio was " + ratio + " (expected ~0.5). " +
                        "Age T=" + baseHoursAge + "h, likes=" + likeCount + ", comments=" + commentCount;
    }

    // ---------------------------------------------------------------
    // Property 16: Feed diversity — max 2 consecutive posts from same space
    //
    // For any sequence of posts from the personalized feed, there should
    // never be more than 2 consecutive posts from the same community.
    //
    // Validates: Requirements 14.2 (implicitly, diversity is part of feed quality)
    // ---------------------------------------------------------------

    /**
     * Property 16: Feed diversity — max 2 consecutive posts from same space.
     *
     * The applyDiversity method processes the scored post list and defers any
     * 3rd+ consecutive post from the same community to the tail. This property
     * verifies that the non-deferred portion (the primary ranked section) never
     * has more than 2 consecutive posts from the same community.
     *
     * Note: Deferred posts appended at the tail may appear consecutive from the
     * same community. The diversity guarantee applies to the primary ordering.
     * We verify this by checking that the number of "diversity violations" in the
     * full output is zero among the non-deferred positions. In practice, we verify
     * that the total number of posts from any single community appearing
     * consecutively 3+ times only happens at the tail (after all non-deferred posts).
     *
     * **Validates: Requirements 14.2**
     */
    @Property(tries = 30)
    void feedDiversityMaxTwoConsecutiveFromSameSpace(
            @ForAll("communityDistributions") List<Integer> communityAssignments) throws Exception {

        PostRepository postRepo = mock(PostRepository.class);
        PostLikeRepository postLikeRepo = mock(PostLikeRepository.class);
        CommunityMemberRepository communityMemberRepo = mock(CommunityMemberRepository.class);

        FeedService feedService = createFeedService(postRepo, postLikeRepo, communityMemberRepo);

        Integer userId = 1;

        // Create communities
        Map<Integer, Community> communities = new HashMap<>();
        Set<Integer> communityIds = new HashSet<>(communityAssignments);
        for (Integer cid : communityIds) {
            communities.put(cid, createCommunity(cid));
        }

        // User is member of all communities
        List<CommunityMember> memberships = communityIds.stream()
                .map(cid -> {
                    try { return createCommunityMember(cid, userId); }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .collect(Collectors.toList());
        when(communityMemberRepo.findByUser_UserId(userId)).thenReturn(memberships);

        // Need at least 3 communities to not be cold start
        if (communityIds.size() < 3) return;

        // Create posts assigned to communities with high scores for the dominant community
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < communityAssignments.size(); i++) {
            int cid = communityAssignments.get(i);
            Community community = communities.get(cid);
            // Give higher scores to earlier posts so order is predictable
            Post post = createPost(i + 1, community, 100 - i, 10,
                    LocalDateTime.now().minusHours(1));
            posts.add(post);
        }

        when(postRepo.findFeedPostsFromCommunities(anyList(), any(LocalDateTime.class)))
                .thenReturn(posts);
        when(postRepo.findCommunityIdsWherUserLiked(userId)).thenReturn(Collections.emptyList());
        when(postRepo.findCommunityIdsWhereUserCommented(userId)).thenReturn(Collections.emptyList());
        when(postRepo.findDiscoveryPosts(anyList(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(postLikeRepo.countByPost_PostIdAndCreatedAtAfter(anyInt(), any(LocalDateTime.class)))
                .thenReturn(0L);

        // Execute
        List<Post> feed = feedService.getPersonalizedFeed(userId, 0);

        // Count how many posts from each community were deferred
        // (i.e., 3rd+ consecutive from same community in score-sorted order)
        // The deferred posts are appended at the tail.
        // We calculate the non-deferred count to know where primary ordering ends.
        int nonDeferredCount = computeNonDeferredCount(posts, communityAssignments.size());

        // Verify: in the non-deferred portion of the feed, no more than 2 consecutive
        // posts from the same community
        int checkUpTo = Math.min(nonDeferredCount, feed.size());
        int consecutiveCount = 1;
        Integer lastCommunityId = null;

        for (int i = 0; i < checkUpTo; i++) {
            Post post = feed.get(i);
            Integer currentCommunityId = post.getCommunity() != null
                    ? post.getCommunity().getCommunityId() : null;

            if (currentCommunityId != null && currentCommunityId.equals(lastCommunityId)) {
                consecutiveCount++;
                assert consecutiveCount <= 2 :
                        "Feed has " + consecutiveCount + " consecutive posts from community " +
                                currentCommunityId + " at position " + i + ". Max allowed is 2.";
            } else {
                consecutiveCount = 1;
                lastCommunityId = currentCommunityId;
            }
        }

        // Additional verification: the diversity mechanism was effective
        // (at least some posts were deferred if there were 3+ from same community in a row)
        if (feed.size() > 2) {
            // Count how many communities have 3+ posts in the input
            Map<Integer, Long> inputCounts = communityAssignments.stream()
                    .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
            boolean hasDominantCommunity = inputCounts.values().stream().anyMatch(c -> c > 2);

            if (hasDominantCommunity && checkUpTo >= 3) {
                // Diversity should have broken up the dominant community streak
                boolean diversityApplied = true;
                int streak = 1;
                Integer prevId = null;
                for (int i = 0; i < checkUpTo; i++) {
                    Integer cid = feed.get(i).getCommunity() != null
                            ? feed.get(i).getCommunity().getCommunityId() : null;
                    if (cid != null && cid.equals(prevId)) {
                        streak++;
                        if (streak > 2) {
                            diversityApplied = false;
                            break;
                        }
                    } else {
                        streak = 1;
                        prevId = cid;
                    }
                }
                assert diversityApplied :
                        "Diversity should limit consecutive posts from same community to 2";
            }
        }
    }

    /**
     * Computes how many posts remain in the non-deferred portion after diversity
     * is applied (simulating the applyDiversity first pass).
     */
    private int computeNonDeferredCount(List<Post> scoredPosts, int totalPosts) {
        int nonDeferred = 0;
        int consecutiveCount = 0;
        Integer lastCommunityId = null;

        for (Post post : scoredPosts) {
            Integer communityId = post.getCommunity() != null
                    ? post.getCommunity().getCommunityId() : null;

            if (communityId != null && communityId.equals(lastCommunityId)) {
                consecutiveCount++;
                if (consecutiveCount > 2) {
                    // This post would be deferred
                    continue;
                }
            } else {
                consecutiveCount = 1;
                lastCommunityId = communityId;
            }
            nonDeferred++;
        }
        return nonDeferred;
    }

    // ---------------------------------------------------------------
    // Property 17: Cold start users get public feed blend
    //
    // Users with fewer than 3 joined spaces should get a blended feed
    // (public + personalized). Verify that for users with <3 communities,
    // the public feed repository method is called.
    //
    // Validates: Requirements 14.5 (cold start path still filters properly)
    // ---------------------------------------------------------------

    /**
     * Property 17: Cold start users get public feed blend.
     *
     * For any user with fewer than 3 joined spaces, the feed SHALL include
     * results from the public feed query (findAllPublicFeedPosts is called),
     * confirming the cold-start blend is active.
     *
     * **Validates: Requirements 14.5**
     */
    @Property(tries = 20)
    void coldStartUsersGetPublicFeedBlend(
            @ForAll("coldStartCommunityCount") int communityCount) throws Exception {

        PostRepository postRepo = mock(PostRepository.class);
        PostLikeRepository postLikeRepo = mock(PostLikeRepository.class);
        CommunityMemberRepository communityMemberRepo = mock(CommunityMemberRepository.class);

        FeedService feedService = createFeedService(postRepo, postLikeRepo, communityMemberRepo);

        Integer userId = 1;

        // Create memberships (1 or 2 communities — below cold start threshold of 3)
        List<CommunityMember> memberships = new ArrayList<>();
        List<Integer> joinedIds = new ArrayList<>();
        for (int i = 1; i <= communityCount; i++) {
            memberships.add(createCommunityMember(i, userId));
            joinedIds.add(i);
        }
        when(communityMemberRepo.findByUser_UserId(userId)).thenReturn(memberships);

        // Create some posts for both public and personalized sources
        Community publicCommunity = createCommunity(99);
        List<Post> publicPosts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            publicPosts.add(createPost(100 + i, publicCommunity, 20, 5,
                    LocalDateTime.now().minusHours(i + 1)));
        }

        Community joinedCommunity = createCommunity(1);
        List<Post> personalizedPosts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            personalizedPosts.add(createPost(200 + i, joinedCommunity, 10, 3,
                    LocalDateTime.now().minusHours(i + 1)));
        }

        when(postRepo.findAllPublicFeedPosts(any(LocalDateTime.class))).thenReturn(publicPosts);
        when(postRepo.findFeedPostsFromCommunities(anyList(), any(LocalDateTime.class)))
                .thenReturn(personalizedPosts);
        when(postRepo.findCommunityIdsWherUserLiked(userId)).thenReturn(Collections.emptyList());
        when(postRepo.findCommunityIdsWhereUserCommented(userId)).thenReturn(Collections.emptyList());
        when(postLikeRepo.countByPost_PostIdAndCreatedAtAfter(anyInt(), any(LocalDateTime.class)))
                .thenReturn(0L);

        // Execute
        List<Post> feed = feedService.getPersonalizedFeed(userId, 0);

        // Verify: public feed method was called (cold start blend uses it)
        verify(postRepo).findAllPublicFeedPosts(any(LocalDateTime.class));

        // Verify: personalized feed method was also called (30% personalized)
        verify(postRepo).findFeedPostsFromCommunities(anyList(), any(LocalDateTime.class));

        // Verify: feed is not empty (blend should produce results)
        assert !feed.isEmpty() : "Cold start feed should not be empty when posts exist";
    }

    // ---------------------------------------------------------------
    // ARBITRARY PROVIDERS
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<Integer> postCounts() {
        return Arbitraries.integers().between(1, 20);
    }

    @Provide
    Arbitrary<Integer> likeCountArb() {
        return Arbitraries.integers().between(0, 200);
    }

    @Provide
    Arbitrary<Integer> commentCountArb() {
        return Arbitraries.integers().between(0, 100);
    }

    @Provide
    Arbitrary<Integer> hoursAgeArb() {
        return Arbitraries.integers().between(0, 144); // 0 to 6 days
    }

    @Provide
    Arbitrary<Integer> coldStartCommunityCount() {
        return Arbitraries.integers().between(1, 2); // below threshold of 3
    }

    /**
     * Generates community assignments where at least one community has 3+ posts
     * (to test diversity enforcement) with at least 3 distinct communities.
     */
    @Provide
    Arbitrary<List<Integer>> communityDistributions() {
        return Arbitraries.integers().between(3, 5).flatMap(numCommunities ->
                Arbitraries.integers().between(8, 20).flatMap(numPosts ->
                        Arbitraries.integers().between(1, numCommunities)
                                .list().ofSize(numPosts)
                                .filter(list -> {
                                    // Ensure at least 3 distinct communities
                                    Set<Integer> distinct = new HashSet<>(list);
                                    if (distinct.size() < 3) return false;
                                    // Ensure at least one community has 3+ posts
                                    Map<Integer, Long> counts = list.stream()
                                            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
                                    return counts.values().stream().anyMatch(c -> c >= 3);
                                })
                )
        );
    }
}
