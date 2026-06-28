package com.yap.backend.repositories;

import com.yap.backend.entities.Report;
import com.yap.backend.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    boolean existsByReporter_UserIdAndPost_PostId(Integer userId, Integer postId);

    boolean existsByReporter_UserIdAndComment_CommentId(Integer userId, Integer commentId);

    @Query("SELECT COUNT(DISTINCT r.reporter.userId) FROM Report r WHERE r.post.postId = :postId")
    long countDistinctReportersByPostId(@Param("postId") Integer postId);

    @Query("SELECT COUNT(DISTINCT r.reporter.userId) FROM Report r WHERE r.comment.commentId = :commentId")
    long countDistinctReportersByCommentId(@Param("commentId") Integer commentId);

    long countByStatus(ReportStatus status);

    @Query("""
        SELECT r FROM Report r
        LEFT JOIN FETCH r.post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.community
        LEFT JOIN FETCH r.comment c LEFT JOIN FETCH c.author
        LEFT JOIN FETCH r.reporter
        WHERE r.status = :status
        ORDER BY r.createdAt DESC
    """)
    List<Report> findByStatus(@Param("status") ReportStatus status);
}
