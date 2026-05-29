-- 게시글 영구삭제 플래그(del_yn) 추가 (idempotent)
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'board_post'
      AND column_name = 'del_yn'
);
SET @sql := IF(
    @col_exists = 0,
    "ALTER TABLE board_post ADD COLUMN del_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '영구삭제 여부(Y/N)' AFTER deleted_at",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 안전하게 기존 데이터 기본값 보정
UPDATE board_post
SET del_yn = 'N'
WHERE del_yn IS NULL;

-- active/trash 조회 필터(del_yn, status, deleted_at) 인덱스
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'board_post'
      AND index_name = 'idx_post_del_status_deleted'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_post_del_status_deleted ON board_post (del_yn, status, deleted_at)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

