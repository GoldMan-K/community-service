package com.community.community.post.event;

import com.community.community.global.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class PostEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PostEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PostEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPostCreated(Long postId, Long memberId) {
        send(KafkaTopics.POST_CREATED, String.valueOf(postId), PostCreatedEvent.of(postId, memberId));
    }

    public void publishPostCommented(Long postId, Long commentId, Long memberId) {
        send(KafkaTopics.POST_COMMENTED, String.valueOf(postId),
                new PostCommentedEvent(postId, commentId, memberId, java.time.Instant.now()));
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Kafka] {} 발행 실패 - key={}, error={}", topic, key, ex.getMessage(), ex);
            } else {
                log.info("[Kafka] {} 발행 완료 - key={}, offset={}", topic, key,
                        result.getRecordMetadata().offset());
            }
        });
    }
}
