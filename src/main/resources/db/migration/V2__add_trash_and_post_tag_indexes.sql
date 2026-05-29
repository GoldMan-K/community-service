-- 휴지통/삭제글 조회 최적화 (idempotent)
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'board_post'
      AND index_name = 'idx_post_writer_status_deleted'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_post_writer_status_deleted ON board_post (writer_member_id, status, deleted_at)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'board_post'
      AND index_name = 'idx_post_status_deleted'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_post_status_deleted ON board_post (status, deleted_at)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 태그 IN 조회 최적화 (idempotent)
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'board_post_tag'
      AND index_name = 'idx_post_tag_post'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_post_tag_post ON board_post_tag (post_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
