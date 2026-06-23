package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.domain.User;
import lombok.Builder;

import java.util.Collections;

@Builder
public record CreateAdminUserCommand(
        String oid,
        String email,
        String encodedPassword,
        String nickName
) {
    public User toEntity() {
        OAuthInfo oauthInfo = OAuthInfo.builder()
                .provider(OAuthProvider.SLACK)
                .oid(oid)
                .email(email)
                .build();

        return User.builder()
                .oauthInfo(oauthInfo)
                .nickName(nickName)
                .favoriteGenreList(Collections.emptySet())
                .password(encodedPassword)
                .role(Role.SUPER_ADMIN)
                .build();
    }
}
