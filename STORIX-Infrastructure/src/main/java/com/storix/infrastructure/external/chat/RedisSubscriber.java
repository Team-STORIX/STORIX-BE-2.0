package com.storix.infrastructure.external.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        log.info(">>>> [Redis Subscriber] Redis로부터 메시지 감지");

        try {
            ChatMessageResponseDto response = objectMapper.readValue(message.getBody(), ChatMessageResponseDto.class);

            log.info(">>>> [Redis Subscriber] 전달 시작 - 방: {}, 메시지: {}", response.roomId(), response.message());

            messagingTemplate.convertAndSend("/sub/chat/room/" + response.roomId(), response);

        } catch (Exception e) {
            log.error(">>>> [Redis Subscriber Error] 메시지 파싱 중 오류: ", e);
        }
    }
}