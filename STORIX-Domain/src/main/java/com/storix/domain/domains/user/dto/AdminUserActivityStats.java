package com.storix.domain.domains.user.dto;

public record AdminUserActivityStats(
        long boardCount,
        long replyCount,
        long topicRoomParticipationCount,
        long reviewCount
) {
}
