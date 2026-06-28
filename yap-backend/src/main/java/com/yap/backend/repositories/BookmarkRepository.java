package com.yap.backend.repositories;

import com.yap.backend.entities.Bookmark;
import com.yap.backend.keys.BookmarkId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {

    @Query("""
        SELECT b FROM Bookmark b
        JOIN FETCH b.post p
        LEFT JOIN FETCH p.author
        LEFT JOIN FETCH p.community
        LEFT JOIN FETCH p.tags pt
        LEFT JOIN FETCH pt.tag
        WHERE b.user.userId = :userId
          AND p.isDeleted = false
          AND p.isRemoved = false
          AND p.isFlagged = false
        ORDER BY b.createdAt DESC
    """)
    Page<Bookmark> findByUserIdExcludingHidden(@Param("userId") Integer userId, Pageable pageable);
}
