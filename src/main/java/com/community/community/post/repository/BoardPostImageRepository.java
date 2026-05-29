package com.community.community.post.repository;

import com.community.community.post.domain.BoardPostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardPostImageRepository extends JpaRepository<BoardPostImage, Long> {

    /**
     * 주어진 postId 목록에 속한 이미지들을 post_id, sort_order ASC 순으로 모두 조회.
     * 목록 응답 hydrate 용 일괄 조회 (N+1 방지).
     */
    @Query("select i from BoardPostImage i " +
            "where i.post.id in :postIds " +
            "order by i.post.id asc, i.sortOrder asc")
    List<BoardPostImage> findAllByPostIds(@Param("postIds") Collection<Long> postIds);
}

