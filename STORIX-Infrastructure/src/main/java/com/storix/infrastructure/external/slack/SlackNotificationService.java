package com.storix.infrastructure.external.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.property.SlackProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final SlackProperties slackProperties;
    private final RestTemplate slackRestTemplate;

    private static final String SLACK_POST_MESSAGE_URL = "https://slack.com/api/chat.postMessage";

    public void sendDeveloperSignupApproval(String pendingId, String nickName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackProperties.getBotToken());

        Map<String, Object> body = Map.of(
                "channel", slackProperties.getChannelId(),
                "text", "어드민 회원가입 승인 요청: " + nickName,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", "*[어드민 회원가입 승인 요청]*\n"
                                                + "- 10분 이내로 승인해야 합니다.\n\n"
                                                + ":bust_in_silhouette: *닉네임:* " + nickName + "\n"
                                                + ":key: *요청 ID:* `" + pendingId + "`"
                                )
                        ),
                        Map.of(
                                "type", "actions",
                                "elements", List.of(
                                        Map.of(
                                                "type", "button",
                                                "text", Map.of("type", "plain_text", "text", "승인"),
                                                "style", "primary",
                                                "action_id", "approve_developer_signup",
                                                "value", pendingId
                                        ),
                                        Map.of(
                                                "type", "button",
                                                "text", Map.of("type", "plain_text", "text", "거절"),
                                                "style", "danger",
                                                "action_id", "reject_developer_signup",
                                                "value", pendingId
                                        )
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = slackRestTemplate.postForEntity(
                    SLACK_POST_MESSAGE_URL, request, String.class);
            log.info(">>>> [Slack] 개발자 승인 요청 전송 완료");
        } catch (Exception e) {
            log.error(">>>> [Slack] 메시지 전송 실패", e);
        }
    }

    public void sendApprovalResult(String pendingId, boolean approved, String responseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String resultText = approved
                ? ":white_check_mark: *승인 완료* — 요청 ID: `" + pendingId + "`"
                : ":x: *거절됨* — 요청 ID: `" + pendingId + "`";

        Map<String, Object> body = Map.of(
                "replace_original", "true",
                "text", resultText
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            slackRestTemplate.postForEntity(responseUrl, request, String.class);
        } catch (Exception e) {
            log.error(">>>> [Slack] 응답 메시지 전송 실패", e);
        }
    }
}
