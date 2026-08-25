package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.bannedword.adaptor.BannedWordAdaptor;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.genrescore.publisher.GenreScorePublisher;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomReportAdaptor;
import com.storix.domain.domains.topicroom.exception.DuplicateTopicRoomReportException;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.TrendingItem;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.ReportReason;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomPreviewResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.*;
import com.storix.domain.domains.topicroom.publisher.TopicRoomActiveUserNumberPublisher;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TopicRoomService {

    private final SearchHistoryService searchHistoryService;
    private final BannedWordAdaptor bannedWordAdaptor;
    private final GenreScorePublisher genreScorePublisher;
    private final ReportCaseAdaptor reportCaseAdaptor;
    private final TopicRoomReportAdaptor topicRoomReportAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final UserAdaptor userAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;
    private final WorksAdaptor worksAdaptor;
    private final TopicRoomActiveUserNumberPublisher activeUserNumberPublisher;
    private final NotificationPublisher notificationPublisher;
    private final TopicRoomUnreadService topicRoomUnreadService;

    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {

        // 참여 정보 조회
        Slice<TopicRoomUser> participations = topicRoomAdaptor.findParticipationsByUserId(userId, pageable);

        // 조회된 토픽룸의 worksId
        List<Long> worksIds = participations.stream()
                .map(p -> p.getTopicRoom().getWorksId())
                .toList();

        // works 정보를 한 번에 조회하여 Map으로 변환
        Map<Long, TopicRoomWorksInfo> worksMap = worksAdaptor.loadWorksMapByIds(worksIds);

        List<Long> roomIds = participations.stream()
                .map(p -> p.getTopicRoom().getId())
                .toList();
        Map<Long, Integer> unreadMap = topicRoomUnreadService.getUnreadCounts(userId, roomIds);

        List<TopicRoomResponseDto> content = participations.getContent().stream()
                .map(participation -> {
                    TopicRoom room = participation.getTopicRoom();
                    TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());
                    if (worksInfo == null) {
                        logMissingWorksInfo(room);
                        return null;
                    }
                    TopicRoomResponseDto dto = TopicRoomResponseDto.from(room, worksInfo, true);
                    dto.applyJoinedRoomState(
                            unreadMap.getOrDefault(room.getId(), 0),
                            participation.isNotificationEnabled());
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();

        return new SliceImpl<>(content, pageable, participations.hasNext());
    }


    public List<TopicRoomResponseDto> getTodayTrendingRooms(Long userId) {

        List<TopicRoomResponseDto> trendingRooms = new java.util.ArrayList<>();

        // 1) 충성 유저 탐색 필터 - 슬롯 1개
        List<TopicRoomResponseDto> loyaltySlot = topicRoomAdaptor.findLoyaltySlot();
        trendingRooms.addAll(loyaltySlot);

        // 2) 신규 유저 락인 필터 - 슬롯 최대 2~3개
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

    public List<TopicRoomPreviewResponseDto> getPopularRooms(Long userId) {
        // 1. 상위 5개 토픽룸 가져오기
        List<TopicRoom> rooms = topicRoomAdaptor.loadHotTopicRooms();
        if (rooms.isEmpty()) return Collections.emptyList();

        List<Long> roomIds = rooms.stream().map(TopicRoom::getId).toList();
        List<Long> worksIds = rooms.stream().map(TopicRoom::getWorksId).distinct().toList();
        List<Long> senderIds = rooms.stream()
                .map(TopicRoom::getLastMessageSenderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, TopicRoomWorksInfo> worksMap = worksAdaptor.loadWorksMapByIds(worksIds);
        Map<Long, String> nicknameMap = userAdaptor.findNicknameMapByUserIds(senderIds);

        // 포트를 통해 Set<Long> 형태의 가입된 방 ID 목록 수신
        Set<Long> joinedRoomIds = (userId != null)
                ? topicRoomAdaptor.loadJoinedRoomIds(userId, roomIds)
                : Collections.emptySet();

        return rooms.stream()
                .map(room -> {
                    TopicRoomWorksInfo worksInfo = worksMap.get(room.getWorksId());
                    if (worksInfo == null) {
                        logMissingWorksInfo(room);
                        return null;
                    }
                    boolean isJoined = joinedRoomIds.contains(room.getId());
                    String lastMessageSenderNickname = nicknameMap.get(room.getLastMessageSenderId());

                    return TopicRoomPreviewResponseDto.from(room, worksInfo, lastMessageSenderNickname, isJoined);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {

        List<Long> worksIds = worksAdaptor.findAllIdsByKeyword(keyword);

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
    @Transactional
    public PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable
    ) {
        List<Long> worksIds = worksAdaptor.findAllIdsByKeywordWithFilters(keyword, worksTypes, genres);

        Slice<TopicRoomResponseDto> rooms = topicRoomAdaptor.searchWithFilters(worksIds, pageable);
        applyMembershipStatus(rooms.getContent(), userId);

        return PlusSearchResponseWrapperDto.<TopicRoomResponseDto>builder()
                .result(rooms)
                .build();
    }

    @Transactional
    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        // 제목 예약어/금칙어 검증
        if (bannedWordAdaptor.containsAdminKeyword(request.getTopicRoomName())) {
            throw InvalidTitleAdminKeywordException.EXCEPTION;
        }
        if (bannedWordAdaptor.containsBannedWord(request.getTopicRoomName())) {
            throw InvalidTitleException.EXCEPTION;
        }

        User user = userAdaptor.findUserById(userId);
        Works works = worksAdaptor.findById(request.getWorksId());

        // 이미 해당 작품의 토픽룸이 존재하는지 확인
        if (topicRoomAdaptor.existsByWorksId(works.getId())) {
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }

        // 토픽룸 참여 개수 제한
        if (topicRoomAdaptor.countJoinedRooms(userId) >= 9) {
            throw MaxLimitException.EXCEPTION;
        }

        AdultContentPolicy.check(works.getAgeClassification(), () -> adultVerificationAdaptor.findLatestVerifiedAtByUserId(user.getId()));

        TopicRoom room = TopicRoom.builder()
                .topicRoomName(request.getTopicRoomName())
                .worksId(works.getId())
                .build();

        try {
            TopicRoom savedRoom = topicRoomAdaptor.saveRoom(room);
            topicRoomAdaptor.saveParticipation(user.getId(), savedRoom, TopicRoomRole.HOST);
            topicRoomAdaptor.incrementActiveUserNumber(savedRoom.getId());

            genreScorePublisher.publishWithGenre(
                    user.getId(), works.getId(), works.getGenre(), GenreScoreEventType.TOPIC_ROOM_JOIN);

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
        Works works = worksAdaptor.findById(room.getWorksId());

        AdultContentPolicy.check(works.getAgeClassification(), () -> adultVerificationAdaptor.findLatestVerifiedAtByUserId(user.getId()));
        if (topicRoomAdaptor.countJoinedRooms(userId) >= 9)
            throw MaxLimitException.EXCEPTION;

        try {
            topicRoomAdaptor.saveParticipation(userId, room, TopicRoomRole.MEMBER);
            topicRoomAdaptor.incrementActiveUserNumber(roomId);
            Integer activeUserNumber = topicRoomAdaptor.findActiveUserNumberById(roomId);
            publishActiveUserNumberChanged(roomId, activeUserNumber);

            genreScorePublisher.publishWithGenre(
                    userId, works.getId(), works.getGenre(), GenreScoreEventType.TOPIC_ROOM_JOIN);
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
            if (room.getActiveUserNumber() <= 0) {
                topicRoomAdaptor.deleteRoom(roomId);
            } else {
                publishActiveUserNumberChanged(room.getId(), room.getActiveUserNumber());
            }
        } catch (UnknownTopicRoomException e) {
            log.info("[leaveRoom] 다른 스레드에 의해 이미 지워진 토픽룸 {}번", roomId);
        }
    }

    // 탈퇴 유저가 참여 중인 방 전체 나가기 — 방별 try/catch로 한 방 실패가 나머지를 막지 않게
    @Transactional
    public void leaveAllRooms(Long userId) {
        List<Long> roomIds = topicRoomAdaptor.findAllJoinedRoomIdsByUserId(userId);
        for (Long roomId : roomIds) {
            try {
                leaveRoom(userId, roomId);
            } catch (Exception e) {
                log.warn(">>> [TopicRoom] 탈퇴 유저 방 나가기 실패 userId={}, roomId={}, cause={}",
                        userId, roomId, e.getMessage());
            }
        }
    }

    @Transactional
    public void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request) {

        if (reporterId.equals(request.getReportedUserId())) {
            throw SelfReportException.EXCEPTION;
        }

        boolean isChatMessageReport = request.getChatMessageId() != null;
        Long chatMessageId = isChatMessageReport ? request.getChatMessageId() : 0L;

        if (topicRoomReportAdaptor.hasAlreadyReported(reporterId, request.getReportedUserId(), roomId, chatMessageId)) {
            throw DuplicateTopicRoomReportException.EXCEPTION;
        }

        TargetContentType targetType = isChatMessageReport ? TargetContentType.CHAT : TargetContentType.TOPIC_ROOM;
        Long targetId = isChatMessageReport ? request.getChatMessageId() : roomId;

        ReportCase reportCase = reportCaseAdaptor.findOrCreate(
                targetType,
                targetId,
                request.getReportedUserId()
        );

        // 신고 사유(reason)는 별도로 받지 않고 유저/채팅 신고 모두 DEFAULT로 저장한다.
        TopicRoomReport report = TopicRoomReport.builder()
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .topicRoomId(roomId)
                .chatMessageId(chatMessageId)
                .reason(ReportReason.DEFAULT)
                .otherReason(isChatMessageReport ? request.getOtherReason() : null)
                .reportCaseId(reportCase.getId())
                .build();

        try {
            topicRoomAdaptor.saveReport(report);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateTopicRoomReportException.EXCEPTION;
        }
        notificationPublisher.publish(NotificationEvent.reportReceived(reporterId));
    }


    // 참여 여부 마킹 로직 공통화
    private void applyMembershipStatus(List<TopicRoomResponseDto> rooms, Long userId) {
        if (userId != null && !rooms.isEmpty()) {
            List<Long> joinedRoomIds = topicRoomAdaptor.findAllJoinedRoomIdsByUserId(userId);
            rooms.forEach(dto -> {
                if (joinedRoomIds.contains(dto.getTopicRoomId())) {
                    dto.markAsJoined(true);
                }
            });
        }
    }

    private void publishActiveUserNumberChanged(Long roomId, Integer activeUserNumber) {
        activeUserNumberPublisher.publish(roomId, activeUserNumber);
    }

    // 참조 작품이 사라진 토픽룸은 응답에서 제외하고 관련 id를 KV로 남긴다
    private void logMissingWorksInfo(TopicRoom room) {
        log.atError()
                .addKeyValue("worksId", room.getWorksId())
                .addKeyValue("topicRoomId", room.getId())
                .log(">>> [TopicRoom] works 정보 없음");
    }
}
