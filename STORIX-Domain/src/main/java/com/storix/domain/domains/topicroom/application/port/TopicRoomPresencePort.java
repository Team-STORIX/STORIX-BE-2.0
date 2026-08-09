package com.storix.domain.domains.topicroom.application.port;

import java.util.Set;

public interface TopicRoomPresencePort {

    void enter(Long roomId, Long userId, String sessionId);

    void leave(Long roomId, Long userId, String sessionId);

    Set<Long> findOnlineUserIds(Long roomId);
}
