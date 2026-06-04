package com.yap.backend.entities;

import com.yap.backend.enums.CommunityCategory;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "communities")
@Entity
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id", nullable = false)
    private Integer communityId;

    @Column(unique = true, length = 30, nullable = false)
    private String communityName;

    @Column(length = 300, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private CommunityCategory category;

    @Column(length = 255)
    private String iconUrl;

    @Column(length = 255)
    private String bannerUrl;

    // stored as comma-separated string for MVP simplicity
    // e.g. "Be kind,No self-promotion,Stay on topic"
    @Column(length = 1000)
    private String guidelines;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isPrivate = false;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int memberCount = 0;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityTag> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    // getters & setters
    public Integer getCommunityId() { return communityId; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public CommunityCategory getCategory() { return category; }
    public void setCategory(CommunityCategory category) { this.category = category; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getGuidelines() { return guidelines; }
    public void setGuidelines(String guidelines) { this.guidelines = guidelines; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public List<CommunityTag> getTags() { return tags; }
    public void setTags(List<CommunityTag> tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}