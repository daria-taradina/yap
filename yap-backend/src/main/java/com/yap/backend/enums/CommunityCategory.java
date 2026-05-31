package com.yap.backend.enums;

public enum CommunityCategory {
    CAREER_AND_MONEY("Career & Money"),
    HEALTH_AND_WELLNESS("Health & Wellness"),
    RELATIONSHIPS_AND_FAMILY("Relationships & Family"),
    IDENTITY_AND_EXPERIENCES("Identity & Life Experiences"),
    KNOWLEDGE_AND_LEARNING("Knowledge & Learning"),
    CREATIVITY_AND_EXPRESSION("Creativity & Expression"),
    HOBBIES_AND_INTERESTS("Hobbies & Interests"),
    LIFESTYLE_AND_TRAVEL("Lifestyle & Travel"),
    ENTERTAINMENT_AND_CULTURE("Entertainment & Culture"),
    TECHNOLOGY_AND_GAMING("Technology & Gaming"),
    QA_AND_STORIES("Q&As & Stories"),
    SOCIETY_AND_CURRENT_ISSUES("Society & Current Issues"),
    SUPPORT_AND_SENSITIVE_TOPICS("Support & Sensitive Topics"),
    OTHER("Other");

    private final String displayName;

    CommunityCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}