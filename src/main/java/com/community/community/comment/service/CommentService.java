package com.community.community.comment.service;

import com.community.community.comment.domain.BoardComment;
import com.community.community.comment.dto.CommentDto;
import com.community.community.comment.repository.BoardCommentRepository;
import com.community.community.global.exception.CommentNotFoundException;
import com.community.community.global.exception.ForbiddenException;
import com.community.community.member.service.MemberNicknameService;
import com.community.community.post.event.PostEventPublisher;
import com.community.community.post.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final BoardCommentRepository commentRepository;
    private final PostService postService;
    private final PostEventPublisher eventPublisher;
    private final MemberNicknameService memberNicknameService;

    public CommentService(BoardCommentRepository commentRepository,
                          PostService postService,
                          PostEventPublisher eventPublisher,
                          MemberNicknameService memberNicknameService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.eventPublisher = eventPublisher;
        this.memberNicknameService = memberNicknameService;
    }

    @Transactional
    public CommentDto.Response createComment(Long memberId, Long postId, CommentDto.CreateRequest req) {
        var post = postService.findActivePost(postId);
        BoardComment comment = new BoardComment(
                postId, memberId, req.parentCommentId(), req.mentionMemberId(), req.content());
        commentRepository.save(comment);
        post.incrementComments();

        Long postWriterMemberId = post.getWriterMemberId();
        Long parentCommentWriterMemberId = resolveParentCommentWriterMemberId(req.parentCommentId());

        // post.commented 이벤트 발행 → Notification Service
        eventPublisher.publishPostCommented(
                postId,
                comment.getId(),
                memberId,
                postWriterMemberId,
                parentCommentWriterMemberId
        );

        Map<Long, String> nicknamesByMember = fetchNicknames(
                Stream.of(comment.getWriterMemberId(), comment.getMentionMemberId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );

        return CommentDto.Response.from(
                comment,
                nicknamesByMember.get(comment.getWriterMemberId()),
                nicknamesByMember.get(comment.getMentionMemberId())
        );
    }

    public List<CommentDto.Response> getComments(Long postId) {
        postService.findActivePost(postId);
        List<BoardComment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Set<Long> memberIds = comments.stream()
                .flatMap(c -> Stream.of(c.getWriterMemberId(), c.getMentionMemberId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nicknamesByMember = fetchNicknames(memberIds);
        return comments.stream()
                .map(c -> CommentDto.Response.from(
                        c,
                        nicknamesByMember.get(c.getWriterMemberId()),
                        nicknamesByMember.get(c.getMentionMemberId())
                ))
                .toList();
    }

    @Transactional
    public CommentDto.Response updateComment(Long memberId, Long commentId, CommentDto.UpdateRequest req) {
        BoardComment comment = findComment(commentId);
        checkOwner(comment, memberId);
        comment.update(req.content());
        Map<Long, String> nicknamesByMember = fetchNicknames(
                Stream.of(comment.getWriterMemberId(), comment.getMentionMemberId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
        return CommentDto.Response.from(
                comment,
                nicknamesByMember.get(comment.getWriterMemberId()),
                nicknamesByMember.get(comment.getMentionMemberId())
        );
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

    private Long resolveParentCommentWriterMemberId(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .map(BoardComment::getWriterMemberId)
                .orElse(null);
    }

    private void checkOwner(BoardComment comment, Long memberId) {
        if (!comment.getWriterMemberId().equals(memberId)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private Map<Long, String> fetchNicknames(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }
        return memberNicknameService.resolveNicknames(memberIds);
    }
}
