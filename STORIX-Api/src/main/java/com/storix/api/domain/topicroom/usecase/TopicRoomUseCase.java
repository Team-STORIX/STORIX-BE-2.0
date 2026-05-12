package com.storix.api.domain.topicroom.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.domain.domains.topicroom.exception.InvalidTitleException;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@UseCase
@RequiredArgsConstructor
public class TopicRoomUseCase {

    // TODO: 리스트 확정 시 별도로 분리 예정
    private final List<String> bannedWords = List.of("비속어", "욕설", "정치");
    private final TopicRoomService topicRoomService;

    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {
        return topicRoomService.getMyJoinedRooms(userId, pageable);
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
        List<Long> roomIds = trendingRooms.stream()
                .map(TopicRoomResponseDto::getTopicRoomId)
                .toList();

        Set<Long> joinedRoomIds = topicRoomService.findJoinedRoomIds(userId, roomIds);

        trendingRooms.forEach(room ->
                room.markAsJoined(joinedRoomIds.contains(room.getTopicRoomId())));

        return trendingRooms;
    }

    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {
        return topicRoomService.searchRooms(keyword, userId, pageable);
    }

    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        validate(request.getTopicRoomName());
        return topicRoomService.createRoom(userId, request);
    }

    public void joinRoom(Long userId, Long roomId) {
        topicRoomService.joinRoom(userId, roomId);
    }

    public void leaveRoom(Long userId, Long roomId) {
        topicRoomService.leaveRoom(userId, roomId);
    }

    public void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request) {
        topicRoomService.reportUser(reporterId, roomId, request);
    }

    public List<TopicRoomResponseDto> getPopularRooms(Long userId) {
        return topicRoomService.getPopularRooms(userId);
    }

    public List<TopicRoomUserResponseDto> getRoomMembers(Long roomId) {
        return topicRoomService.getRoomMembers(roomId);
    }

    public PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable
    ) {
        return topicRoomService.searchRoomsWithFilters(userId,  keyword, worksTypes,  genres, pageable);
    }

    public void validate(String text) {

        for (String word : bannedWords) {

            if (text.contains(word)) {
                throw InvalidTitleException.EXCEPTION;
            }
        }
    }
}
