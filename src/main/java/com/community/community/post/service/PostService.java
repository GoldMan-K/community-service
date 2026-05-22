package com.community.community.post.service;

import com.community.community.global.exception.ForbiddenException;
import com.community.community.global.exception.PostNotFoundException;
import com.community.community.post.domain.BoardPost;
import com.community.community.post.domain.BoardPostImage;
import com.community.community.post.domain.BoardPostTag;
import com.community.community.post.dto.PostDto;
import com.community.community.post.event.PostEventPublisher;
import com.community.community.post.repository.BoardPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final BoardPostRepository postRepository;
    private final PostEventPublisher  eventPublisher;

    public PostService(BoardPostRepository postRepository, PostEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PostDto.Response createPost(Long memberId, PostDto.CreateRequest req) {
        BoardPost post = new BoardPost(
                memberId, req.title(), req.content(),
                req.regionCode(), req.categoryCode(), req.subCategoryCode());

        postRepository.save(post);
        syncTags(post, req.tags());
        syncImages(post, req.imageUrls());

        eventPublisher.publishPostCreated(post.getId(), memberId);
        return PostDto.Response.from(post);
    }

    public Page<PostDto.Summary> getPosts(String regionCode, String categoryCode,
                                          String keyword, Pageable pageable) {
        return postRepository.findAllActive(regionCode, categoryCode, keyword, pageable)
                .map(PostDto.Summary::from);
    }

    @Transactional
    public PostDto.Response getPost(Long postId) {
        BoardPost post = findActivePost(postId);
        post.incrementViews();
        return PostDto.Response.from(post);
    }

    @Transactional
    public PostDto.Response updatePost(Long memberId, Long postId, PostDto.UpdateRequest req) {
        BoardPost post = findActivePost(postId);
        checkOwner(post, memberId);

        // PATCH: null인 필드는 기존 값 유지
        String newTitle    = req.title()    != null ? req.title()    : post.getTitle();
        String newContent  = req.content()  != null ? req.content()  : post.getContent();
        String newRegion   = req.regionCode()   != null ? req.regionCode()   : post.getRegionCode();
        String newCategory = req.categoryCode() != null ? req.categoryCode() : post.getCategoryCode();
        String newSubCat   = req.subCategoryCode() != null ? req.subCategoryCode() : post.getSubCategoryCode();

        post.update(newTitle, newContent, newRegion, newCategory, newSubCat);

        if (req.tags() != null) {
            post.getTags().clear();
            syncTags(post, req.tags());
        }
        if (req.imageUrls() != null) {
            post.getImages().clear();
            syncImages(post, req.imageUrls());
        }

        return PostDto.Response.from(post);
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        BoardPost post = findActivePost(postId);
        checkOwner(post, memberId);
        post.softDelete();
    }

    @Transactional
    public PostDto.Response restorePost(Long postId) {
        BoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        post.restore();
        return PostDto.Response.from(post);
    }

    public BoardPost findActivePost(Long postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    // ── private ───────────────────────────────────────────────────────────────

    private void checkOwner(BoardPost post, Long memberId) {
        if (!post.getWriterMemberId().equals(memberId)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private void syncTags(BoardPost post, List<String> tags) {
        if (tags == null) return;
        tags.forEach(tag -> post.getTags().add(new BoardPostTag(post, tag)));
    }

    private void syncImages(BoardPost post, List<String> imageUrls) {
        if (imageUrls == null) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            post.getImages().add(new BoardPostImage(post, imageUrls.get(i), i));
        }
    }
}
