package com.community.community.post.dto;

import com.community.community.post.domain.BoardPost;
import com.community.community.post.domain.BoardPostImage;
import com.community.community.post.domain.BoardPostTag;
import com.fasterxml.jackson.annotation.JsonProperty;
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
            String writerNickname,
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
            LocalDateTime updatedAt,
            @Schema(description = "로그인 사용자가 좋아요 했는지 여부")
            boolean likedByMe,
            @Schema(description = "로그인 사용자가 북마크 했는지 여부")
            boolean bookmarkedByMe
    ) {
        /** 프론트 호환용 alias: isLiked */
        @JsonProperty("isLiked")
        public boolean isLiked() { return likedByMe; }

        /** 프론트 호환용 alias: isBookmarked */
        @JsonProperty("isBookmarked")
        public boolean isBookmarked() { return bookmarkedByMe; }

        public static Response from(BoardPost post) {
            return from(post, false, false);
        }

        public static Response from(BoardPost post, boolean likedByMe, boolean bookmarkedByMe) {
            return from(post, likedByMe, bookmarkedByMe, null);
        }

        public static Response from(BoardPost post,
                                    boolean likedByMe,
                                    boolean bookmarkedByMe,
                                    String writerNickname) {
            return new Response(
                    post.getId(),
                    post.getWriterMemberId(),
                    writerNickname,
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
                    post.getTags().stream().map(BoardPostTag::getTagName).toList(),
                    post.getImages().stream().map(BoardPostImage::getImageUrl).toList(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    likedByMe,
                    bookmarkedByMe
            );
        }
    }

    @Schema(description = "게시글 목록 응답")
    public record Summary(
            Long id,
            Long writerMemberId,
            String writerNickname,
            String regionCode,
            String categoryCode,
            String title,
            int viewsCount,
            int likesCount,
            int commentsCount,
            String pinnedYn,
            String status,
            LocalDateTime createdAt,
            @Schema(description = "로그인 사용자가 좋아요 했는지 여부")
            boolean likedByMe,
            @Schema(description = "로그인 사용자가 북마크 했는지 여부")
            boolean bookmarkedByMe,
            @Schema(description = "대표 이미지 URL (첫 이미지). 없으면 null")
            String thumbnailUrl,
            @Schema(description = "게시글에 첨부된 이미지 URL 배열. 없으면 빈 배열")
            List<String> imageUrls,
            @Schema(description = "이미지 개수 (imageUrls.size())")
            int imageCount,
            @Schema(description = "게시글 태그 배열. 없으면 빈 배열")
            List<String> tags
    ) {
        /** 프론트 호환용 alias: isLiked */
        @JsonProperty("isLiked")
        public boolean isLiked() { return likedByMe; }

        /** 프론트 호환용 alias: isBookmarked */
        @JsonProperty("isBookmarked")
        public boolean isBookmarked() { return bookmarkedByMe; }

        public static Summary from(BoardPost post) {
            return from(post, false, false, List.of(), List.of());
        }

        public static Summary from(BoardPost post, boolean likedByMe, boolean bookmarkedByMe) {
            return from(post, likedByMe, bookmarkedByMe, List.of(), List.of());
        }

        public static Summary from(BoardPost post,
                                    boolean likedByMe,
                                    boolean bookmarkedByMe,
                                    List<String> imageUrls,
                                    List<String> tags) {
            return from(post, likedByMe, bookmarkedByMe, null, imageUrls, tags);
        }

        public static Summary from(BoardPost post,
                                   boolean likedByMe,
                                   boolean bookmarkedByMe,
                                   String writerNickname,
                                   List<String> imageUrls,
                                   List<String> tags) {
            List<String> safeImages = imageUrls != null ? imageUrls : List.of();
            List<String> safeTags   = tags != null ? tags : List.of();
            String thumb = safeImages.isEmpty() ? null : safeImages.get(0);
            return new Summary(
                    post.getId(),
                    post.getWriterMemberId(),
                    writerNickname,
                    post.getRegionCode(),
                    post.getCategoryCode(),
                    post.getTitle(),
                    post.getViewsCount(),
                    post.getLikesCount(),
                    post.getCommentsCount(),
                    post.getPinnedYn(),
                    post.getStatus(),
                    post.getCreatedAt(),
                    likedByMe,
                    bookmarkedByMe,
                    thumb,
                    safeImages,
                    safeImages.size(),
                    safeTags
            );
        }
    }

    @Schema(description = "내 휴지통 게시글 목록 응답")
    public record TrashSummary(
            Long id,
            String title,
            Long writerMemberId,
            String writerNickname,
            String categoryCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            @Schema(description = "삭제 시각")
            LocalDateTime deletedAt,
            @Schema(description = "게시글 상태(DELETED)")
            String status,
            @Schema(description = "휴지통 보관일(일)")
            int retentionDays,
            @Schema(description = "자동 영구삭제 예정 시각")
            LocalDateTime purgeAt
    ) {
        public static TrashSummary from(BoardPost post, int retentionDays) {
            return from(post, retentionDays, null);
        }

        public static TrashSummary from(BoardPost post, int retentionDays, String writerNickname) {
            LocalDateTime deletedAt = post.getDeletedAt();
            LocalDateTime purgeAt = deletedAt != null ? deletedAt.plusDays(retentionDays) : null;
            return new TrashSummary(
                    post.getId(),
                    post.getTitle(),
                    post.getWriterMemberId(),
                    writerNickname,
                    post.getCategoryCode(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    deletedAt,
                    post.getStatus(),
                    retentionDays,
                    purgeAt
            );
        }
    }
}
