package com.yap.backend.controllers;

import com.yap.backend.dtos.PageResponse;
import com.yap.backend.dtos.PostSummary;
import com.yap.backend.services.BookmarkService;
import com.yap.backend.util.PaginationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleBookmark(@PathVariable Integer postId) {
        Map<String, Boolean> result = bookmarkService.toggleBookmark(postId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostSummary>> getBookmarks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        int normalizedPage = PaginationUtil.normalizePage(page);
        int clampedSize = PaginationUtil.clampSize(size);
        PageResponse<PostSummary> response = bookmarkService.getBookmarkedPosts(normalizedPage, clampedSize);
        return ResponseEntity.ok(response);
    }
}
