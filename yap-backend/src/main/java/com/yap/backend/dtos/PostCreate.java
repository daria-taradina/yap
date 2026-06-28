package com.yap.backend.dtos;

import com.yap.backend.enums.PostType;
import jakarta.validation.constraints.*;
import java.util.List;

public class PostCreate {

    private Integer communityId;

    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String contentText;

    private PostType postType = PostType.DISCUSSION;

    @Size(max = 5, message = "Maximum 5 tags allowed")
    private List<String> tags;

    @Pattern(regexp = "^https://media[0-9]*\\.giphy\\.com/.*$",
             message = "Invalid GIF URL")
    private String gifUrl;

    @Size(max = 50, message = "Flair must not exceed 50 characters")
    private String flair;

    public Integer getCommunityId() { return communityId; }
    public void setCommunityId(Integer communityId) { this.communityId = communityId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getFlair() { return flair; }
    public void setFlair(String flair) { this.flair = flair; }
}