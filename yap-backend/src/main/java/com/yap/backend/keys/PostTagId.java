package com.yap.backend.keys;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;

@Embeddable
public class PostTagId implements Serializable {
    private Integer postId;
    private Integer tagId;

    public PostTagId() {}

    public PostTagId(Integer postId, Integer tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PostTagId other = (PostTagId) obj;
        return Objects.equals(postId, other.postId) && Objects.equals(tagId, other.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, tagId);
    }
}