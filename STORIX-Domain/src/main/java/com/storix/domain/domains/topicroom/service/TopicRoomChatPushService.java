package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.pushdevice.dto.ActivePushToken;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.application.port.TopicRoomPresencePort;
import com.storix.domain.domains.topicroom.dto.RoomLastMessageId;
import com.storix.domain.domains.topicroom.dto.TopicRoomChatPushTarget;
import com.storix.domain.domains.topicroom.dto.UserUnreadCount;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicRoomChatPushService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final TopicRoomPresencePort topicRoomPresencePort;
    private final PushDeviceAdaptor pushDeviceAdaptor;
    private final NotificationAdaptor notificationAdaptor;
    private final ChatAdaptor chatAdaptor;
    private final UserAdaptor userAdaptor;

    @Transactional(readOnly = true)
    public List<TopicRoomChatPushTarget> resolveTargets(
            Long roomId, Long senderId, Long afterMessageId, Long upToMessageId) {
        List<Long> candidates = topicRoomAdaptor.findChatPushTargetUserIds(roomId, senderId);
        if (candidates.isEmpty()) return List.of();

        Set<Long> online = topicRoomPresencePort.findOnlineUserIds(roomId);
        List<Long> offline = candidates.stream().filter(id -> !online.contains(id)).toList();
        if (offline.isEmpty()) return List.of();

        Map<Long, Long> batchCount = toCountMap(chatAdaptor.countMessagesAfterForUsers(
                roomId, offline, afterMessageId != null ? afterMessageId : upToMessageId - 1, upToMessageId));
        List<Long> receiverIds = offline.stream()
                .filter(id -> batchCount.getOrDefault(id, 0L) > 0)
                .toList();
        if (receiverIds.isEmpty()) return List.of();

        Map<Long, List<String>> tokensByUser = pushDeviceAdaptor.findActiveTokensByUserIds(receiverIds).stream()
                .collect(Collectors.groupingBy(
                        ActivePushToken::userId,
                        Collectors.mapping(ActivePushToken::fcmToken, Collectors.toList())));
        if (tokensByUser.isEmpty()) return List.of();

        List<Long> reachable = List.copyOf(tokensByUser.keySet());
        Map<Long, Integer> inboxUnread = notificationAdaptor.countUnreadByUserIds(reachable);
        Map<Long, Long> topicRoomUnread = toCountMap(chatAdaptor.countTotalUnreadByUserIds(reachable));

        return tokensByUser.entrySet().stream()
                .map(entry -> new TopicRoomChatPushTarget(
                        entry.getKey(),
                        entry.getValue(),
                        batchCount.getOrDefault(entry.getKey(), 0L).intValue(),
                        inboxUnread.getOrDefault(entry.getKey(), 0)
                                + topicRoomUnread.getOrDefault(entry.getKey(), 0L).intValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String findRoomName(Long roomId) {
        return topicRoomAdaptor.findTopicRoomNameById(roomId);
    }

    @Transactional(readOnly = true)
    public Long findLastMessageId(Long roomId) {
        return chatAdaptor.findLastMessageId(roomId);
    }

    @Transactional(readOnly = true)
    public ChatMessageResponseDto findLatestMessage(Long roomId) {
        return chatAdaptor.findLatestMessage(roomId);
    }

    @Transactional(readOnly = true)
    public List<Long> findRecentlyActiveRoomIds(LocalDateTime since) {
        return topicRoomAdaptor.findRoomIdsByLastChatTimeAfter(since);
    }

    @Transactional(readOnly = true)
    public List<RoomLastMessageId> findLastMessageIds(List<Long> roomIds) {
        return chatAdaptor.findLastMessageIdsByRoomIds(roomIds);
    }

    @Transactional(readOnly = true)
    public String findSenderProfileImageUrl(Long senderId) {
        StandardProfileInfo info = userAdaptor.findStandardProfileInfoByUserIds(List.of(senderId)).get(senderId);
        return info == null ? null : info.profileImageUrl();
    }

    private Map<Long, Long> toCountMap(List<UserUnreadCount> counts) {
        return counts.stream().collect(Collectors.toMap(UserUnreadCount::userId, UserUnreadCount::unreadCount));
    }
}
