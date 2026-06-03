package com.yap.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yap.backend.keys.CommentLikeId;
import com.yap.backend.entities.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
	boolean existsByComment_CommentIdAndUser_UserId(Integer commentId, Integer userId);
}
