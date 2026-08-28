package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.adultverification.domain.AdultVerificationPolicy;
import com.storix.domain.domains.notification.service.FeaturedNotificationService;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicRoomFeatureService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final WorksAdaptor worksAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;
    private final FeaturedNotificationService featuredNotificationService;

    public void selectAndNotify() {
        List<TopicRoom> rooms = topicRoomAdaptor.loadHotTopicRooms();
        if (rooms.isEmpty()) {
            return;
        }

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        Map<Long, List<Long>> membersByRoom = topicRoomAdaptor.loadMembersByRoomIds(roomIds);

        // 방 참여 시점 이후 성인인증이 만료된 멤버는 성인 작품 방 알림에서 제외한다
        List<Long> allMemberIds = membersByRoom.values().stream().flatMap(List::stream).distinct().toList();
        Map<Long, LocalDateTime> verifiedAtByUserId = adultVerificationAdaptor.findLatestVerifiedAtByUserIds(allMemberIds);

        for (TopicRoom room : rooms) {
            try {
                List<Long> members = membersByRoom.getOrDefault(room.getId(), List.of());
                boolean isAdultRoom = Boolean.TRUE.equals(worksAdaptor.isWorksForAdult(room.getWorksId()));

                List<Long> targetMembers = isAdultRoom
                        ? members.stream()
                                .filter(memberId -> AdultVerificationPolicy.isValidOn(
                                        verifiedAtByUserId.get(memberId), LocalDate.now()))
                                .toList()
                        : members;

                featuredNotificationService.notifyHotTopicRoomIfFirst(room.getId(), room.getTopicRoomName(), targetMembers);
            } catch (Exception e) {
                log.error(">>> [HotTopicRoom] 룸 선정 알림 실패 roomId={}", room.getId(), e);
            }
        }
    }
}
