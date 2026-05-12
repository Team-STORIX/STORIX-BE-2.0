package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.search.exception.SearchNoTopicRoomFoundException;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.TodayTopicRoomNotFoundException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TopicRoomAdaptor {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;
    private final TopicRoomReportRepository topicRoomReportRepository;

    // 토픽룸 반환
    public TopicRoom findById(Long roomId) {
        return topicRoomRepository.findById(roomId)
                .orElseThrow(() -> UnknownTopicRoomException.EXCEPTION);
    }

    // 토픽룸 존재 여부 검증
    public boolean existsById(Long roomId) {
        return topicRoomRepository.existsById(roomId);
    }

    // 작품 기반 토픽룸 존재 여부 검증
    public boolean existsByWorksId(Long worksId) {
        return topicRoomRepository.existsByWorksId(worksId);
    }

    // 토픽룸 참여 정보 조회
    public Slice<TopicRoomUser> findParticipationsByUserId(Long userId, Pageable pageable) {
        return topicRoomUserRepository.findByUserIdWithTopicRoom(userId, pageable);
    }

    // 오늘의 토픽룸 (최근 활성 방 중 참여자 증가율 기준 상위 1개 조회)
    @Cacheable(cacheNames = "trendingLoyaltyRooms", cacheManager = "trendingCacheManager")
    public List<TopicRoomResponseDto> findLoyaltyRooms() {
        return topicRoomRepository.findLoyaltyRooms();
    }

    // 오늘의 토픽룸 (대표 슬롯 제외 후 최근 활성 방 중 현재 참여자 수 기준으로 조회)
    @Cacheable(cacheNames = "trendingNewUserRooms", cacheManager = "trendingCacheManager")
    public List<TopicRoomResponseDto> findNewUserRooms(List<Long> excludeIds, int limit) {
        List<TopicRoomResponseDto> result = topicRoomRepository.findNewUserRooms(excludeIds, limit);

        if (excludeIds.isEmpty() && result.isEmpty()) {
            throw TodayTopicRoomNotFoundException.EXCEPTION;
        }

        return result;
    }

    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.existsByUserIdAndTopicRoomId(userId, roomId);
    }

    public void updateLastChatTime(Long roomId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastChatTime(roomId, lastChatTime);
    }

    public Set<Long> loadJoinedRoomIds(Long userId, List<Long> roomIds) {

        // 빈 리스트일 경우 -> 빈 Set 반환
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptySet();
        }

        return topicRoomUserRepository.findJoinedRoomIdsByUserIdAndRoomIds(userId, roomIds);
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

    public List<Long> findAllJoinedRoomIdsByUserId(Long userId) {
        return topicRoomUserRepository.findAllJoinedRoomIdsByUserId(userId);
    }

    public Slice<TopicRoomResponseDto> searchBySearchCondition(List<Long> worksIds, String keyword, Pageable pageable) {
        return topicRoomRepository.findBySearchCondition(worksIds, keyword, pageable);
    }

    public long countJoinedRooms(Long userId) {
        return topicRoomUserRepository.countByUserId(userId);
    }

    public List<TopicRoomResponseDto> findTop5PopularRooms() {
        return topicRoomRepository.findTop5PopularRooms();
    }

    public List<Long> loadMemberIdsByRoomId(Long roomId) {
        return topicRoomUserRepository.findMemberIdsByRoomId(roomId);
    }

    public TopicRoom saveRoom(TopicRoom room) {
        return topicRoomRepository.save(room);
    }

    public void saveParticipation(Long userId, TopicRoom room, TopicRoomRole role) {
        topicRoomUserRepository.save(new TopicRoomUser(room, userId, role));
    }

    public void incrementActiveUserNumber(Long roomId) {
        topicRoomRepository.incrementActiveUserNumber(roomId);
    }

    public int deleteParticipation(Long userId, Long roomId) {
        return topicRoomUserRepository.deleteByUserIdAndTopicRoomId(userId, roomId);
    }

    public void decrementActiveUserNumber(Long roomId) {
        topicRoomRepository.decrementActiveUserNumber(roomId);
    }

    public void deleteRoom(Long roomId) {
        topicRoomRepository.deleteById(roomId);
    }
    public void saveReport(TopicRoomReport report) {
        topicRoomReportRepository.save(report);
    }
}
