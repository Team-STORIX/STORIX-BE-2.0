package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.TrendingItem;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.RecordTopicRoomPort;
import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.*;
import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final RecordTopicRoomPort recordTopicRoomPort;
    private final LoadUserPort loadUserPort;
    private final LoadWorksPort loadWorksPort;
    private final SearchHistoryService searchHistoryService;
    private final ProfanityFilterService profanityFilterService;
    private final LoadTopicRoomUserPort loadTopicRoomMemberPort;

    @Override
    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {

        // 참여 정보 조회
        Slice<TopicRoomUser> participations = loadTopicRoomPort.findParticipationsByUserId(userId, pageable);

        // 조회된 토픽룸의 worksId
        List<Long> worksIds = participations.stream()
                .map(p -> p.getTopicRoom().getWorksId())
                .toList();

        // works 정보를 한 번에 조회하여 Map으로 변환
        Map<Long, TopicRoomWorksInfo> worksMap = loadWorksPort.loadWorksMapByIds(worksIds);

        return participations.map(participation -> {
            TopicRoom room = participation.getTopicRoom();
            TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());

            return TopicRoomResponseDto.from(room, worksInfo, true);
        });
    }

    @Override
    public List<TopicRoomResponseDto> getTodayTrendingRooms(Long userId) {

        // 현재 시간으로부터 24시간 전 시점 계산
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        // 24시간 내 인기 토픽룸 DTO로 조회
        List<TopicRoomResponseDto> trendingRooms = loadTopicRoomPort.findTop3TrendingWithWorks(threshold);

        // fallback 로직 추가 - 24시간 내 생성된 토픽룸이 없는 경우
        if (trendingRooms.size() < 3) {
            int needed = 3 - trendingRooms.size();

            // 중복 토픽룸 방지
            List<Long> excludeIds = trendingRooms.stream()
                    .map(TopicRoomResponseDto::getTopicRoomId)
                    .toList();

            // 부족한 개수만큼만 전체 인기순 적용
            List<TopicRoomResponseDto> fallbackRooms = loadTopicRoomPort.findTopAllTimeExcludingWithWorks(needed, excludeIds);
            trendingRooms.addAll(fallbackRooms);
        }

        // 로그인 유저인 경우 참여 여부(isJoined) 일괄 업데이트
        if (userId != null && !trendingRooms.isEmpty()) {
            List<Long> joinedRoomIds = loadTopicRoomPort.findAllJoinedRoomIdsByUserId(userId);
            trendingRooms.forEach(dto -> {
                if (joinedRoomIds.contains(dto.getTopicRoomId())) {
                    dto.markAsJoined(true);
                }
            });
        }
        applyMembershipStatus(trendingRooms, userId);
        return trendingRooms;
    }

    @Override
    public List<TopicRoomResponseDto> getPopularRooms(Long userId) {
        // 1. 상위 5개 토픽룸 가져오기
        List<TopicRoom> rooms = loadTopicRoomPort.loadTop5PopularRooms();
        if (rooms.isEmpty()) return Collections.emptyList();

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        List<Long> worksIds = rooms.stream().map(TopicRoom::getWorksId).distinct().toList();

        Map<Long, TopicRoomWorksInfo> worksMap = loadWorksPort.loadWorksMapByIds(worksIds);

        // 포트를 통해 Set<Long> 형태의 가입된 방 ID 목록 수신
        Set<Long> joinedRoomIds = (userId != null)
                ? loadTopicRoomMemberPort.loadJoinedRoomIds(userId, roomIds)
                : Collections.emptySet();

        return rooms.stream()
                .map(room -> {
                    TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());
                    boolean isJoined = joinedRoomIds.contains(room.getId());

                    return TopicRoomResponseDto.from(room, worksInfo, isJoined);
                })
                .toList();
    }

    @Override
    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {

        List<Long> worksIds = loadWorksPort.findAllIdsByKeyword(keyword);

        Slice<TopicRoomResponseDto> rooms = loadTopicRoomPort.searchBySearchCondition(worksIds, keyword, pageable);
        applyMembershipStatus(rooms.getContent(), userId);

        // 로그인 유저라면 참여 중인 방 ID 리스트를 가져와서 마킹
        if (userId != null && !rooms.isEmpty()) {
            List<Long> joinedRoomIds = loadTopicRoomPort.findAllJoinedRoomIdsByUserId(userId);
            rooms.forEach(dto -> {
                if (joinedRoomIds.contains(dto.getTopicRoomId())) {
                    dto.markAsJoined(true);
                }
            });
        }

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

    @Override
    @Transactional
    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        profanityFilterService.validate(request.getTopicRoomName());

        User user = loadUserPort.findById(userId);
        Works works = loadWorksPort.findById(request.getWorksId());

        // 이미 해당 작품의 토픽룸이 존재하는지 확인
        if (loadTopicRoomPort.existsByWorksId(works.getId())) {
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }

        if (!user.getIsAdultVerified() && "18세 이용가".equals(works.getAgeClassification()))
            throw UnverifiedException.EXCEPTION;

        TopicRoom room = TopicRoom.builder()
                .topicRoomName(request.getTopicRoomName())
                .worksId(works.getId())
                .build();

        try {
            TopicRoom savedRoom = recordTopicRoomPort.saveRoom(room);
            recordTopicRoomPort.saveParticipation(user.getId(), savedRoom, TopicRoomRole.HOST);
            recordTopicRoomPort.incrementActiveUserNumber(savedRoom.getId());

            return savedRoom.getId();
        } catch (DataIntegrityViolationException e) {

            // uk constraints 위반 시 에러 던지도록
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }
    }

    @Override
    @Transactional
    public void joinRoom(Long userId, Long roomId) {
        User user = loadUserPort.findById(userId);
        TopicRoom room = loadTopicRoomPort.findById(roomId);
        Works works = loadWorksPort.findById(room.getWorksId());

        if (!user.getIsAdultVerified() && "18세 이용가".equals(works.getAgeClassification()))
            throw UnverifiedException.EXCEPTION;
        if (loadTopicRoomPort.countJoinedRooms(userId) >= 9)
            throw MaxLimitException.EXCEPTION;

        try {
            recordTopicRoomPort.saveParticipation(userId, room, TopicRoomRole.MEMBER);
            recordTopicRoomPort.incrementActiveUserNumber(roomId);
        } catch (DataIntegrityViolationException e) {
            throw AlreadyJoinedException.EXCEPTION;
        }
    }

    @Override
    @Transactional
    public void leaveRoom(Long userId, Long roomId) {

        int deleteCount = recordTopicRoomPort.deleteParticipation(userId, roomId);

        // 삭제된 행이 0개면 이미 나갔거나 참여 정보가 없는 상태
        if (deleteCount == 0) { return; }

        recordTopicRoomPort.decrementActiveUserNumber(roomId);

        try {
            TopicRoom room = loadTopicRoomPort.findById(roomId);

            // 인원수가 0 이하면 방 삭제 로직 실행
            if (room.getActiveUserNumber() <= 0) {
                recordTopicRoomPort.deleteRoom(roomId);
            }
        } catch (UnknownTopicRoomException e) {
            log.info("[leaveRoom] 다른 스레드에 의해 이미 지워진 토픽룸 {}번", roomId);
        }
    }

    @Override
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
        recordTopicRoomPort.saveReport(report);
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
