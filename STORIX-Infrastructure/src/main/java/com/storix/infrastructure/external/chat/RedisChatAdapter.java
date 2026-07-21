package com.storix.infrastructure.external.chat;
import com.storix.common.utils.RedisKeyStatic;

import com.storix.domain.domains.chat.application.port.PublishChatPort;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.exception.ChatConnectionFailureException;
import com.storix.domain.domains.topicroom.exception.MessageDeliveryException;
import com.storix.common.utils.STORIXStatic;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
            ChatMessageEnvelope envelope = ChatMessageEnvelope.of(MDC.get(STORIXStatic.Mdc.TRACE_ID), response);
            jsonRedisTemplate.convertAndSend(RedisKeyStatic.Channel.CHAT_ROOM_PREFIX + response.roomId(), envelope);
        } catch (RedisConnectionFailureException e) {

            // Redis 연결 실패
            log.error(">>> [Chat] Redis 연결 실패로 메시지 발행 불가 roomId={}", response.roomId(), e);

            throw ChatConnectionFailureException.EXCEPTION;
        } catch (Exception e) {

            // 기타 오류
            log.error(">>> [Chat] 메시지 발행 실패 roomId={}", response.roomId(), e);
            throw MessageDeliveryException.EXCEPTION;
        }
    }
}