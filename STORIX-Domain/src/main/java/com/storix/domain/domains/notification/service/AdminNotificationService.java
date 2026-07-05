package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.dto.AdminNotificationCommand;
import com.storix.domain.domains.notification.exception.AdminNotificationNotCancelableException;
import com.storix.domain.domains.notification.exception.AdminNotificationNotUpdatableException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private static final int NOTIFICATION_PAGE_SIZE = 10;

    private final AdminNotificationAdaptor adminNotificationAdaptor;

    @Transactional
    public Long create(AdminNotificationCommand cmd, Long assigneeAdminId) {
        return adminNotificationAdaptor.save(AdminNotification.builder()
                .title(cmd.title())
                .content(cmd.content())
                .notificationType(cmd.notificationType())
                .targetAudience(cmd.targetAudience())
                .sendType(cmd.sendType())
                .scheduledAt(cmd.scheduledAt())
                .targetType(cmd.targetType())
                .eventTargetId(cmd.eventTargetId())
                .targetLink(cmd.targetLink())
                .assigneeAdminId(assigneeAdminId)
                .build()).getId();
    }

    @Transactional
    public AdminNotification update(Long adminNotificationId, AdminNotificationCommand cmd) {
        AdminNotification adminNotification = adminNotificationAdaptor.findByIdForUpdate(adminNotificationId);
        if (!adminNotification.isScheduled()) {
            throw AdminNotificationNotUpdatableException.EXCEPTION;
        }

        adminNotification.update(
                cmd.title(),
                cmd.content(),
                cmd.notificationType(),
                cmd.targetAudience(),
                cmd.sendType(),
                cmd.scheduledAt(),
                cmd.targetType(),
                cmd.eventTargetId(),
                cmd.targetLink()
        );
        return adminNotification;
    }

    @Transactional(readOnly = true)
    public AdminNotification getById(Long adminNotificationId) {
        return adminNotificationAdaptor.findById(adminNotificationId);
    }

    @Transactional(readOnly = true)
    public Page<AdminNotification> getNotifications(int page) {
        int safePage = Math.max(0, page);
        return adminNotificationAdaptor.findAll(PageRequest.of(safePage, NOTIFICATION_PAGE_SIZE));
    }

    @Transactional
    public AdminNotification cancel(Long adminNotificationId) {
        AdminNotification adminNotification = adminNotificationAdaptor.findByIdForUpdate(adminNotificationId);
        if (!adminNotification.isScheduled()) {
            throw AdminNotificationNotCancelableException.EXCEPTION;
        }
        adminNotification.cancel();
        return adminNotification;
    }
}
