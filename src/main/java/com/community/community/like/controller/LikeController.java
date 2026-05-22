package com.community.community.like.controller;

import com.community.community.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequestMapping("/api/boards/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "좋아요 추가 → likes_count +1 동기화")
    @PostMapping
    public ResponseEntity<Void> addLike(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId) {
        likeService.addLike(memberId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "좋아요 취소 → likes_count -1 동기화")
    @DeleteMapping
    public ResponseEntity<Void> removeLike(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId) {
        likeService.removeLike(memberId, postId);
        return ResponseEntity.noContent().build();
    }
}
