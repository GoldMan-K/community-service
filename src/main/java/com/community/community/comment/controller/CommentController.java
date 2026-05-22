package com.community.community.comment.controller;

import com.community.community.comment.dto.CommentDto;
import com.community.community.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 작성 (parent_comment_id로 대댓글)")
    @PostMapping("/api/boards/{postId}/comments")
    public ResponseEntity<CommentDto.Response> createComment(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(memberId, postId, req));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/api/boards/{postId}/comments")
    public ResponseEntity<List<CommentDto.Response>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @Operation(summary = "댓글 수정 (작성자만)")
    @PatchMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentDto.Response> updateComment(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.UpdateRequest req) {
        return ResponseEntity.ok(commentService.updateComment(memberId, commentId, req));
    }

    @Operation(summary = "댓글 삭제 — is_deleted=true (트리 보존)")
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long commentId) {
        commentService.deleteComment(memberId, commentId);
        return ResponseEntity.noContent().build();
    }
}
