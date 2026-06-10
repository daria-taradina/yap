package com.yap.backend.repositories;

import com.yap.backend.entities.ModerationDecision;
import com.yap.backend.enums.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ModerationDecisionRepository extends JpaRepository<ModerationDecision, Integer> {

    @Query("SELECT d FROM ModerationDecision d " +
           "LEFT JOIN FETCH d.post p LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH d.comment c LEFT JOIN FETCH c.author " +
           "WHERE d.action IN :actions AND d.overridden = false " +
           "ORDER BY d.createdAt DESC")
    List<ModerationDecision> findByActionInAndOverriddenFalse(List<ModerationAction> actions);

    List<ModerationDecision> findByPost_PostId(Integer postId);

    List<ModerationDecision> findByComment_CommentId(Integer commentId);
}
