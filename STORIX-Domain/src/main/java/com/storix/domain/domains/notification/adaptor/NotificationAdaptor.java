package com.storix.domain.domains.notification.adaptor;

import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.exception.UnknownNotificationException;
import com.storix.domain.domains.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationAdaptor {

    private final NotificationRepository notificationRepository;

    /** 조회 작업 관련 메서드 */
    // 단건 조회 — 없으면 UnknownNotificationException
    public Notification findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> UnknownNotificationException.EXCEPTION);
    }

    // 최신순 첫 페이지
    public Slice<Notification> findRecentByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
    }

    // 커서 기반 다음 페이지 — cursorId 보다 과거
    public Slice<Notification> findOlderByUserId(Long userId, Long cursorId, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndIdLessThanOrderByIdDesc(userId, cursorId, pageable);
    }

    // 안 읽은 알림 개수
    public int countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }


    /** 쓰기 작업 관련 메서드 */
    // 알림 저장
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    // 한 유저의 모든 알림 읽음 처리
    public void bulkMarkAsRead(Long userId) {
        notificationRepository.bulkMarkAsRead(userId);
    }
}
