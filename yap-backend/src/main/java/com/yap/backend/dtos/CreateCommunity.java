package com.yap.backend.dtos;

import com.yap.backend.enums.CommunityCategory;
import jakarta.validation.constraints.*;
import java.util.List;

public class CreateCommunity {

    @NotBlank(message = "Space name is required")
    @Size(min = 3, max = 30, message = "Name must be 3-30 characters")
    @Pattern(regexp = "^[a-z0-9_]+$",
             message = "Only lowercase letters, numbers, and underscores allowed")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @NotNull(message = "Category is required")
    private CommunityCategory category;

    private List<String> guidelines;
    private List<String> tags;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CommunityCategory getCategory() { return category; }
    public void setCategory(CommunityCategory category) { this.category = category; }

    public List<String> getGuidelines() { return guidelines; }
    public void setGuidelines(List<String> guidelines) { this.guidelines = guidelines; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}