package com.community.community.bookmark.repository;

import com.community.community.bookmark.domain.BoardBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BoardBookmarkRepository extends JpaRepository<BoardBookmark, Long> {
    Optional<BoardBookmark> findByPostIdAndMemberId(Long postId, Long memberId);
    Page<BoardBookmark> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Query("select b.postId from BoardBookmark b " +
            "where b.memberId = :memberId and b.postId in :postIds")
    List<Long> findBookmarkedPostIds(@Param("memberId") Long memberId,
                                     @Param("postIds") Collection<Long> postIds);
}
