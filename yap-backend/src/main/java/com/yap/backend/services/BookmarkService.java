package com.yap.backend.services;

import com.yap.backend.dtos.PageResponse;
import com.yap.backend.dtos.PostSummary;
import com.yap.backend.entities.Bookmark;
import com.yap.backend.entities.Post;
import com.yap.backend.entities.User;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.exceptions.ResourceNotFoundException;
import com.yap.backend.keys.BookmarkId;
import com.yap.backend.keys.PostLikeId;
import com.yap.backend.repositories.BookmarkRepository;
import com.yap.backend.repositories.CommunityMemberRepository;
import com.yap.backend.repositories.PostLikeRepository;
import com.yap.backend.repositories.PostRepository;
import com.yap.backend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookmarkService extends BaseService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommunityMemberRepository communityMemberRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           PostRepository postRepository,
                           PostLikeRepository postLikeRepository,
                           CommunityMemberRepository communityMemberRepository,
                           UserRepository userRepository) {
        super(userRepository);
        this.bookmarkRepository = bookmarkRepository;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.communityMemberRepository = communityMemberRepository;
    }

    @Transactional
    public Map<String, Boolean> toggleBookmark(Integer postId) {
        User currentUser = getAuthenticatedUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        BookmarkId bookmarkId = new BookmarkId(currentUser.getUserId(), postId);

        if (bookmarkRepository.existsById(bookmarkId)) {
            bookmarkRepository.deleteById(bookmarkId);
            return Map.of("bookmarked", false);
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setId(bookmarkId);
            bookmark.setUser(currentUser);
            bookmark.setPost(post);
            bookmarkRepository.save(bookmark);
            return Map.of("bookmarked", true);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummary> getBookmarkedPosts(int page, int size) {
        User currentUser = getAuthenticatedUser();
        Integer currentUserId = currentUser.getUserId();

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserIdExcludingHidden(
                currentUserId, PageRequest.of(page, size));

        List<PostSummary> summaries = bookmarkPage.getContent().stream()
                .map(bookmark -> mapToSummary(bookmark.getPost(), currentUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(summaries, page, size, bookmarkPage.getTotalElements());
    }

    private PostSummary mapToSummary(Post post, Integer currentUserId) {
        List<String> tagNames = post.getTags().stream()
                .map(pt -> pt.getTag().getName())
                .collect(Collectors.toList());

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
                post.getFlair(),
                true // isBookmarked — this is always true from the bookmarks list
        );
    }
}
