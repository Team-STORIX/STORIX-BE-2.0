package com.storix.infrastructure.external.slack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.property.SlackProperties;
import com.storix.domain.domains.user.exception.oauth.SlackInvalidSignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackSignatureVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String VERSION = "v0";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300;

    private static final String HEADER_TIMESTAMP = "X-Slack-Request-Timestamp";
    private static final String HEADER_SIGNATURE = "X-Slack-Signature";
    private static final String PAYLOAD_PREFIX = "payload=";

    private final SlackProperties slackProperties;
    private final ObjectMapper objectMapper;


    public SlackInteractionDto verify(HttpServletRequest request) throws IOException {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            verifySignature(request.getHeader(HEADER_TIMESTAMP), rawBody, request.getHeader(HEADER_SIGNATURE));

            String payload = URLDecoder.decode(
                    rawBody.substring(PAYLOAD_PREFIX.length()), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(payload);
            return new SlackInteractionDto(
                    root.at("/actions/0/action_id").asText(),
                    root.at("/actions/0/value").asText(),
                    root.at("/response_url").asText()
            );
        } catch (IOException e) {
            log.error(">>>> [Slack] 콜백 파싱 실패", e);
            throw e;
        }
    }

    private void verifySignature(String timestamp, String body, String signature) {
        if (timestamp == null || body == null || signature == null) {
            throw SlackInvalidSignatureException.EXCEPTION;
        }

        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - Long.parseLong(timestamp)) > TIMESTAMP_TOLERANCE_SECONDS) {
            throw SlackInvalidSignatureException.EXCEPTION;
        }

        String baseString = VERSION + ":" + timestamp + ":" + body;
        String expectedSignature = VERSION + "=" + hmacSha256(baseString);

        if (!expectedSignature.equals(signature)) {
            throw SlackInvalidSignatureException.EXCEPTION;
        }
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(
                    slackProperties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw SlackInvalidSignatureException.EXCEPTION;
        }
    }
}
