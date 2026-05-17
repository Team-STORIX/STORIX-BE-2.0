package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.dto.NotificationResponseDto;
import com.storix.domain.domains.notification.exception.UnauthorizedNotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationAdaptor notificationAdaptor;

    // 1. 전체 알림 목록 조회 (커서 기반)
    @Transactional(readOnly = true)
    public Slice<NotificationResponseDto> getNotifications(Long userId, Long cursorId, Pageable pageable) {
        Slice<Notification> notifications = (cursorId == null)
                ? notificationAdaptor.findRecentByUserId(userId, pageable)
                : notificationAdaptor.findOlderByUserId(userId, cursorId, pageable);

        return notifications.map(NotificationResponseDto::from);
    }

    // 2. 안 읽은 알림 개수 조회
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        return notificationAdaptor.countUnreadByUserId(userId);
    }

    // 3. 단건 알림 읽음 처리
    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        Notification notification = notificationAdaptor.findById(notificationId);
        if (!notification.getUserId().equals(userId)) {
            throw UnauthorizedNotificationException.EXCEPTION;
        }
        notification.read();
    }

    // 4. 전체 알림 읽음 처리
    @Transactional
    public void readAllNotifications(Long userId) {
        notificationAdaptor.bulkMarkAsRead(userId);
    }
}
