package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomUserException;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final UserAdaptor userAdaptor;
    private final ChatAdaptor chatAdaptor;
    private final UserBlockAdaptor userBlockAdaptor;
    private final AdultWorksHelper adultWorksHelper;

    @Transactional(readOnly = true)
    public StandardProfileInfo validateRoomMemberAndGetProfile(Long userId, Long roomId) {

        // 토픽룸 존재 여부 검증
        TopicRoom room = topicRoomAdaptor.findById(roomId);

        // 해당 토픽룸에 참여 중인 유저인지 검증
        if (!topicRoomAdaptor.existsByUserIdAndRoomId(userId, roomId)) {
            throw UnknownTopicRoomUserException.EXCEPTION;
        }

        // 성인인증 유효한지 확인
        adultWorksHelper.CheckUserAuthorityWithWorks(userId, room.getWorksId());

        return userAdaptor.findStandardProfileInfoByUserId(userId);
    }

    // 토픽룸 존재 여부 + 성인 인증 검증
    @Transactional(readOnly = true)
    public void validateRoomAccess(Long userId, Long roomId) {
        TopicRoom room = topicRoomAdaptor.findById(roomId);

        adultWorksHelper.CheckUserAuthorityWithWorks(userId, room.getWorksId());
    }

    // 발행 전에 저장해야 실시간 메시지에도 실제 id 와 저장 시각이 실린다
    @Transactional
    public ChatMessage save(ChatMessage chatMessage) {
        return chatAdaptor.saveMessage(chatMessage);
    }

    @Transactional(readOnly = true)
    public Slice<ChatMessageResponseDto> getChatMessages(Long roomId, List<Long> blockedIds, Pageable pageable) {
        return chatAdaptor.loadMessages(roomId, blockedIds, pageable);
    }

    public Integer getActiveUserNumber(Long roomId) {
        return topicRoomAdaptor.findActiveUserNumberById(roomId);
    }

    // 차단한 유저의 메시지는 목록에서 빼고, 실시간 수신 필터용으로 앱에도 내려준다
    public List<Long> getBlockedUserIds(Long userId) {
        return userBlockAdaptor.findBlockedUserIds(userId);
    }

    public LocalDateTime getRoomJoinedAt(Long userId, Long roomId) {
        TopicRoomUser participation = topicRoomAdaptor.findByUserIdAndRoomId(userId, roomId);
        return participation.getCreatedAt();
    }
}
