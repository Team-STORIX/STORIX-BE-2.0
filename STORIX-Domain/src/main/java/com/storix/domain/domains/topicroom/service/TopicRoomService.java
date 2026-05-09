package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.TrendingItem;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.*;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TopicRoomService implements TopicRoomUseCase {

    private final LoadTopicRoomPort loadTopicRoomPort;
    private final LoadWorksPort loadWorksPort;
    private final SearchHistoryService searchHistoryService;
    private final LoadTopicRoomUserPort loadTopicRoomMemberPort;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final WorksAdaptor worksAdapter;
    private final UserAdaptor userAdaptor;

    @Transactional(readOnly = true)
    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {

        // 참여 정보 조회
        Slice<TopicRoomUser> participations = topicRoomAdaptor.findParticipationsByUserId(userId, pageable);

        // 조회된 토픽룸의 worksId
        List<Long> worksIds = participations.stream()
                .map(p -> p.getTopicRoom().getWorksId())
                .toList();

        // works 정보를 한 번에 조회하여 Map으로 변환
        Map<Long, TopicRoomWorksInfo> worksMap = worksAdapter.loadWorksMapByIds(worksIds);

        return participations.map(participation -> {
            TopicRoom room = participation.getTopicRoom();
            TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());

            return TopicRoomResponseDto.from(room, worksInfo, true);
        });
    }

    @Transactional(readOnly = true)
    public List<TopicRoomResponseDto> getTodayTopicRooms(Long userId) {

        List<TopicRoomResponseDto> trendingRooms = new java.util.ArrayList<>();

        // 1) 충성 유저 탐색 필터 - 슬롯 1개
        List<TopicRoomResponseDto> loyaltySlot = topicRoomAdaptor.findLoyaltySlot();
        trendingRooms.addAll(loyaltySlot);

        // 2) 신규 유저 락인 필터 - 나머지 슬롯
        int newUserSlotCount = 3 - trendingRooms.size();

        List<Long> excludeIds = loyaltySlot.stream()
                .map(TopicRoomResponseDto::getTopicRoomId)
                .toList();

        List<TopicRoomResponseDto> newUserSlots = topicRoomAdaptor.findNewUserSlots(excludeIds, newUserSlotCount);
        trendingRooms.addAll(newUserSlots);

        // 참여 여부 마킹
        applyMembershipStatus(trendingRooms, userId);
        return trendingRooms;
    }

    @Transactional(readOnly = true)
    public List<TopicRoomResponseDto> getPopularRooms(Long userId) {

        // 1. 상위 5개 토픽룸 가져오기
        List<TopicRoom> rooms = topicRoomAdaptor.loadTop5PopularRooms();
        if (rooms.isEmpty()) return Collections.emptyList();

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        List<Long> worksIds = rooms.stream().map(TopicRoom::getWorksId).distinct().toList();

        Map<Long, TopicRoomWorksInfo> worksMap = worksAdapter.loadWorksMapByIds(worksIds);

        // 포트를 통해 Set<Long> 형태의 가입된 방 ID 목록 수신
        Set<Long> joinedRoomIds = (userId != null)
                ? topicRoomAdaptor.loadJoinedRoomIds(userId, roomIds)
                : Collections.emptySet();

        return rooms.stream()
                .map(room -> {
                    TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());
                    boolean isJoined = joinedRoomIds.contains(room.getId());

                    return TopicRoomResponseDto.from(room, worksInfo, isJoined);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {

        List<Long> worksIds = worksAdapter.findAllIdsByKeyword(keyword);

        Slice<TopicRoomResponseDto> rooms = topicRoomAdaptor.searchBySearchCondition(worksIds, keyword, pageable);
        applyMembershipStatus(rooms.getContent(), userId);

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

    // 토픽룸 다중 필터 검색
    @Override
    @Transactional
    public PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable
    ) {
        List<Long> worksIds = loadWorksPort.findAllIdsByKeywordWithFilters(keyword, worksTypes, genres);

        Slice<TopicRoomResponseDto> rooms = loadTopicRoomPort.searchWithFilters(worksIds, pageable);
        applyMembershipStatus(rooms.getContent(), userId);

        return PlusSearchResponseWrapperDto.<TopicRoomResponseDto>builder()
                .result(rooms)
                .build();
    }

    @Transactional
    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        User user = userAdaptor.findUserById(userId);
        Works works = worksAdapter.findById(request.getWorksId());

        // 이미 해당 작품의 토픽룸이 존재하는지 확인
        if (topicRoomAdaptor.existsByWorksId(works.getId())) {
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }

        if (!user.getIsAdultVerified() && "18세 이용가".equals(works.getAgeClassification()))
            throw UnverifiedException.EXCEPTION;

        TopicRoom room = TopicRoom.builder()
                .topicRoomName(request.getTopicRoomName())
                .worksId(works.getId())
                .build();

        try {
            TopicRoom savedRoom = topicRoomAdaptor.saveRoom(room);
            topicRoomAdaptor.saveParticipation(user.getId(), savedRoom, TopicRoomRole.HOST);
            topicRoomAdaptor.incrementActiveUserNumber(savedRoom.getId());

            return savedRoom.getId();
        } catch (DataIntegrityViolationException e) {

            // uk constraints 위반 시 에러 던지도록
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }
    }

    @Transactional
    public void joinRoom(Long userId, Long roomId) {
        User user = userAdaptor.findUserById(userId);
        TopicRoom room = topicRoomAdaptor.findById(roomId);
        Works works = worksAdapter.findById(room.getWorksId());

        if (!user.getIsAdultVerified() && "18세 이용가".equals(works.getAgeClassification()))
            throw UnverifiedException.EXCEPTION;

        if (topicRoomAdaptor.countJoinedRooms(userId) >= 9)
            throw MaxLimitException.EXCEPTION;

        try {
            topicRoomAdaptor.saveParticipation(userId, room, TopicRoomRole.MEMBER);
            topicRoomAdaptor.incrementActiveUserNumber(roomId);
        } catch (DataIntegrityViolationException e) {
            throw AlreadyJoinedException.EXCEPTION;
        }
    }

    @Transactional
    public void leaveRoom(Long userId, Long roomId) {

        int deleteCount = topicRoomAdaptor.deleteParticipation(userId, roomId);

        // 삭제된 행이 0개면 이미 나갔거나 참여 정보가 없는 상태
        if (deleteCount == 0) { return; }

        topicRoomAdaptor.decrementActiveUserNumber(roomId);

        try {
            TopicRoom room = topicRoomAdaptor.findById(roomId);

            // 인원수가 0 이하면 방 삭제 로직 실행
            if (room.getActiveUserNumber() <= 0) {
                topicRoomAdaptor.deleteRoom(roomId);
            }
        } catch (UnknownTopicRoomException e) {
            log.info("[leaveRoom] 다른 스레드에 의해 이미 지워진 토픽룸 {}번", roomId);
        }
    }

    @Transactional
    public void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request) {

        if (reporterId.equals(request.getReportedUserId())) {
            throw SelfReportException.EXCEPTION;
        }

        TopicRoomReport report = TopicRoomReport.builder()
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .topicRoomId(roomId)
                .reason(request.getReason())
                .otherReason(request.getOtherReason())
                .build();
        topicRoomAdaptor.saveReport(report);
    }


    // 참여 여부 마킹 로직 공통화
    private void applyMembershipStatus(List<TopicRoomResponseDto> rooms, Long userId) {
        if (userId != null && !rooms.isEmpty()) {
            List<Long> joinedRoomIds = loadTopicRoomPort.findAllJoinedRoomIdsByUserId(userId);
            rooms.forEach(dto -> {
                if (joinedRoomIds.contains(dto.getTopicRoomId())) {
                    dto.markAsJoined(true);
                }
            });
        }
    }
}
