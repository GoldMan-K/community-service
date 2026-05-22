package com.community.community.post.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_post_tag")
public class BoardPostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BoardPostTag() {}

    public BoardPostTag(BoardPost post, String tagName) {
        this.post    = post;
        this.tagName = tagName.toLowerCase().trim();
    }

    public Long getId()           { return id; }
    public BoardPost getPost()    { return post; }
    public String getTagName()    { return tagName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
