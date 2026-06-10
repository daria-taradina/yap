package com.yap.backend.dtos;

import com.yap.backend.enums.ContentCategory;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.enums.RiskLevel;

public record ModerationResult(
    ContentCategory category,
    RiskLevel risk,
    ModerationAction action,
    String reasoning
) {}
