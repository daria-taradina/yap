package com.yap.backend.repositories;

import com.yap.backend.keys.PostLikeId;
import com.yap.backend.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
	
}