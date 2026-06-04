package com.yap.backend.dtos;

import com.yap.backend.enums.CommunityCategory;
import java.time.LocalDateTime;
import java.util.List;

public class CommunityResponse {

    private Integer communityId;
    private String name;
    private String description;
    private int memberCount;
    private LocalDateTime createdAt;
    private Integer ownerId;
    private String ownerUsername;
    private CommunityCategory category;
    private String iconUrl;
    private String bannerUrl;
    private String guidelines;
    private List<String> tags;
    private boolean isMember;

    public CommunityResponse() {}

    public CommunityResponse(Integer communityId, String name, String description,
            int memberCount, LocalDateTime createdAt, Integer ownerId, String ownerUsername,
            CommunityCategory category, String iconUrl, String bannerUrl,
            String guidelines, List<String> tags, boolean isMember) {
        this.communityId = communityId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.category = category;
        this.iconUrl = iconUrl;
        this.bannerUrl = bannerUrl;
        this.guidelines = guidelines;
        this.tags = tags;
        this.isMember = isMember;
    }

    public Integer getCommunityId() { return communityId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMemberCount() { return memberCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public CommunityCategory getCategory() { return category; }
    public String getIconUrl() { return iconUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public String getGuidelines() { return guidelines; }
    public List<String> getTags() { return tags; }
    public boolean isMember() { return isMember; }
    public void setMember(boolean member) { isMember = member; }
}