package com.community.community.post.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board_post")
public class BoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "writer_member_id", nullable = false)
    private Long writerMemberId;

    @Column(name = "region_code", length = 30)
    private String regionCode;

    @Column(name = "category_code", length = 30)
    private String categoryCode;

    @Column(name = "sub_category_code", length = 30)
    private String subCategoryCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "views_count", nullable = false)
    private int viewsCount;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "pinned_yn", nullable = false, length = 1)
    private String pinnedYn = "N";

    @Column(nullable = false, length = 10)
    private String status = "NORMAL";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "del_yn", nullable = false, length = 1)
    private String delYn = "N";

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardPostTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<BoardPostImage> images = new ArrayList<>();

    protected BoardPost() {}

    public BoardPost(Long writerMemberId, String title, String content,
                     String regionCode, String categoryCode, String subCategoryCode) {
        this.writerMemberId   = writerMemberId;
        this.title            = title;
        this.content          = content;
        this.regionCode       = regionCode;
        this.categoryCode     = categoryCode;
        this.subCategoryCode  = subCategoryCode;
    }

    public void update(String title, String content,
                       String regionCode, String categoryCode, String subCategoryCode) {
        this.title           = title;
        this.content         = content;
        this.regionCode      = regionCode;
        this.categoryCode    = categoryCode;
        this.subCategoryCode = subCategoryCode;
    }

    public void softDelete() {
        this.status    = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementViews()    { this.viewsCount++; }
    public void incrementLikes()    { this.likesCount++; }
    public void decrementLikes()    { if (this.likesCount > 0) this.likesCount--; }
    public void incrementComments() { this.commentsCount++; }
    public void decrementComments() { if (this.commentsCount > 0) this.commentsCount--; }

    public void restore() {
        this.status    = "NORMAL";
        this.deletedAt = null;
    }

    public void markHardDeleted() {
        this.delYn = "Y";
    }

    public boolean isDeleted() { return this.deletedAt != null || "DELETED".equals(this.status); }
    public boolean isHardDeleted() { return "Y".equals(this.delYn); }

    // Getters
    public Long getId()               { return id; }
    public Long getWriterMemberId()   { return writerMemberId; }
    public String getRegionCode()     { return regionCode; }
    public String getCategoryCode()   { return categoryCode; }
    public String getSubCategoryCode(){ return subCategoryCode; }
    public String getTitle()          { return title; }
    public String getContent()        { return content; }
    public int getViewsCount()        { return viewsCount; }
    public int getLikesCount()        { return likesCount; }
    public int getCommentsCount()     { return commentsCount; }
    public String getPinnedYn()       { return pinnedYn; }
    public String getStatus()         { return status; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public LocalDateTime getDeletedAt(){ return deletedAt; }
    public String getDelYn()         { return delYn; }
    public List<BoardPostTag>   getTags()   { return tags; }
    public List<BoardPostImage> getImages() { return images; }
}
