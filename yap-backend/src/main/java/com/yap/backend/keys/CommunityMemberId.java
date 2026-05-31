package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommunityMemberId implements Serializable {
	private Integer communityId;
    private Integer userId;
    
    public CommunityMemberId() {}
    
    public CommunityMemberId(Integer communityId, Integer userId) {
    	this.communityId = communityId;
    	this.userId = userId;
    }    
    // getters&setters   
	public Integer getCommunityId() {
		return communityId;
	}

	public void setCommunityId(Integer communityId) {
		this.communityId = communityId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, userId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommunityMemberId other = (CommunityMemberId) obj;
		return Objects.equals(communityId, other.communityId) && Objects.equals(userId, other.userId);
	}    
    	
}
