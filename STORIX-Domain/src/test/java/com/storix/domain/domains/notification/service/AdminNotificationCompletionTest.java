package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationStatus;
import com.storix.domain.domains.notification.domain.AdminNotificationSendType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.adaptor.AdminNotificationLogAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationStartResult;
import com.storix.domain.domains.notification.exception.AdminNotificationNotRebroadcastableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[운영자 알림] 완료 기반 비동기 발송 - 완료 판정/상태 변경")
class AdminNotificationCompletionTest {

    private static final Long ID = 100L;

    @Mock
    private AdminNotificationAdaptor adminNotificationAdaptor;

    @Mock
    private AdminNotificationLogAdaptor adminNotificationLogAdaptor;

    @Mock
    private NotificationAdaptor notificationAdaptor;

    private AdminNotificationLifecycleService lifecycleService;
    private AdminNotificationDeliveryResultService deliveryResultService;

    @BeforeEach
    void setUp() {
        lifecycleService = new AdminNotificationLifecycleService(adminNotificationAdaptor, adminNotificationLogAdaptor);
        deliveryResultService = new AdminNotificationDeliveryResultService(
                adminNotificationAdaptor, adminNotificationLogAdaptor, notificationAdaptor, lifecycleService);
    }

    private AdminNotification notification(AdminNotificationStatus status, boolean allChunkPublished,
                                          int target, int sent, int failed) {
        return notification(status, allChunkPublished, target, sent, failed, 0);
    }

    private AdminNotification notification(AdminNotificationStatus status, boolean allChunkPublished,
                                          int target, int sent, int failed, int skipped) {
        AdminNotification e = AdminNotification.builder()
                .title("제목").content("본문")
                .targetAudience(AdminNotificationTargetAudience.ALL)
                .sendType(AdminNotificationSendType.IMMEDIATE)
                .build();
        ReflectionTestUtils.setField(e, "id", ID);
        ReflectionTestUtils.setField(e, "status", status);
        ReflectionTestUtils.setField(e, "isAllChunkPublished", allChunkPublished);
        ReflectionTestUtils.setField(e, "targetCount", target);
        ReflectionTestUtils.setField(e, "sentCount", sent);
        ReflectionTestUtils.setField(e, "failedCount", failed);
        ReflectionTestUtils.setField(e, "skippedCount", skipped);
        return e;
    }

    private Map<AdminNotificationLogStatus, Integer> logCounts(int sent, int failed, int skipped) {
        Map<AdminNotificationLogStatus, Integer> counts = new EnumMap<>(AdminNotificationLogStatus.class);
        if (sent > 0) counts.put(AdminNotificationLogStatus.SENT, sent);
        if (failed > 0) counts.put(AdminNotificationLogStatus.FAILED, failed);
        if (skipped > 0) counts.put(AdminNotificationLogStatus.SKIPPED, skipped);
        return counts;
    }

    private void stubLogCounts(int pending, int sent, int failed, int skipped) {
        given(adminNotificationLogAdaptor.existsByStatus(eq(ID), eq(AdminNotificationLogStatus.PENDING))).willReturn(pending > 0);
        given(adminNotificationLogAdaptor.countGroupByStatus(eq(ID))).willReturn(logCounts(sent, failed, skipped));
    }

    @Nested
    @DisplayName("startSending - 발송 진입 가드 (원자적 조건부 상태 변경)")
    class StartSending {

        @Test
        @DisplayName("상태 변경 성공(1행) -> started=true, status=SENDING (승자는 재조회 안 함)")
        void transition_wins() {
            given(adminNotificationAdaptor.startSending(eq(ID), any(LocalDateTime.class))).willReturn(1);

            AdminNotificationStartResult result = lifecycleService.startSending(ID);

            assertThat(result.started()).isTrue();
            assertThat(result.status()).isEqualTo(AdminNotificationStatus.SENDING);
            verify(adminNotificationAdaptor, never()).findById(ID);
        }

        @Test
        @DisplayName("상태 변경 실패(0행) -> started=false, 막은 현재 상태 반환")
        void transition_loses_returns_current_status() {
            given(adminNotificationAdaptor.startSending(eq(ID), any(LocalDateTime.class))).willReturn(0);
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENT, false, 0, 0, 0));

            AdminNotificationStartResult result = lifecycleService.startSending(ID);

            assertThat(result.started()).isFalse();
            assertThat(result.status()).isEqualTo(AdminNotificationStatus.SENT);
        }
    }

    @Nested
    @DisplayName("tryFinalize - 남은 PENDING 로그 0 일 때만 종료 (로그 count 기반)")
    class TryFinalize {

        @Test
        @DisplayName("SENDING 이 아니면 종료하지 않는다")
        void skip_when_not_sending() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENT, true, 0, 0, 0));

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor, never())
                    .finalizeIfSending(any(), any(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("아직 모든 청크가 발행되지 않았으면 종료하지 않는다")
        void skip_when_not_all_published() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, false, 0, 0, 0));

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor, never())
                    .finalizeIfSending(any(), any(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("아직 처리할 PENDING 로그가 남아있으면 종료하지 않는다")
        void skip_when_pending_remains() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            given(adminNotificationLogAdaptor.existsByStatus(eq(ID), eq(AdminNotificationLogStatus.PENDING)))
                    .willReturn(true);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor, never())
                    .finalizeIfSending(any(), any(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("PENDING 0 && 실패 0 → SENT, 카운트도 로그값으로 확정")
        void finalize_sent_when_no_failure() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 10, 0, 0);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.SENT), eq(10), eq(0), eq(0), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("PENDING 0 && 실패 존재 → FAILED")
        void finalize_failed_when_some_failed() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 7, 3, 0);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(7), eq(3), eq(0), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("skipped(대상 외)만 있고 실패 0 이면 SENT (FAILED 아님)")
        void finalize_sent_when_only_skipped() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 7, 0, 3);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.SENT), eq(7), eq(0), eq(3), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("skipped 가 있어도 실패가 1건이라도 있으면 FAILED")
        void finalize_failed_when_failed_with_skipped() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 7, 2, 1);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(7), eq(2), eq(1), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("대상 0명(로그 0건)이면 즉시 SENT 로 종료된다")
        void empty_target_finalizes_immediately_sent() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 0, 0, 0);

            lifecycleService.tryFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.SENT), eq(0), eq(0), eq(0), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("forceFinalize - 리컨실러 stale 강제 종료 (로그 count 기반)")
    class ForceFinalize {

        @Test
        @DisplayName("보낸 게 있어도 FAILED (강제 종료는 항상 실패, sent 는 counts 로 남음)")
        void failed_when_some_sent() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            given(adminNotificationLogAdaptor.countGroupByStatus(eq(ID))).willReturn(logCounts(4, 2, 0));

            lifecycleService.forceFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(4), eq(2), eq(0), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("하나도 못 보냈으면 FAILED")
        void failed_when_none_sent() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, false, 0, 0, 0));
            given(adminNotificationLogAdaptor.countGroupByStatus(eq(ID))).willReturn(logCounts(0, 0, 0));

            lifecycleService.forceFinalize(ID);

            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(0), eq(0), eq(0), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("이미 SENDING 이 아니면 강제 종료하지 않는다 (승자 단일화)")
        void skip_when_not_sending() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.FAILED, true, 0, 0, 0));

            lifecycleService.forceFinalize(ID);

            verify(adminNotificationAdaptor, never())
                    .finalizeIfSending(any(), any(), anyInt(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("강제 종료 시 남은 PENDING 로그를 FAILED 로 닫는다 (종료 후 좀비 재시도 방지)")
        void closes_pending_logs_before_finalize() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            given(adminNotificationLogAdaptor.countGroupByStatus(eq(ID))).willReturn(logCounts(3, 2, 0));

            lifecycleService.forceFinalize(ID);

            verify(adminNotificationLogAdaptor).failPendingLogs(ID);
            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(3), eq(2), eq(0), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("prepareRebroadcast - 수동 재발송 준비 (실패 로그 revive + 부모 SENDING 상태 변경, fan-out 미사용)")
    class PrepareRebroadcast {

        @Test
        @DisplayName("FAILED→SENDING 전이 성공(1행) → 실패 로그 revive 진행")
        void revive_when_transition_wins() {
            given(adminNotificationAdaptor.startRebroadcast(eq(ID), any(LocalDateTime.class))).willReturn(1);
            given(adminNotificationLogAdaptor.reviveFailedLogs(eq(ID), any(LocalDateTime.class))).willReturn(5);

            lifecycleService.prepareRebroadcast(ID);

            verify(adminNotificationAdaptor).startRebroadcast(eq(ID), any(LocalDateTime.class));
            verify(adminNotificationLogAdaptor).reviveFailedLogs(eq(ID), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("전이 실패(0행=FAILED 아님) → 재발송 불가 예외, revive 하지 않는다")
        void reject_when_transition_loses() {
            given(adminNotificationAdaptor.startRebroadcast(eq(ID), any(LocalDateTime.class))).willReturn(0);

            assertThatThrownBy(() -> lifecycleService.prepareRebroadcast(ID))
                    .isInstanceOf(AdminNotificationNotRebroadcastableException.class);

            verify(adminNotificationLogAdaptor, never()).reviveFailedLogs(any(), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("touchProgress - 재시도 tail 진행 표시 (30분 강제종료 리셋)")
    class TouchProgress {

        @Test
        @DisplayName("부모 updatedAt touch 를 위임한다 (발행완료 SENDING 조건은 쿼리 WHERE 가 담당)")
        void delegates_touch() {
            lifecycleService.touchProgress(ID);

            verify(adminNotificationAdaptor).touchProgress(eq(ID), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("accumulateProgress / markAllChunkPublished - 누적/발행완료 후 완료 시도")
    class AccumulateAndPublish {

        @Test
        @DisplayName("accumulateProgress 는 원자적 누적(addCounts) 후 완료를 시도한다")
        void accumulate_then_try_finalize() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 10, 0, 0); // PENDING 0 → SENT 로 종료

            deliveryResultService.accumulateProgress(ID, 5, 0, 0);

            verify(adminNotificationAdaptor).addCounts(eq(ID), eq(5), eq(0), eq(0), any(LocalDateTime.class));
            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.SENT), eq(10), eq(0), eq(0), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("markAllChunkPublished 는 발행완료 표시 후 완료를 시도한다")
        void mark_all_published_then_try_finalize() {
            given(adminNotificationAdaptor.findById(ID))
                    .willReturn(notification(AdminNotificationStatus.SENDING, true, 0, 0, 0));
            stubLogCounts(0, 7, 3, 0); // PENDING 0, 실패 존재 → FAILED

            lifecycleService.markAllChunkPublished(ID, 10);

            verify(adminNotificationAdaptor).markAllChunkPublished(eq(ID), eq(10), any(LocalDateTime.class));
            verify(adminNotificationAdaptor).finalizeIfSending(
                    eq(ID), eq(AdminNotificationStatus.FAILED), eq(7), eq(3), eq(0), any(LocalDateTime.class));
        }
    }
}
