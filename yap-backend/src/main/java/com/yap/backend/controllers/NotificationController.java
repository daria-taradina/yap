package com.yap.backend.controllers;

import com.yap.backend.dtos.NotificationSummary;
import com.yap.backend.services.NotificationService;
import com.yap.backend.util.PaginationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationSummary>> getMyNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<NotificationSummary> notifications = notificationService.getMyNotifications();
        int fromIndex = normalizedPage * clampedSize;
        if (fromIndex >= notifications.size()) {
            return ResponseEntity.ok(List.of());
        }
        int toIndex = Math.min(fromIndex + clampedSize, notifications.size());
        return ResponseEntity.ok(notifications.subList(fromIndex, toIndex));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount()));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }
}
