package com.community.community.post.event;

import java.time.Instant;

public record PostCreatedEvent(Long postId, Long memberId, Instant occurredAt) {
    public static PostCreatedEvent of(Long postId, Long memberId) {
        return new PostCreatedEvent(postId, memberId, Instant.now());
    }
}
