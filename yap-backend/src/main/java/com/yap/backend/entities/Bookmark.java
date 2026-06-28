package com.yap.backend.entities;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.yap.backend.keys.BookmarkId;
import jakarta.persistence.*;

@Table(name = "bookmark")
@Entity
public class Bookmark {
	@EmbeddedId
	private BookmarkId id;

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

	public Bookmark() {}

	public BookmarkId getId() {
		return id;
	}

	public void setId(BookmarkId id) {
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
