package com.yap.backend.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Table(name = "comments")
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id", nullable = false)
    private User author;

    // self-referencing for nested replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "comment_id")
    private Comment parentComment;

    @Column(length = 1000)
    private String contentText;

    @Column(name = "gif_url")
    private String gifUrl;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    // Perspective API
    @Column
    private Float toxicityScore;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isFlagged = false;

    // mod removal — separate from author delete
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isRemoved = false;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Comment() {}

    // getters & setters
    public Integer getCommentId() { return commentId; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }

    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public Float getToxicityScore() { return toxicityScore; }
    public void setToxicityScore(Float toxicityScore) { this.toxicityScore = toxicityScore; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public boolean isRemoved() { return isRemoved; }
    public void setRemoved(boolean removed) { isRemoved = removed; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}