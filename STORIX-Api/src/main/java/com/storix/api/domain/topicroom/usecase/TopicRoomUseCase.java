package com.storix.api.domain.topicroom.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomPreviewResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import com.storix.domain.domains.topicroom.service.TopicRoomUnreadService;
import com.storix.domain.domains.topicroom.service.TopicRoomUserService;
import com.storix.domain.domains.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TopicRoomUseCase {

    private final TopicRoomService topicRoomService;
    private final TopicRoomUserService topicRoomUserService;
    private final TopicRoomUnreadService topicRoomUnreadService;
    private final SearchHistoryService searchHistoryService;

    public Slice<TopicRoomResponseDto> getMyJoinedRooms(Long userId, Pageable pageable) {
        return topicRoomService.getMyJoinedRooms(userId, pageable);
    }

    public List<TopicRoomResponseDto> getTodayTrendingRooms(Long userId) {
        return topicRoomService.getTodayTrendingRooms(userId);
    }

    public SearchResponseWrapperDto<TopicRoomResponseDto> searchRooms(String keyword, Long userId, Pageable pageable) {
        Slice<TopicRoomResponseDto> rooms = topicRoomService.searchRooms(keyword, userId, pageable);
        return SearchResponseWrapperDto.of(rooms, rooms.isEmpty() ? searchHistoryService.pickTrendingKeyword() : null);
    }

    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {
        return topicRoomService.createRoom(userId, request);
    }

    public void joinRoom(Long userId, Long roomId) {
        topicRoomService.joinRoom(userId, roomId);
    }

    public void leaveRoom(Long userId, Long roomId) {
        topicRoomService.leaveRoom(userId, roomId);
    }

    public void reportUser(Long reporterId, Long roomId, TopicRoomReportRequestDto request) {
        topicRoomService.reportUser(reporterId, roomId, request);
    }

    public List<TopicRoomPreviewResponseDto> getPopularRooms(Long userId) {
        return topicRoomService.getPopularRooms(userId);
    }

    public List<TopicRoomUserResponseDto> getRoomMembers(Long roomId) {
        return topicRoomUserService.getRoomMembers(roomId);
    }

    public void markRoomRead(Long userId, Long roomId) {
        topicRoomUnreadService.markRoomRead(userId, roomId);
    }

    public boolean hasAnyUnread(Long userId) {
        return topicRoomUnreadService.hasAnyUnread(userId);
    }

    public void changeNotification(Long userId, Long roomId, boolean enabled) {
        topicRoomUserService.changeNotification(userId, roomId, enabled);
    }

    public boolean isNotificationEnabled(Long userId, Long roomId) {
        return topicRoomUserService.isNotificationEnabled(userId, roomId);
    }
}
