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

    public void sendTesterSignupApproval(String pendingId, String nickName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackProperties.getBotToken());

        Map<String, Object> body = Map.of(
                "channel", slackProperties.getChannelId(),
                "text", "테스터 회원가입 승인 요청: " + nickName,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", "*[👨‍💻 테스터 회원가입 승인 요청]*\n"
                                                + "- 권한: `ADMIN`\n"
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
                                                "action_id", "approve_tester_signup",
                                                "value", pendingId
                                        ),
                                        Map.of(
                                                "type", "button",
                                                "text", Map.of("type", "plain_text", "text", "거절"),
                                                "style", "danger",
                                                "action_id", "reject_tester_signup",
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
            log.info(">>>> [Slack] 테스터 승인 요청 전송 완료");
        } catch (Exception e) {
            log.error(">>>> [Slack] 메시지 전송 실패", e);
        }
    }

    public void sendAdminSignupApproval(String pendingId, String nickName, String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackProperties.getBotToken());

        Map<String, Object> body = Map.of(
                "channel", slackProperties.getChannelId(),
                "text", "관리자 회원가입 승인 요청: " + nickName,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", "*[🛡️ 관리자 회원가입 승인 요청]*\n"
                                                + "- 권한: `ADMIN`\n"
                                                + "- 10분 이내로 승인해야 합니다.\n\n"
                                                + ":bust_in_silhouette: *닉네임:* " + nickName + "\n"
                                                + ":email: *로그인 ID:* `" + email + "`\n"
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
                                                "action_id", "approve_admin_signup",
                                                "value", pendingId
                                        ),
                                        Map.of(
                                                "type", "button",
                                                "text", Map.of("type", "plain_text", "text", "거절"),
                                                "style", "danger",
                                                "action_id", "reject_admin_signup",
                                                "value", pendingId
                                        )
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            slackRestTemplate.postForEntity(SLACK_POST_MESSAGE_URL, request, String.class);
            log.info(">>>> [Slack] 관리자 승인 요청 전송 완료");
        } catch (Exception e) {
            log.error(">>>> [Slack] 메시지 전송 실패", e);
        }
    }

    public void sendTesterApprovalResult(String pendingId, String nickName, boolean approved, String responseUrl) {
        sendApprovalResult(buildApprovalResultText("👨‍💻", "테스터", nickName, null, pendingId, approved), responseUrl);
    }

    public void sendAdminApprovalResult(String pendingId, String nickName, String email, boolean approved, String responseUrl) {
        sendApprovalResult(buildApprovalResultText("🛡️", "관리자", nickName, email, pendingId, approved), responseUrl);
    }

    private String buildApprovalResultText(String emoji, String role, String nickName, String email,
                                           String pendingId, boolean approved) {
        StringBuilder sb = new StringBuilder();
        sb.append("*[").append(emoji).append(" ").append(role).append(" 회원가입 ")
                .append(approved ? "승인" : "거절").append("]*\n");
        sb.append(approved ? ":white_check_mark: *승인 완료*" : ":x: *거절됨*").append("\n\n");
        sb.append(":bust_in_silhouette: *닉네임:* ").append(nickName).append("\n");
        if (email != null) {
            sb.append(":email: *로그인 ID:* `").append(email).append("`\n");
        }
        sb.append(":key: *요청 ID:* `").append(pendingId).append("`");
        return sb.toString();
    }

    public void sendApprovalFailure(String pendingId, String responseUrl, String reason) {
        String text = "*[⚠️ 승인 처리 실패]*\n" +
                ":x: *오류:* " + reason + "\n\n" +
                ":key: *요청 ID:* `" + pendingId + "`";
        sendApprovalResult(text, responseUrl);
    }

    private void sendApprovalResult(String text, String responseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "replace_original", "true",
                "text", text
        );

        try {
            slackRestTemplate.postForEntity(responseUrl, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.error(">>>> [Slack] 응답 메시지 전송 실패", e);
        }
    }
}
