package com.yap.backend.entities;

import com.yap.backend.enums.PostType;
import java.time.LocalDateTime;
import java.util.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Table(name = "posts", indexes = {
    @Index(name = "idx_post_created_at", columnList = "is_deleted, created_at DESC")
})
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", referencedColumnName = "community_id", nullable = false)
    private Community community;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'DISCUSSION'")
    private PostType postType = PostType.DISCUSSION;

    @Column(length = 300, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String contentText;

    @Column(name = "gif_url")
    private String gifUrl;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int commentCount = 0;

    // Perspective API score — null until scored
    @Column
    private Float toxicityScore;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isFlagged = false;

    // mod action — different from isDeleted (author action)
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isRemoved = false;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> tags = new ArrayList<>();

    // getters & setters
    public Integer getPostId() { return postId; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public Float getToxicityScore() { return toxicityScore; }
    public void setToxicityScore(Float toxicityScore) { this.toxicityScore = toxicityScore; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public boolean isRemoved() { return isRemoved; }
    public void setRemoved(boolean removed) { isRemoved = removed; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<PostTag> getTags() { return tags; }
    public void setTags(List<PostTag> tags) { this.tags = tags; }
}