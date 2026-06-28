package com.yap.backend.repositories;

import com.yap.backend.keys.PostLikeId;
import com.yap.backend.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

	long countByPost_PostIdAndCreatedAtAfter(Integer postId, LocalDateTime since);

}