package com.storix.infrastructure.external.slack;

import com.storix.common.property.SlackProperties;
import com.storix.domain.domains.user.exception.oauth.SlackInvalidSignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    private final SlackProperties slackProperties;

    public void verify(String timestamp, String body, String signature) {
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
