package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.AdminNotificationLogAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;
import com.storix.domain.domains.event.adaptor.AppEventParticipantAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationTargetService {

    private final AdminNotificationAdaptor adminNotificationAdaptor;
    private final AdminNotificationLogAdaptor adminNotificationLogAdaptor;

    private final UserAdaptor userAdaptor;
    private final AppEventParticipantAdaptor appEventParticipantAdaptor;

    // 발송용 어드민 알림 조회
    @Transactional(readOnly = true)
    public AdminNotificationBroadcastInfo getBroadcastInfo(Long adminNotificationId) {
        return adminNotificationAdaptor.findBroadcastInfo(adminNotificationId);
    }

    // 발송 대상 청크 단위 조회
    @Transactional(readOnly = true)
    public List<Long> findTargetChunk(AdminNotificationTargetAudience audience, Long eventTargetId, Long lastUserId, LocalDateTime now, int size) {
        PageRequest page = PageRequest.of(0, size);
        return switch (audience) {
            case ALL -> userAdaptor.findAdminNotificationTargetUserIds(lastUserId, null, page);
            case NEW_USERS -> userAdaptor.findAdminNotificationTargetUserIds(lastUserId, now.minusMonths(1), page);
            case EVENT_WINNERS -> appEventParticipantAdaptor.findWinnerUserIds(eventTargetId, lastUserId, page);
        };
    }

    // 청크 발송 로그 선저장 + 진행 커서 전진
    @Transactional
    public void persistPendingChunk(Long adminNotificationId, List<Long> userIds, LocalDateTime eligibleAt, Long lastUserId) {
        Set<Long> existing = adminNotificationLogAdaptor.findByChunk(adminNotificationId, userIds).stream()
                .map(AdminNotificationLog::getUserId)
                .collect(Collectors.toSet());
        List<AdminNotificationLog> newRows = userIds.stream()
                .filter(u -> !existing.contains(u))
                .map(u -> AdminNotificationLog.pending(adminNotificationId, u, eligibleAt))
                .toList();
        adminNotificationLogAdaptor.saveAll(newRows);

        adminNotificationAdaptor.advanceBroadcastCursor(adminNotificationId, lastUserId, LocalDateTime.now());
    }

    // 저장된 발송 로그 수
    @Transactional(readOnly = true)
    public int countEnrolled(Long adminNotificationId) {
        return (int) adminNotificationLogAdaptor.countTotal(adminNotificationId);
    }
}
