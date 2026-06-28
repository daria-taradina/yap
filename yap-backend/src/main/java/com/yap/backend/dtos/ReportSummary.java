package com.yap.backend.dtos;

import java.time.LocalDateTime;

public class ReportSummary {
    private Integer reportId;
    private String reason;
    private String details;
    private String reporterUsername;
    private LocalDateTime createdAt;
    private PostInfo post;
    private CommentInfo comment;

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public PostInfo getPost() { return post; }
    public void setPost(PostInfo post) { this.post = post; }

    public CommentInfo getComment() { return comment; }
    public void setComment(CommentInfo comment) { this.comment = comment; }

    public static class PostInfo {
        private Integer postId;
        private String title;
        private String contentText;
        private String authorUsername;
        private String communityName;
        private LocalDateTime createdAt;

        public Integer getPostId() { return postId; }
        public void setPostId(Integer postId) { this.postId = postId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContentText() { return contentText; }
        public void setContentText(String contentText) { this.contentText = contentText; }

        public String getAuthorUsername() { return authorUsername; }
        public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

        public String getCommunityName() { return communityName; }
        public void setCommunityName(String communityName) { this.communityName = communityName; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CommentInfo {
        private Integer commentId;
        private String contentText;
        private String authorUsername;
        private LocalDateTime createdAt;

        public Integer getCommentId() { return commentId; }
        public void setCommentId(Integer commentId) { this.commentId = commentId; }

        public String getContentText() { return contentText; }
        public void setContentText(String contentText) { this.contentText = contentText; }

        public String getAuthorUsername() { return authorUsername; }
        public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
