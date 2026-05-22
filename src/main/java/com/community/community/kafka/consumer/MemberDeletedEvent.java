package com.community.community.kafka.consumer;

import java.time.Instant;

public record MemberDeletedEvent(Long memberId, Instant occurredAt) {}
