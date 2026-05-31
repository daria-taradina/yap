package com.yap.backend.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.yap.backend.keys.PostLikeId;
import jakarta.persistence.*;

@Table(name = "post_like")
@Entity
public class PostLike {
	@EmbeddedId
	private PostLikeId id;
	
	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne
	@MapsId("postId")
	@JoinColumn(name = "post_id")
	private Post post;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	
	public PostLike() {}
	
	// getters & setters
	public PostLikeId getId() {
		return id;
	}

	public void setId(PostLikeId id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}	

}
