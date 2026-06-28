package com.yap.backend.enums;

public enum NotificationType {
    POST_LIKED,
    POST_COMMENTED,
    COMMENT_LIKED,
    COMMENT_REPLIED,
    USER_FOLLOWED,
    POST_REMOVED,       // mod action
    COMMENT_REMOVED,    // mod action
    NEW_REPORT,         // notify mods/admins of new report
    CONTENT_REMOVED     // notify author that content was removed
}