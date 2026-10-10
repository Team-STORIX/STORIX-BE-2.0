package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicRoomRankingService {

    private final TopicRoomAdaptor topicRoomAdaptor;

    // 인기도 점수 · 증가율 갱신
    @Transactional
    public int calculatePopularity(LocalDateTime now) {
        List<TopicRoom> activeRooms = topicRoomAdaptor.findAllActiveRooms();
        if (activeRooms.isEmpty()) return 0;

        for (TopicRoom room : activeRooms) {
            // 인기도 점수: 참여자 수를 마지막 채팅 이후 시간으로 감쇠
            int users = room.getActiveUserNumber();
            LocalDateTime lastChat = room.getLastChatTime() != null ? room.getLastChatTime() : room.getCreatedAt();
            long hours = Math.max(ChronoUnit.HOURS.between(lastChat, now), 0);
            room.updatePopularityScore((users * 10.0) / Math.pow((hours + 1), 1.8));

            // 증가율 (%)
            int previous = room.getPreviousActiveUserNumber();
            room.updatePopularityGrowthRate((double) (users - previous) / Math.max(previous, 1) * 100);
        }
        topicRoomAdaptor.updatePopularity(activeRooms);
        return activeRooms.size();
    }

    // 참여자 수 스냅샷 갱신
    @Transactional
    public int snapshotActiveUserNumbers() {
        List<TopicRoom> activeRooms = topicRoomAdaptor.findAllActiveRooms();
        if (activeRooms.isEmpty()) return 0;

        activeRooms.forEach(TopicRoom::snapshotActiveUserNumber);
        topicRoomAdaptor.updatePreviousActiveUserNumbers(activeRooms);
        return activeRooms.size();
    }
}
