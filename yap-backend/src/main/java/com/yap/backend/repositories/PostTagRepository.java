package com.yap.backend.repositories;

import com.yap.backend.keys.PostTagId;
import com.yap.backend.entities.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
    void deleteByPost_PostId(Integer postId);
}