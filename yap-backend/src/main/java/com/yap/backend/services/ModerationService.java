package com.yap.backend.services;

import com.yap.backend.dtos.ModerationQueueItem;
import com.yap.backend.dtos.ReportCreate;
import com.yap.backend.entities.*;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.enums.ReportStatus;
import com.yap.backend.exceptions.InvalidInputException;
import com.yap.backend.exceptions.ResourceNotFoundException;
import com.yap.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModerationService extends BaseService {

    private final ReportRepository reportRepository;
    private final ModerationDecisionRepository decisionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ModerationService(ReportRepository reportRepository,
                             ModerationDecisionRepository decisionRepository,
                             PostRepository postRepository,
                             CommentRepository commentRepository,
                             UserRepository userRepository) {
        super(userRepository);
        this.reportRepository = reportRepository;
        this.decisionRepository = decisionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void createReport(ReportCreate dto) {
        User reporter = getAuthenticatedUser();

        if (dto.getPostId() == null && dto.getCommentId() == null)
            throw new InvalidInputException("Must report either a post or a comment");

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(dto.getReason());
        report.setDetails(dto.getDetails());

        if (dto.getPostId() != null) {
            if (reportRepository.existsByReporter_UserIdAndPost_PostId(reporter.getUserId(), dto.getPostId()))
                throw new InvalidInputException("You already reported this post");
            Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            report.setPost(post);
        } else {
            if (reportRepository.existsByReporter_UserIdAndComment_CommentId(reporter.getUserId(), dto.getCommentId()))
                throw new InvalidInputException("You already reported this comment");
            Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
            report.setComment(comment);
        }

        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ModerationQueueItem> getModerationQueue() {
        // Get all decisions that resulted in REVIEW or HIDE and haven't been overridden
        List<ModerationDecision> decisions = decisionRepository
            .findByActionInAndOverriddenFalse(List.of(ModerationAction.REVIEW, ModerationAction.HIDE));

        return decisions.stream().map(d -> {
            String contentText = null;
            String authorUsername = null;
            Integer postId = null;
            Integer commentId = null;

            if (d.getPost() != null) {
                postId = d.getPost().getPostId();
                contentText = d.getPost().getTitle() + ": " + d.getPost().getContentText();
                authorUsername = d.getPost().getAuthor().getUsername();
            } else if (d.getComment() != null) {
                commentId = d.getComment().getCommentId();
                contentText = d.getComment().getContentText();
                authorUsername = d.getComment().getAuthor().getUsername();
            }

            return new ModerationQueueItem(
                d.getDecisionId(), postId, commentId, contentText,
                authorUsername, d.getCategory(), d.getRisk(), d.getAction(),
                d.getReasoning(), d.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModerationQueueItem> getAllDecisions() {
        return decisionRepository.findAll().stream().map(d -> {
            String contentText = null;
            String authorUsername = null;
            Integer postId = null;
            Integer commentId = null;

            if (d.getPost() != null) {
                postId = d.getPost().getPostId();
                contentText = d.getPost().getTitle() + ": " + d.getPost().getContentText();
                authorUsername = d.getPost().getAuthor().getUsername();
            } else if (d.getComment() != null) {
                commentId = d.getComment().getCommentId();
                contentText = d.getComment().getContentText();
                authorUsername = d.getComment().getAuthor().getUsername();
            }

            return new ModerationQueueItem(
                d.getDecisionId(), postId, commentId, contentText,
                authorUsername, d.getCategory(), d.getRisk(), d.getAction(),
                d.getReasoning(), d.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void overrideDecision(Integer decisionId, ModerationAction newAction) {
        User mod = getAuthenticatedUser();
        ModerationDecision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        decision.setOverridden(true);
        decision.setOverriddenBy(mod);

        // Apply the mod's override
        if (newAction == ModerationAction.ALLOW) {
            // Mod approved — unflag the content
            if (decision.getPost() != null) {
                decision.getPost().setFlagged(false);
                decision.getPost().setRemoved(false);
                postRepository.save(decision.getPost());
            } else if (decision.getComment() != null) {
                decision.getComment().setFlagged(false);
                decision.getComment().setRemoved(false);
                commentRepository.save(decision.getComment());
            }
        } else if (newAction == ModerationAction.HIDE) {
            // Mod confirmed removal
            if (decision.getPost() != null) {
                decision.getPost().setRemoved(true);
                postRepository.save(decision.getPost());
            } else if (decision.getComment() != null) {
                decision.getComment().setRemoved(true);
                commentRepository.save(decision.getComment());
            }
        }

        decisionRepository.save(decision);
    }

    @Transactional(readOnly = true)
    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    @Transactional
    public void resolveReport(Integer reportId, ReportStatus resolution) {
        User mod = getAuthenticatedUser();
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        report.setStatus(resolution);
        report.setResolvedBy(mod);
        reportRepository.save(report);
    }
}
