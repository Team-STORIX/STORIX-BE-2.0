package com.storix.infrastructure.global;

import com.storix.domain.domains.user.exception.token.ExpiredRefreshTokenException;
import com.storix.domain.domains.user.exception.token.ExpiredTokenException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
import com.storix.infrastructure.global.dto.AccessTokenInfo;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[보안] 토큰 발급 · 파싱")
class TokenProviderTest {

    private static final String SECRET = Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());

    private TokenProvider provider(String secret, long validityMs) {
        TokenProvider provider = new TokenProvider();
        ReflectionTestUtils.setField(provider, "secretKey", secret);
        ReflectionTestUtils.setField(provider, "accessTokenValidityMs", validityMs);
        ReflectionTestUtils.setField(provider, "refreshTokenValidityMs", validityMs);
        ReflectionTestUtils.setField(provider, "onboardingTokenValidityMs", validityMs);
        provider.afterPropertiesSet();
        return provider;
    }

    @Test
    @DisplayName("같은 파서로 여러 번 파싱해도 같은 값을 읽는다")
    void parseRepeatedly() {
        TokenProvider provider = provider(SECRET, 60_000);
        String token = provider.createAccessToken(7L, "READER");

        for (int i = 0; i < 3; i++) {
            AccessTokenInfo info = provider.parseAccessToken(token);
            assertThat(info.userId()).isEqualTo(7L);
            assertThat(info.role()).isEqualTo("READER");
        }
    }

    @Test
    @DisplayName("만료된 액세스 토큰은 만료 예외")
    void expiredAccessToken() {
        TokenProvider provider = provider(SECRET, -1_000);
        String token = provider.createAccessToken(7L, "READER");

        assertThatThrownBy(() -> provider.parseAccessToken(token)).isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰은 리프레시 만료 예외")
    void expiredRefreshToken() {
        TokenProvider provider = provider(SECRET, -1_000);
        String token = provider.createRefreshToken(7L);

        assertThatThrownBy(() -> provider.parseRefreshToken(token)).isInstanceOf(ExpiredRefreshTokenException.class);
    }

    @Test
    @DisplayName("다른 키로 서명한 토큰은 잘못된 토큰 예외")
    void otherKey() {
        String otherSecret = Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
        String token = provider(otherSecret, 60_000).createAccessToken(7L, "READER");

        assertThatThrownBy(() -> provider(SECRET, 60_000).parseAccessToken(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("리프레시 토큰을 액세스 토큰으로 쓰면 잘못된 토큰 예외")
    void refreshAsAccess() {
        TokenProvider provider = provider(SECRET, 60_000);
        String token = provider.createRefreshToken(7L);

        assertThatThrownBy(() -> provider.parseAccessToken(token)).isInstanceOf(InvalidTokenException.class);
    }
}
