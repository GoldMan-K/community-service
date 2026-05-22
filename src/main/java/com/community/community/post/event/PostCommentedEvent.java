package com.community.community.post.event;

import java.time.Instant;

public record PostCommentedEvent(Long postId, Long commentId, Long memberId, Instant occurredAt) {}
