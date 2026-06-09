package com.yap.backend.repositories;

import com.yap.backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
	
	@Query("""
		    SELECT p FROM Post p
		    LEFT JOIN FETCH p.author
		    LEFT JOIN FETCH p.community
		    LEFT JOIN FETCH p.tags pt
		    LEFT JOIN FETCH pt.tag
		    WHERE p.postId = :postId
		      AND p.isDeleted = false
		""")
		Optional<Post> findByIdWithTags(@Param("postId") Integer postId);
	
	
    // ---------------------------------------------------------------
    // FEED: posts from communities the user is a member of
    //       + posts from users the user follows
    //       newest first, not deleted
    // ---------------------------------------------------------------
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND (
            p.community.communityId IN (
                SELECT cm.id.communityId FROM CommunityMember cm
                WHERE cm.id.userId = :userId
            )
            OR
            p.author.userId IN (
                SELECT uf.id.followingId FROM UserFollow uf
                WHERE uf.id.followerId = :userId
            )
          )
        ORDER BY p.createdAt DESC
    """)
    List<Post> findFeedPostsForUser(@Param("userId") Integer userId);
    
 // ---------------------------------------------------------------
 // FEED ALGORITHM 1: posts that have any of the given trending tags
 // newest first, not deleted
 // ---------------------------------------------------------------
 @Query("""
     SELECT DISTINCT p FROM Post p
     LEFT JOIN FETCH p.author
     LEFT JOIN FETCH p.community
     LEFT JOIN FETCH p.tags pt
     LEFT JOIN FETCH pt.tag
     WHERE p.isDeleted = false
       AND EXISTS (
           SELECT 1 FROM PostTag ptx
           JOIN ptx.tag tx
           WHERE ptx.post = p AND tx.name IN :tagNames
       )
     ORDER BY p.createdAt DESC
 """)
 List<Post> findPostsByTrendingTags(@Param("tagNames") List<String> tagNames);

    // ---------------------------------------------------------------
    // TRENDING TAGS: top tags by distinct post count, last 30 days
    // Returns Object[] rows: [tagName, postCount]
    // ---------------------------------------------------------------
    @Query("""
        SELECT pt.tag.name, COUNT(DISTINCT pt.post.postId) AS postCount
        FROM PostTag pt
        WHERE pt.post.isDeleted = false
          AND pt.post.createdAt >= :since
        GROUP BY pt.tag.name
        ORDER BY postCount DESC
    """)
    List<Object[]> findTrendingTagsRaw(@Param("since") LocalDateTime since);


    // ---------------------------------------------------------------
    // POSTS BY EXACT TAG NAME (for tag-click from trending / tag page)
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND EXISTS (
              SELECT 1 FROM PostTag ptx
              JOIN ptx.tag tx
              WHERE ptx.post = p AND tx.name = :tagName
          )
        ORDER BY p.createdAt DESC
    """)
    List<Post> findPostsByExactTag(@Param("tagName") String tagName);


    // ---------------------------------------------------------------
    // SEARCH BY TAG (contains match on tag name)
    // Returns all posts that have at least one tag whose name contains
    // the search string — exact match ranks first via ORDER BY
    // ---------------------------------------------------------------
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND EXISTS (
              SELECT 1 FROM PostTag ptx
              JOIN ptx.tag tx
              WHERE ptx.post = p AND LOWER(tx.name) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY p.createdAt DESC
    """)
    List<Post> findPostsByTagContaining(@Param("query") String query);


    // ---------------------------------------------------------------
    // PROFILE POSTS: posts with no community by a given user
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND p.author.userId = :userId
          AND p.community IS NULL
        ORDER BY p.createdAt DESC
    """)
    List<Post> findProfilePostsByUser(@Param("userId") Integer userId);


    // ---------------------------------------------------------------
    // COMMUNITY POSTS MADE BY A USER (posts they authored in any community)
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND p.author.userId = :userId
          AND p.community IS NOT NULL
        ORDER BY p.createdAt DESC
    """)
    List<Post> findCommunityPostsByUser(@Param("userId") Integer userId);
    
    // ---------------------------------------------------------------
    // POSTS USER LIKED
    // ---------------------------------------------------------------
    @Query("""
    	    SELECT DISTINCT p FROM Post p
    	    LEFT JOIN FETCH p.author
    	    LEFT JOIN FETCH p.community
    	    LEFT JOIN FETCH p.tags pt
    	    LEFT JOIN FETCH pt.tag
    	    WHERE p.isDeleted = false
    	      AND EXISTS (
    	          SELECT 1 FROM PostLike pl
    	          WHERE pl.post = p AND pl.user.userId = :userId
    	      )
    	    ORDER BY p.createdAt DESC
    	""")
    	List<Post> findPostsLikedByUser(@Param("userId") Integer userId);

    // ---------------------------------------------------------------
    // ALL POSTS IN A COMMUNITY
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND p.community.communityId = :communityId
        ORDER BY p.createdAt DESC
    """)
    List<Post> findPostsByCommunity(@Param("communityId") Integer communityId);

    // ---------------------------------------------------------------
    // ALL DISCUSSION POSTS from communities the user is a member of
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND p.postType = com.yap.backend.enums.PostType.DISCUSSION
          AND p.community.communityId IN (
              SELECT cm.id.communityId FROM CommunityMember cm
              WHERE cm.id.userId = :userId
          )
        ORDER BY p.createdAt DESC
    """)
    List<Post> findDiscussionPostsForUser(@Param("userId") Integer userId);

    // ---------------------------------------------------------------
    // ALL BLOG POSTS (community IS NULL, postType = BLOG)
    // newest first
    // ---------------------------------------------------------------
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.isDeleted = false
          AND p.postType = com.yap.backend.enums.PostType.BLOG
          AND p.community IS NULL
        ORDER BY p.createdAt DESC
    """)
    List<Post> findAllBlogPosts();
}