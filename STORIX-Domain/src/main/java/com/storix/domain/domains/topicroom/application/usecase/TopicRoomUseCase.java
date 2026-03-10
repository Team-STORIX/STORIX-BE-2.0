package com.storix.domain.domains.topicroom.application.usecase;

import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface TopicRoomUseCase {

    Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable);

    List<TopicRoomResponseDto> getTodayTrendingRooms(Long userId);

    SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable);

    Long createRoom(Long userId, TopicRoomCreateRequestDto request);

    void joinRoom(Long userId, Long roomId);

    void leaveRoom(Long userId, Long roomId);

    void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request);

    List<TopicRoomResponseDto> getPopularRooms(Long userId);

}