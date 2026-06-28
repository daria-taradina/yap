package com.yap.backend.controllers;

import com.yap.backend.dtos.PageResponse;
import com.yap.backend.dtos.ReportSummary;
import com.yap.backend.services.ModQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mod")
@PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
public class ModQueueController {

    private final ModQueueService modQueueService;

    public ModQueueController(ModQueueService modQueueService) {
        this.modQueueService = modQueueService;
    }

    @GetMapping("/reports/count")
    public ResponseEntity<Map<String, Long>> getPendingReportCount() {
        long count = modQueueService.getPendingReportCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportSummary>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ReportSummary> reports = modQueueService.getPendingReports(page, size);
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/reports/{reportId}/dismiss")
    public ResponseEntity<Map<String, String>> dismissReport(@PathVariable Integer reportId) {
        modQueueService.dismissReport(reportId);
        return ResponseEntity.ok(Map.of("status", "DISMISSED"));
    }

    @PatchMapping("/reports/{reportId}/remove")
    public ResponseEntity<Map<String, String>> removeReport(@PathVariable Integer reportId) {
        modQueueService.removeReport(reportId);
        return ResponseEntity.ok(Map.of("status", "RESOLVED"));
    }

    @PatchMapping("/reports/{reportId}/ban")
    public ResponseEntity<Map<String, String>> banReport(@PathVariable Integer reportId) {
        modQueueService.banReport(reportId);
        return ResponseEntity.ok(Map.of("status", "RESOLVED"));
    }
}
