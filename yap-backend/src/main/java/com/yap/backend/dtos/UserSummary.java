package com.yap.backend.dtos;

public class UserSummary {
    private Integer userId;
    private String username;
    private String avatarUrl;

    public UserSummary(Integer userId, String username, String avatarUrl) {
        this.userId = userId; this.username = username; this.avatarUrl = avatarUrl;
    }
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
}