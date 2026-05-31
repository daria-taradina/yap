package com.yap.backend.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.yap.backend.keys.CommunityMemberId;
import com.yap.backend.enums.*;

@Table(name = "community_member")
@Entity
public class CommunityMember {
	@EmbeddedId
	private CommunityMemberId id;
	
	@ManyToOne
	@MapsId("communityId")
	@JoinColumn(name = "community_id")
	private Community community;
	
	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime joinedAt;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 50, nullable = false) // nullable false
	private CommunityMemberRole role;
	
	public CommunityMember() {}
	
	public CommunityMember(Community community, User user, CommunityMemberRole role) {
		this.id = new CommunityMemberId(community.getCommunityId(), user.getUserId());
		this.community = community;
		this.user = user;
		this.role = role;
	}
	
	//getters&setters
	public CommunityMemberId getId() {
		return id;
	}

	public void setId(CommunityMemberId id) {
		this.id = id;
	}

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getJoinedAt() {
		return joinedAt;
	}

	public CommunityMemberRole getRole() {
		return role;
	}

	public void setRole(CommunityMemberRole role) {
		this.role = role;
	}
		
}
