package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.adultverification.domain.AdultVerificationPolicy;
import com.storix.domain.domains.notification.service.FeaturedNotificationService;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<Long> worksIds = rooms.stream().map(TopicRoom::getWorksId).distinct().toList();
        Map<Long, TopicRoomWorksInfo> worksByWorksId = safeLoadWorks(worksIds);

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        Map<Long, List<Long>> membersByRoom = safeLoadMembers(roomIds);

        Set<Long> adultRoomMemberIds = rooms.stream()
                .filter(room -> isAdultRoom(worksByWorksId, room))
                .flatMap(room -> membersByRoom.getOrDefault(room.getId(), List.of()).stream())
                .collect(Collectors.toSet());
        Map<Long, LocalDateTime> verifiedAtByUserId = adultRoomMemberIds.isEmpty()
                ? Collections.emptyMap()
                : safeLoadVerifiedAt(adultRoomMemberIds.stream().toList());

        LocalDate today = LocalDate.now();

        for (TopicRoom room : rooms) {
            try {
                List<Long> members = membersByRoom.getOrDefault(room.getId(), List.of());
                boolean isAdultRoom = isAdultRoom(worksByWorksId, room);

                List<Long> targetMembers = isAdultRoom
                        ? members.stream()
                                .filter(memberId -> AdultVerificationPolicy.isValidOn(
                                        verifiedAtByUserId.get(memberId), today))
                                .toList()
                        : members;

                featuredNotificationService.notifyHotTopicRoomIfFirst(room.getId(), room.getTopicRoomName(), targetMembers);
            } catch (Exception e) {
                log.error(">>> [HotTopicRoom] 룸 선정 알림 실패 roomId={}", room.getId(), e);
            }
        }
    }

    // 작품 정보를 못 찾으면 성인 여부를 확신할 수 없으므로 안전하게 성인 방으로 간주한다
    private boolean isAdultRoom(Map<Long, TopicRoomWorksInfo> worksByWorksId, TopicRoom room) {
        TopicRoomWorksInfo works = worksByWorksId.get(room.getWorksId());
        if (works == null) {
            log.atError()
                    .addKeyValue("roomId", room.getId())
                    .addKeyValue("worksId", room.getWorksId())
                    .log(">>> [HotTopicRoom] 방의 참조 works 정보 없음");
            return true;
        }
        return AdultContentPolicy.isAdultOnly(works.ageClassification());
    }

    private Map<Long, TopicRoomWorksInfo> safeLoadWorks(List<Long> worksIds) {
        try {
            return worksAdaptor.loadWorksMapByIds(worksIds);
        } catch (Exception e) {
            log.error(">>> [HotTopicRoom] 작품 정보 배치 조회 실패", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, List<Long>> safeLoadMembers(List<Long> roomIds) {
        try {
            return topicRoomAdaptor.loadMembersByRoomIds(roomIds);
        } catch (Exception e) {
            log.error(">>> [HotTopicRoom] 방별 멤버 조회 실패", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, LocalDateTime> safeLoadVerifiedAt(List<Long> memberIds) {
        try {
            return adultVerificationAdaptor.findLatestVerifiedAtByUserIds(memberIds);
        } catch (Exception e) {
            log.error(">>> [HotTopicRoom] 성인 인증 배치 조회 실패", e);
            return Collections.emptyMap();
        }
    }
}
