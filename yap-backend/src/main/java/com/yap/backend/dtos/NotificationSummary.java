package com.yap.backend.dtos;

import com.yap.backend.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationSummary {

    private Integer notificationId;
    private NotificationType type;
    private String actorUsername;
    private String actorAvatarUrl;
    private Integer postId;
    private String postTitle;
    private Integer commentId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationSummary() {}

    public NotificationSummary(Integer notificationId, NotificationType type,
                                String actorUsername, String actorAvatarUrl,
                                Integer postId, String postTitle,
                                Integer commentId, boolean isRead,
                                LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.type = type;
        this.actorUsername = actorUsername;
        this.actorAvatarUrl = actorAvatarUrl;
        this.postId = postId;
        this.postTitle = postTitle;
        this.commentId = commentId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // human-readable message for frontend
    public String getMessage() {
        String actor = actorUsername != null ? "@" + actorUsername : "Someone";
        return switch (type) {
            case POST_LIKED     -> actor + " liked your post";
            case POST_COMMENTED -> actor + " commented on your post";
            case COMMENT_LIKED  -> actor + " liked your comment";
            case COMMENT_REPLIED -> actor + " replied to your comment";
            case USER_FOLLOWED  -> actor + " started following you";
            case POST_REMOVED   -> "Your post was removed by a moderator";
            case COMMENT_REMOVED -> "Your comment was removed by a moderator";
        };
    }

    public Integer getNotificationId() { return notificationId; }
    public NotificationType getType() { return type; }
    public String getActorUsername() { return actorUsername; }
    public String getActorAvatarUrl() { return actorAvatarUrl; }
    public Integer getPostId() { return postId; }
    public String getPostTitle() { return postTitle; }
    public Integer getCommentId() { return commentId; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}