-- 하드 삭제 대상을 deleted_at 범위로 고른다. 없으면 지울 게 없는 날에도 PRIMARY 를 끝까지 읽는다 (#410)
-- deleted 로 시작하면 deleted = false 조건을 가진 다른 조회까지 이 인덱스를 고를 수 있어 deleted_at 하나만 건다
ALTER TABLE chat_message ADD INDEX idx_chat_message_deleted_at (deleted_at), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE reader_board ADD INDEX idx_reader_board_deleted_at (deleted_at), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE reader_board_reply ADD INDEX idx_reader_board_reply_deleted_at (deleted_at), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE review ADD INDEX idx_review_deleted_at (deleted_at), ALGORITHM=INPLACE, LOCK=NONE;

-- 살아 있는 자식 댓글 찾기를 인덱스로 끝낸다. 앞 컬럼이 같아 parent_reply_id 외래 키 · 기존 조회도 이 인덱스를 쓴다
ALTER TABLE reader_board_reply ADD INDEX idx_reader_board_reply_parent_deleted (parent_reply_id, deleted), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE reader_board_reply DROP INDEX idx_reader_board_reply_parent_id, ALGORITHM=INPLACE, LOCK=NONE;
