package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.Gender;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record CreateReaderUserCommand(
        Boolean marketingAgree,
        OAuthProvider provider,
        String oid,
        String nickName,
        Gender gender,
        Set<Genre> favoriteGenreList
) {
    public User toEntity() {
        OAuthInfo oauthInfo = new OAuthInfo(provider, oid);
        Set<Genre> genres = (favoriteGenreList == null) ?
                        Collections.emptySet() : new LinkedHashSet<>(favoriteGenreList);

        return new User(
                marketingAgree,
                oauthInfo,
                nickName,
                gender,
                genres
        );
    }
}