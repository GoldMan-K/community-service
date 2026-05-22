CREATE TABLE IF NOT EXISTS board_post (
    id                BIGINT        NOT NULL AUTO_INCREMENT   COMMENT '게시글 PK',
    writer_member_id  BIGINT        NOT NULL                  COMMENT '작성자 member_id',
    region_code       VARCHAR(30)   NULL                      COMMENT '지역 코드',
    category_code     VARCHAR(30)   NULL                      COMMENT '카테고리 코드',
    sub_category_code VARCHAR(30)   NULL                      COMMENT '서브 카테고리 코드',
    title             VARCHAR(255)  NOT NULL                  COMMENT '제목',
    content           LONGTEXT      NOT NULL                  COMMENT '본문',
    views_count       INT           NOT NULL DEFAULT 0,
    likes_count       INT           NOT NULL DEFAULT 0,
    comments_count    INT           NOT NULL DEFAULT 0,
    pinned_yn         CHAR(1)       NOT NULL DEFAULT 'N',
    status            VARCHAR(10)   NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL|HIDDEN|DELETED|REPORTED',
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at        DATETIME(3)   NULL,
    PRIMARY KEY (id),
    KEY idx_post_writer   (writer_member_id),
    KEY idx_post_category (category_code),
    KEY idx_post_status   (status),
    KEY idx_post_created  (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';

CREATE TABLE IF NOT EXISTS board_post_image (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    post_id       BIGINT       NOT NULL,
    media_file_id BIGINT       NULL,
    image_url     VARCHAR(500) NOT NULL,
    sort_order    INT          NOT NULL DEFAULT 0,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_post_image_order (post_id, sort_order),
    KEY idx_post_image_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 이미지';

CREATE TABLE IF NOT EXISTS board_post_tag (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    tag_name   VARCHAR(50) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_post_tag (post_id, tag_name),
    KEY idx_post_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 태그';

CREATE TABLE IF NOT EXISTS board_comment (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    post_id           BIGINT      NOT NULL,
    writer_member_id  BIGINT      NOT NULL,
    parent_comment_id BIGINT      NULL,
    mention_member_id BIGINT      NULL,
    content           TEXT        NOT NULL,
    is_deleted        TINYINT(1)  NOT NULL DEFAULT 0,
    created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_comment_post    (post_id),
    KEY idx_comment_writer  (writer_member_id),
    KEY idx_comment_parent  (parent_comment_id),
    KEY idx_comment_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글';

CREATE TABLE IF NOT EXISTS board_like (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    member_id  BIGINT      NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_like (post_id, member_id),
    KEY idx_like_post   (post_id),
    KEY idx_like_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='좋아요';

CREATE TABLE IF NOT EXISTS board_bookmark (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    member_id  BIGINT      NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_bookmark (post_id, member_id),
    KEY idx_bookmark_post   (post_id),
    KEY idx_bookmark_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='북마크';
