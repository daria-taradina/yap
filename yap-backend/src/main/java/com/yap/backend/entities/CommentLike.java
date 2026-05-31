package com.yap.backend.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.yap.backend.keys.CommentLikeId;
import jakarta.persistence.*;

@Table(name = "comment_like")
@Entity
public class CommentLike {
    @EmbeddedId
    private CommentLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    public CommentLike() {}

	// getters & setters
	public CommentLikeId getId() {
		return id;
	}

	public void setId(CommentLikeId id) {
		this.id = id;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}    
    
}
