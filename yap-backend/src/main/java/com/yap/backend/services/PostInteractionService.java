// services/PostInteractionService.java
package com.yap.backend.services;

import com.yap.backend.dtos.*;
import com.yap.backend.entities.*;
import com.yap.backend.enums.CommunityMemberRole;
import com.yap.backend.exceptions.*;
import com.yap.backend.keys.*;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostInteractionService extends BaseService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommentLikeRepository commentLikeRepository;

    public PostInteractionService(PostRepository postRepository,
                                   PostLikeRepository postLikeRepository,
                                   CommentRepository commentRepository,
                                   CommunityMemberRepository communityMemberRepository,
                                   CommentLikeRepository commentLikeRepository,
                                   UserRepository userRepository) {
        super(userRepository);
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Transactional
    public void likePost(Integer postId) {
        User currentUser = getAuthenticatedUser();
        Post post = getActivePost(postId);
        PostLikeId likeId = new PostLikeId(currentUser.getUserId(), postId);
        if (postLikeRepository.existsById(likeId)) throw new ResourceAlreadyExistsException("Already liked");
        PostLike like = new PostLike();
        like.setId(likeId); like.setUser(currentUser); like.setPost(post);
        postLikeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Integer postId) {
        User currentUser = getAuthenticatedUser();
        Post post = getActivePost(postId);
        PostLikeId likeId = new PostLikeId(currentUser.getUserId(), postId);
        if (!postLikeRepository.existsById(likeId)) throw new ResourceNotFoundException("Not liked");
        postLikeRepository.deleteById(likeId);
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    @Transactional
    public CommentSummary createComment(CommentCreate dto) {
        User currentUser = getAuthenticatedUser();
        if ((dto.getContentText() == null || dto.getContentText().isBlank()) &&
            (dto.getGifUrl() == null || dto.getGifUrl().isBlank()))
            throw new InvalidInputException("Comment must have text or a GIF");

        Post post = getActivePost(dto.getPostId());
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setContentText(dto.getContentText());
        comment.setGifUrl(dto.getGifUrl());

        if (dto.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentCommentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            if (parent.isDeleted()) throw new ResourceNotFoundException("Parent comment not found");
            comment.setParentComment(parent.getParentComment() != null ? parent.getParentComment() : parent);
        }

        Comment saved = commentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return mapToDTO(saved, currentUser.getUserId());
    }

    @Transactional
    public void deleteComment(Integer commentId) {
        User currentUser = getAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (comment.isDeleted()) throw new ResourceNotFoundException("Comment not found: " + commentId);

        boolean isAuthor = comment.getAuthor().getUserId().equals(currentUser.getUserId());
        if (!isAuthor) {
            boolean isMod = communityMemberRepository.existsById_CommunityIdAndId_UserIdAndRole(
                comment.getPost().getCommunity().getCommunityId(), currentUser.getUserId(), CommunityMemberRole.MOD);
            if (!isMod) throw new UnauthorizedException("Not allowed");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<CommentSummary> getCommentsForPost(Integer postId) {
        if (!postRepository.existsById(postId)) throw new ResourceNotFoundException("Post not found: " + postId);
        User currentUser = getAuthenticatedUser();
        List<Comment> flat = commentRepository.findByPostId(postId);

        Map<Integer, CommentSummary> byId = new LinkedHashMap<>();
        List<CommentSummary> roots = new ArrayList<>();

        for (Comment c : flat) byId.put(c.getCommentId(), mapToDTO(c, currentUser.getUserId()));

        for (Comment c : flat) {
            CommentSummary dto = byId.get(c.getCommentId());
            if (c.getParentComment() == null) {
                roots.add(dto);
            } else {
                CommentSummary parent = byId.get(c.getParentComment().getCommentId());
                if (parent != null) parent.addReply(dto); else roots.add(dto);
            }
        }
        return roots;
    }

    @Transactional
    public void likeComment(Integer commentId) {
        User currentUser = getAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        CommentLikeId likeId = new CommentLikeId(currentUser.getUserId(), commentId);
        if (commentLikeRepository.existsById(likeId)) throw new ResourceAlreadyExistsException("Already liked");
        CommentLike like = new CommentLike();
        like.setId(likeId); like.setUser(currentUser); like.setComment(comment);
        commentLikeRepository.save(like);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    @Transactional
    public void unlikeComment(Integer commentId) {
        User currentUser = getAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        CommentLikeId likeId = new CommentLikeId(currentUser.getUserId(), commentId);
        if (!commentLikeRepository.existsById(likeId)) throw new ResourceNotFoundException("Not liked");
        commentLikeRepository.deleteById(likeId);
        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        commentRepository.save(comment);
    }

    private Post getActivePost(Integer postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (post.isDeleted()) throw new ResourceNotFoundException("Post not found: " + postId);
        return post;
    }

    private CommentSummary mapToDTO(Comment comment, Integer currentUserId) {
        boolean liked = commentLikeRepository.existsByComment_CommentIdAndUser_UserId(
            comment.getCommentId(), currentUserId);
        boolean canDelete = comment.getAuthor().getUserId().equals(currentUserId)
            || (comment.getPost().getCommunity() != null &&
                communityMemberRepository.existsById_CommunityIdAndId_UserIdAndRole(
                    comment.getPost().getCommunity().getCommunityId(), currentUserId, CommunityMemberRole.MOD));

        return new CommentSummary(
            comment.getCommentId(),
            comment.getPost().getPostId(),
            comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
            comment.getAuthor().getUsername(),
            comment.getAuthor().getUserId(),
            comment.getAuthor().getAvatarUrl(),
            comment.getContentText(),
            comment.getCreatedAt(),
            comment.getGifUrl(),
            comment.getLikeCount(),
            liked,
            canDelete
        );
    }
}