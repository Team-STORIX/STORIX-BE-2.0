package com.storix.domain.domains.user.domain;

import com.storix.common.utils.STORIXStatic;
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

    @Builder
    public OAuthInfo(OAuthProvider provider, String oid) {
        this.provider = provider;
        this.oid = oid;
    }

    // 회원 탈퇴 시
    public OAuthInfo withDrawOauthInfo() {
        return OAuthInfo.builder()
                .oid(STORIXStatic.WITHDRAW_PREFIX + LocalDateTime.now() + ":" + oid)
                .provider(provider)
                .build();
    }
}
