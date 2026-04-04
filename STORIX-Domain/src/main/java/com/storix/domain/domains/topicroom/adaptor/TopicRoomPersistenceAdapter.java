package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.search.exception.SearchNoTopicRoomFoundException;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.RecordTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import com.storix.domain.domains.topicroom.exception.TodayTopicRoomNotFoundException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TopicRoomPersistenceAdapter implements LoadTopicRoomPort, RecordTopicRoomPort, UpdateTopicRoomPort, LoadTopicRoomUserPort {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;
    private final TopicRoomReportRepository topicRoomReportRepository;

    @Override public TopicRoom findById(Long roomId) {

        return topicRoomRepository.findById(roomId)
                .orElseThrow(() -> UnknownTopicRoomException.EXCEPTION);
    }

    @Override public Slice<TopicRoomUser> findParticipationsByUserId(Long userId, Pageable pageable) {
        return topicRoomUserRepository.findByUserIdWithTopicRoom(userId, pageable);
    }

    @Override
    public List<TopicRoomResponseDto> findLoyaltySlot() {
        return topicRoomRepository.findLoyaltySlot();
    }

    @Override
    public List<TopicRoomResponseDto> findNewUserSlots(List<Long> excludeIds, int limit) {
        List<TopicRoomResponseDto> result = topicRoomRepository.findNewUserSlots(excludeIds, limit);

        if (excludeIds.isEmpty() && result.isEmpty()) {
            throw TodayTopicRoomNotFoundException.EXCEPTION;
        }

        return result;
    }

    @Override public Slice<TopicRoomResponseDto> searchBySearchCondition(List<Long> worksIds, String keyword, Pageable pageable) {
        return topicRoomRepository.findBySearchCondition(worksIds, keyword, pageable);
    }

    @Override public Slice<TopicRoomResponseDto> searchWithFilters(List<Long> worksIds, Pageable pageable) {
        if (worksIds.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }

        Slice<TopicRoomResponseDto> result = topicRoomRepository.findBySearchWithFilters(worksIds, pageable);
        if (result.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }
        return result;
    }

    @Override public long countJoinedRooms(Long userId) {
        return topicRoomUserRepository.countByUserId(userId);
    }

    @Override public TopicRoom saveRoom(TopicRoom room) {
        return topicRoomRepository.save(room);
    }

    @Override
    public void saveParticipation(Long userId, TopicRoom room, TopicRoomRole role) {
        topicRoomUserRepository.save(new TopicRoomUser(room, userId, role));
    }

    @Override public int deleteParticipation(Long userId, Long roomId) {
        return topicRoomUserRepository.deleteByUserIdAndTopicRoomId(userId, roomId);
    }

    @Override public void saveReport(TopicRoomReport report) {
        topicRoomReportRepository.save(report);
    }

    @Override
    public void incrementActiveUserNumber(Long roomId) {
        topicRoomRepository.incrementActiveUserNumber(roomId);
    }

    @Override
    public void decrementActiveUserNumber(Long roomId) {
        topicRoomRepository.decrementActiveUserNumber(roomId);
    }

    @Override
    public void updateLastChatTime(Long roomId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastChatTime(roomId, lastChatTime);
    }

    @Override
    public List<TopicRoom> loadTop5PopularRooms() {
        return topicRoomRepository.findTop5ByOrderByPopularityScoreDescLastChatTimeDesc();
    }

    @Override
    public Set<Long> loadJoinedRoomIds(Long userId, List<Long> roomIds) {

        // 빈 리스트일 경우 -> 빈 Set 반환
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptySet();
        }

        return topicRoomUserRepository.findJoinedRoomIdsByUserIdAndRoomIds(userId, roomIds);
    }

    @Override
    public List<Long> findAllJoinedRoomIdsByUserId(Long userId) {
        return topicRoomUserRepository.findAllJoinedRoomIdsByUserId(userId);
    }

    // 0명 시 방 삭제
    @Override public void deleteRoom(Long roomId) { topicRoomRepository.deleteById(roomId); }

    @Override
    public boolean existsByWorksId(Long worksId) {
        return topicRoomRepository.existsByWorksId(worksId);
    }

    @Override
    public boolean existsById(Long roomId) {
        return topicRoomRepository.existsById(roomId);
    }

    @Override
    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.existsByUserIdAndTopicRoomId(userId, roomId);
    }

    @Override
    public List<Long> loadMemberIdsByRoomId(Long roomId) {
        return topicRoomUserRepository.findMemberIdsByRoomId(roomId);
    }

    @Override
    public void updatePopularity(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePopularity(rooms);
    }

    @Override
    public void updatePreviousActiveUserNumbers(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePreviousActiveUserNumbers(rooms);
    }

    @Override
    public List<TopicRoom> findAllActiveRooms() {
        return topicRoomRepository.findAllActiveRooms();
    }
}