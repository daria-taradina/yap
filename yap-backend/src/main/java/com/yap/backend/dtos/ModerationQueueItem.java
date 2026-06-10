package com.yap.backend.dtos;

import com.yap.backend.enums.ContentCategory;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.enums.RiskLevel;
import java.time.LocalDateTime;

public record ModerationQueueItem(
    Integer decisionId,
    Integer postId,
    Integer commentId,
    String contentText,
    String authorUsername,
    ContentCategory category,
    RiskLevel risk,
    ModerationAction action,
    String reasoning,
    LocalDateTime createdAt
) {}
