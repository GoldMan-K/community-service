package com.community.community.comment.repository;

import com.community.community.comment.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
