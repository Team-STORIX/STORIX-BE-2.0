-- 안 읽은 수를 방마다 (room_id = ? AND id > 읽은 위치) 범위로 세려고 읽은 위치를 항상 채운다 (#402)
-- 한 번도 안 읽은 참여는 지금까지 "참여 시각 이후 메시지" 를 안 읽은 것으로 셌다. 같은 뜻이 되게 참여 시각까지의 마지막 메시지 id 로 채운다
UPDATE topic_room_user tu
SET tu.last_read_message_id = COALESCE(
        (SELECT MAX(cm.id) FROM chat_message cm
         WHERE cm.room_id = tu.topic_room_id AND cm.created_at <= tu.created_at),
        0)
WHERE tu.last_read_message_id IS NULL;

ALTER TABLE topic_room_user MODIFY `last_read_message_id` bigint NOT NULL DEFAULT 0;
