package com.community.community.post.repository;

import com.community.community.post.domain.BoardPostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardPostTagRepository extends JpaRepository<BoardPostTag, Long> {

    /**
     * 주어진 postId 목록에 속한 태그를 일괄 조회 (N+1 방지).
     */
    @Query("select t from BoardPostTag t " +
            "where t.post.id in :postIds " +
            "order by t.post.id asc, t.id asc")
    List<BoardPostTag> findAllByPostIds(@Param("postIds") Collection<Long> postIds);
}

