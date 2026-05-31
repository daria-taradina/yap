package com.yap.backend.entities;

import com.yap.backend.keys.CommunityTagId;
import jakarta.persistence.*;

@Table(name = "community_tag")
@Entity
public class CommunityTag {
	@EmbeddedId
	private CommunityTagId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("communityId")
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
    
    public CommunityTag() {}
    
    public CommunityTag(Community community, Tag tag) {
    	this.id = new CommunityTagId(community.getCommunityId(), tag.getTagId());
    	this.community = community;
    	this.tag = tag;
    }
    
    //getters&setters
	public CommunityTagId getId() {
		return id;
	}

	public void setId(CommunityTagId id) {
		this.id = id;
	}

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
    
}
