package com.yap.backend.services;

import com.yap.backend.dtos.PageResponse;
import com.yap.backend.dtos.ReportSummary;
import com.yap.backend.entities.Comment;
import com.yap.backend.entities.Post;
import com.yap.backend.entities.Report;
import com.yap.backend.entities.User;
import com.yap.backend.enums.ReportStatus;
import com.yap.backend.exceptions.ResourceNotFoundException;
import com.yap.backend.repositories.CommentRepository;
import com.yap.backend.repositories.PostRepository;
import com.yap.backend.repositories.ReportRepository;
import com.yap.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModQueueService extends BaseService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public ModQueueService(UserRepository userRepository,
                           ReportRepository reportRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           NotificationService notificationService) {
        super(userRepository);
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    public long getPendingReportCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    public PageResponse<ReportSummary> getPendingReports(int page, int size) {
        List<Report> allPending = reportRepository.findByStatus(ReportStatus.PENDING);
        long totalElements = allPending.size();

        int start = page * size;
        int end = Math.min(start + size, allPending.size());

        List<ReportSummary> content = (start >= allPending.size())
                ? List.of()
                : allPending.subList(start, end).stream()
                    .map(this::toSummary)
                    .toList();

        return new PageResponse<>(content, page, size, totalElements);
    }

    @Transactional
    public void dismissReport(Integer reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User moderator = getAuthenticatedUser();
        report.setStatus(ReportStatus.DISMISSED);
        report.setResolvedBy(moderator);
        reportRepository.save(report);
    }

    @Transactional
    public void removeReport(Integer reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User moderator = getAuthenticatedUser();
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(moderator);
        reportRepository.save(report);

        // Set target as removed
        if (report.getPost() != null) {
            Post post = report.getPost();
            post.setRemoved(true);
            postRepository.save(post);
            notificationService.notifyContentRemoved(post.getAuthor(), post, null);
        } else if (report.getComment() != null) {
            Comment comment = report.getComment();
            comment.setRemoved(true);
            commentRepository.save(comment);
            notificationService.notifyContentRemoved(comment.getAuthor(), null, comment);
        }
    }

    @Transactional
    public void banReport(Integer reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User moderator = getAuthenticatedUser();
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(moderator);
        reportRepository.save(report);

        // Set target as removed and ban the author
        if (report.getPost() != null) {
            Post post = report.getPost();
            post.setRemoved(true);
            postRepository.save(post);

            User author = post.getAuthor();
            author.setBanned(true);
            userRepository.save(author);

            notificationService.notifyContentRemoved(author, post, null);
        } else if (report.getComment() != null) {
            Comment comment = report.getComment();
            comment.setRemoved(true);
            commentRepository.save(comment);

            User author = comment.getAuthor();
            author.setBanned(true);
            userRepository.save(author);

            notificationService.notifyContentRemoved(author, null, comment);
        }
    }

    private ReportSummary toSummary(Report report) {
        ReportSummary summary = new ReportSummary();
        summary.setReportId(report.getReportId());
        summary.setReason(report.getReason().name());
        summary.setDetails(report.getDetails());
        summary.setReporterUsername(report.getReporter().getUsername());
        summary.setCreatedAt(report.getCreatedAt());

        if (report.getPost() != null) {
            Post post = report.getPost();
            ReportSummary.PostInfo postInfo = new ReportSummary.PostInfo();
            postInfo.setPostId(post.getPostId());
            postInfo.setTitle(post.getTitle());
            postInfo.setContentText(post.getContentText());
            postInfo.setAuthorUsername(post.getAuthor().getUsername());
            postInfo.setCommunityName(post.getCommunity() != null ? post.getCommunity().getName() : null);
            postInfo.setCreatedAt(post.getCreatedAt());
            summary.setPost(postInfo);
        }

        if (report.getComment() != null) {
            Comment comment = report.getComment();
            ReportSummary.CommentInfo commentInfo = new ReportSummary.CommentInfo();
            commentInfo.setCommentId(comment.getCommentId());
            commentInfo.setContentText(comment.getContentText());
            commentInfo.setAuthorUsername(comment.getAuthor().getUsername());
            commentInfo.setCreatedAt(comment.getCreatedAt());
            summary.setComment(commentInfo);
        }

        return summary;
    }
}
