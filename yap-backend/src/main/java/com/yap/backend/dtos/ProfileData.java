package com.yap.backend.dtos;

public class ProfileData {
    private Integer userId;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private String bannerUrl;
    private int followerCount;
    private int followingCount;
    private int communityCount;
    private String role;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int v) { this.followerCount = v; }
    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int v) { this.followingCount = v; }
    public int getCommunityCount() { return communityCount; }
    public void setCommunityCount(int v) { this.communityCount = v; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}