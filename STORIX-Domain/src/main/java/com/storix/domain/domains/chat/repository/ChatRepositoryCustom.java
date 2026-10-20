package com.storix.domain.domains.chat.repository;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.dto.RoomUnreadCount;

import java.util.List;

public interface ChatRepositoryCustom {

    List<RoomUnreadCount> countUnreadByRoomIds(Long userId, List<Long> roomIds, MessageType messageType);

    long countTotalUnreadByUserId(Long userId, MessageType messageType);

    boolean existsUnreadByUserId(Long userId, MessageType messageType);
}
