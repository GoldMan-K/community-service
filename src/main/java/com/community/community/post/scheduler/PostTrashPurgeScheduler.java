package com.community.community.post.scheduler;

import com.community.community.post.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PostTrashPurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostTrashPurgeScheduler.class);

    private final PostService postService;

    public PostTrashPurgeScheduler(PostService postService) {
        this.postService = postService;
    }

    /**
     * 매일 자정(Asia/Seoul)에 30일 경과 휴지통 게시글을 영구삭제(del_yn=Y) 처리한다.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void purgeExpiredTrashPosts() {
        int affected = postService.autoHardDeleteExpiredTrash();
        if (affected > 0) {
            log.info("[PostTrashPurgeScheduler] {} posts marked as del_yn=Y", affected);
        }
    }
}

