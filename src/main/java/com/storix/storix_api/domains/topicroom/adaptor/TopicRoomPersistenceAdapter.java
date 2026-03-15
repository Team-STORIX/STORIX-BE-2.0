package com.storix.storix_api.domains.topicroom.adaptor;

import com.storix.storix_api.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.storix_api.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.storix_api.domains.topicroom.application.port.RecordTopicRoomPort;
import com.storix.storix_api.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.storix_api.domains.topicroom.domain.TopicRoom;
import com.storix.storix_api.domains.topicroom.domain.TopicRoomReport;
import com.storix.storix_api.domains.topicroom.domain.TopicRoomUser;
import com.storix.storix_api.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.storix_api.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.storix_api.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.storix_api.domains.topicroom.repository.TopicRoomReportRepository;
import com.storix.storix_api.domains.topicroom.repository.TopicRoomRepository;
import com.storix.storix_api.domains.topicroom.repository.TopicRoomUserRepository;
import com.storix.storix_api.global.apiPayload.exception.topicRoom.UnknownTopicRoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    public List<TopicRoomResponseDto> findTop3TrendingWithWorks(LocalDateTime threshold) {
        return topicRoomRepository.findTop3TrendingWithWorks(threshold, PageRequest.of(0, 3));
    }

    @Override
    public List<TopicRoomResponseDto> findTopAllTimeExcludingWithWorks(int limit, List<Long> excludeIds) {
        if (excludeIds.isEmpty()) excludeIds = List.of(-1L);
        return topicRoomRepository.findTopAllTimeExcludingWithWorks(excludeIds, PageRequest.of(0, limit));
    }

    @Override public Slice<TopicRoomResponseDto> searchBySearchCondition(List<Long> worksIds, String keyword, Pageable pageable) {
        return topicRoomRepository.findBySearchCondition(worksIds, keyword, pageable);
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
    public void updatePopularityScores(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePopularityScores(rooms);
    }

    @Override
    public List<TopicRoom> findAllActiveRooms() {
        return topicRoomRepository.findAllActiveRooms();
    }
}