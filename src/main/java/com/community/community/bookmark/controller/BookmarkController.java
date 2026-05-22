package com.community.community.bookmark.controller;

import com.community.community.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookmark", description = "북마크 API")
@RestController
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @Operation(summary = "북마크 추가")
    @PostMapping("/api/boards/{postId}/bookmarks")
    public ResponseEntity<Void> addBookmark(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId) {
        bookmarkService.addBookmark(memberId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "북마크 취소")
    @DeleteMapping("/api/boards/{postId}/bookmarks")
    public ResponseEntity<Void> removeBookmark(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId) {
        bookmarkService.removeBookmark(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 북마크 게시글 ID 목록")
    @GetMapping("/api/bookmarks")
    public ResponseEntity<Page<Long>> getMyBookmarks(
            @RequestHeader("X-Member-Id") Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getMyBookmarkedPostIds(memberId, pageable));
    }
}
