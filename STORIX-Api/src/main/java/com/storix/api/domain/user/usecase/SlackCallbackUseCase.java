package com.storix.api.domain.user.usecase;

import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackCallbackUseCase {

    private final DeveloperAuthUseCase developerAuthUseCase;
    private final AdminAuthUseCase adminAuthUseCase;
    private final SlackNotificationService slackNotificationService;

    @Async("slackTaskExecutor")
    public void processAsync(String actionId, String pendingId, String responseUrl) {
        try {
            switch (actionId) {
                case "approve_developer_signup" -> {
                    developerAuthUseCase.approveSignup(pendingId);
                    slackNotificationService.sendApprovalResult(pendingId, true, responseUrl);
                    log.info(">>>> [Slack] 개발자 회원가입 승인 완료");
                }
                case "reject_developer_signup" -> {
                    slackNotificationService.sendApprovalResult(pendingId, false, responseUrl);
                    log.info(">>>> [Slack] 개발자 회원가입 거절");
                }
                case "approve_admin_signup" -> {
                    adminAuthUseCase.approveSignup(pendingId);
                    slackNotificationService.sendApprovalResult(pendingId, true, responseUrl);
                    log.info(">>>> [Slack] 관리자 회원가입 승인 완료");
                }
                case "reject_admin_signup" -> {
                    slackNotificationService.sendApprovalResult(pendingId, false, responseUrl);
                    log.info(">>>> [Slack] 관리자 회원가입 거절");
                }
                default -> log.warn(">>>> [Slack] 알 수 없는 actionId: {}", actionId);
            }
        } catch (Exception e) {
            log.error(">>>> [Slack] 콜백 비동기 처리 실패", e);
        }
    }
}
