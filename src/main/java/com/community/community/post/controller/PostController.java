package com.community.community.post.controller;

import com.community.community.post.dto.PostDto;
import com.community.community.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Board", description = "게시글 API")
@RestController
@RequestMapping("/api/boards")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 목록 조회 (지역·카테고리·키워드 필터)")
    @GetMapping
    public ResponseEntity<Page<PostDto.Summary>> getPosts(
            @Parameter(description = "Gateway가 주입하는 회원 ID (비로그인 시 미전달)")
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                postService.getPosts(regionCode, categoryCode, keyword, pageable, memberId));
    }

    @Operation(summary = "게시글 상세 조회 + 조회수 증가 (incrementView=false 시 조회수 미증가)")
    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> getPost(
            @Parameter(description = "Gateway가 주입하는 회원 ID (비로그인 시 미전달)")
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @PathVariable Long id,
            @Parameter(description = "false 이면 조회수를 증가시키지 않음 (편집/재조회 용도). 기본 true")
            @RequestParam(value = "incrementView", required = false, defaultValue = "true") boolean incrementView) {
        return ResponseEntity.ok(postService.getPost(id, memberId, incrementView));
    }

    @Operation(summary = "게시글 작성")
    @PostMapping
    public ResponseEntity<PostDto.Response> createPost(
            @Parameter(description = "Gateway가 주입하는 회원 ID", required = true)
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody PostDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(memberId, req));
    }

    @Operation(summary = "게시글 수정 (작성자·관리자만, 변경할 필드만 전송)")
    @PatchMapping("/{id}")
    public ResponseEntity<PostDto.Response> updatePost(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long id,
            @RequestBody PostDto.UpdateRequest req) {
        return ResponseEntity.ok(postService.updatePost(memberId, id, req));
    }

    @Operation(summary = "게시글 삭제 — status=DELETED (소프트)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long id) {
        postService.deletePost(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 휴지통 목록 조회 (삭제된 글만)")
    @GetMapping("/me/trash")
    public ResponseEntity<Page<PostDto.TrashSummary>> getMyTrashPosts(
            @RequestHeader("X-Member-Id") Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getMyTrashPosts(memberId, pageable));
    }

    @Operation(summary = "삭제 게시글 복구 (작성자 또는 관리자)")
    @PostMapping("/{id}/restore")
    public ResponseEntity<PostDto.Response> restorePost(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestHeader(value = "X-Member-Role", required = false) String memberRole,
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.restorePost(id, memberId, isAdmin(memberRole)));
    }

    @Operation(summary = "삭제 게시글 영구삭제 (작성자 또는 관리자, del_yn=Y 플래그)")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeletePost(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestHeader(value = "X-Member-Role", required = false) String memberRole,
            @PathVariable Long id) {
        postService.hardDeletePost(id, memberId, isAdmin(memberRole));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(String memberRole) {
        return memberRole != null && "ADMIN".equalsIgnoreCase(memberRole.trim());
    }
}
