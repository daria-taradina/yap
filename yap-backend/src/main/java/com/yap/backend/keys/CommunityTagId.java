package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;

@Embeddable
public class CommunityTagId implements Serializable {
	private Integer communityId;	
	private Integer tagId;
	
	public CommunityTagId() {}
	
	public CommunityTagId(Integer communityId, Integer tagId) {
		this.communityId = communityId;
		this.tagId = tagId;		
	}
	
	// getters&setters
	public Integer getCommunityId() {
		return communityId;
	}

	public void setCommunityId(Integer communityId) {
		this.communityId = communityId;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, tagId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommunityTagId other = (CommunityTagId) obj;
		return Objects.equals(communityId, other.communityId) && Objects.equals(tagId, other.tagId);
	}
		
}
