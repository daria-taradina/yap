package com.yap.backend.repositories;

import com.yap.backend.entities.Report;
import com.yap.backend.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    @Query("SELECT r FROM Report r " +
           "LEFT JOIN FETCH r.post p LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH r.comment c LEFT JOIN FETCH c.author " +
           "LEFT JOIN FETCH r.reporter " +
           "WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByStatus(ReportStatus status);

    boolean existsByReporter_UserIdAndPost_PostId(Integer userId, Integer postId);

    boolean existsByReporter_UserIdAndComment_CommentId(Integer userId, Integer commentId);
}
