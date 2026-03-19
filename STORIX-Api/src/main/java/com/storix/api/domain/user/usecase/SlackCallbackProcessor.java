package com.storix.api.domain.user.usecase;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackCallbackProcessor {

    private final DeveloperAuthUseCase developerAuthUseCase;
    private final SlackNotificationService slackNotificationService;

    @Async("slackTaskExecutor")
    public void processAsync(String actionId, String pendingId, String responseUrl) {
        try {
            if ("approve_developer_signup".equals(actionId)) {
                developerAuthUseCase.approveSignup(pendingId);
                slackNotificationService.sendApprovalResult(pendingId, true, responseUrl);
                log.info(">>>> [Slack] 개발자 회원가입 승인 완료");
            } else if ("reject_developer_signup".equals(actionId)) {
                slackNotificationService.sendApprovalResult(pendingId, false, responseUrl);
                log.info(">>>> [Slack] 개발자 회원가입 거절");
            }
        } catch (Exception e) {
            log.error(">>>> [Slack] 콜백 비동기 처리 실패", e);
        }
    }
}
