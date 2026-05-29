package com.community.community.post.repository;

import com.community.community.post.domain.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    @Query("SELECT p FROM BoardPost p WHERE p.delYn = 'N' AND p.deletedAt IS NULL AND p.status = 'NORMAL'" +
           " AND (:regionCode IS NULL OR p.regionCode = :regionCode)" +
           " AND (:categoryCode IS NULL OR p.categoryCode = :categoryCode)" +
           " AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<BoardPost> findAllActive(
            @Param("regionCode") String regionCode,
            @Param("categoryCode") String categoryCode,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM BoardPost p WHERE p.id = :id AND p.delYn = 'N' AND p.deletedAt IS NULL")
    Optional<BoardPost> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM BoardPost p " +
            "WHERE p.writerMemberId = :memberId AND p.delYn = 'N' AND p.deletedAt IS NOT NULL AND p.status = 'DELETED'")
    Page<BoardPost> findDeletedByWriterMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT p FROM BoardPost p " +
            "WHERE p.id = :id AND p.delYn = 'N' AND p.deletedAt IS NOT NULL AND p.status = 'DELETED'")
    Optional<BoardPost> findDeletedById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardPost p " +
            "SET p.delYn = 'Y' " +
            "WHERE p.delYn = 'N' AND p.status = 'DELETED' " +
            "AND p.deletedAt IS NOT NULL AND p.deletedAt <= :cutoff")
    int markHardDeletedBefore(@Param("cutoff") LocalDateTime cutoff);
}
