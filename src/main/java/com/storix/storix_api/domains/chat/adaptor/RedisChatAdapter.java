package com.storix.storix_api.domains.chat.adaptor;

import com.storix.storix_api.domains.chat.application.port.PublishChatPort;
import com.storix.storix_api.domains.chat.dto.ChatMessageResponseDto;
import com.storix.storix_api.global.apiPayload.exception.topicRoom.ChatConnectionFailureException;
import com.storix.storix_api.global.apiPayload.exception.topicRoom.MessageDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisChatAdapter implements PublishChatPort {

    private final RedisTemplate<String, Object> jsonRedisTemplate;

    public RedisChatAdapter(@Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate) {
        this.jsonRedisTemplate = jsonRedisTemplate;
    }

    @Override
    public void publish(ChatMessageResponseDto response) {

        try {
            // "room:1" 형식으로 메시지 전송
            jsonRedisTemplate.convertAndSend("room:" + response.roomId(), response);
        } catch (RedisConnectionFailureException e) {

            // Redis 연결 실패
            log.error(">>>> [Redis Error] Redis 연결 실패로 메시지 전송 불가 - RoomId: {}, Msg: {}",
                    response.roomId(), e);

            throw ChatConnectionFailureException.EXCEPTION;
        } catch (Exception e) {

            // 기타 오류
            log.error(">>>> [Redis Error] 메시지 발행 중 기타 오류 발생", e);
            throw MessageDeliveryException.EXCEPTION;
        }
    }
}