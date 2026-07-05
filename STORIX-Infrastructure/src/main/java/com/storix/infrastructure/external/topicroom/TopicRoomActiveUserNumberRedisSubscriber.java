package com.storix.infrastructure.external.topicroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.topicroom.dto.TopicRoomActiveUserNumberResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicRoomActiveUserNumberRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info(">>>> [Redis Subscriber] 토픽룸 활성 유저 수 메시지 감지");

        try {
            TopicRoomActiveUserNumberResponseDto response = objectMapper.readValue(
                    message.getBody(),
                    TopicRoomActiveUserNumberResponseDto.class
            );

            log.info(">>>> [Redis Subscriber] 토픽룸 활성 유저 수 전달 시작 - 방: {}, 활성 유저 수: {}",
                    response.topicRoomId(), response.activeUserNumber());

            messagingTemplate.convertAndSend(
                    "/sub/topic-rooms/" + response.topicRoomId() + "/active-users",
                    response
            );
        } catch (Exception e) {
            log.error(">>>> [Redis Subscriber Error] 토픽룸 활성 유저 수 메시지 파싱 중 오류: ", e);
        }
    }
}
