package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.notification.adaptor.FeaturedNotificationDedupAdaptor;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotTopicRoomFeatureService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final LoadTopicRoomUserPort loadTopicRoomUserPort;
    private final FeaturedNotificationDedupAdaptor dedupAdaptor;

    private final NotificationPublisher notificationPublisher;

    @Transactional
    public void selectAndNotify() {
        // 활동 점수 상위 15개 중 오늘 아직 안 보낸 방만
        List<TopicRoom> targets = new ArrayList<>();
        for (TopicRoom room : topicRoomAdaptor.loadHotTopicRooms()) {
            if (dedupAdaptor.markTopicRoomIfFirstToday(room.getId())) {
                targets.add(room);
            }
        }
        if (targets.isEmpty()) {
            return;
        }

        List<Long> roomIds = targets.stream().map(TopicRoom::getId).toList();
        Map<Long, List<Long>> membersByRoom = loadTopicRoomUserPort.loadMembersByRoomIds(roomIds);

        for (TopicRoom room : targets) {
            for (Long memberId : membersByRoom.getOrDefault(room.getId(), List.of())) {
                notificationPublisher.publish(
                        NotificationEvent.hotTopicRoom(memberId, room.getId(), room.getTopicRoomName())
                );
            }
        }
    }
}
