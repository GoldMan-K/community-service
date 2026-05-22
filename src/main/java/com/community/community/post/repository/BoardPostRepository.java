package com.community.community.post.repository;

import com.community.community.post.domain.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    @Query("SELECT p FROM BoardPost p WHERE p.deletedAt IS NULL AND p.status = 'NORMAL'" +
           " AND (:regionCode IS NULL OR p.regionCode = :regionCode)" +
           " AND (:categoryCode IS NULL OR p.categoryCode = :categoryCode)" +
           " AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<BoardPost> findAllActive(
            @Param("regionCode") String regionCode,
            @Param("categoryCode") String categoryCode,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM BoardPost p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<BoardPost> findActiveById(@Param("id") Long id);
}
