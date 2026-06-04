package com.yap.backend.dtos;

public class TrendingTag {

    private String tagName;
    private long postCount;

    public TrendingTag(String tagName, long postCount) {
        this.tagName = tagName;
        this.postCount = postCount;
    }

    public String getTagName()  { return tagName; }
    public long getPostCount()  { return postCount; }
}