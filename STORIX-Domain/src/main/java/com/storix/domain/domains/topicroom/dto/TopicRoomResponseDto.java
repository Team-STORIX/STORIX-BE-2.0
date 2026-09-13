package com.storix.domain.domains.topicroom.dto;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
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
    private Integer unreadCount;
    private Boolean notificationEnabled;
    private Boolean isAdultOnly;

    public TopicRoomResponseDto(Long topicRoomId, String topicRoomName, WorksType worksType, String worksName,
                                String thumbnailUrl, Integer activeUserNumber, LocalDateTime lastChatTime, boolean isJoined,
                                boolean isAdultOnly) {
        this.topicRoomId = topicRoomId;
        this.topicRoomName = topicRoomName;
        this.worksType = (worksType != null) ? worksType.getDbValue() : null;
        this.worksName = worksName;
        this.thumbnailUrl = thumbnailUrl;
        this.activeUserNumber = activeUserNumber;
        this.lastChatTime = formatTimeAgo(lastChatTime); // 시간 포맷팅 로직 적용
        this.isJoined = isJoined;
        this.unreadCount = 0;
        this.isAdultOnly = isAdultOnly;
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
                .unreadCount(0)
                .isAdultOnly(AdultContentPolicy.isAdultOnly(worksInfo.ageClassification()))
                .build();
    }

    // 캐시에서 온 인스턴스를 그대로 고치면 다른 유저의 응답까지 오염될 수 있으므로, 유저별 가공은 복사본에서 한다
    public TopicRoomResponseDto copy() {
        return TopicRoomResponseDto.builder()
                .topicRoomId(topicRoomId)
                .topicRoomName(topicRoomName)
                .worksType(worksType)
                .worksName(worksName)
                .thumbnailUrl(thumbnailUrl)
                .activeUserNumber(activeUserNumber)
                .lastChatTime(lastChatTime)
                .isJoined(isJoined)
                .unreadCount(unreadCount)
                .notificationEnabled(notificationEnabled)
                .isAdultOnly(isAdultOnly)
                .build();
    }

    public void maskAdultThumbnail(boolean excludeAdult) {
        if (excludeAdult && Boolean.TRUE.equals(isAdultOnly)) {
            this.thumbnailUrl = null;
        }
    }

    public void markAsJoined(boolean status) {
        this.isJoined = status;
    }

    public void applyJoinedRoomState(int unreadCount, boolean notificationEnabled) {
        this.unreadCount = unreadCount;
        this.notificationEnabled = notificationEnabled;
    }

    private static String formatTimeAgo(LocalDateTime time) {
        long diff = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (diff < 1) return "방금 전";
        if (diff < 60) return diff + "분 전";
        if (diff < 1440) return (diff / 60) + "시간 전";
        return (diff / 1440) + "일 전";
    }
}
