package com.yap.backend.controllers;

import com.yap.backend.dtos.ModerationQueueItem;
import com.yap.backend.dtos.ModerationResult;
import com.yap.backend.dtos.ReportCreate;
import com.yap.backend.enums.ModerationAction;
import com.yap.backend.services.ModerationAIService;
import com.yap.backend.services.ModerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    private final ModerationService moderationService;
    private final ModerationAIService moderationAIService;

    public ModerationController(ModerationService moderationService,
                                ModerationAIService moderationAIService) {
        this.moderationService = moderationService;
        this.moderationAIService = moderationAIService;
    }

    // --- User-facing: report content ---

    @PostMapping("/report")
    public ResponseEntity<Void> reportContent(@RequestBody @Valid ReportCreate dto) {
        moderationService.createReport(dto);
        return ResponseEntity.status(201).build();
    }

    // --- Mod-facing: review queue ---

    @GetMapping("/queue")
    public ResponseEntity<List<ModerationQueueItem>> getModerationQueue() {
        return ResponseEntity.ok(moderationService.getModerationQueue());
    }

    @GetMapping("/decisions")
    public ResponseEntity<List<ModerationQueueItem>> getAllDecisions() {
        return ResponseEntity.ok(moderationService.getAllDecisions());
    }

    @PostMapping("/decisions/{decisionId}/override")
    public ResponseEntity<Void> overrideDecision(
            @PathVariable Integer decisionId,
            @RequestBody Map<String, String> body) {
        ModerationAction newAction = ModerationAction.valueOf(body.get("action"));
        moderationService.overrideDecision(decisionId, newAction);
        return ResponseEntity.noContent().build();
    }

    // --- Demo endpoint: test classification without creating a post ---

    @PostMapping("/classify")
    public ResponseEntity<ModerationResult> classifyContent(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // Use a temporary approach — call classify directly without saving
        ModerationResult result = moderationAIService.classifyOnly(content);
        return ResponseEntity.ok(result);
    }
}
