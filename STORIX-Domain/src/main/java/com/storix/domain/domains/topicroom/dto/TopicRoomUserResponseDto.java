package com.storix.domain.domains.topicroom.dto;

public record TopicRoomUserResponseDto(
        Long userId,
        String nickName,
        String profileImageUrl
) {}
