package com.storix.domain.domains.notification.adaptor;

import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;
import com.storix.domain.domains.notification.exception.AdminNotificationNotFoundException;
import com.storix.domain.domains.notification.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminNotificationAdaptor {

    private final AdminNotificationRepository adminNotificationRepository;

    /* ===== 읽기 작업 ===== */

    public AdminNotification findById(Long adminNotificationId) {
        return adminNotificationRepository.findById(adminNotificationId)
                .orElseThrow(() -> AdminNotificationNotFoundException.EXCEPTION);
    }

    public AdminNotification findByIdForUpdate(Long adminNotificationId) {
        return adminNotificationRepository.findByIdForUpdate(adminNotificationId)
                .orElseThrow(() -> AdminNotificationNotFoundException.EXCEPTION);
    }

    public AdminNotificationBroadcastInfo findBroadcastInfo(Long adminNotificationId) {
        return adminNotificationRepository.findBroadcastInfo(adminNotificationId)
                .orElseThrow(() -> AdminNotificationNotFoundException.EXCEPTION);
    }

    public Page<AdminNotification> findAll(Pageable pageable) {
        return adminNotificationRepository.findAllByOrderByIdDesc(pageable);
    }

    public List<AdminNotification> findDue(LocalDateTime now, Pageable pageable) {
        return adminNotificationRepository.findDueNotifications(
                AdminNotificationStatus.SCHEDULED, now, pageable
        );
    }

    public List<Long> findCompletableSendingIds(Pageable pageable) {
        return adminNotificationRepository.findCompletableSendingIds(pageable);
    }

    public List<Long> findStaleIncompleteSendingIds(LocalDateTime cutoff, Pageable pageable) {
        return adminNotificationRepository.findStaleIncompleteSendingIds(cutoff, pageable);
    }

    public List<Long> findStaleCompletedSendingIds(LocalDateTime cutoff, Pageable pageable) {
        return adminNotificationRepository.findStaleCompletedSendingIds(cutoff, pageable);
    }


    /* ===== 쓰기 작업 ===== */

    public AdminNotification save(AdminNotification adminNotification) {
        return adminNotificationRepository.save(adminNotification);
    }

    public void addCounts(Long id, int sent, int failed, int skipped, LocalDateTime now) {
        adminNotificationRepository.addCounts(id, sent, failed, skipped, now);
    }

    public void markAllChunkPublished(Long id, int total, LocalDateTime now) {
        adminNotificationRepository.markAllChunkPublished(id, total, now);
    }

    public void advanceBroadcastCursor(Long id, Long cursor, LocalDateTime now) {
        adminNotificationRepository.advanceBroadcastCursor(id, cursor, now);
    }

    public int startSending(Long id, LocalDateTime now) {
        return adminNotificationRepository.startSending(id, now);
    }

    public int startRebroadcast(Long id, LocalDateTime now) {
        return adminNotificationRepository.startRebroadcast(id, now);
    }

    public void touchProgress(Long id, LocalDateTime now) {
        adminNotificationRepository.touchProgress(id, now);
    }

    public int claimStaleForResume(Long id, LocalDateTime cutoff, LocalDateTime now) {
        return adminNotificationRepository.claimStaleForResume(id, cutoff, now);
    }

    public int finalizeIfSending(Long id, AdminNotificationStatus status, int sent, int failed, int skipped, LocalDateTime now) {
        return adminNotificationRepository.finalizeIfSending(id, status, sent, failed, skipped, now);
    }
}
