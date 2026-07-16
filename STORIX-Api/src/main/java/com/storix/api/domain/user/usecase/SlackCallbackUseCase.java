package com.storix.api.domain.user.usecase;

import com.storix.domain.domains.user.adaptor.InternalSignupPendingAdaptor;
import com.storix.domain.domains.user.domain.AdminSignupPending;
import com.storix.domain.domains.user.domain.TesterSignupPending;
import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackCallbackUseCase {

    private final TesterAuthUseCase testerAuthUseCase;
    private final AdminAuthUseCase adminAuthUseCase;
    private final InternalSignupPendingAdaptor internalSignupPendingAdaptor;
    private final SlackNotificationService slackNotificationService;

    @Async("slackTaskExecutor")
    public void processAsync(String actionId, String pendingId, String responseUrl) {
        try {
            switch (actionId) {
                case "approve_tester_signup" -> {
                    TesterSignupPending pending = internalSignupPendingAdaptor.getTesterPending(pendingId);
                    testerAuthUseCase.approveSignup(pendingId);
                    slackNotificationService.sendTesterApprovalResult(pendingId, pending.getNickName(), true, responseUrl);
                    log.info(">>>> [Slack] 테스터 회원가입 승인 완료");
                }
                case "reject_tester_signup" -> {
                    TesterSignupPending pending = internalSignupPendingAdaptor.getTesterPending(pendingId);
                    internalSignupPendingAdaptor.deleteTesterPending(pendingId);
                    slackNotificationService.sendTesterApprovalResult(pendingId, pending.getNickName(), false, responseUrl);
                    log.info(">>>> [Slack] 테스터 회원가입 거절");
                }
                case "approve_admin_signup" -> {
                    AdminSignupPending pending = internalSignupPendingAdaptor.getAdminPending(pendingId);
                    adminAuthUseCase.approveSignup(pendingId);
                    slackNotificationService.sendAdminApprovalResult(pendingId, pending.getNickName(), pending.getEmail(), true, responseUrl);
                    log.info(">>>> [Slack] 관리자 회원가입 승인 완료");
                }
                case "reject_admin_signup" -> {
                    AdminSignupPending pending = internalSignupPendingAdaptor.getAdminPending(pendingId);
                    internalSignupPendingAdaptor.deleteAdminPending(pendingId);
                    slackNotificationService.sendAdminApprovalResult(pendingId, pending.getNickName(), pending.getEmail(), false, responseUrl);
                    log.info(">>>> [Slack] 관리자 회원가입 거절");
                }
                default -> log.warn(">>>> [Slack] 알 수 없는 actionId: {}", actionId);
            }
        } catch (Exception e) {
            log.error(">>>> [Slack] 콜백 비동기 처리 실패", e);
            slackNotificationService.sendApprovalFailure(pendingId, responseUrl, e.getMessage());
        }
    }
}
