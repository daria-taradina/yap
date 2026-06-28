package com.yap.backend.services;

import com.yap.backend.dtos.NotificationSummary;
import com.yap.backend.entities.*;
import com.yap.backend.enums.NotificationType;
import com.yap.backend.enums.UserRole;
import com.yap.backend.repositories.NotificationRepository;
import com.yap.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService extends BaseService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        super(userRepository);
        this.notificationRepository = notificationRepository;
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<NotificationSummary> getMyNotifications() {
        User currentUser = getAuthenticatedUser();
        return notificationRepository
            .findByRecipient_UserIdOrderByCreatedAtDesc(currentUser.getUserId())
            .stream()
            .map(this::mapToSummary)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = getAuthenticatedUser();
        return notificationRepository
            .countByRecipient_UserIdAndIsReadFalse(currentUser.getUserId());
    }

    @Transactional
    public void markAllRead() {
        User currentUser = getAuthenticatedUser();
        notificationRepository.markAllReadForUser(currentUser.getUserId());
    }

    // ---------------------------------------------------------------
    // CREATE — called internally from other services
    // All methods are @Async-safe (no return value, no checked exceptions)
    // ---------------------------------------------------------------

    @Transactional
    public void notifyPostLiked(User actor, Post post) {
        if (isSelf(actor, post.getAuthor())) return;
        create(actor, post.getAuthor(), NotificationType.POST_LIKED, post, null);
    }

    @Transactional
    public void notifyPostCommented(User actor, Post post) {
        if (isSelf(actor, post.getAuthor())) return;
        create(actor, post.getAuthor(), NotificationType.POST_COMMENTED, post, null);
    }

    @Transactional
    public void notifyCommentLiked(User actor, Comment comment) {
        if (isSelf(actor, comment.getAuthor())) return;
        create(actor, comment.getAuthor(), NotificationType.COMMENT_LIKED,
               comment.getPost(), comment);
    }

    @Transactional
    public void notifyCommentReplied(User actor, Comment parentComment) {
        if (isSelf(actor, parentComment.getAuthor())) return;
        create(actor, parentComment.getAuthor(), NotificationType.COMMENT_REPLIED,
               parentComment.getPost(), parentComment);
    }

    @Transactional
    public void notifyUserFollowed(User actor, User target) {
        if (isSelf(actor, target)) return;
        create(actor, target, NotificationType.USER_FOLLOWED, null, null);
    }

    @Transactional
    public void notifyPostRemoved(User recipient, Post post) {
        create(null, recipient, NotificationType.POST_REMOVED, post, null);
    }

    @Transactional
    public void notifyCommentRemoved(User recipient, Comment comment) {
        create(null, recipient, NotificationType.COMMENT_REMOVED,
               comment.getPost(), comment);
    }

    @Transactional
    public void notifyNewReport(User actor, Post post, Comment comment) {
        List<User> modsAndAdmins = new ArrayList<>();
        modsAndAdmins.addAll(userRepository.findByRole(UserRole.ADMIN));
        modsAndAdmins.addAll(userRepository.findByRole(UserRole.MOD));
        for (User recipient : modsAndAdmins) {
            create(actor, recipient, NotificationType.NEW_REPORT, post, comment);
        }
    }

    @Transactional
    public void notifyContentRemoved(User recipient, Post post, Comment comment) {
        create(null, recipient, NotificationType.CONTENT_REMOVED, post, comment);
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------

    private void create(User actor, User recipient, NotificationType type,
                        Post post, Comment comment) {
        Notification n = new Notification();
        n.setActor(actor);
        n.setRecipient(recipient);
        n.setType(type);
        n.setPost(post);
        n.setComment(comment);
        notificationRepository.save(n);
    }

    private boolean isSelf(User a, User b) {
        return a != null && b != null && a.getUserId().equals(b.getUserId());
    }

    private NotificationSummary mapToSummary(Notification n) {
        return new NotificationSummary(
            n.getNotificationId(),
            n.getType(),
            n.getActor() != null ? n.getActor().getUsername() : null,
            n.getActor() != null ? n.getActor().getAvatarUrl() : null,
            n.getPost() != null ? n.getPost().getPostId() : null,
            n.getPost() != null ? n.getPost().getTitle() : null,
            n.getComment() != null ? n.getComment().getCommentId() : null,
            n.isRead(),
            n.getCreatedAt()
        );
    }
}