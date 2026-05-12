package com.storix.api.domain.topicroom.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.TrendingItem;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.domain.domains.topicroom.exception.SelfReportException;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.user.service.UserService;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.service.WorksService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.*;

@UseCase
@RequiredArgsConstructor
public class TopicRoomUseCase {

    private final TopicRoomService topicRoomService;
    private final WorksService worksService;
    private final SearchHistoryService searchHistoryService;
    private final UserService userService;

    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {

        // 참여 정보 조회
        Slice<TopicRoomUser> participations = topicRoomService.getParticipation(userId, pageable);

        // 조회된 토픽룸의 worksId
        List<Long> worksIds = participations.stream()
                .map(p -> p.getTopicRoom().getWorksId())
                .toList();

        // Works 로딩
        Map<Long, TopicRoomWorksInfo> worksMap = worksService.getWorksMapByIds(worksIds);

        return participations.map(participation -> {
            TopicRoom room = participation.getTopicRoom();
            TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());

            return TopicRoomResponseDto.from(room, worksInfo, true);
        });
    }

    public List<TopicRoomResponseDto> getTodayTopicRooms(Long userId) {

        // 충성 유저 필터 - 단일 토픽룸
        List<TopicRoomResponseDto> loyaltyRooms = topicRoomService.findLoyaltyRooms();

        // 제외 ID 생성
        List<Long> excludeIds = loyaltyRooms.stream()
                .map(TopicRoomResponseDto::getTopicRoomId)
                .toList();

        // 인기 상승 토픽룸 조회
        List<TopicRoomResponseDto> newUserRooms = topicRoomService.findNewUserRooms(excludeIds, 3 - loyaltyRooms.size());

        // 오늘의 토픽룸 후보
        List<TopicRoomResponseDto> trendingRooms = new ArrayList<>(loyaltyRooms);
        trendingRooms.addAll(newUserRooms);

        if (userId == null || trendingRooms.isEmpty()) {
            return trendingRooms;
        }

        // 참여 여부 마킹
        markMembershipStatus(userId, trendingRooms);

        return trendingRooms;
    }

    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {

        // 키워드 기반 작품 ID 조회
        List<Long> worksIds = worksService.getIdsByKeyword(keyword);

        // 조건 기반 토픽룸 검색
        Slice<TopicRoomResponseDto> rooms = topicRoomService.searchRoomsByCondition(worksIds, keyword, pageable);

        // 참여 여부 마킹
        markMembershipStatus(userId, rooms.getContent());

        String fallback = null;

        if (rooms.isEmpty()) {
            List<TrendingItem> trending = searchHistoryService.getTrendingKeywords();
            if (!trending.isEmpty()) {
                Collections.shuffle(trending);
                fallback = trending.get(0).getKeyword();
            }
        }

        return SearchResponseWrapperDto.<TopicRoomResponseDto>builder()
                .result(rooms)
                .fallbackRecommendation(fallback)
                .build();
    }

    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        User user = userService.findUserById(userId);
        Works works = worksService.findWorksById(request.getWorksId());

        // 토픽룸 개설 가능 여부 확인
        topicRoomService.checkInvalidity(user, works, request.getTopicRoomName());

        // 토픽룸 생성
        return topicRoomService.createRoom(user, works, request.getTopicRoomName());
    }

    public void joinRoom(Long userId, Long roomId) {

        User user = userService.findUserById(userId);
        TopicRoom room = topicRoomService.findTopicRoomById(roomId);
        Works works = worksService.findWorksById(room.getWorksId());

        // 연령 제한 검증
        topicRoomService.checkAgeValidity(user, works.getAgeClassification());

        // 참여 제한 검증
        topicRoomService.checkJoinLimit(user.getId());

        // 토픽룸 입장
        topicRoomService.joinRoom(user, room, works);
    }

    public void leaveRoom(Long userId, Long roomId) {
        topicRoomService.leaveRoom(userId, roomId);
    }

    public void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request) {

        if (reporterId.equals(request.getReportedUserId())) {
            throw SelfReportException.EXCEPTION;
        }

        topicRoomService.reportUser(reporterId, roomId, request);
    }

    public List<TopicRoomResponseDto> getPopularRooms(Long userId) {

        // 상위 5개 인기 토픽룸 조회
        List<TopicRoomResponseDto> rooms =  topicRoomService.getPopularRooms();

        // 참여 여부 마킹
        markMembershipStatus(userId, rooms);

        return rooms;
    }

    public List<TopicRoomUserResponseDto> getRoomMembers(Long roomId) {

        // 토픽룸 존재 여부 검증
        topicRoomService.checkRoomExistence(roomId);

        // 참여자 ID 조회
        List<Long> memberIds = topicRoomService.getRoomMembers(roomId);

        // 참여자 정보 조회
        Map<Long, StandardProfileInfo> profileMap = userService.getProfileByUserIds(memberIds);

        return memberIds.stream()
                .map(profileMap::get)
                .filter(Objects::nonNull)
                .map(info -> new TopicRoomUserResponseDto(
                        info.userId(),
                        info.nickName(),
                        info.profileImageUrl() // S3 BaseUrl이 적용된 URL
                ))
                .toList();
    }

    public PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable
    ) {
        return topicRoomService.searchRoomsWithFilters(userId,  keyword, worksTypes,  genres, pageable);
    }

    private void markMembershipStatus(Long userId, List<TopicRoomResponseDto> rooms) {

        if (userId == null || rooms.isEmpty()) {
            return;
        }

        List<Long> roomIds = rooms.stream()
                .map(TopicRoomResponseDto::getTopicRoomId)
                .toList();

        Set<Long> joinedRoomIds = topicRoomService.findJoinedRoomIds(userId, roomIds);

        rooms.forEach(room ->
                room.markAsJoined(joinedRoomIds.contains(room.getTopicRoomId())));
    }
}
