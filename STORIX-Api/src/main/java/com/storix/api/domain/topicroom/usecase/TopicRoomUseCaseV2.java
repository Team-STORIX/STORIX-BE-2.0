package com.storix.api.domain.topicroom.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.exception.InvalidTitleException;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TopicRoomUseCaseV2 {

    // TODO: 리스트 확정 시 별도로 분리 예정
    private final List<String> bannedWords = List.of("비속어", "욕설", "정치");
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

    public Long createRoom(Long userId, TopicRoomCreateRequestDto request) {

        validate(request.getTopicRoomName());
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

    public void validate(String text) {

        for (String word : bannedWords) {

            if (text.contains(word)) {
                throw InvalidTitleException.EXCEPTION;
            }
        }
    }
}
