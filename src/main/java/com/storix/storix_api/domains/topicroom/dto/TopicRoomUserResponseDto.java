package com.storix.storix_api.domains.topicroom.dto;

public record TopicRoomUserResponseDto(
        Long userId,
        String nickName,
        String profileImageUrl
) {}
