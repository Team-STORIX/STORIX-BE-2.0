package com.storix.domain.domains.topicroom.dto;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;

import java.time.Duration;
import java.time.LocalDateTime;

public record TopicRoomPreviewResponseDto(
        Long topicRoomId,
        String topicRoomName,
        String worksType,
        String worksName,
        String thumbnailUrl,
        Integer activeUserNumber,
        String lastMessage,
        MessageType lastMessageType,
        Long lastMessageSenderId,
        String lastMessageSenderNickname,
        String lastChatTime,
        Boolean isJoined
) {
    public static TopicRoomPreviewResponseDto from(
            TopicRoom room,
            TopicRoomWorksInfo worksInfo,
            String lastMessageSenderNickname,
            boolean isJoined
    ) {
        return new TopicRoomPreviewResponseDto(
                room.getId(),
                room.getTopicRoomName(),
                worksInfo.worksType() != null ? worksInfo.worksType().getDbValue() : null,
                worksInfo.worksName(),
                worksInfo.imageUrl(),
                room.getActiveUserNumber(),
                room.getLastMessage(),
                room.getLastMessageType(),
                room.getLastMessageSenderId(),
                lastMessageSenderNickname,
                formatTimeAgo(room.getLastChatTime()),
                isJoined
        );
    }

    private static String formatTimeAgo(LocalDateTime time) {
        long diff = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (diff < 1) return "방금 전";
        if (diff < 60) return diff + "분 전";
        if (diff < 1440) return (diff / 60) + "시간 전";
        return (diff / 1440) + "일 전";
    }
}
