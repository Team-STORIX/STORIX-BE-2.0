package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TopicRoomServiceV2 {

    private final TopicRoomAdaptor topicRoomAdaptor;

    // 토픽룸 조회
    public TopicRoom findTopicRoomById(Long roomId) {
        return topicRoomAdaptor.findById(roomId);
    }
}
