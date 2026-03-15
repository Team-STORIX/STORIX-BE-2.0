package com.storix.storix_api.domains.topicroom.dto;

import com.storix.storix_api.domains.topicroom.domain.TopicRoom;
import com.storix.storix_api.domains.works.domain.WorksType;
import com.storix.storix_api.domains.works.dto.TopicRoomWorksInfo;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicRoomResponseDto {

    private Long topicRoomId;
    private String topicRoomName;
    private String worksType;
    private String worksName;
    private String thumbnailUrl;
    private Integer activeUserNumber;
    private String lastChatTime;
    private Boolean isJoined;

    public TopicRoomResponseDto(Long topicRoomId, String topicRoomName, WorksType worksType, String worksName,
                                String thumbnailUrl, Integer activeUserNumber, LocalDateTime lastChatTime, boolean isJoined) {
        this.topicRoomId = topicRoomId;
        this.topicRoomName = topicRoomName;
        this.worksType = (worksType != null) ? worksType.getDbValue() : null;
        this.worksName = worksName;
        this.thumbnailUrl = thumbnailUrl;
        this.activeUserNumber = activeUserNumber;
        this.lastChatTime = formatTimeAgo(lastChatTime); // 시간 포맷팅 로직 적용
        this.isJoined = isJoined;
    }

    public static TopicRoomResponseDto from(TopicRoom room, TopicRoomWorksInfo worksInfo, boolean isJoined) {
        return TopicRoomResponseDto.builder()
                .topicRoomId(room.getId())
                .topicRoomName(room.getTopicRoomName())
                .worksType(worksInfo.worksType() != null ? worksInfo.worksType().getDbValue() : null)
                .worksName(worksInfo.worksName())
                .thumbnailUrl(worksInfo.imageUrl())
                .activeUserNumber(room.getActiveUserNumber())
                .lastChatTime(formatTimeAgo(room.getLastChatTime()))
                .isJoined(isJoined)
                .build();
    }

    public void markAsJoined(boolean status) {
        this.isJoined = status;
    }

    private static String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "대화 없음";
        long diff = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (diff < 1) return "방금 전";
        if (diff < 60) return diff + "분 전";
        if (diff < 1440) return (diff / 60) + "시간 전";
        return (diff / 1440) + "일 전";
    }
}
