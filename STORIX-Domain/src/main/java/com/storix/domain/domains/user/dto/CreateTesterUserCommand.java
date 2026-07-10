package com.storix.domain.domains.user.dto;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;
import lombok.Builder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Builder
public record CreateTesterUserCommand(
        String oid,
        String nickName,
        Set<Genre> favoriteGenreList
) {
    public User toEntity() {
        OAuthInfo oauthInfo = OAuthInfo.builder()
                .provider(OAuthProvider.SLACK)
                .oid(oid)
                .build();
        Set<Genre> genres = (favoriteGenreList == null) ?
                Collections.emptySet() : new LinkedHashSet<>(favoriteGenreList);

        // 테스터 계정은 닉네임 중복 체크 대상에서 제외되므로, suffix로 nick_name 유니크 제약을 우회
        String uniqueNickName = nickName + STORIXStatic.NICK_NAME_SUFFIX_DELIMITER + UUID.randomUUID();

        return User.builder()
                .ageOver14(true)
                .oauthInfo(oauthInfo)
                .nickName(uniqueNickName)
                .favoriteGenreList(genres)
                .role(Role.TESTER)
                .build();
    }
}
