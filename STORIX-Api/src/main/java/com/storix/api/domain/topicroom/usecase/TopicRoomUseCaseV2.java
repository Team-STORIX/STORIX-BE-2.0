package com.storix.api.domain.topicroom.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TopicRoomUseCaseV2 {

    private final TopicRoomService topicRoomService;

    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {
        return topicRoomService.getMyJoinedRooms(userId, pageable);
    }

    public List<TopicRoomResponseDto> getTodayTopicRooms(Long userId) {
        return topicRoomService.getTodayTopicRooms(userId);
    }

    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {
        return topicRoomService.searchRooms(keyword, userId, pageable);
    }
}
