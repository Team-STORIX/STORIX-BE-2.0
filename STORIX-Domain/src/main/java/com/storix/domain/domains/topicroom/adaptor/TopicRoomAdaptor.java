package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.search.exception.SearchNoTopicRoomFoundException;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.RoomMember;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.DuplicateTopicRoomReportException;
import com.storix.domain.domains.topicroom.exception.TodayTopicRoomNotFoundException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomUserException;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TopicRoomAdaptor {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;
    private final TopicRoomReportRepository topicRoomReportRepository;

    public TopicRoom findById(Long roomId) {
        return topicRoomRepository.findById(roomId)
                .orElseThrow(() -> UnknownTopicRoomException.EXCEPTION);
    }

    public boolean existsById(Long roomId) {
        return topicRoomRepository.existsById(roomId);
    }

    public boolean existsByWorksId(Long worksId) {
        return topicRoomRepository.existsByWorksId(worksId);
    }

    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.existsByUserIdAndTopicRoomId(userId, roomId);
    }

    public String findTopicRoomNameById(Long roomId) {
        return topicRoomRepository.findTopicRoomNameById(roomId);
    }

    public Integer findActiveUserNumberById(Long roomId) {
        Integer activeUserNumber = topicRoomRepository.findActiveUserNumberById(roomId);
        if (activeUserNumber == null) {
            throw UnknownTopicRoomException.EXCEPTION;
        }
        return activeUserNumber;
    }

    public List<TopicRoom> loadHotTopicRooms() {
        return topicRoomRepository.findTop15ByOrderByActivityScoreDescLastChatTimeDesc();
    }

    public List<TopicRoom> findAllActiveRooms() {
        return topicRoomRepository.findAllActiveRooms();
    }

    public List<Long> findRoomIdsByLastChatTimeAfter(LocalDateTime since) {
        return topicRoomRepository.findRoomIdsByLastChatTimeAfter(since);
    }

    @Cacheable(cacheNames = "trendingLoyaltySlot", cacheManager = "trendingCacheManager")
    public List<TopicRoomResponseDto> findLoyaltySlot() {
        return topicRoomRepository.findLoyaltySlot();
    }

    @Cacheable(cacheNames = "trendingNewUserSlots", cacheManager = "trendingCacheManager")
    public List<TopicRoomResponseDto> findNewUserSlots(List<Long> excludeIds, int limit) {
        List<TopicRoomResponseDto> result = topicRoomRepository.findNewUserSlots(excludeIds, limit);

        if (excludeIds.isEmpty() && result.isEmpty()) {
            throw TodayTopicRoomNotFoundException.EXCEPTION;
        }

        return result;
    }

    public Slice<TopicRoomResponseDto> searchBySearchCondition(List<Long> worksIds, String keyword, Pageable pageable) {
        return topicRoomRepository.findBySearchCondition(worksIds, keyword, pageable);
    }

    public Slice<TopicRoomResponseDto> searchWithFilters(List<Long> worksIds, Pageable pageable) {
        if (worksIds.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }

        Slice<TopicRoomResponseDto> result = topicRoomRepository.findBySearchWithFilters(worksIds, pageable);
        if (result.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }
        return result;
    }

    public TopicRoom saveRoom(TopicRoom room) {
        return topicRoomRepository.save(room);
    }

    // 0명 시 방 삭제
    public void deleteRoom(Long roomId) {
        topicRoomRepository.deleteById(roomId);
    }

    public void updateLastMessage(Long roomId, Long messageId, String message, MessageType messageType, Long senderId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastMessage(roomId, messageId, message, messageType, senderId, lastChatTime);
    }

    public void updatePopularity(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePopularity(rooms);
    }

    public void updatePreviousActiveUserNumbers(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePreviousActiveUserNumbers(rooms);
    }

    public int updateActivityScores(LocalDateTime messageSince, LocalDateTime freshnessSince) {
        return topicRoomRepository.updateActivityScores(messageSince, freshnessSince);
    }

    public void incrementActiveUserNumber(Long roomId) {
        topicRoomRepository.incrementActiveUserNumber(roomId);
    }

    public void decrementActiveUserNumber(Long roomId) {
        topicRoomRepository.decrementActiveUserNumber(roomId);
    }

    public TopicRoomUser findByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.findByUserIdAndTopicRoomId(userId, roomId)
                .orElseThrow(() -> UnknownTopicRoomUserException.EXCEPTION);
    }

    public Slice<TopicRoomUser> findParticipationsByUserId(Long userId, Pageable pageable) {
        return topicRoomUserRepository.findByUserIdWithTopicRoom(userId, pageable);
    }

    public void saveParticipation(Long userId, TopicRoom room, TopicRoomRole role) {
        topicRoomUserRepository.save(new TopicRoomUser(room, userId, role));
    }

    public int deleteParticipation(Long userId, Long roomId) {
        return topicRoomUserRepository.deleteByUserIdAndTopicRoomId(userId, roomId);
    }

    public long countJoinedRooms(Long userId) {
        return topicRoomUserRepository.countByUserId(userId);
    }

    public List<Long> findAllJoinedRoomIdsByUserId(Long userId) {
        return topicRoomUserRepository.findAllJoinedRoomIdsByUserId(userId);
    }

    // 주어진 토픽룸 ID 목록 중 해당 유저가 참여하고 있는 방의 ID 조회
    public Set<Long> loadJoinedRoomIds(Long userId, List<Long> roomIds) {

        // 빈 리스트일 경우 -> 빈 Set 반환
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptySet();
        }

        return topicRoomUserRepository.findJoinedRoomIdsByUserIdAndRoomIds(userId, roomIds);
    }

    public List<Long> loadMemberIdsByRoomId(Long roomId) {
        return topicRoomUserRepository.findMemberIdsByRoomId(roomId);
    }

    public Map<Long, List<Long>> loadMembersByRoomIds(List<Long> roomIds) {
        return topicRoomUserRepository.findMembersByRoomIds(roomIds).stream()
                .collect(Collectors.groupingBy(
                        RoomMember::roomId,
                        Collectors.mapping(RoomMember::userId, Collectors.toList())
                ));
    }

    public List<Long> findChatPushTargetUserIds(Long roomId, Long senderId) {
        return topicRoomUserRepository.findChatPushTargetUserIds(roomId, senderId);
    }

    public int advanceReadCursor(Long userId, Long roomId, Long messageId) {
        return topicRoomUserRepository.advanceReadCursor(userId, roomId, messageId);
    }

    public void saveReport(TopicRoomReport report) {
        try {
            topicRoomReportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateTopicRoomReportException.EXCEPTION;
        }
    }
}
