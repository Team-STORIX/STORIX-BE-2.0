package com.storix.infrastructure.external.topicroom;
import com.storix.common.utils.RedisKeyStatic;

import com.storix.domain.domains.topicroom.dto.TopicRoomActiveUserNumberResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisTopicRoomActiveUserNumberAdapter {

    private static final String CHANNEL_PREFIX = RedisKeyStatic.Channel.TOPIC_ROOM_ACTIVE_USERS_PREFIX;

    private final RedisTemplate<String, Object> jsonRedisTemplate;

    public RedisTopicRoomActiveUserNumberAdapter(
            @Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate
    ) {
        this.jsonRedisTemplate = jsonRedisTemplate;
    }

    public void publish(TopicRoomActiveUserNumberResponseDto response) {
        try {
            jsonRedisTemplate.convertAndSend(getChannel(response.topicRoomId()), response);
        } catch (RedisConnectionFailureException e) {
            log.error(">>>> [Redis Error] Redis 연결 실패로 토픽룸 활성 유저 수 전송 불가 - RoomId: {}",
                    response.topicRoomId(), e);
        } catch (Exception e) {
            log.error(">>>> [Redis Error] 토픽룸 활성 유저 수 발행 중 오류 발생", e);
        }
    }

    public static String getChannel(Long roomId) {
        return CHANNEL_PREFIX + roomId;
    }

    public static String getChannel(String roomId) {
        return CHANNEL_PREFIX + roomId;
    }
}
