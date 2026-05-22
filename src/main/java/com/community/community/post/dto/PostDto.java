package com.community.community.post.dto;

import com.community.community.post.domain.BoardPost;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    @Schema(description = "게시글 작성 요청")
    public record CreateRequest(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
            String content,

            String regionCode,
            String categoryCode,
            String subCategoryCode,
            List<String> tags,
            List<String> imageUrls
    ) {}

    @Schema(description = "게시글 수정 요청 (변경할 필드만 전송)")
    public record UpdateRequest(
            @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
            String title,

            String content,
            String regionCode,
            String categoryCode,
            String subCategoryCode,
            List<String> tags,
            List<String> imageUrls
    ) {}

    @Schema(description = "게시글 상세 응답")
    public record Response(
            Long id,
            Long writerMemberId,
            String regionCode,
            String categoryCode,
            String subCategoryCode,
            String title,
            String content,
            int viewsCount,
            int likesCount,
            int commentsCount,
            String pinnedYn,
            String status,
            List<String> tags,
            List<String> imageUrls,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(BoardPost post) {
            return new Response(
                    post.getId(),
                    post.getWriterMemberId(),
                    post.getRegionCode(),
                    post.getCategoryCode(),
                    post.getSubCategoryCode(),
                    post.getTitle(),
                    post.getContent(),
                    post.getViewsCount(),
                    post.getLikesCount(),
                    post.getCommentsCount(),
                    post.getPinnedYn(),
                    post.getStatus(),
                    post.getTags().stream().map(t -> t.getTagName()).toList(),
                    post.getImages().stream().map(i -> i.getImageUrl()).toList(),
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }
    }

    @Schema(description = "게시글 목록 응답")
    public record Summary(
            Long id,
            Long writerMemberId,
            String regionCode,
            String categoryCode,
            String title,
            int viewsCount,
            int likesCount,
            int commentsCount,
            String pinnedYn,
            String status,
            LocalDateTime createdAt
    ) {
        public static Summary from(BoardPost post) {
            return new Summary(
                    post.getId(),
                    post.getWriterMemberId(),
                    post.getRegionCode(),
                    post.getCategoryCode(),
                    post.getTitle(),
                    post.getViewsCount(),
                    post.getLikesCount(),
                    post.getCommentsCount(),
                    post.getPinnedYn(),
                    post.getStatus(),
                    post.getCreatedAt()
            );
        }
    }
}
