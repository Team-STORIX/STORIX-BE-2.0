package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.notification.service.FeaturedNotificationService;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicRoomFeatureService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final LoadTopicRoomUserPort loadTopicRoomUserPort;
    private final FeaturedNotificationService featuredNotificationService;

    public void selectAndNotify() {
        List<TopicRoom> rooms = topicRoomAdaptor.loadHotTopicRooms();
        if (rooms.isEmpty()) {
            return;
        }

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        Map<Long, List<Long>> membersByRoom = loadTopicRoomUserPort.loadMembersByRoomIds(roomIds);

        for (TopicRoom room : rooms) {
            try {
                featuredNotificationService.notifyHotTopicRoomIfFirst(room.getId(), room.getTopicRoomName(),
                        membersByRoom.getOrDefault(room.getId(), List.of()));
            } catch (Exception e) {
                log.error(">>> [HotTopicRoom] 룸 선정 알림 실패 roomId={}", room.getId(), e);
            }
        }
    }
}
