package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserFollowId implements Serializable {
	private Integer followerId;  // current user in session
    private Integer followingId; // user followed by current user in session
    
    public UserFollowId () {}	
    
    public UserFollowId(Integer followerId, Integer followingId) {
    	this.followerId = followerId;
    	this.followingId = followingId;
    }
    
    // getters&setters
	public Integer getFollowerId() {
		return followerId;
	}

	public void setFollowerId(Integer followerId) {
		this.followerId = followerId;
	}

	public Integer getFollowingId() {
		return followingId;
	}

	public void setFollowingId(Integer followingId) {
		this.followingId = followingId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(followerId, followingId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserFollowId other = (UserFollowId) obj;
		return Objects.equals(followerId, other.followerId) && Objects.equals(followingId, other.followingId);
	}
    
}
