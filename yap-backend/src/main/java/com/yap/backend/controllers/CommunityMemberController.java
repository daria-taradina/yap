package com.yap.backend.controllers;

import com.yap.backend.services.CommunityMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spaces")
public class CommunityMemberController {

    private final CommunityMemberService communityMemberService;

    public CommunityMemberController(CommunityMemberService communityMemberService) {
        this.communityMemberService = communityMemberService;
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<String> join(@PathVariable Integer communityId) {
        return ResponseEntity.ok(communityMemberService.joinCommunity(communityId));
    }

    @DeleteMapping("/{communityId}/leave")
    public ResponseEntity<String> leave(@PathVariable Integer communityId) {
        return ResponseEntity.ok(communityMemberService.leaveCommunity(communityId));
    }
}