package com.community.community.global.kafka;

public final class KafkaTopics {

    private KafkaTopics() {}

    // ── Publish (Community Service 발행) ──────────────────────────────────────

    /** 게시글 작성 → Notification(팔로워 알림), Moderation(자동 필터링) */
    public static final String POST_CREATED = "post.created";

    /** 댓글 작성 → Notification(작성자·멘션 대상 댓글 알림) */
    public static final String POST_COMMENTED = "post.commented";

    // ── Consume (Community Service 수신) ──────────────────────────────────────

    /** Member Service 발행 → 회원 탈퇴 시 게시글/댓글 익명화 */
    public static final String MEMBER_DELETED = "member.deleted";
}
