package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.dto.RoomUnreadCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicRoomUnreadService {

    private final ChatAdaptor chatAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;

    @Transactional(readOnly = true)
    public Map<Long, Integer> getUnreadCounts(Long userId, List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        return chatAdaptor.countUnreadByRoomIds(userId, roomIds).stream()
                .collect(Collectors.toMap(RoomUnreadCount::roomId, r -> r.unreadCount().intValue()));
    }

    @Transactional(readOnly = true)
    public long getTotalUnread(Long userId) {
        return chatAdaptor.countTotalUnread(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyUnread(Long userId) {
        return chatAdaptor.existsUnread(userId);
    }

    @Transactional
    public void markRoomRead(Long userId, Long roomId) {
        topicRoomAdaptor.findByUserIdAndRoomId(userId, roomId);

        Long lastMessageId = chatAdaptor.findLastMessageId(roomId);
        if (lastMessageId == null) return;

        topicRoomAdaptor.advanceReadCursor(userId, roomId, lastMessageId);
    }
}
