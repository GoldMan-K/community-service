package com.community.community.post.service;

import com.community.community.bookmark.repository.BoardBookmarkRepository;
import com.community.community.global.exception.ForbiddenException;
import com.community.community.global.exception.PostNotFoundException;
import com.community.community.like.repository.BoardLikeRepository;
import com.community.community.member.service.MemberNicknameService;
import com.community.community.post.domain.BoardPost;
import com.community.community.post.domain.BoardPostImage;
import com.community.community.post.domain.BoardPostTag;
import com.community.community.post.dto.PostDto;
import com.community.community.post.event.PostEventPublisher;
import com.community.community.post.repository.BoardPostImageRepository;
import com.community.community.post.repository.BoardPostRepository;
import com.community.community.post.repository.BoardPostTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    public static final int TRASH_RETENTION_DAYS = 30;

    private final BoardPostRepository postRepository;
    private final PostEventPublisher  eventPublisher;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardBookmarkRepository boardBookmarkRepository;
    private final BoardPostImageRepository boardPostImageRepository;
    private final BoardPostTagRepository boardPostTagRepository;
    private final MemberNicknameService memberNicknameService;

    public PostService(BoardPostRepository postRepository,
                       PostEventPublisher eventPublisher,
                       BoardLikeRepository boardLikeRepository,
                       BoardBookmarkRepository boardBookmarkRepository,
                       BoardPostImageRepository boardPostImageRepository,
                       BoardPostTagRepository boardPostTagRepository,
                       MemberNicknameService memberNicknameService) {
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.boardLikeRepository = boardLikeRepository;
        this.boardBookmarkRepository = boardBookmarkRepository;
        this.boardPostImageRepository = boardPostImageRepository;
        this.boardPostTagRepository = boardPostTagRepository;
        this.memberNicknameService = memberNicknameService;
    }

    @Transactional
    public PostDto.Response createPost(Long memberId, PostDto.CreateRequest req) {
        String pinnedYn = resolveCreatePinnedYn(req.pinnedYn(), req.pinned());

        BoardPost post = new BoardPost(
                memberId, req.title(), req.content(),
                req.regionCode(), req.categoryCode(), req.subCategoryCode(), pinnedYn);

        postRepository.save(post);
        syncTags(post, req.tags());
        syncImages(post, req.imageUrls());

        eventPublisher.publishPostCreated(post.getId(), memberId);
        String writerNickname = fetchNickname(post.getWriterMemberId());
        return PostDto.Response.from(post, false, false, writerNickname);
    }

    public Page<PostDto.Summary> getPosts(String regionCode, String categoryCode,
                                          String keyword, Pageable pageable) {
        return getPosts(regionCode, categoryCode, keyword, pageable, null);
    }

    public Page<PostDto.Summary> getPosts(String regionCode, String categoryCode,
                                          String keyword, Pageable pageable, Long memberId) {
        Page<BoardPost> page = postRepository.findAllActive(regionCode, categoryCode, keyword, pageable);

        List<Long> postIds = page.getContent().stream().map(BoardPost::getId).toList();
        Set<Long> likedIds = fetchLikedIds(memberId, postIds);
        Set<Long> bookmarkedIds = fetchBookmarkedIds(memberId, postIds);
        Map<Long, List<String>> imagesByPost = fetchImagesByPost(postIds);
        Map<Long, List<String>> tagsByPost = fetchTagsByPost(postIds);
        Map<Long, String> nicknamesByMember = fetchNicknamesByMember(page.getContent().stream()
                .map(BoardPost::getWriterMemberId)
                .toList());

        return page.map(p -> PostDto.Summary.from(
                p,
                likedIds.contains(p.getId()),
                bookmarkedIds.contains(p.getId()),
                nicknamesByMember.get(p.getWriterMemberId()),
                imagesByPost.getOrDefault(p.getId(), List.of()),
                tagsByPost.getOrDefault(p.getId(), List.of())
        ));
    }

    @Transactional
    public PostDto.Response getPost(Long postId) {
        return getPost(postId, null, true);
    }

    @Transactional
    public PostDto.Response getPost(Long postId, Long memberId) {
        return getPost(postId, memberId, true);
    }

    @Transactional
    public PostDto.Response getPost(Long postId, Long memberId, boolean incrementView) {
        BoardPost post = findActivePost(postId);
        if (incrementView) {
            post.incrementViews();
        }

        boolean liked = false;
        boolean bookmarked = false;
        if (memberId != null) {
            liked = boardLikeRepository.existsByPostIdAndMemberId(postId, memberId);
            bookmarked = boardBookmarkRepository.findByPostIdAndMemberId(postId, memberId).isPresent();
        }
        String writerNickname = fetchNickname(post.getWriterMemberId());
        return PostDto.Response.from(post, liked, bookmarked, writerNickname);
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
        String newPinnedYn = resolveUpdatePinnedYn(req.pinnedYn(), req.pinned(), post.getPinnedYn());

        post.update(newTitle, newContent, newRegion, newCategory, newSubCat, newPinnedYn);

        boolean hasTagReplace = req.tags() != null;
        boolean hasImageReplace = req.imageUrls() != null;

        if (hasTagReplace) {
            post.getTags().clear();
        }
        if (hasImageReplace) {
            post.getImages().clear();
        }

        // orphanRemoval delete를 먼저 DB에 반영해 unique 충돌(uq_post_image_order/uq_post_tag) 방지.
        if (hasTagReplace || hasImageReplace) {
            postRepository.flush();
        }

        if (hasTagReplace) {
            syncTags(post, req.tags());
        }
        if (hasImageReplace) {
            syncImages(post, req.imageUrls());
        }

        boolean liked = boardLikeRepository.existsByPostIdAndMemberId(postId, memberId);
        boolean bookmarked = boardBookmarkRepository.findByPostIdAndMemberId(postId, memberId).isPresent();
        String writerNickname = fetchNickname(post.getWriterMemberId());
        return PostDto.Response.from(post, liked, bookmarked, writerNickname);
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        BoardPost post = findActivePost(postId);
        checkOwner(post, memberId);
        post.softDelete();
    }

    @Transactional
    public PostDto.Response restorePost(Long postId) {
        // 레거시 호환: 기존 관리자 복구 엔드포인트는 권한 헤더 없이도 동작.
        BoardPost post = postRepository.findDeletedById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        post.restore();
        String writerNickname = fetchNickname(post.getWriterMemberId());
        return PostDto.Response.from(post, false, false, writerNickname);
    }

    public Page<PostDto.TrashSummary> getMyTrashPosts(Long memberId, Pageable pageable) {
        Page<BoardPost> page = postRepository.findDeletedByWriterMemberId(memberId, pageable);
        Map<Long, String> nicknamesByMember = fetchNicknamesByMember(page.getContent().stream()
                .map(BoardPost::getWriterMemberId)
                .toList());
        return page.map(post -> PostDto.TrashSummary.from(
                post,
                TRASH_RETENTION_DAYS,
                nicknamesByMember.get(post.getWriterMemberId())
        ));
    }

    @Transactional
    public PostDto.Response restorePost(Long postId, Long actorMemberId, boolean isAdmin) {
        BoardPost post = postRepository.findDeletedById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        checkOwnerOrAdmin(post, actorMemberId, isAdmin);
        post.restore();
        String writerNickname = fetchNickname(post.getWriterMemberId());
        return PostDto.Response.from(post, false, false, writerNickname);
    }

    @Transactional
    public void hardDeletePost(Long postId, Long actorMemberId, boolean isAdmin) {
        BoardPost post = postRepository.findDeletedById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        checkOwnerOrAdmin(post, actorMemberId, isAdmin);

        // 물리 DELETE 대신 영구삭제 플래그만 올려 조회 대상에서 제외한다.
        post.markHardDeleted();
    }

    @Transactional
    public int autoHardDeleteExpiredTrash() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(TRASH_RETENTION_DAYS);
        return postRepository.markHardDeletedBefore(cutoff);
    }

    public BoardPost findActivePost(Long postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Set<Long> fetchLikedIds(Long memberId, List<Long> postIds) {
        if (memberId == null || postIds.isEmpty()) return Collections.emptySet();
        return new HashSet<>(boardLikeRepository.findLikedPostIds(memberId, postIds));
    }

    private Set<Long> fetchBookmarkedIds(Long memberId, List<Long> postIds) {
        if (memberId == null || postIds.isEmpty()) return Collections.emptySet();
        return new HashSet<>(boardBookmarkRepository.findBookmarkedPostIds(memberId, postIds));
    }

    /** postId 목록의 이미지를 단 1회 IN 쿼리로 조회하여 postId → URL 리스트(Map)로 그룹화. */
    private Map<Long, List<String>> fetchImagesByPost(List<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();
        return boardPostImageRepository.findAllByPostIds(postIds).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getPost().getId(),
                        Collectors.mapping(BoardPostImage::getImageUrl, Collectors.toList())
                ));
    }

    /** postId 목록의 태그를 단 1회 IN 쿼리로 조회하여 postId → 태그명 리스트(Map)로 그룹화. */
    private Map<Long, List<String>> fetchTagsByPost(List<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();
        return boardPostTagRepository.findAllByPostIds(postIds).stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPost().getId(),
                        Collectors.mapping(BoardPostTag::getTagName, Collectors.toList())
                ));
    }

    private void checkOwner(BoardPost post, Long memberId) {
        if (!post.getWriterMemberId().equals(memberId)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private void checkOwnerOrAdmin(BoardPost post, Long actorMemberId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (actorMemberId == null || !post.getWriterMemberId().equals(actorMemberId)) {
            throw new ForbiddenException("작성자 또는 관리자만 수행할 수 있습니다.");
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

    private String fetchNickname(Long memberId) {
        return fetchNicknamesByMember(List.of(memberId)).get(memberId);
    }

    private Map<Long, String> fetchNicknamesByMember(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberNicknameService.resolveNicknames(memberIds);
    }

    private String resolveCreatePinnedYn(String pinnedYn, Boolean pinned) {
        if (pinnedYn != null) {
            return normalizePinnedYn(pinnedYn);
        }
        if (pinned != null) {
            return pinned ? "Y" : "N";
        }
        return "N";
    }

    private String resolveUpdatePinnedYn(String pinnedYn, Boolean pinned, String currentPinnedYn) {
        if (pinnedYn != null) {
            return normalizePinnedYn(pinnedYn);
        }
        if (pinned != null) {
            return pinned ? "Y" : "N";
        }
        return currentPinnedYn;
    }

    private String normalizePinnedYn(String pinnedYn) {
        String normalized = pinnedYn.trim().toUpperCase();
        if (!"Y".equals(normalized) && !"N".equals(normalized)) {
            throw new IllegalArgumentException("pinnedYn은 Y 또는 N 이어야 합니다.");
        }
        return normalized;
    }
}
