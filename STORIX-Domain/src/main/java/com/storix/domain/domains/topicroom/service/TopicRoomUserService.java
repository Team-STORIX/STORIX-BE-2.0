package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TopicRoomUserService {

    private final LoadTopicRoomUserPort loadTopicRoomUserPort;
    private final LoadTopicRoomPort loadTopicRoomPort;
    private final UserAdaptor userAdaptor;

    // 특정 토픽룸에 참여 중인 멤버들의 프로필 목록 조회
    @Transactional(readOnly = true)
    public List<TopicRoomUserResponseDto> getRoomMembers(Long roomId) {

        if  (!loadTopicRoomPort.existsById(roomId)) {
            throw UnknownTopicRoomException.EXCEPTION;
        }

        // 참여자 ID 목록 조회
        List<Long> memberIds = loadTopicRoomUserPort.loadMemberIdsByRoomId(roomId);

        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, StandardProfileInfo> profileMap = userAdaptor.findStandardProfileInfoByUserIds(memberIds);

        return memberIds.stream()
                .map(profileMap::get)
                .filter(Objects::nonNull)
                .map(info -> new TopicRoomUserResponseDto(
                        info.userId(),
                        info.nickName(),
                        info.profileImageUrl() // S3 BaseUrl이 적용된 URL
                ))
                .toList();
    }
}
