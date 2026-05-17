package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record CreateDeveloperUserCommand(
        String oid,
        String nickName,
        Set<Genre> favoriteGenreList
) {
    public User toEntity() {
        OAuthInfo oauthInfo = new OAuthInfo(OAuthProvider.SLACK, oid);
        Set<Genre> genres = (favoriteGenreList == null) ?
                Collections.emptySet() : new LinkedHashSet<>(favoriteGenreList);

        return User.builder()
                .termsAgree(true)
                .oauthInfo(oauthInfo)
                .nickName(nickName)
                .favoriteGenreList(genres)
                .role(Role.ADMIN)
                .build();
    }
}
