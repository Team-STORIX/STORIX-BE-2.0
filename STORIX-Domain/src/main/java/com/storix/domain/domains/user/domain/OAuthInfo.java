package com.storix.domain.domains.user.domain;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.converter.OAuthRefreshTokenConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class OAuthInfo {

    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    private String oid;

    @Convert(converter = OAuthRefreshTokenConverter.class)
    private String oauthRefreshToken;

    private String email; // 관리자 이메일

    @Builder
    public OAuthInfo(OAuthProvider provider, String oid, String oauthRefreshToken, String email) {
        this.provider = provider;
        this.oid = oid;
        this.oauthRefreshToken = oauthRefreshToken;
        this.email = email;
    }

    // refresh_token 갱신
    public void updateOauthRefreshToken(String oauthRefreshToken) {
        this.oauthRefreshToken = oauthRefreshToken;
    }

    // 회원 탈퇴 시
    public OAuthInfo withDrawOauthInfo() {
        return OAuthInfo.builder()
                .oid(STORIXStatic.WITHDRAW_PREFIX + LocalDateTime.now() + ":" + oid)
                .provider(provider)
                .build();
    }
}
