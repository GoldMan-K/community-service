package com.community.community.post.event;

import java.time.Instant;

public record PostDeletedEvent(Long postId, Long memberId, Instant occurredAt) {
    public static PostDeletedEvent of(Long postId, Long memberId) {
        return new PostDeletedEvent(postId, memberId, Instant.now());
    }
}
