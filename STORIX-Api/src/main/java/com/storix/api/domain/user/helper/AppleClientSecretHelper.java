package com.storix.api.domain.user.helper;

import com.storix.common.property.OAuthProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.storix.domain.domains.user.exception.oauth.ApplePrivateKeyException;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AppleClientSecretHelper {

    private final OAuthProperties oauthProperties;

    // Apple client_secret JWT 생성 (ES256 서명)
    public String generateClientSecret() {
        var apple = oauthProperties.getApple();
        Instant now = Instant.now();

        return Jwts.builder()
                .setHeaderParam("kid", apple.getKeyId())
                .setHeaderParam("alg", "ES256")
                .setIssuer(apple.getTeamId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(15 * 60))) // 15분
                .setAudience("https://appleid.apple.com")
                .setSubject(apple.getClientId())
                .signWith(getPrivateKey(apple.getPrivateKey()), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey(String privateKeyString) {
        try {
            String key = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw ApplePrivateKeyException.EXCEPTION;
        }
    }
}
