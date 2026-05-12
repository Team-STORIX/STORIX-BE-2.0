package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.genrescore.publisher.GenreScorePublisher;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.domain.domains.topicroom.exception.*;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicRoomService  {

    private final GenreScorePublisher genreScorePublisher;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final WorksAdaptor worksAdaptor;
    private final UserAdaptor userAdaptor;

    // TODO: 리스트 확정 시 별도로 분리 예정
    private final List<String> bannedWords = List.of("비속어", "욕설", "정치");

    @Transactional(readOnly = true)
    public TopicRoom findTopicRoomById(Long roomId) {
        return topicRoomAdaptor.findById(roomId);
    }

    @Transactional(readOnly = true)
    public Slice<TopicRoomUser> getParticipation(Long userId, Pageable pageable) {
        return topicRoomAdaptor.findParticipationsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<TopicRoomResponseDto> findLoyaltyRooms(){
        return topicRoomAdaptor.findLoyaltyRooms();
    }

    @Transactional(readOnly = true)
    public List<TopicRoomResponseDto> findNewUserRooms(List<Long> excludeIds, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        return topicRoomAdaptor.findNewUserRooms(excludeIds, limit);
    }

    @Transactional(readOnly = true)
    public Set<Long> findJoinedRoomIds(Long userId, List<Long> roomIds) {
        return topicRoomAdaptor.loadJoinedRoomIds(userId, roomIds);
    }

    @Transactional(readOnly = true)
    public List<TopicRoomResponseDto> getPopularRooms() {

        // 상위 5개 토픽룸 가져오기
        List<TopicRoomResponseDto> rooms = topicRoomAdaptor.findTop5PopularRooms();
        if (rooms.isEmpty()) return Collections.emptyList();

        return rooms;
    }

    @Transactional(readOnly = true)
    public Slice<TopicRoomResponseDto> searchRoomsByCondition(List<Long> worksIds, String keyword, Pageable pageable) {
        return topicRoomAdaptor.searchBySearchCondition(worksIds, keyword, pageable);
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

    @Transactional(readOnly = true)
    public void validateRoomMember(Long userId, Long roomId) {

        // 토픽룸 존재 여부 검증
        if (!topicRoomAdaptor.existsById(roomId)) {
            throw UnknownTopicRoomException.EXCEPTION;
        }

        // 해당 토픽룸에 참여 중인 유저인지 검증
        if (!topicRoomAdaptor.existsByUserIdAndRoomId(userId, roomId)){
            throw UnknownTopicRoomUserException.EXCEPTION;
        }
    }

    public void checkInvalidity(User user, Works works, String roomName) {

        // 유저 연령 제한 검증
        checkAgeValidity(user, works.getAgeClassification());

        // 이미 해당 작품의 토픽룸이 존재하는지 확인
        checkTopicRoomExistence(works.getId());

        // 금지어 필터링
        checkProhibitedWord(works.getId(), roomName);
    }

    public void checkAgeValidity(User user, AgeClassification ageClassification) {
        if (!user.getIsAdultVerified() && "18세 이용가".equals(ageClassification)) {
            throw UnverifiedException.EXCEPTION;
        }
    }

    @Transactional(readOnly = true)
    public void checkTopicRoomExistence(Long worksId){
        if (topicRoomAdaptor.existsByWorksId(worksId)) {
            throw TopicRoomAlreadyExistsException.EXCEPTION;
        }
    }

    public void checkProhibitedWord(Long worksId, String roomName) {
        for (String word : bannedWords) {
            if (roomName.contains(word)) {
                throw InvalidTitleException.EXCEPTION;
            }
        }
    }

    @Transactional
    public Long createRoom(User user, Works works, String topicRoomName) {

        TopicRoom room = TopicRoom.builder()
                .topicRoomName(topicRoomName)
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

    public void checkJoinLimit(Long userId) {
        if (topicRoomAdaptor.countJoinedRooms(userId) >= 9) {
            throw MaxLimitException.EXCEPTION;
        }
    }

    @Transactional
    public void joinRoom(User user, TopicRoom room, Works works) {

        try {
            topicRoomAdaptor.saveParticipation(user.getId(), room, TopicRoomRole.MEMBER);
            topicRoomAdaptor.incrementActiveUserNumber(room.getId());

            genreScorePublisher.publishWithGenre(
                    user.getId(), works.getId(), works.getGenre(), GenreScoreEventType.TOPIC_ROOM_JOIN);
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

        TopicRoomReport report = TopicRoomReport.builder()
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .topicRoomId(roomId)
                .reason(request.getReason())
                .otherReason(request.getOtherReason())
                .build();
        topicRoomAdaptor.saveReport(report);
    }

    // 토픽룸 존재 여부 검증
    @Transactional(readOnly = true)
    public void checkRoomExistence(Long roomId) {
        if  (!topicRoomAdaptor.existsById(roomId)) {
            throw UnknownTopicRoomException.EXCEPTION;
        }
    }

    // 특정 토픽룸에 참여 중인 멤버들의 프로필 목록 조회
    @Transactional(readOnly = true)
    public List<Long> getRoomMembers(Long roomId) {

        // 참여자 ID 목록 조회
        List<Long> memberIds = topicRoomAdaptor.loadMemberIdsByRoomId(roomId);

        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        return memberIds;
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
}
