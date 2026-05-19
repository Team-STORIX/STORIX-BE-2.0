package com.storix.domain.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.user.domain.OAuthProvider;
import lombok.Builder;

@Builder
public record UserInfo(
    Long userId,
    String role,
    String profileImageUrl,
    String nickName,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer level,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer point,

    String profileDescription,

    // 소셜 로그인 방식 (설정 탭 표시용)
    OAuthProvider oauthProvider
) {
}
