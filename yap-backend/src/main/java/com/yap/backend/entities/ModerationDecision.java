package com.yap.backend.entities;

import com.yap.backend.enums.ContentCategory;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.enums.RiskLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Table(name = "moderation_decisions")
@Entity
public class ModerationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "decision_id", nullable = false)
    private Integer decisionId;

    // one of these two must be set
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RiskLevel risk;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ModerationAction action;

    @Column(length = 1000)
    private String reasoning;

    @Column(nullable = false)
    private boolean overridden = false;

    // mod who overrode the AI decision
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by", referencedColumnName = "user_id")
    private User overriddenBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ModerationDecision() {}

    // getters & setters
    public Integer getDecisionId() { return decisionId; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }

    public ContentCategory getCategory() { return category; }
    public void setCategory(ContentCategory category) { this.category = category; }

    public RiskLevel getRisk() { return risk; }
    public void setRisk(RiskLevel risk) { this.risk = risk; }

    public ModerationAction getAction() { return action; }
    public void setAction(ModerationAction action) { this.action = action; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public boolean isOverridden() { return overridden; }
    public void setOverridden(boolean overridden) { this.overridden = overridden; }

    public User getOverriddenBy() { return overriddenBy; }
    public void setOverriddenBy(User overriddenBy) { this.overriddenBy = overriddenBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
