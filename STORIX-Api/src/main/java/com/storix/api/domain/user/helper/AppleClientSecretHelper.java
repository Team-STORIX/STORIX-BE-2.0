package com.storix.api.domain.user.helper;

import com.storix.common.property.OAuthProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.storix.domain.domains.user.exception.oauth.ApplePrivateKeyException;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleClientSecretHelper {

    private final OAuthProperties oauthProperties;

    // Apple client_secret JWT 생성 (ES256 서명)
    public String generateClientSecret() {
        var apple = oauthProperties.getApple();
        Instant now = Instant.now();

        log.info("[Apple] generateClientSecret 시작: clientId={}, teamId={}, keyId={}, privateKeyLength={}, privateKeyNull={}",
                apple.getClientId(),
                apple.getTeamId(),
                apple.getKeyId(),
                apple.getPrivateKey() == null ? 0 : apple.getPrivateKey().length(),
                apple.getPrivateKey() == null);

        try {
            String secret = Jwts.builder()
                    .setHeaderParam("kid", apple.getKeyId())
                    .setHeaderParam("alg", "ES256")
                    .setIssuer(apple.getTeamId())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(15 * 60))) // 15분
                    .setAudience("https://appleid.apple.com")
                    .setSubject(apple.getClientId())
                    .signWith(getPrivateKey(apple.getPrivateKey()), SignatureAlgorithm.ES256)
                    .compact();
            log.info("[Apple] generateClientSecret 성공: jwtLength={}", secret.length());
            return secret;
        } catch (Exception e) {
            log.error("[Apple] generateClientSecret 실패: type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private PrivateKey getPrivateKey(String privateKeyString) {
        if (privateKeyString == null || privateKeyString.isBlank()) {
            log.error("[Apple] privateKey 값이 비어있습니다. 환경변수 APPLE_PRIVATE_KEY 주입 확인 필요. raw={}", privateKeyString);
            throw ApplePrivateKeyException.EXCEPTION;
        }
        try {
            log.debug("[Apple] privateKey raw(앞 40자)={}, rawLength={}, containsBEGIN={}, containsEND={}",
                    privateKeyString.substring(0, Math.min(40, privateKeyString.length())),
                    privateKeyString.length(),
                    privateKeyString.contains("-----BEGIN PRIVATE KEY-----"),
                    privateKeyString.contains("-----END PRIVATE KEY-----"));

            String key = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            log.debug("[Apple] privateKey base64 정제 후 length={}, prefix={}",
                    key.length(), key.substring(0, Math.min(20, key.length())));

            byte[] keyBytes = Base64.getDecoder().decode(key);
            log.debug("[Apple] base64 decode 완료: bytes={}", keyBytes.length);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey pk = keyFactory.generatePrivate(keySpec);
            log.debug("[Apple] PrivateKey 생성 성공: algorithm={}, format={}", pk.getAlgorithm(), pk.getFormat());
            return pk;
        } catch (Exception e) {
            log.error("[Apple] private key 파싱 실패: type={}, message={}, keyPrefix={}, length={}",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    privateKeyString.substring(0, Math.min(30, privateKeyString.length())),
                    privateKeyString.length(), e);
            throw ApplePrivateKeyException.EXCEPTION;
        }
    }
}
