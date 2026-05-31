package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;

public class CommentLikeId implements Serializable {
    private Integer userId;
    private Integer commentId;

    public CommentLikeId() {}

    public CommentLikeId(Integer userId, Integer commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
    
    // getters & setters
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(commentId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommentLikeId other = (CommentLikeId) obj;
		return Objects.equals(commentId, other.commentId) && Objects.equals(userId, other.userId);
	}
    
}