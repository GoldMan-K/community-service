package com.community.community.comment.dto;

import com.community.community.comment.domain.BoardComment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CommentDto {

    @Schema(description = "댓글 작성 요청")
    public record CreateRequest(
            Long parentCommentId,
            Long mentionMemberId,
            @NotBlank(message = "댓글 내용은 필수입니다.") String content
    ) {}

    @Schema(description = "댓글 수정 요청")
    public record UpdateRequest(
            @NotBlank(message = "댓글 내용은 필수입니다.") String content
    ) {}

    @Schema(description = "댓글 응답")
    public record Response(
            Long id,
            Long postId,
            Long writerMemberId,
            String writerNickname,
            Long parentCommentId,
            Long mentionMemberId,
            String mentionWriterNickname,
            String content,
            boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(BoardComment c) {
            return from(c, null, null);
        }

        public static Response from(BoardComment c,
                                    String writerNickname,
                                    String mentionWriterNickname) {
            return new Response(
                    c.getId(), c.getPostId(), c.getWriterMemberId(), writerNickname,
                    c.getParentCommentId(), c.getMentionMemberId(), mentionWriterNickname,
                    c.getContent(), c.isDeleted(),
                    c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
