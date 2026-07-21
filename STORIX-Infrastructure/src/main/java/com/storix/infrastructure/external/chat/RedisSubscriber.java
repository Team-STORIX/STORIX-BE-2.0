package com.storix.infrastructure.external.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

        try {
            JsonNode root = objectMapper.readTree(message.getBody());

            // 배포 과도기에는 봉투 없이 발행된 메시지도 들어온다
            boolean enveloped = root.has("message");
            JsonNode payload = enveloped ? root.get("message") : root;

            if (enveloped && root.hasNonNull("traceId")) {
                MDC.put(STORIXStatic.Mdc.TRACE_ID, root.get("traceId").asText());
            }

            ChatMessageResponseDto response = objectMapper.treeToValue(payload, ChatMessageResponseDto.class);
            messagingTemplate.convertAndSend("/sub/chat/room/" + response.roomId(), response);

            log.info(">>> [Chat] 구독자 전달 roomId={}", response.roomId());

        } catch (Exception e) {
            log.error(">>> [Chat] 구독 메시지 처리 실패", e);
        } finally {
            MDC.remove(STORIXStatic.Mdc.TRACE_ID);
        }
    }
}
