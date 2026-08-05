package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.dto.AdminNotificationCommand;
import com.storix.domain.domains.notification.exception.AdminNotificationEventNoWinnerException;
import com.storix.domain.domains.notification.exception.AdminNotificationNotCancelableException;
import com.storix.domain.domains.notification.exception.AdminNotificationNotUpdatableException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private static final int NOTIFICATION_PAGE_SIZE = 10;

    private final AdminNotificationAdaptor adminNotificationAdaptor;
    private final AppEventAdaptor appEventAdaptor;

    @Transactional
    public Long create(AdminNotificationCommand cmd, Long assigneeAdminId) {
        validateEventWinnersTarget(cmd);
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
        validateEventWinnersTarget(cmd);

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

    // 당첨자 발송은 대상 이벤트가 당첨자를 뽑는 이벤트일 때만 허용한다.
    // hasWinner=false 인데 과거에 확정된 당첨자 행이 남아 있으면 그대로 발송돼버린다
    private void validateEventWinnersTarget(AdminNotificationCommand cmd) {
        if (cmd.targetAudience() != AdminNotificationTargetAudience.EVENT_WINNERS || cmd.eventTargetId() == null) {
            return;
        }
        if (!appEventAdaptor.findById(cmd.eventTargetId()).isHasWinner()) {
            throw AdminNotificationEventNoWinnerException.EXCEPTION;
        }
    }

    @Transactional(readOnly = true)
    public AdminNotification getById(Long adminNotificationId) {
        return adminNotificationAdaptor.findById(adminNotificationId);
    }

    @Transactional(readOnly = true)
    public Page<AdminNotification> getNotifications(int page, String keyword) {
        int safePage = Math.max(0, page);
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return adminNotificationAdaptor.searchByTitle(normalized, PageRequest.of(safePage, NOTIFICATION_PAGE_SIZE));
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
