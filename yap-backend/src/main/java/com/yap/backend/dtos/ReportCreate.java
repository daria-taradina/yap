package com.yap.backend.dtos;

import com.yap.backend.enums.ReportReason;
import jakarta.validation.constraints.NotNull;

public class ReportCreate {

    private Integer postId;
    private Integer commentId;

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    private String details;

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }

    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
