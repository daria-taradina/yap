package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class BookmarkId implements Serializable {
	private Integer userId;
	private Integer postId;

	public BookmarkId() {}

	public BookmarkId(Integer userId, Integer postId) {
		this.userId = userId;
		this.postId = postId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(postId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarkId other = (BookmarkId) obj;
		return Objects.equals(postId, other.postId) && Objects.equals(userId, other.userId);
	}
}
