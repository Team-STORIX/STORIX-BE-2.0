package com.storix.domain.domains.topicroom.listener;

import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.user.event.UserAccessRevokedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomWithdrawListener {

    private final TopicRoomUseCase topicRoomUseCase;

    // 탈퇴 커밋 후 참여 중인 토픽룸 전체 나가기
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserAccessRevokedEvent event) {
        if (event.type() != UserAccessRevokedEvent.UserAccessRevokedType.WITHDRAWN) {
            return;
        }
        topicRoomUseCase.leaveAllRooms(event.userId());
    }
}
