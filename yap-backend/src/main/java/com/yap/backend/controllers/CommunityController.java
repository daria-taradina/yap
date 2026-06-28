package com.yap.backend.controllers;

import com.yap.backend.dtos.*;
import com.yap.backend.services.CommunityService;
import com.yap.backend.util.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createSpace(
            @Valid @RequestBody CreateCommunity dto) {
        return ResponseEntity.status(201).body(communityService.createCommunity(dto));
    }

    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllSpaces(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<CommunityResponse> communities = communityService.getAllCommunities();
        int fromIndex = normalizedPage * clampedSize;
        if (fromIndex >= communities.size()) {
            return ResponseEntity.ok(List.of());
        }
        int toIndex = Math.min(fromIndex + clampedSize, communities.size());
        return ResponseEntity.ok(communities.subList(fromIndex, toIndex));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CommunityResponse>> getByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int clampedSize = PaginationUtil.clampSize(size);
        int normalizedPage = PaginationUtil.normalizePage(page);
        List<CommunityResponse> communities = communityService.getCommunitiesByCategory(category);
        int fromIndex = normalizedPage * clampedSize;
        if (fromIndex >= communities.size()) {
            return ResponseEntity.ok(List.of());
        }
        int toIndex = Math.min(fromIndex + clampedSize, communities.size());
        return ResponseEntity.ok(communities.subList(fromIndex, toIndex));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CommunityResponse>> getMySpaces() {
        return ResponseEntity.ok(communityService.getMyCommunities());
    }

    @GetMapping("/{name}")
    public ResponseEntity<CommunityResponse> getSpaceByName(@PathVariable String name) {
        return ResponseEntity.ok(communityService.getCommunityByName(name));
    }

    @GetMapping("/trending-tags")
    public ResponseEntity<List<String>> getTrendingTags() {
        return ResponseEntity.ok(communityService.getTrendingTags());
    }
}
