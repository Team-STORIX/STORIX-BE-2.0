-- 답글 달린 댓글 본인 삭제분 deleted_at 백필 #413
-- 삭제 때 본문을 바꿔 updated_at 이 삭제 시각
UPDATE reader_board_reply
SET deleted_at = COALESCE(updated_at, created_at)
WHERE deleted = 1 AND deleted_at IS NULL;
