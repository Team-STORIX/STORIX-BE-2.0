package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.TodayTopicRoomNotFoundException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TopicRoomAdaptor {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;

    // 토픽룸 존재 여부 검증
    public TopicRoom findById(Long roomId) {
        return topicRoomRepository.findById(roomId)
                .orElseThrow(() -> UnknownTopicRoomException.EXCEPTION);
    }

    // 토픽룸 참여 정보 조회
    public Slice<TopicRoomUser> findParticipationsByUserId(Long userId, Pageable pageable) {
        return topicRoomUserRepository.findByUserIdWithTopicRoom(userId, pageable);
    }

    // 오늘의 토픽룸 (최근 활성 방 중 참여자 증가율 기준 상위 1개 조회)
    @Cacheable(cacheNames = "trendingLoyaltySlot", cacheManager = "trendingCacheManager")
    public List<TopicRoomResponseDto> findLoyaltySlot() {
        return topicRoomRepository.findLoyaltySlot();
    }

    // 오늘의 토픽룸 (대표 슬롯 제외 후 최근 활성 방 중 현재 참여자 수 기준으로 조회)
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




}
