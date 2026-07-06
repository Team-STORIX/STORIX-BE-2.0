package com.storix.domain.domains.topicroom.event;

public record TopicRoomActiveUserNumberChangedEvent(
        Long topicRoomId,
        Integer activeUserNumber
) {
    public static TopicRoomActiveUserNumberChangedEvent of(Long topicRoomId, Integer activeUserNumber) {
        return new TopicRoomActiveUserNumberChangedEvent(topicRoomId, activeUserNumber);
    }
}
