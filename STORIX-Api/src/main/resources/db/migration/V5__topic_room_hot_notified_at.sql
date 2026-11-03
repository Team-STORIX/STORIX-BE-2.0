-- HOT 토픽룸 알림을 하루 1번에서 처음 뽑혔을 때 · 밀렸다가 7일 뒤 다시 뽑혔을 때로 바꾼다 (#417)
ALTER TABLE topic_room
    ADD COLUMN `hot_notified_at` datetime(6) NULL,
    ADD COLUMN `hot_dropped_at` datetime(6) NULL,
    ALGORITHM=INSTANT;

-- 지금 HOT 목록에 있는 방은 오늘 이미 알림을 받았으니 보낸 것으로 친다
UPDATE topic_room t
JOIN (SELECT topic_room_id FROM topic_room
      ORDER BY activity_score DESC, last_chat_time DESC
      LIMIT 15) hot ON hot.topic_room_id = t.topic_room_id
SET t.hot_notified_at = NOW(6);
