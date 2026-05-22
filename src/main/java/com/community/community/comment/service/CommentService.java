package com.community.community.comment.service;

import com.community.community.comment.domain.BoardComment;
import com.community.community.comment.dto.CommentDto;
import com.community.community.comment.repository.BoardCommentRepository;
import com.community.community.global.exception.CommentNotFoundException;
import com.community.community.global.exception.ForbiddenException;
import com.community.community.post.event.PostEventPublisher;
import com.community.community.post.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final BoardCommentRepository commentRepository;
    private final PostService postService;
    private final PostEventPublisher eventPublisher;

    public CommentService(BoardCommentRepository commentRepository,
                          PostService postService,
                          PostEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommentDto.Response createComment(Long memberId, Long postId, CommentDto.CreateRequest req) {
        var post = postService.findActivePost(postId);
        BoardComment comment = new BoardComment(
                postId, memberId, req.parentCommentId(), req.mentionMemberId(), req.content());
        commentRepository.save(comment);
        post.incrementComments();

        // post.commented 이벤트 발행 → Notification Service
        eventPublisher.publishPostCommented(postId, comment.getId(), memberId);

        return CommentDto.Response.from(comment);
    }

    public List<CommentDto.Response> getComments(Long postId) {
        postService.findActivePost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentDto.Response::from)
                .toList();
    }

    @Transactional
    public CommentDto.Response updateComment(Long memberId, Long commentId, CommentDto.UpdateRequest req) {
        BoardComment comment = findComment(commentId);
        checkOwner(comment, memberId);
        comment.update(req.content());
        return CommentDto.Response.from(comment);
    }

    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        BoardComment comment = findComment(commentId);
        checkOwner(comment, memberId);
        comment.softDelete();
        postService.findActivePost(comment.getPostId()).decrementComments();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private BoardComment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    private void checkOwner(BoardComment comment, Long memberId) {
        if (!comment.getWriterMemberId().equals(memberId)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}
