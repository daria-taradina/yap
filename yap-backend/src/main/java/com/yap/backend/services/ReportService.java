package com.yap.backend.services;

import com.yap.backend.dtos.ReportRequest;
import com.yap.backend.entities.Comment;
import com.yap.backend.entities.Post;
import com.yap.backend.entities.Report;
import com.yap.backend.entities.User;
import com.yap.backend.enums.ReportStatus;
import com.yap.backend.exceptions.InvalidInputException;
import com.yap.backend.exceptions.ResourceAlreadyExistsException;
import com.yap.backend.exceptions.ResourceNotFoundException;
import com.yap.backend.repositories.CommentRepository;
import com.yap.backend.repositories.PostRepository;
import com.yap.backend.repositories.ReportRepository;
import com.yap.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService extends BaseService {

    private static final int AUTO_FLAG_THRESHOLD = 3;
    private static final int AUTO_REMOVE_THRESHOLD = 5;

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public ReportService(UserRepository userRepository,
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

    @Transactional
    public Report createReport(ReportRequest request) {
        User reporter = getAuthenticatedUser();

        // Validate that exactly one target is specified
        if (request.getPostId() == null && request.getCommentId() == null) {
            throw new InvalidInputException("Either postId or commentId must be provided");
        }
        if (request.getPostId() != null && request.getCommentId() != null) {
            throw new InvalidInputException("Cannot report both a post and a comment in the same request");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(request.getReason());
        report.setDetails(request.getDetails());
        report.setStatus(ReportStatus.PENDING);

        if (request.getPostId() != null) {
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

            // Duplicate check
            if (reportRepository.existsByReporter_UserIdAndPost_PostId(
                    reporter.getUserId(), post.getPostId())) {
                throw new ResourceAlreadyExistsException("You have already reported this post");
            }

            report.setPost(post);
            Report saved = reportRepository.save(report);

            // Notify all ADMIN/MOD users about the new report
            notificationService.notifyNewReport(reporter, post, null);

            // Auto-flag: if 3+ distinct reporters have reported this post
            long distinctReporters = reportRepository.countDistinctReportersByPostId(post.getPostId());
            if (distinctReporters >= AUTO_FLAG_THRESHOLD && !post.isFlagged()) {
                post.setFlagged(true);
                postRepository.save(post);
            }

            // Auto-remove: if 5+ distinct reporters have reported this post
            if (distinctReporters >= AUTO_REMOVE_THRESHOLD && !post.isRemoved()) {
                post.setRemoved(true);
                postRepository.save(post);
                notificationService.notifyPostRemoved(post.getAuthor(), post);
            }

            return saved;
        } else {
            Comment comment = commentRepository.findById(request.getCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

            // Duplicate check
            if (reportRepository.existsByReporter_UserIdAndComment_CommentId(
                    reporter.getUserId(), comment.getCommentId())) {
                throw new ResourceAlreadyExistsException("You have already reported this comment");
            }

            report.setComment(comment);
            Report saved = reportRepository.save(report);

            // Notify all ADMIN/MOD users about the new report
            notificationService.notifyNewReport(reporter, null, comment);

            // Auto-flag: if 3+ distinct reporters have reported this comment
            long distinctReporters = reportRepository.countDistinctReportersByCommentId(comment.getCommentId());
            if (distinctReporters >= AUTO_FLAG_THRESHOLD && !comment.isFlagged()) {
                comment.setFlagged(true);
                commentRepository.save(comment);
            }

            // Auto-remove: if 5+ distinct reporters have reported this comment
            if (distinctReporters >= AUTO_REMOVE_THRESHOLD && !comment.isRemoved()) {
                comment.setRemoved(true);
                commentRepository.save(comment);
                notificationService.notifyCommentRemoved(comment.getAuthor(), comment);
            }

            return saved;
        }
    }
}
