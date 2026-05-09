package com.storix.domain.domains.topicroom.application.usecase;

import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomSortType;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface TopicRoomUseCase {

    // 토픽룸 탭 필터 검색
    PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable);

    void joinRoom(Long userId, Long roomId);

    void leaveRoom(Long userId, Long roomId);

    void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request);

    List<TopicRoomResponseDto> getPopularRooms(Long userId);

}