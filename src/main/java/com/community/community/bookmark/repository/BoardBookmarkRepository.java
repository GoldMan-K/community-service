package com.community.community.bookmark.repository;

import com.community.community.bookmark.domain.BoardBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardBookmarkRepository extends JpaRepository<BoardBookmark, Long> {
    Optional<BoardBookmark> findByPostIdAndMemberId(Long postId, Long memberId);
    Page<BoardBookmark> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
