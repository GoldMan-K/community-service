package com.community.community.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_comment")
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "writer_member_id", nullable = false)
    private Long writerMemberId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "mention_member_id")
    private Long mentionMemberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BoardComment() {}

    public BoardComment(Long postId, Long writerMemberId,
                        Long parentCommentId, Long mentionMemberId, String content) {
        this.postId           = postId;
        this.writerMemberId   = writerMemberId;
        this.parentCommentId  = parentCommentId;
        this.mentionMemberId  = mentionMemberId;
        this.content          = content;
        this.isDeleted        = false;
    }

    public void update(String content) {
        if (this.isDeleted) throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        this.content = content;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.content   = "삭제된 댓글입니다.";
    }

    public Long getId()                   { return id; }
    public Long getPostId()               { return postId; }
    public Long getWriterMemberId()       { return writerMemberId; }
    public Long getParentCommentId()      { return parentCommentId; }
    public Long getMentionMemberId()      { return mentionMemberId; }
    public String getContent()            { return content; }
    public boolean isDeleted()            { return isDeleted; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}
