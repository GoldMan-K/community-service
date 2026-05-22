package com.community.community.kafka.consumer;

import com.community.community.global.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * member.deleted 이벤트 수신
 * 탈퇴 회원의 게시글/댓글을 익명화 처리한다.
 * TODO: PostRepository / CommentRepository 를 주입하여 익명화 로직 구현
 */
@Component
public class MemberEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MemberEventConsumer.class);

    @KafkaListener(
            topics = KafkaTopics.MEMBER_DELETED,
            groupId = "community-service-group",
            containerFactory = "memberDeletedListenerFactory"
    )
    public void handleMemberDeleted(MemberDeletedEvent event) {
        log.info("[Kafka] member.deleted 수신 - memberId={}", event.memberId());
        // TODO: 해당 memberId 게시글 status = DELETED, 댓글 is_deleted = 1 처리
    }
}
