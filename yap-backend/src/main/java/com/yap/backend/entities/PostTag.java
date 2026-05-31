package com.yap.backend.entities;

import com.yap.backend.keys.PostTagId;
import jakarta.persistence.*;

@Table(name = "post_tag", indexes = {
	    @Index(name = "idx_post_tag_tag_id", columnList = "tag_id"),
	    @Index(name = "idx_post_tag_post_id", columnList = "post_id")
	})
@Entity
public class PostTag {
	@EmbeddedId
	private PostTagId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
	
	public PostTag() {}
	
	public PostTag(Post post, Tag tag) {
		this.id = new PostTagId(post.getPostId(), tag.getTagId());
		this.post = post;
		this.tag = tag;		
	}
	
	//getters&setters
	public PostTagId getId() {
		return id;
	}

	public void setId(PostTagId id) {
		this.id = id;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
}
