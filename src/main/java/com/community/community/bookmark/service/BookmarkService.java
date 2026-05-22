package com.community.community.bookmark.service;

import com.community.community.bookmark.domain.BoardBookmark;
import com.community.community.bookmark.repository.BoardBookmarkRepository;
import com.community.community.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookmarkService {

    private final BoardBookmarkRepository bookmarkRepository;
    private final PostService postService;

    public BookmarkService(BoardBookmarkRepository bookmarkRepository, PostService postService) {
        this.bookmarkRepository = bookmarkRepository;
        this.postService = postService;
    }

    @Transactional
    public void addBookmark(Long memberId, Long postId) {
        if (bookmarkRepository.findByPostIdAndMemberId(postId, memberId).isPresent()) {
            throw new IllegalStateException("이미 북마크한 게시글입니다.");
        }
        postService.findActivePost(postId);
        bookmarkRepository.save(new BoardBookmark(postId, memberId));
    }

    @Transactional
    public void removeBookmark(Long memberId, Long postId) {
        BoardBookmark bookmark = bookmarkRepository.findByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("북마크 기록이 없습니다."));
        bookmarkRepository.delete(bookmark);
    }

    public Page<Long> getMyBookmarkedPostIds(Long memberId, Pageable pageable) {
        return bookmarkRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(BoardBookmark::getPostId);
    }
}
