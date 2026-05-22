package com.community.community.like.service;

import com.community.community.like.domain.BoardLike;
import com.community.community.like.repository.BoardLikeRepository;
import com.community.community.post.domain.BoardPost;
import com.community.community.post.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LikeService {

    private final BoardLikeRepository likeRepository;
    private final PostService postService;

    public LikeService(BoardLikeRepository likeRepository, PostService postService) {
        this.likeRepository = likeRepository;
        this.postService = postService;
    }

    @Transactional
    public void addLike(Long memberId, Long postId) {
        if (likeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new IllegalStateException("이미 좋아요한 게시글입니다.");
        }
        BoardPost post = postService.findActivePost(postId);
        likeRepository.save(new BoardLike(postId, memberId));
        post.incrementLikes();
    }

    @Transactional
    public void removeLike(Long memberId, Long postId) {
        BoardLike like = likeRepository.findByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 기록이 없습니다."));
        likeRepository.delete(like);
        postService.findActivePost(postId).decrementLikes();
    }
}
