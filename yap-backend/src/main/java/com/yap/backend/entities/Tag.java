package com.yap.backend.entities;

import jakarta.persistence.*;

@Table(name = "tag")
@Entity
public class Tag {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id",nullable = false)
    private Integer tagId;
	
	// length = 20
	@Column(length = 20, unique = true, nullable = false)
	private String name; // stored lowercase, no spaces/special chars, nums allowed e.g. "computerscience"
	
	public Tag() {}
	
	public Tag(String name) { this.name = name; }
	
	//getters&setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTagId() {
		return tagId;
	}
	
}
