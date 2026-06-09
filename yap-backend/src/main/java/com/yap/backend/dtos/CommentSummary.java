package com.yap.backend.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentSummary {

    private Integer commentId;
    private Integer postId;
    private Integer parentCommentId;
    private String authorUsername;
    private Integer authorId;
    private String authorProfilePicture;
    private String contentText;
    private LocalDateTime createdAt;
    private String gifUrl;
    private int likeCount;
    private boolean likedByCurrentUser;
    private boolean canDelete;
    private List<CommentSummary> replies = new ArrayList<>();

    public CommentSummary() {}

    public CommentSummary(Integer commentId, Integer postId, Integer parentCommentId,
                          String authorUsername, Integer authorId, String authorProfilePicture,
                          String contentText, LocalDateTime createdAt,
                          String gifUrl, int likeCount,
                          boolean likedByCurrentUser, boolean canDelete) {
        this.commentId = commentId;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.authorUsername = authorUsername;
        this.authorId = authorId;
        this.authorProfilePicture = authorProfilePicture;
        this.contentText = contentText;
        this.createdAt = createdAt;
        this.gifUrl = gifUrl;
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.canDelete = canDelete;
    }

    public void addReply(CommentSummary reply) {
        this.replies.add(reply);
    }

    // getters
    public Integer getCommentId() {
        return commentId;
    }

    public Integer getPostId() {
        return postId;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public String getAuthorProfilePicture() {
        return authorProfilePicture;
    }

    public String getContentText() {
        return contentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public List<CommentSummary> getReplies() {
        return replies;
    }
}
