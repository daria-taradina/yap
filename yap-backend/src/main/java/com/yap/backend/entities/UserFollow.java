package com.yap.backend.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.yap.backend.keys.*;

@Table(name = "user_follow", 
	   indexes = { @Index(name = "idx_following_id", columnList = "following_id") })
@Entity
public class UserFollow {
	@EmbeddedId
	private UserFollowId id;
	
	@ManyToOne
	@MapsId("followerId")
	@JoinColumn(name = "follower_id") // current user in session
	private User follower;
	
	@ManyToOne
	@MapsId("followingId")
	@JoinColumn(name = "following_id") // user followed by current user in session
	private User following;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	
	public UserFollow() {}
	
	//getters&setters
	public UserFollowId getId() {
		return id;
	}

	public void setId(UserFollowId id) {
		this.id = id;
	}

	public User getFollower() {
		return follower;
	}

	public void setFollower(User follower) {
		this.follower = follower;
	}

	public User getFollowing() {
		return following;
	}

	public void setFollowing(User following) {
		this.following = following;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}	

}
