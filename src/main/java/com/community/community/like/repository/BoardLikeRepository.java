package com.community.community.like.repository;

import com.community.community.like.domain.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByPostIdAndMemberId(Long postId, Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    @Query("select l.postId from BoardLike l " +
            "where l.memberId = :memberId and l.postId in :postIds")
    List<Long> findLikedPostIds(@Param("memberId") Long memberId,
                                @Param("postIds") Collection<Long> postIds);
}
