package com.storix.domain.domains.topicroom.dto;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
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
        Boolean isJoined,
        Boolean isAdultOnly
) {
    public static TopicRoomPreviewResponseDto from(
            TopicRoom room,
            TopicRoomWorksInfo worksInfo,
            String lastMessageSenderNickname,
            boolean isJoined,
            boolean excludeAdult
    ) {
        boolean isAdultOnly = AdultContentPolicy.isAdultOnly(worksInfo.ageClassification());

        // 미리보기 채팅 마스킹
        boolean mask = isAdultOnly && excludeAdult;

        return new TopicRoomPreviewResponseDto(
                room.getId(),
                room.getTopicRoomName(),
                worksInfo.worksType() != null ? worksInfo.worksType().getDbValue() : null,
                worksInfo.worksName(),
                mask ? null : worksInfo.imageUrl(),
                room.getActiveUserNumber(),
                mask ? null : room.getLastMessage(),
                mask ? null : room.getLastMessageType(),
                mask ? null : room.getLastMessageSenderId(),
                mask ? null : lastMessageSenderNickname,
                formatTimeAgo(room.getLastChatTime()),
                isJoined,
                isAdultOnly
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
