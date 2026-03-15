package com.storix.storix_api.domains.notification.service;

import com.storix.storix_api.domains.notification.domain.Notification;
import com.storix.storix_api.domains.notification.dto.NotificationResponseDto;
import com.storix.storix_api.domains.notification.repository.NotificationRepository;
import com.storix.storix_api.global.apiPayload.exception.notification.UnauthorizedNotificationException;
import com.storix.storix_api.global.apiPayload.exception.notification.UnknownNotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 전체 알림 목록 조회
     * */
    @Transactional(readOnly = true)
    public Slice<NotificationResponseDto> getNotifications(Long userId, Long cursorId, Pageable pageable) {
        Slice<Notification> notifications;

        if (cursorId == null) {
            // 커서가 없으면 가장 최신 데이터 조회
            notifications = notificationRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
        } else {
            // 커서가 있으면 그보다 과거 데이터 조회
            notifications = notificationRepository.findAllByUserIdAndIdLessThanOrderByIdDesc(userId, cursorId, pageable);
        }

        return notifications.map(NotificationResponseDto::from);
    }

    /**
     * 안 읽은 알림 개수 조회
     * */
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 단건 알림 읽음 처리
     */
    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> UnknownNotificationException.EXCEPTION);

        if (!notification.getUserId().equals(userId)) {
            throw UnauthorizedNotificationException.EXCEPTION;
        }

        notification.read();
    }

    /**
     * 전체 알림 읽음 처리
     * */
    @Transactional
    public void readAllNotifications(Long userId) {
        notificationRepository.bulkMarkAsRead(userId);
    }
}
