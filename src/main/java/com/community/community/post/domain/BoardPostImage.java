package com.community.community.post.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_post_image")
public class BoardPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    @Column(name = "media_file_id")
    private Long mediaFileId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BoardPostImage() {}

    public BoardPostImage(BoardPost post, String imageUrl, int sortOrder) {
        this.post      = post;
        this.imageUrl  = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Long getId()           { return id; }
    public BoardPost getPost()    { return post; }
    public String getImageUrl()   { return imageUrl; }
    public int getSortOrder()     { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
